package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import com.example.BuildConfig
import com.example.data.CustomAlbumEntity
import com.example.data.GalleryDatabase
import com.example.data.PhotoDao
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val APP_CHECK_TAG = "AppCheckInit"
private const val AI_SERVICE_TAG = "AiPhotoOrganizer"

// App Check contract keys. Defined by the Firebase SDK and runtime harness.
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

/**
 * Configures Firebase App Check before any UI, ViewModel, or Firebase AI call.
 * Uses synchronous .commit() in debug builds as required by the App Check contract.
 */
fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        // 1. Ensure FirebaseApp is initialized
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        if (FirebaseApp.getApps(context).isEmpty()) {
            Log.w(APP_CHECK_TAG, "FirebaseApp unavailable; skipping App Check setup.")
            return
        }

        if (BuildConfig.DEBUG) {
            // 2. Debug: Read debug token from Intent extra
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)

            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                // CRITICAL: MUST use commit() instead of apply() so the secret is written
                // synchronously before DebugAppCheckProviderFactory reads it.
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                Log.d(APP_CHECK_TAG, "App Check debug secret configured successfully.")
            } else {
                Log.w(APP_CHECK_TAG, "No debug token found in Intent extra.")
            }

            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(APP_CHECK_TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            // 3. Release: Look up recaptcha_site_key safely without throwing Resources.NotFoundException
            val resId = context.resources.getIdentifier(
                RECAPTCHA_SITE_KEY_RES,
                "string",
                context.packageName
            )
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                    Log.d(APP_CHECK_TAG, "RecaptchaAppCheckProviderFactory installed.")
                } else {
                    Log.e(APP_CHECK_TAG, "recaptcha_site_key resource string is blank.")
                }
            } else {
                Log.e(APP_CHECK_TAG, "recaptcha_site_key string resource not found in package.")
            }
        }
    } catch (e: Throwable) {
        Log.e(APP_CHECK_TAG, "Failed to initialize Firebase App Check", e)
    }
}

val AI_GALLERY_CATEGORIES = listOf(
    "Food",
    "Travel",
    "Portraits",
    "Nature",
    "Family",
    "Architecture",
    "Pets",
    "Nightlife",
    "Art & Design",
    "Events"
)

data class AiPhotoAnalysisResult(
    val photoId: Long,
    val photoTitle: String,
    val primaryCategory: String,
    val tags: List<String>,
    val smartCaption: String,
    val detectedSceneMood: String,
    val movedToAlbum: Boolean
)

sealed interface AiOrganizerUiState {
    data object Idle : AiOrganizerUiState
    data class Running(
        val currentPhotoTitle: String,
        val processedCount: Int,
        val totalCount: Int,
        val latestResult: AiPhotoAnalysisResult? = null
    ) : AiOrganizerUiState

    data class Completed(
        val organizedCount: Int,
        val results: List<AiPhotoAnalysisResult>,
        val categoryCounts: Map<String, Int>
    ) : AiOrganizerUiState

    data class Error(
        val message: String,
        val processedCount: Int = 0
    ) : AiOrganizerUiState
}

/**
 * Shared observable bus between the background [AiPhotoOrganizerService] and UI ViewModels.
 */
object AiOrganizerStatusBus {
    private val _uiState = MutableStateFlow<AiOrganizerUiState>(AiOrganizerUiState.Idle)
    val uiState: StateFlow<AiOrganizerUiState> = _uiState.asStateFlow()

    private val _recentResults = MutableStateFlow<List<AiPhotoAnalysisResult>>(emptyList())
    val recentResults: StateFlow<List<AiPhotoAnalysisResult>> = _recentResults.asStateFlow()

    fun updateState(newState: AiOrganizerUiState) {
        _uiState.value = newState
    }

    fun recordResult(result: AiPhotoAnalysisResult) {
        _recentResults.update { existing ->
            (listOf(result) + existing.filterNot { it.photoId == result.photoId }).take(30)
        }
    }

    fun resetToIdle() {
        _uiState.value = AiOrganizerUiState.Idle
    }
}

/**
 * Multimodal Gemini AI engine using Firebase AI Logic (`gemini-3.8-flash`) with a strict
 * structured JSON schema (`Schema.obj`) to classify photos into categories like 'Food',
 * 'Travel', and 'Portraits', generate descriptive tags, and craft smart captions.
 */
object AiPhotoCategorizerEngine {

    private val photoClassificationSchema = Schema.obj(
        properties = mapOf(
            "primaryCategory" to Schema.enumeration(
                values = AI_GALLERY_CATEGORIES,
                description = "Primary category for organizing the photo (e.g. Food, Travel, Portraits, Nature, Family, Architecture)"
            ),
            "tags" to Schema.array(
                items = Schema.string(),
                description = "3 to 5 concise descriptive visual tags for this photo"
            ),
            "smartCaption" to Schema.string(
                description = "A vivid, concise one-sentence editorial caption describing what is in the photo"
            ),
            "detectedSceneMood" to Schema.string(
                description = "A 1-2 word mood label such as Joyful, Serene, Wanderlust, Cozy, Nostalgic, or Golden Hour"
            )
        )
    )

    private val config = generationConfig {
        responseMimeType = "application/json"
        responseSchema = photoClassificationSchema
    }

    // Lazy initialization per Firebase AI Logic skill guidelines
    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config
        )
    }

    /**
     * Analyzes a single [PhotoEntity] using `gemini-3.8-flash` multimodal input and updates Room.
     */
    suspend fun analyzeAndOrganizePhoto(
        photo: PhotoEntity,
        photoDao: PhotoDao,
        moveIntoCategoryAlbum: Boolean,
        context: Context? = null
    ): AiPhotoAnalysisResult {
        val bitmap = decodeScaledBitmapForAi(context, photo.filePath, maxDimension = 768)
            ?: throw IllegalStateException("Could not decode image file for '${photo.title}'.")

        try {
            val prompt = buildString {
                append("Analyze this photo from the user's local gallery and classify it into ")
                append("the best matching category from: ${AI_GALLERY_CATEGORIES.joinToString(", ")} ")
                append("(prioritizing 'Food', 'Travel', 'Portraits', 'Nature', 'Family', 'Architecture' where applicable). ")
                append("Also provide 3 to 5 specific visual tags, a concise one-sentence smart caption, and a scene mood. ")
                append("Existing photo title: '${photo.title}', location: '${photo.locationTag}'.")
            }

            val response = withTimeout(60_000L) {
                model.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
            }

            val jsonString = response.text
                ?: throw IllegalStateException("Gemini model returned an empty response.")

            // Parse raw JSON directly without markdown code fence stripping
            val json = JSONObject(jsonString)
            val rawCategory = json.optString("primaryCategory", "Travel").trim()
            val primaryCategory = AI_GALLERY_CATEGORIES.firstOrNull {
                it.equals(rawCategory, ignoreCase = true)
            } ?: "Travel"

            val tagsArray = json.optJSONArray("tags")
            val aiTags = mutableListOf<String>()
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    val tag = tagsArray.optString(i).trim()
                    if (tag.isNotEmpty()) {
                        aiTags.add(tag)
                    }
                }
            }
            if (aiTags.isEmpty()) {
                aiTags.add(primaryCategory)
            }

            val smartCaption = json.optString("smartCaption", photo.title).trim()
            val sceneMood = json.optString("detectedSceneMood", "Curated").trim()

            // Merge tags: primaryCategory + "AI Tagged" + generated tags + existing tags
            val mergedTags = (listOf(primaryCategory, "AI Tagged", sceneMood) + aiTags + photo.tagList)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinctBy { it.lowercase() }
                .joinToString(",")

            val targetAlbum = if (moveIntoCategoryAlbum) {
                photoDao.insertCustomAlbum(
                    CustomAlbumEntity(
                        name = primaryCategory,
                        description = "AI Auto-Organized '$primaryCategory' collection"
                    )
                )
                primaryCategory
            } else {
                // Still ensure the category album exists for browsing
                photoDao.insertCustomAlbum(
                    CustomAlbumEntity(
                        name = primaryCategory,
                        description = "AI Auto-Organized '$primaryCategory' collection"
                    )
                )
                photo.albumName
            }

            val updatedNotes = if (photo.notes.isBlank()) {
                "$smartCaption ($sceneMood)"
            } else if (!photo.notes.contains(smartCaption)) {
                "${photo.notes} • AI: $smartCaption"
            } else {
                photo.notes
            }

            val updatedPhoto = photo.copy(
                albumName = targetAlbum,
                tags = mergedTags,
                notes = updatedNotes
            )
            photoDao.updatePhoto(updatedPhoto)

            return AiPhotoAnalysisResult(
                photoId = photo.id,
                photoTitle = photo.title,
                primaryCategory = primaryCategory,
                tags = aiTags,
                smartCaption = smartCaption,
                detectedSceneMood = sceneMood,
                movedToAlbum = moveIntoCategoryAlbum
            )
        } finally {
            bitmap.recycle()
        }
    }

    private fun decodeScaledBitmapForAi(context: Context?, filePath: String, maxDimension: Int): Bitmap? {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        PhotoImageProcessor.decodeBitmapFromPathOrUri(context, filePath, boundsOpts)
        val w = boundsOpts.outWidth
        val h = boundsOpts.outHeight
        if (w <= 0 || h <= 0) return null

        var sampleSize = 1
        val longest = max(w, h)
        while (longest / sampleSize > maxDimension) {
            sampleSize *= 2
        }
        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return PhotoImageProcessor.decodeBitmapFromPathOrUri(context, filePath, decodeOpts)
    }
}

/**
 * Android Background [Service] that uses the Gemini 3.8 Flash model via Firebase AI Logic
 * to automatically tag and organize local photos into categories ('Food', 'Travel', 'Portraits', etc.).
 */
class AiPhotoOrganizerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_ORGANIZE_ALL

        if (action == ACTION_CANCEL) {
            activeJob?.cancel()
            AiOrganizerStatusBus.resetToIdle()
            stopSelf(startId)
            return START_NOT_STICKY
        }

        val moveIntoCategoryAlbums = intent?.getBooleanExtra(EXTRA_MOVE_ALBUMS, true) ?: true
        val onlyUntagged = intent?.getBooleanExtra(EXTRA_ONLY_UNTAGGED, false) ?: false
        val targetPhotoIds = intent?.getLongArrayExtra(EXTRA_PHOTO_IDS)?.toList().orEmpty()

        activeJob?.cancel()
        activeJob = serviceScope.launch {
            val db = GalleryDatabase.getInstance(applicationContext)
            val photoDao = db.photoDao()
            var processedCount = 0

            try {
                val candidatePhotos = if (targetPhotoIds.isNotEmpty()) {
                    photoDao.getPhotosByIds(targetPhotoIds)
                } else {
                    val allVisible = photoDao.getVisiblePhotosOnce()
                    if (onlyUntagged) {
                        val untagged = allVisible.filter { photo ->
                            !photo.tagList.any { it.equals("AI Tagged", ignoreCase = true) }
                        }
                        untagged.ifEmpty { allVisible }
                    } else {
                        allVisible
                    }
                }

                if (candidatePhotos.isEmpty()) {
                    AiOrganizerStatusBus.updateState(
                        AiOrganizerUiState.Completed(
                            organizedCount = 0,
                            results = emptyList(),
                            categoryCounts = emptyMap()
                        )
                    )
                    stopSelf(startId)
                    return@launch
                }

                val sessionResults = mutableListOf<AiPhotoAnalysisResult>()
                val total = candidatePhotos.size

                for ((index, photo) in candidatePhotos.withIndex()) {
                    AiOrganizerStatusBus.updateState(
                        AiOrganizerUiState.Running(
                            currentPhotoTitle = photo.title,
                            processedCount = index,
                            totalCount = total,
                            latestResult = sessionResults.lastOrNull()
                        )
                    )

                    val result = AiPhotoCategorizerEngine.analyzeAndOrganizePhoto(
                        photo = photo,
                        photoDao = photoDao,
                        moveIntoCategoryAlbum = moveIntoCategoryAlbums,
                        context = applicationContext
                    )
                    processedCount++
                    sessionResults.add(result)
                    AiOrganizerStatusBus.recordResult(result)

                    AiOrganizerStatusBus.updateState(
                        AiOrganizerUiState.Running(
                            currentPhotoTitle = photo.title,
                            processedCount = processedCount,
                            totalCount = total,
                            latestResult = result
                        )
                    )
                }

                val breakdown = sessionResults
                    .groupingBy { it.primaryCategory }
                    .eachCount()

                AiOrganizerStatusBus.updateState(
                    AiOrganizerUiState.Completed(
                        organizedCount = sessionResults.size,
                        results = sessionResults,
                        categoryCounts = breakdown
                    )
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(AI_SERVICE_TAG, "AI Photo categorization timed out", e)
                AiOrganizerStatusBus.updateState(
                    AiOrganizerUiState.Error(
                        message = "AI categorization timed out after 60 seconds (processed $processedCount photo(s)). Please try again.",
                        processedCount = processedCount
                    )
                )
            } catch (e: CancellationException) {
                // Always rethrow CancellationException to preserve cooperative coroutine cancellation
                throw e
            } catch (e: Exception) {
                Log.e(AI_SERVICE_TAG, "AI Photo categorization failed", e)
                AiOrganizerStatusBus.updateState(
                    AiOrganizerUiState.Error(
                        message = e.localizedMessage ?: "AI photo categorization failed.",
                        processedCount = processedCount
                    )
                )
            } finally {
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_ORGANIZE_ALL = "com.example.service.action.ORGANIZE_ALL"
        const val ACTION_ORGANIZE_IDS = "com.example.service.action.ORGANIZE_IDS"
        const val ACTION_CANCEL = "com.example.service.action.CANCEL"

        const val EXTRA_MOVE_ALBUMS = "extra_move_albums"
        const val EXTRA_ONLY_UNTAGGED = "extra_only_untagged"
        const val EXTRA_PHOTO_IDS = "extra_photo_ids"

        fun startOrganizeLibrary(
            context: Context,
            moveIntoCategoryAlbums: Boolean = true,
            onlyUntagged: Boolean = false
        ) {
            val intent = Intent(context, AiPhotoOrganizerService::class.java).apply {
                action = ACTION_ORGANIZE_ALL
                putExtra(EXTRA_MOVE_ALBUMS, moveIntoCategoryAlbums)
                putExtra(EXTRA_ONLY_UNTAGGED, onlyUntagged)
            }
            context.startService(intent)
        }

        fun startOrganizePhotosByIds(
            context: Context,
            photoIds: List<Long>,
            moveIntoCategoryAlbums: Boolean = true
        ) {
            if (photoIds.isEmpty()) return
            val intent = Intent(context, AiPhotoOrganizerService::class.java).apply {
                action = ACTION_ORGANIZE_IDS
                putExtra(EXTRA_PHOTO_IDS, photoIds.toLongArray())
                putExtra(EXTRA_MOVE_ALBUMS, moveIntoCategoryAlbums)
            }
            context.startService(intent)
        }

        fun cancelRunningService(context: Context) {
            val intent = Intent(context, AiPhotoOrganizerService::class.java).apply {
                action = ACTION_CANCEL
            }
            context.startService(intent)
        }
    }
}
