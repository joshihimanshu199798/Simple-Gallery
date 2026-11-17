package com.example.ui.screens

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.data.formatBytes
import com.example.ui.theme.CompleteUiThemeSpec
import com.example.ui.theme.LocalCompleteUiTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Represents an image entry fetched from the device's local storage via the Android [MediaStore] API.
 */
data class MediaStorePhotoItem(
    val id: Long,
    val contentUri: Uri,
    val displayName: String,
    val bucketName: String,
    val dateAddedMillis: Long,
    val dateTakenMillis: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val rawFilePath: String = ""
) {
    val cleanTitle: String
        get() = displayName.substringBeforeLast(".")
            .replace("_", " ")
            .replace("-", " ")
            .ifBlank { "IMG_$id" }

    val formattedDate: String
        get() = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(dateTakenMillis))

    val resolutionLabel: String
        get() = if (width > 0 && height > 0) "${width}×${height}" else "HD"

    val formattedSize: String
        get() = formatBytes(sizeBytes)
}

/**
 * Queries the device's local storage for images using the Android [MediaStore] API
 * ([MediaStore.Images.Media.EXTERNAL_CONTENT_URI]).
 */
suspend fun fetchLocalMediaStoreImages(
    context: Context,
    fallbackGalleryPhotos: List<PhotoEntity> = emptyList()
): List<MediaStorePhotoItem> = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val prefs = context.getSharedPreferences("simple_gallery_prefs", Context.MODE_PRIVATE)
    val deletedLocalPaths = prefs.getStringSet("deleted_local_photo_paths", emptySet()) ?: emptySet()

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.DATE_TAKEN,
        MediaStore.Images.Media.DATE_MODIFIED,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
        MediaStore.Images.Media.MIME_TYPE,
        MediaStore.Images.Media.DATA
    )

    val queryUris = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL))
        }
        add(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }.distinct()

    val results = mutableListOf<MediaStorePhotoItem>()
    val seenIds = HashSet<Long>()
    val seenUris = HashSet<String>()

    for (collectionUri in queryUris) {
        val cursor = try {
            resolver.query(
                collectionUri,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )
        } catch (_: Exception) {
            try {
                resolver.query(
                    collectionUri,
                    arrayOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media.DATE_TAKEN,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.WIDTH,
                        MediaStore.Images.Media.HEIGHT,
                        MediaStore.Images.Media.MIME_TYPE
                    ),
                    null,
                    null,
                    "${MediaStore.Images.Media.DATE_ADDED} DESC"
                )
            } catch (_: Exception) {
                null
            }
        }

        cursor?.use { c ->
            val idCol = c.getColumnIndex(MediaStore.Images.Media._ID)
            if (idCol < 0) return@use
            val nameCol = c.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            val bucketCol = c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val dateAddedCol = c.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
            val dateTakenCol = c.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
            val dateModCol = c.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)
            val sizeCol = c.getColumnIndex(MediaStore.Images.Media.SIZE)
            val widthCol = c.getColumnIndex(MediaStore.Images.Media.WIDTH)
            val heightCol = c.getColumnIndex(MediaStore.Images.Media.HEIGHT)
            val mimeCol = c.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
            val dataCol = c.getColumnIndex(MediaStore.Images.Media.DATA)

            while (c.moveToNext()) {
                val mediaId = if (!c.isNull(idCol)) c.getLong(idCol) else continue
                if (!seenIds.add(mediaId)) continue

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    mediaId
                )
                val uriString = contentUri.toString()

                val rawDataPath = if (dataCol >= 0 && !c.isNull(dataCol)) {
                    c.getString(dataCol).orEmpty()
                } else {
                    ""
                }

                if (uriString in deletedLocalPaths ||
                    "media_id:$mediaId" in deletedLocalPaths ||
                    (rawDataPath.isNotEmpty() && rawDataPath in deletedLocalPaths)
                ) {
                    continue
                }

                val displayName = if (nameCol >= 0 && !c.isNull(nameCol)) {
                    c.getString(nameCol) ?: "IMG_$mediaId.jpg"
                } else {
                    rawDataPath.substringAfterLast('/').ifBlank { "IMG_$mediaId.jpg" }
                }

                if (PhotoImageProcessor.isStarterSamplePath(displayName)) continue

                val bucketName = if (bucketCol >= 0 && !c.isNull(bucketCol)) {
                    c.getString(bucketCol)?.trim()?.takeIf { it.isNotEmpty() } ?: "Camera Roll"
                } else {
                    rawDataPath.substringBeforeLast('/', "")
                        .substringAfterLast('/')
                        .takeIf { it.isNotEmpty() } ?: "Camera Roll"
                }

                val rawAdded = if (dateAddedCol >= 0 && !c.isNull(dateAddedCol)) c.getLong(dateAddedCol) else 0L
                val rawTaken = if (dateTakenCol >= 0 && !c.isNull(dateTakenCol)) c.getLong(dateTakenCol) else 0L
                val rawMod = if (dateModCol >= 0 && !c.isNull(dateModCol)) c.getLong(dateModCol) else 0L

                val dateAddedMs = when {
                    rawAdded > 1_000_000_000_000L -> rawAdded
                    rawAdded > 0L -> rawAdded * 1000L
                    else -> System.currentTimeMillis()
                }
                val dateTakenMs = when {
                    rawTaken > 1_000_000_000_000L -> rawTaken
                    rawTaken > 0L -> rawTaken * 1000L
                    rawAdded > 0L -> dateAddedMs
                    rawMod > 1_000_000_000_000L -> rawMod
                    rawMod > 0L -> rawMod * 1000L
                    else -> dateAddedMs
                }

                val sizeBytes = (if (sizeCol >= 0 && !c.isNull(sizeCol)) c.getLong(sizeCol) else 0L)
                    .takeIf { it > 0L } ?: 250_000L
                val width = (if (widthCol >= 0 && !c.isNull(widthCol)) c.getInt(widthCol) else 0)
                    .takeIf { it > 0 } ?: 1080
                val height = (if (heightCol >= 0 && !c.isNull(heightCol)) c.getInt(heightCol) else 0)
                    .takeIf { it > 0 } ?: 1080
                val mimeType = (if (mimeCol >= 0 && !c.isNull(mimeCol)) c.getString(mimeCol) else null)
                    ?: "image/jpeg"

                seenUris.add(uriString)
                if (rawDataPath.isNotEmpty()) seenUris.add(rawDataPath)

                results.add(
                    MediaStorePhotoItem(
                        id = mediaId,
                        contentUri = contentUri,
                        displayName = displayName,
                        bucketName = bucketName,
                        dateAddedMillis = dateAddedMs,
                        dateTakenMillis = dateTakenMs,
                        sizeBytes = sizeBytes,
                        width = width,
                        height = height,
                        mimeType = mimeType,
                        rawFilePath = rawDataPath
                    )
                )
            }
        }
    }

    // Fallback scan of public media directories (DCIM, Pictures, Downloads, Screenshots)
    // in case files on the device have not yet been indexed by MediaScanner.
    val publicDirs = listOfNotNull(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS)
        } else null
    )
    val validExtensions = setOf("jpg", "jpeg", "png", "webp", "heic", "heif")
    for (dir in publicDirs) {
        if (!dir.exists() || !dir.canRead()) continue
        try {
            dir.walkTopDown()
                .maxDepth(3)
                .filter { file ->
                    file.isFile &&
                        file.canRead() &&
                        file.length() > 0L &&
                        !file.name.startsWith(".") &&
                        file.extension.lowercase() in validExtensions &&
                        !PhotoImageProcessor.isStarterSamplePath(file.name)
                }
                .take(150)
                .forEach { file ->
                    val absPath = file.absolutePath
                    val fileUri = Uri.fromFile(file)
                    if (absPath !in seenUris &&
                        fileUri.toString() !in seenUris &&
                        absPath !in deletedLocalPaths
                    ) {
                        val syntheticId = absPath.hashCode().toLong() and 0x7FFFFFFFL
                        if (seenIds.add(syntheticId)) {
                            seenUris.add(absPath)
                            val folder = file.parentFile?.name?.takeIf { it.isNotEmpty() } ?: "Device Storage"
                            results.add(
                                MediaStorePhotoItem(
                                    id = syntheticId,
                                    contentUri = fileUri,
                                    displayName = file.name,
                                    bucketName = folder,
                                    dateAddedMillis = file.lastModified(),
                                    dateTakenMillis = file.lastModified(),
                                    sizeBytes = file.length(),
                                    width = 1080,
                                    height = 1080,
                                    mimeType = "image/${file.extension.lowercase()}",
                                    rawFilePath = absPath
                                )
                            )
                        }
                    }
                }
        } catch (_: Exception) {
        }
    }

    // Also include any local gallery photos already imported into the app database that aren't duplicated
    for (entity in fallbackGalleryPhotos) {
        if (entity.filePath !in seenUris && entity.filePath !in deletedLocalPaths) {
            seenUris.add(entity.filePath)
            val parsedUri = if (entity.filePath.startsWith("content://") || entity.filePath.startsWith("file://")) {
                Uri.parse(entity.filePath)
            } else {
                Uri.fromFile(File(entity.filePath))
            }
            val itemId = if (seenIds.add(entity.id)) entity.id else (entity.id + 1_000_000L)
            results.add(
                MediaStorePhotoItem(
                    id = itemId,
                    contentUri = parsedUri,
                    displayName = entity.title,
                    bucketName = entity.albumName.ifBlank { "Camera Roll" },
                    dateAddedMillis = entity.dateTaken,
                    dateTakenMillis = entity.dateTaken,
                    sizeBytes = entity.sizeBytes,
                    width = entity.width,
                    height = entity.height,
                    mimeType = entity.mimeType,
                    rawFilePath = entity.filePath
                )
            )
        }
    }

    results.sortedByDescending { it.dateTakenMillis }
}

/**
 * Deletes the specified [MediaStorePhotoItem] entries from local storage / MediaStore.
 */
suspend fun deleteLocalMediaStoreImages(
    context: Context,
    items: List<MediaStorePhotoItem>
): Int = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val prefs = context.getSharedPreferences("simple_gallery_prefs", Context.MODE_PRIVATE)
    val deletedSet = (prefs.getStringSet("deleted_local_photo_paths", emptySet()) ?: emptySet()).toMutableSet()
    var removedCount = 0

    for (item in items) {
        val uriStr = item.contentUri.toString()
        deletedSet.add(uriStr)
        deletedSet.add("media_id:${item.id}")
        if (item.rawFilePath.isNotEmpty()) {
            deletedSet.add(item.rawFilePath)
        }
        try {
            if (uriStr.startsWith("content://")) {
                resolver.delete(item.contentUri, null, null)
            }
        } catch (_: Exception) {
        }
        try {
            if (item.rawFilePath.isNotEmpty() && !item.rawFilePath.startsWith("content://")) {
                val rawFile = File(item.rawFilePath)
                if (rawFile.exists()) {
                    rawFile.delete()
                }
            }
        } catch (_: Exception) {
        }
        removedCount++
    }

    prefs.edit().putStringSet("deleted_local_photo_paths", deletedSet).apply()
    removedCount
}

/**
 * `PhotoGalleryScreen` component using `LazyVerticalGrid` to display images from local storage,
 * fetching them using the Android `MediaStore` API (`MediaStore.Images.Media.EXTERNAL_CONTENT_URI`).
 *
 * Includes:
 * - Automatic & manual `MediaStore` querying with `ContentObserver` real-time updates
 * - Runtime storage permission handling (`READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE`) via Accompanist Permissions
 * - Pull-to-refresh (`PullToRefreshBox`) to trigger a re-scan of device storage
 * - Folder / Bucket (`BUCKET_DISPLAY_NAME`) filtering & search bar
 * - Configurable `LazyVerticalGrid` column density (`2×`, `3×`, `4×`)
 * - Multi-select & delete from local storage with confirmation dialog
 * - Built-in full-screen pinch-to-zoom image viewer when an image is tapped
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun PhotoGalleryScreen(
    modifier: Modifier = Modifier,
    activeTheme: CompleteUiThemeSpec = LocalCompleteUiTheme.current,
    fallbackPhotos: List<PhotoEntity> = emptyList(),
    initialColumns: Int = 3,
    onBackClick: (() -> Unit)? = null,
    onOpenInGalleryLightbox: ((PhotoEntity) -> Unit)? = null,
    onImportPhotosClick: (() -> Unit)? = null,
    onTakeCameraPhotoClick: (() -> Unit)? = null,
    onDeleteLinkedGalleryPhotos: ((List<String>) -> Unit)? = null,
    onRescanTriggered: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    fun hasSystemMediaReadPermission(ctx: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val fullGranted = ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
            val partialGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                false
            }
            fullGranted || partialGranted
        } else {
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    val primaryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var mediaStoreImages by remember { mutableStateOf<List<MediaStorePhotoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedBucket by remember { mutableStateOf("All Folders") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var gridColumns by remember { mutableIntStateOf(initialColumns.coerceIn(2, 4)) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var itemsPendingDeletion by remember { mutableStateOf<List<MediaStorePhotoItem>>(emptyList()) }
    var activePreviewItem by remember { mutableStateOf<MediaStorePhotoItem?>(null) }

    fun loadImagesFromMediaStore(showRefreshIndicator: Boolean = false) {
        coroutineScope.launch {
            if (showRefreshIndicator) {
                isRefreshing = true
            } else if (mediaStoreImages.isEmpty()) {
                isLoading = true
            }
            val startMs = System.currentTimeMillis()
            val fetched = fetchLocalMediaStoreImages(
                context = context,
                fallbackGalleryPhotos = fallbackPhotos
            )
            if (showRefreshIndicator) {
                val elapsed = System.currentTimeMillis() - startMs
                if (elapsed < 400L) {
                    delay(400L - elapsed)
                }
            }
            mediaStoreImages = fetched
            isLoading = false
            isRefreshing = false
        }
    }

    val permissionState = rememberPermissionState(permission = primaryPermission) { granted ->
        if (granted || hasSystemMediaReadPermission(context)) {
            loadImagesFromMediaStore(showRefreshIndicator = false)
            onRescanTriggered?.invoke()
        }
    }

    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> listOf(
                Manifest.permission.READ_MEDIA_IMAGES
            )
            else -> listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    ) { result ->
        if (result.values.any { it } || hasSystemMediaReadPermission(context)) {
            loadImagesFromMediaStore(showRefreshIndicator = false)
            onRescanTriggered?.invoke()
        }
    }

    var systemPermissionGranted by remember { mutableStateOf(hasSystemMediaReadPermission(context)) }
    val hasMediaPermission = permissionState.status.isGranted ||
        multiplePermissionsState.permissions.any { it.status.isGranted } ||
        systemPermissionGranted

    val requestStoragePermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    val openAppSettings = {
        try {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    // Load MediaStore images on initial composition & whenever fallbackPhotos or permissions update
    LaunchedEffect(hasMediaPermission, fallbackPhotos.size) {
        loadImagesFromMediaStore(showRefreshIndicator = false)
    }

    // Register a MediaStore ContentObserver so new device photos appear immediately
    DisposableEffect(context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                loadImagesFromMediaStore(showRefreshIndicator = false)
            }
        }
        try {
            context.contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )
        } catch (_: Exception) {
        }
        onDispose {
            try {
                context.contentResolver.unregisterContentObserver(observer)
            } catch (_: Exception) {
            }
        }
    }

    // Re-check permission & rescan when returning from system Settings or Camera
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = hasSystemMediaReadPermission(context)
                systemPermissionGranted = granted
                if (granted) {
                    loadImagesFromMediaStore(showRefreshIndicator = false)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Extract unique MediaStore folders (buckets) with counts
    val buckets = remember(mediaStoreImages) {
        val grouped = mediaStoreImages.groupBy { it.bucketName }
        listOf("All Folders" to mediaStoreImages.size) +
            grouped.entries
                .sortedByDescending { it.value.size }
                .map { it.key to it.value.size }
    }

    val filteredImages = remember(mediaStoreImages, selectedBucket, searchQuery) {
        val query = searchQuery.trim().lowercase()
        mediaStoreImages.filter { item ->
            val matchesBucket = selectedBucket == "All Folders" ||
                item.bucketName.equals(selectedBucket, ignoreCase = true)
            val matchesQuery = query.isEmpty() ||
                item.displayName.lowercase().contains(query) ||
                item.cleanTitle.lowercase().contains(query) ||
                item.bucketName.lowercase().contains(query)
            matchesBucket && matchesQuery
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        activeTheme.background,
                        activeTheme.backgroundSecondary
                    )
                )
            )
            .testTag("photo_gallery_screen")
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                onRescanTriggered?.invoke()
                loadImagesFromMediaStore(showRefreshIndicator = true)
            },
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    containerColor = activeTheme.surface,
                    color = activeTheme.primaryAccent,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("photo_gallery_pull_to_refresh")
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("photo_gallery_lazy_vertical_grid"),
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 10.dp,
                    top = 8.dp,
                    bottom = 112.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(if (gridColumns >= 3) 6.dp else 8.dp),
                verticalArrangement = Arrangement.spacedBy(if (gridColumns >= 3) 6.dp else 8.dp)
            ) {
                // 1. Top Header Card: Title, MediaStore stats, Search, Grid Columns & Rescan Controls
                item(
                    key = "mediastore_header_section",
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = activeTheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(18.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (onBackClick != null) {
                                            Surface(
                                                shape = CircleShape,
                                                color = activeTheme.surfaceVariant,
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .clickable { onBackClick() }
                                                    .testTag("photo_gallery_back_button")
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back",
                                                        tint = activeTheme.textPrimary,
                                                        modifier = Modifier.size(19.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Surface(
                                                shape = CircleShape,
                                                color = activeTheme.primaryAccent.copy(alpha = 0.15f),
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.PhotoLibrary,
                                                        contentDescription = null,
                                                        tint = activeTheme.primaryAccent,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = "LOCAL MEDIASTORE GALLERY",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 0.5.sp,
                                                    fontSize = 12.5.sp
                                                ),
                                                color = activeTheme.primaryAccent,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${filteredImages.size} of ${mediaStoreImages.size} local photos • ${(buckets.size - 1).coerceAtLeast(1)} device folders",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = activeTheme.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Search Toggle
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSearchVisible || searchQuery.isNotEmpty()) {
                                                activeTheme.primaryAccent
                                            } else {
                                                activeTheme.surfaceVariant
                                            },
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .clickable { isSearchVisible = !isSearchVisible }
                                                .testTag("photo_gallery_search_toggle")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = "Search MediaStore images",
                                                    tint = if (isSearchVisible || searchQuery.isNotEmpty()) {
                                                        activeTheme.onPrimaryAccent
                                                    } else {
                                                        activeTheme.textPrimary
                                                    },
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }
                                        }

                                        // Column Density Switcher (2x / 3x / 4x)
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = activeTheme.surfaceVariant,
                                            modifier = Modifier
                                                .height(38.dp)
                                                .clip(RoundedCornerShape(50))
                                                .clickable {
                                                    gridColumns = when (gridColumns) {
                                                        2 -> 3
                                                        3 -> 4
                                                        else -> 2
                                                    }
                                                }
                                                .testTag("photo_gallery_column_toggle")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.GridView,
                                                    contentDescription = "Change grid columns",
                                                    tint = activeTheme.primaryAccent,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                                Text(
                                                    text = "${gridColumns}×",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold
                                                    ),
                                                    color = activeTheme.textPrimary
                                                )
                                            }
                                        }

                                        // Multi-select Toggle
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelectionMode || selectedIds.isNotEmpty()) {
                                                activeTheme.primaryAccent
                                            } else {
                                                activeTheme.surfaceVariant
                                            },
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    if (isSelectionMode || selectedIds.isNotEmpty()) {
                                                        isSelectionMode = false
                                                        selectedIds = emptySet()
                                                    } else {
                                                        isSelectionMode = true
                                                    }
                                                }
                                                .testTag("photo_gallery_select_mode_button")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Checklist,
                                                    contentDescription = "Toggle multi-selection",
                                                    tint = if (isSelectionMode || selectedIds.isNotEmpty()) {
                                                        activeTheme.onPrimaryAccent
                                                    } else {
                                                        activeTheme.textPrimary
                                                    },
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }
                                        }

                                        // Rescan MediaStore Button
                                        Surface(
                                            shape = CircleShape,
                                            color = activeTheme.surfaceVariant,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    if (!hasMediaPermission) {
                                                        requestStoragePermission()
                                                    }
                                                    onRescanTriggered?.invoke()
                                                    loadImagesFromMediaStore(showRefreshIndicator = true)
                                                }
                                                .testTag("photo_gallery_rescan_button")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Rescan MediaStore",
                                                    tint = activeTheme.primaryAccent,
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Expandable Search Bar
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = isSearchVisible || searchQuery.isNotEmpty(),
                                    enter = expandVertically(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    ) + fadeIn(tween(180)),
                                    exit = shrinkVertically(tween(150)) + fadeOut(tween(130))
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("photo_gallery_search_input"),
                                        placeholder = {
                                            Text(
                                                "Filter local images by filename or folder…",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { searchQuery = "" }) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Clear search",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = activeTheme.background,
                                            unfocusedContainerColor = activeTheme.background.copy(alpha = 0.7f)
                                        )
                                    )
                                }

                                // Horizontal MediaStore Bucket / Folder Chips
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("photo_gallery_bucket_filter_row"),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(buckets, key = { "bucket_${it.first}" }) { (bucketName, count) ->
                                        val isSelected = selectedBucket.equals(bucketName, ignoreCase = true)
                                        val chipScale by animateFloatAsState(
                                            targetValue = if (isSelected) 1.03f else 1f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            label = "bucket_chip_scale"
                                        )
                                        val chipBg by animateColorAsState(
                                            targetValue = if (isSelected) {
                                                activeTheme.primaryAccent
                                            } else {
                                                activeTheme.surfaceVariant
                                            },
                                            label = "bucket_chip_bg"
                                        )
                                        val chipText by animateColorAsState(
                                            targetValue = if (isSelected) {
                                                activeTheme.onPrimaryAccent
                                            } else {
                                                activeTheme.textPrimary
                                            },
                                            label = "bucket_chip_text"
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = chipBg,
                                            modifier = Modifier
                                                .scale(chipScale)
                                                .clip(RoundedCornerShape(50))
                                                .clickable { selectedBucket = bucketName }
                                                .testTag("bucket_chip_${bucketName.lowercase().replace(" ", "_")}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Folder,
                                                    contentDescription = null,
                                                    tint = chipText,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "$bucketName ($count)",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                                    ),
                                                    color = chipText
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Contextual Multi-Select Action Banner
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSelectionMode || selectedIds.isNotEmpty(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            val selectedItems = remember(mediaStoreImages, selectedIds) {
                                mediaStoreImages.filter { it.id in selectedIds }
                            }
                            val totalBytes = remember(selectedItems) {
                                selectedItems.sumOf { it.sizeBytes }
                            }
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = activeTheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, activeTheme.primaryAccent, RoundedCornerShape(16.dp))
                                    .testTag("photo_gallery_selection_banner")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (selectedIds.isEmpty()) {
                                                "Tap photos to select"
                                            } else {
                                                "${selectedIds.size} Selected • ${formatBytes(totalBytes)}"
                                            },
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = activeTheme.textPrimary
                                        )
                                        Text(
                                            text = "Select local MediaStore images to delete from device",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = activeTheme.textSecondary
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Select All / Deselect All
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = activeTheme.surfaceVariant,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .clickable {
                                                    if (selectedIds.size == filteredImages.size && filteredImages.isNotEmpty()) {
                                                        selectedIds = emptySet()
                                                    } else {
                                                        selectedIds = filteredImages.map { it.id }.toSet()
                                                    }
                                                }
                                                .testTag("photo_gallery_select_all_button")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SelectAll,
                                                    contentDescription = "Select all",
                                                    tint = activeTheme.textPrimary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Text(
                                                    text = if (selectedIds.size == filteredImages.size && filteredImages.isNotEmpty()) {
                                                        "None"
                                                    } else {
                                                        "All"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = activeTheme.textPrimary
                                                )
                                            }
                                        }

                                        // Delete Selected Button
                                        if (selectedItems.isNotEmpty()) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = Color(0xFFE11D48),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .clickable {
                                                        itemsPendingDeletion = selectedItems
                                                    }
                                                    .testTag("photo_gallery_delete_selected_button")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteForever,
                                                        contentDescription = "Delete selected photos",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Text(
                                                        text = "Delete (${selectedItems.size})",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.ExtraBold
                                                        ),
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Permission Prompt Banner if READ_MEDIA_IMAGES / READ_EXTERNAL_STORAGE is not granted
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !hasMediaPermission
                        ) {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.2.dp,
                                        activeTheme.primaryAccent.copy(alpha = 0.6f),
                                        RoundedCornerShape(18.dp)
                                    )
                                    .testTag("photo_gallery_permission_card")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Local Storage MediaStore Permission",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = activeTheme.textPrimary
                                    )
                                    Text(
                                        text = if (permissionState.status.shouldShowRationale) {
                                            "Storage permission (READ_MEDIA_IMAGES / READ_EXTERNAL_STORAGE) is required to query MediaStore and display local photos from your device."
                                        } else {
                                            "Grant photo access to scan and display all images from your device's local MediaStore."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = activeTheme.textSecondary
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = requestStoragePermission,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = activeTheme.primaryAccent,
                                                contentColor = activeTheme.onPrimaryAccent
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("photo_gallery_request_permission_button")
                                        ) {
                                            Text(
                                                text = "Grant Photo Access",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = openAppSettings,
                                            modifier = Modifier.testTag("photo_gallery_open_settings_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Settings")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Loading, Empty State, or MediaStore LazyVerticalGrid Items
                if (isLoading && filteredImages.isEmpty()) {
                    item(
                        key = "mediastore_loading_state",
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = activeTheme.primaryAccent)
                                Text(
                                    text = "Scanning device local storage via MediaStore…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = activeTheme.textSecondary
                                )
                            }
                        }
                    }
                } else if (filteredImages.isEmpty()) {
                    item(
                        key = "mediastore_empty_state",
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(22.dp))
                                .testTag("photo_gallery_empty_state")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = activeTheme.primaryAccent.copy(alpha = 0.14f),
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            tint = activeTheme.primaryAccent,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (!hasMediaPermission) {
                                        "Allow Access to Scan Device Photos"
                                    } else if (searchQuery.isNotBlank() || selectedBucket != "All Folders") {
                                        "No Matching Local Photos"
                                    } else {
                                        "No Local Photos Found in MediaStore"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = activeTheme.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Pull down to re-scan MediaStore.Images.Media.EXTERNAL_CONTENT_URI, capture a camera shot, or select images from your device.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = activeTheme.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (!hasMediaPermission) {
                                                requestStoragePermission()
                                            }
                                            onRescanTriggered?.invoke()
                                            loadImagesFromMediaStore(showRefreshIndicator = true)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = activeTheme.primaryAccent,
                                            contentColor = activeTheme.onPrimaryAccent
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("photo_gallery_empty_rescan_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Rescan MediaStore", fontWeight = FontWeight.Bold)
                                    }
                                    if (onImportPhotosClick != null) {
                                        OutlinedButton(
                                            onClick = onImportPhotosClick,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("photo_gallery_empty_pick_button")
                                        ) {
                                            Icon(
                                                Icons.Default.AddPhotoAlternate,
                                                contentDescription = null,
                                                modifier = Modifier.size(17.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Pick Photos")
                                        }
                                    }
                                }
                                if (onTakeCameraPhotoClick != null) {
                                    OutlinedButton(
                                        onClick = onTakeCameraPhotoClick,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("photo_gallery_empty_camera_button")
                                    ) {
                                        Icon(
                                            Icons.Default.PhotoCamera,
                                            contentDescription = null,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Take Camera Photo")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(
                        items = filteredImages,
                        key = { item -> "mediastore_${item.id}_${item.contentUri}" },
                        span = { GridItemSpan(1) }
                    ) { item ->
                        val isSelected = item.id in selectedIds
                        Box(modifier = Modifier.animateItem()) {
                            MediaStoreGridPhotoCard(
                                item = item,
                                activeTheme = activeTheme,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode || selectedIds.isNotEmpty(),
                                isCompact = gridColumns >= 3,
                                onClick = {
                                    if (isSelectionMode || selectedIds.isNotEmpty()) {
                                        selectedIds = if (isSelected) {
                                            selectedIds - item.id
                                        } else {
                                            selectedIds + item.id
                                        }
                                    } else {
                                        val matchingEntity = fallbackPhotos.firstOrNull {
                                            it.filePath == item.contentUri.toString() ||
                                                (item.rawFilePath.isNotEmpty() && it.filePath == item.rawFilePath)
                                        }
                                        if (matchingEntity != null && onOpenInGalleryLightbox != null) {
                                            onOpenInGalleryLightbox(matchingEntity)
                                        } else {
                                            activePreviewItem = item
                                        }
                                    }
                                },
                                onLongClick = {
                                    isSelectionMode = true
                                    selectedIds = if (isSelected) {
                                        selectedIds - item.id
                                    } else {
                                        selectedIds + item.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Built-in Full-Screen Pinch-to-Zoom Image Preview Overlay
        val currentPreview = activePreviewItem
        if (currentPreview != null) {
            MediaStorePinchZoomViewerOverlay(
                item = currentPreview,
                onDismiss = { activePreviewItem = null },
                onRequestDelete = {
                    activePreviewItem = null
                    itemsPendingDeletion = listOf(currentPreview)
                }
            )
        }

        // Local Storage Deletion Confirmation Dialog
        if (itemsPendingDeletion.isNotEmpty()) {
            val targetItems = itemsPendingDeletion
            val totalDeleteBytes = remember(targetItems) {
                targetItems.sumOf { it.sizeBytes }
            }
            AlertDialog(
                onDismissRequest = { itemsPendingDeletion = emptyList() },
                modifier = Modifier.testTag("photo_gallery_delete_dialog"),
                icon = {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(
                        text = if (targetItems.size == 1) {
                            "Delete Photo from Local Storage?"
                        } else {
                            "Delete ${targetItems.size} Selected Photos?"
                        },
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                text = {
                    Text(
                        text = if (targetItems.size == 1) {
                            "Permanently delete \"${targetItems.first().cleanTitle}\" (${formatBytes(totalDeleteBytes)}) from your device's local storage?"
                        } else {
                            "Permanently delete ${targetItems.size} selected photos (${formatBytes(totalDeleteBytes)}) from your device's local storage?"
                        }
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        onClick = {
                            val toDelete = targetItems
                            itemsPendingDeletion = emptyList()
                            coroutineScope.launch {
                                deleteLocalMediaStoreImages(context, toDelete)
                                val paths = toDelete.flatMap {
                                    listOfNotNull(
                                        it.contentUri.toString(),
                                        it.rawFilePath.takeIf { p -> p.isNotEmpty() }
                                    )
                                }
                                onDeleteLinkedGalleryPhotos?.invoke(paths)
                                selectedIds = selectedIds - toDelete.map { it.id }.toSet()
                                if (selectedIds.isEmpty()) {
                                    isSelectionMode = false
                                }
                                loadImagesFromMediaStore(showRefreshIndicator = false)
                            }
                        },
                        modifier = Modifier.testTag("photo_gallery_confirm_delete_button")
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { itemsPendingDeletion = emptyList() },
                        modifier = Modifier.testTag("photo_gallery_cancel_delete_button")
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaStoreGridPhotoCard(
    item: MediaStorePhotoItem,
    activeTheme: CompleteUiThemeSpec,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isCompact: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "mediastore_card_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.5.dp else 0.8.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "mediastore_card_border"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) activeTheme.primaryAccent else activeTheme.borderStroke,
        label = "mediastore_card_border_color"
    )

    val context = LocalContext.current
    val thumbnailRequest = remember(item.id, item.contentUri, item.sizeBytes) {
        PhotoImageProcessor.buildThumbnailRequest(context, item.contentUri.toString())
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(cardScale)
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("photo_gallery_grid_item_${item.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = thumbnailRequest,
                contentDescription = item.cleanTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom gradient with title & folder badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.72f)
                            )
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.cleanTitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isCompact) 10.sp else 11.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!isCompact) {
                        Text(
                            text = "${item.bucketName} • ${item.formattedSize}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = Color.White.copy(alpha = 0.82f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Selection Checkbox Badge
            if (isSelectionMode || isSelected) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) activeTheme.primaryAccent else Color.Black.copy(alpha = 0.45f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSelected) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.RadioButtonUnchecked
                            },
                            contentDescription = if (isSelected) "Selected" else "Not selected",
                            tint = if (isSelected) activeTheme.onPrimaryAccent else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaStorePinchZoomViewerOverlay(
    item: MediaStorePhotoItem,
    onDismiss: () -> Unit,
    onRequestDelete: () -> Unit
) {
    var scale by remember(item.id) { mutableFloatStateOf(1f) }
    var offset by remember(item.id) { mutableStateOf(Offset.Zero) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "preview_zoom_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.96f))
            .pointerInput(item.id) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                    scale = newScale
                    offset = if (newScale > 1f) {
                        Offset(
                            x = (offset.x + pan.x).coerceIn(-1000f, 1000f),
                            y = (offset.y + pan.y).coerceIn(-1000f, 1000f)
                        )
                    } else {
                        Offset.Zero
                    }
                }
            }
            .pointerInput(item.id) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.05f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            }
            .testTag("photo_gallery_fullscreen_viewer")
    ) {
        AsyncImage(
            model = PhotoImageProcessor.resolveCoilModel(item.contentUri.toString()),
            contentDescription = item.cleanTitle,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                    translationX = offset.x
                    translationY = offset.y
                }
        )

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("photo_gallery_viewer_close")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close viewer",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = item.cleanTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.bucketName} • ${item.resolutionLabel} • ${item.formattedSize} • ${item.formattedDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (scale > 1.05f) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.18f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                scale = 1f
                                offset = Offset.Zero
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Reset zoom",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${(scale * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onRequestDelete,
                    modifier = Modifier.testTag("photo_gallery_viewer_delete")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete from local storage",
                        tint = Color(0xFFFB7185)
                    )
                }
            }
        }
    }
}
