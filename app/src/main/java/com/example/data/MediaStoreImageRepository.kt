package com.example.data

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Represents a single image discovered in the Android [MediaStore].
 *
 * [displayPath] provides a URI/path string ready for Coil and Compose UI rendering
 * (`content://` URI on MediaStore-backed entries, or absolute file path for filesystem fallbacks),
 * while [rawFilePath] retains the filesystem path (`MediaStore.Images.Media.DATA`) when available.
 */
data class DeviceImageRecord(
    val mediaId: Long,
    val contentUri: Uri,
    val rawFilePath: String,
    val displayName: String,
    val bucketName: String,
    val relativePath: String,
    val dateAddedMillis: Long,
    val dateTakenMillis: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val mimeType: String
) {
    /** Scoped-storage safe path/URI string suitable for passing directly to UI image loaders. */
    val displayPath: String
        get() = contentUri.toString().ifBlank { rawFilePath }

    /** Best human-readable file path for file details or path-based UI lists. */
    val resolvedFilePath: String
        get() = rawFilePath.ifBlank { contentUri.toString() }
}

sealed interface MediaStoreScanState {
    data object Loading : MediaStoreScanState
    data class Success(
        val images: List<DeviceImageRecord>,
        val imagePaths: List<String>
    ) : MediaStoreScanState

    data class Error(val message: String) : MediaStoreScanState
}

/**
 * Dedicated data repository that scans the device's [MediaStore] (`MediaStore.Images.Media`)
 * and retrieves validated image file paths / content URIs for display in the UI.
 */
class MediaStoreImageRepository(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val appContext = context.applicationContext
    private val contentResolver = appContext.contentResolver

    private fun readDeletedPathSet(): Set<String> {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_DELETED_LOCAL_PATHS, emptySet()) ?: emptySet()
    }

    /**
     * Scans the device's [MediaStore] and returns a list of image file paths/URIs ordered newest-first.
     *
     * @param preferContentUri When true (recommended for Android 10+ Scoped Storage Coil loading),
     * returns `content://media/external/images/media/<id>` URIs; when false, returns raw filesystem
     * paths (`DATA` column) falling back to content URIs if unavailable.
     */
    suspend fun getImageFilePaths(
        sinceTimestampSec: Long? = null,
        includePublicDirectoryFallback: Boolean = false,
        preferContentUri: Boolean = true
    ): List<String> = withContext(ioDispatcher) {
        scanDeviceImages(
            sinceTimestampSec = sinceTimestampSec,
            includePublicDirectoryFallback = includePublicDirectoryFallback
        ).map { record ->
            if (preferContentUri) record.displayPath else record.resolvedFilePath
        }
    }

    /**
     * Reactive stream of image file paths/URIs that automatically re-scans whenever
     * [MediaStore.Images.Media.EXTERNAL_CONTENT_URI] changes.
     */
    fun observeImageFilePaths(
        preferContentUri: Boolean = true
    ): Flow<List<String>> = observeDeviceImages()
        .map { records ->
            records.map { record ->
                if (preferContentUri) record.displayPath else record.resolvedFilePath
            }
        }
        .distinctUntilChanged()

    /**
     * Reactive stream of [DeviceImageRecord] entries backed by a [ContentObserver] on
     * [MediaStore.Images.Media.EXTERNAL_CONTENT_URI].
     */
    fun observeDeviceImages(): Flow<List<DeviceImageRecord>> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                launch {
                    trySend(scanDeviceImages())
                }
            }
        }

        trySend(scanDeviceImages())

        val registered = try {
            contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )
            true
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }

        awaitClose {
            if (registered) {
                try {
                    contentResolver.unregisterContentObserver(observer)
                } catch (_: Exception) {
                }
            }
        }
    }.flowOn(ioDispatcher)

    /**
     * Triggers Android's [MediaScannerConnection] on public image directories so newly saved
     * files are indexed into [MediaStore].
     */
    fun triggerPublicMediaScan() {
        try {
            val publicDirs = publicImageDirectories()
                .filter { it.exists() }
                .map { it.absolutePath }
                .toTypedArray()

            if (publicDirs.isNotEmpty()) {
                MediaScannerConnection.scanFile(appContext, publicDirs, null, null)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Scans [MediaStore.Images.Media] for valid images (filtering out non-media files, 0-byte rows,
     * starter demo filenames, and user-deleted entries) and returns them ordered newest-first.
     */
    suspend fun scanDeviceImages(
        sinceTimestampSec: Long? = null,
        includePublicDirectoryFallback: Boolean = false
    ): List<DeviceImageRecord> = withContext(ioDispatcher) {
        val deletedLocalPaths = readDeletedPathSet()

        val projection = buildList {
            add(MediaStore.Images.Media._ID)
            add(MediaStore.Images.Media.DISPLAY_NAME)
            add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            add(MediaStore.Images.Media.DATE_TAKEN)
            add(MediaStore.Images.Media.DATE_ADDED)
            add(MediaStore.Images.Media.DATE_MODIFIED)
            add(MediaStore.Images.Media.WIDTH)
            add(MediaStore.Images.Media.HEIGHT)
            add(MediaStore.Images.Media.SIZE)
            add(MediaStore.Images.Media.MIME_TYPE)
            add(MediaStore.Images.Media.DATA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Images.Media.RELATIVE_PATH)
            }
        }.toTypedArray()

        val fallbackProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE
        )

        val queryUris = if (sinceTimestampSec != null) {
            listOf(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        } else {
            buildList {
                add(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val volumeUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    if (volumeUri != MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                        add(volumeUri)
                    }
                }
            }
        }

        val selection = if (sinceTimestampSec != null) {
            "${MediaStore.Images.Media.DATE_ADDED} >= ? OR ${MediaStore.Images.Media.DATE_MODIFIED} >= ?"
        } else {
            null
        }
        val selectionArgs = if (sinceTimestampSec != null) {
            val threshold = sinceTimestampSec.coerceAtLeast(0L).toString()
            arrayOf(threshold, threshold)
        } else {
            null
        }

        val results = ArrayList<DeviceImageRecord>(256)
        val seenMediaIds = HashSet<Long>()
        val seenPaths = HashSet<String>()

        for (collectionUri in queryUris) {
            val cursor = try {
                contentResolver.query(
                    collectionUri,
                    projection,
                    selection,
                    selectionArgs,
                    "${MediaStore.Images.Media.DATE_ADDED} DESC"
                )
            } catch (_: SecurityException) {
                null
            } catch (_: Exception) {
                try {
                    contentResolver.query(
                        collectionUri,
                        fallbackProjection,
                        selection,
                        selectionArgs,
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
                val dateTakenCol = c.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                val dateAddedCol = c.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                val dateModCol = c.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)
                val widthCol = c.getColumnIndex(MediaStore.Images.Media.WIDTH)
                val heightCol = c.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                val sizeCol = c.getColumnIndex(MediaStore.Images.Media.SIZE)
                val mimeCol = c.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
                val dataCol = c.getColumnIndex(MediaStore.Images.Media.DATA)
                val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    c.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
                } else {
                    -1
                }

                while (c.moveToNext()) {
                    val mediaId = if (!c.isNull(idCol)) c.getLong(idCol) else continue
                    if (!seenMediaIds.add(mediaId)) continue

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        mediaId
                    )
                    val contentUriString = contentUri.toString()

                    val rawDataPath = if (dataCol >= 0 && !c.isNull(dataCol)) {
                        c.getString(dataCol).orEmpty()
                    } else {
                        ""
                    }

                    if (contentUriString in deletedLocalPaths ||
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

                    val rawMimeType = (if (mimeCol >= 0 && !c.isNull(mimeCol)) c.getString(mimeCol) else null)
                        ?.trim()
                        .orEmpty()
                    if (!PhotoImageProcessor.isValidMediaFile(rawMimeType, displayName.ifBlank { rawDataPath })) {
                        continue
                    }
                    if (sizeCol >= 0 && !c.isNull(sizeCol) && c.getLong(sizeCol) <= 0L) {
                        continue
                    }

                    val bucketName = if (bucketCol >= 0 && !c.isNull(bucketCol)) {
                        c.getString(bucketCol)?.trim()?.takeIf { it.isNotEmpty() } ?: "Camera Roll"
                    } else {
                        rawDataPath.substringBeforeLast('/', "")
                            .substringAfterLast('/')
                            .takeIf { it.isNotEmpty() } ?: "Camera Roll"
                    }

                    val relativePath = if (relPathCol >= 0 && !c.isNull(relPathCol)) {
                        c.getString(relPathCol).orEmpty()
                    } else {
                        rawDataPath.substringBeforeLast('/', "")
                    }

                    val rawDateTaken = if (dateTakenCol >= 0 && !c.isNull(dateTakenCol)) c.getLong(dateTakenCol) else 0L
                    val rawDateAdded = if (dateAddedCol >= 0 && !c.isNull(dateAddedCol)) c.getLong(dateAddedCol) else 0L
                    val rawDateMod = if (dateModCol >= 0 && !c.isNull(dateModCol)) c.getLong(dateModCol) else 0L

                    val dateAddedMs = normalizeTimestampMillis(rawDateAdded)
                    val dateTakenMs = normalizeTimestampMillis(
                        primary = rawDateTaken,
                        fallback1 = rawDateAdded,
                        fallback2 = rawDateMod
                    )

                    val width = (if (widthCol >= 0 && !c.isNull(widthCol)) c.getInt(widthCol) else 0)
                        .takeIf { it > 0 } ?: 1080
                    val height = (if (heightCol >= 0 && !c.isNull(heightCol)) c.getInt(heightCol) else 0)
                        .takeIf { it > 0 } ?: 1080
                    val sizeBytes = (if (sizeCol >= 0 && !c.isNull(sizeCol)) c.getLong(sizeCol) else 0L)
                        .takeIf { it > 0L } ?: 250_000L
                    val mimeType = rawMimeType.ifBlank { "image/jpeg" }

                    seenPaths.add(contentUriString)
                    if (rawDataPath.isNotEmpty()) seenPaths.add(rawDataPath)

                    results.add(
                        DeviceImageRecord(
                            mediaId = mediaId,
                            contentUri = contentUri,
                            rawFilePath = rawDataPath,
                            displayName = displayName,
                            bucketName = bucketName,
                            relativePath = relativePath,
                            dateAddedMillis = dateAddedMs,
                            dateTakenMillis = dateTakenMs,
                            sizeBytes = sizeBytes,
                            width = width,
                            height = height,
                            mimeType = mimeType
                        )
                    )
                }
            }
        }

        if (includePublicDirectoryFallback) {
            appendPublicDirectoryImages(
                results = results,
                seenMediaIds = seenMediaIds,
                seenPaths = seenPaths,
                deletedLocalPaths = deletedLocalPaths
            )
        }

        results
    }

    /**
     * Deletes the specified device image records via [ContentResolver] (and raw path fallback)
     * and records their paths in the deleted-paths preference set so they do not reappear.
     */
    suspend fun deleteDeviceImages(records: List<DeviceImageRecord>): Int = withContext(ioDispatcher) {
        if (records.isEmpty()) return@withContext 0
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val deletedSet = (prefs.getStringSet(KEY_DELETED_LOCAL_PATHS, emptySet()) ?: emptySet()).toMutableSet()
        var removedCount = 0

        for (item in records) {
            val uriStr = item.contentUri.toString()
            deletedSet.add(uriStr)
            deletedSet.add("media_id:${item.mediaId}")
            if (item.rawFilePath.isNotEmpty()) {
                deletedSet.add(item.rawFilePath)
            }
            try {
                if (uriStr.startsWith("content://")) {
                    contentResolver.delete(item.contentUri, null, null)
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

        prefs.edit().putStringSet(KEY_DELETED_LOCAL_PATHS, deletedSet).apply()
        removedCount
    }

    private fun appendPublicDirectoryImages(
        results: MutableList<DeviceImageRecord>,
        seenMediaIds: MutableSet<Long>,
        seenPaths: MutableSet<String>,
        deletedLocalPaths: Set<String>
    ) {
        val validExtensions = setOf("jpg", "jpeg", "png", "webp", "heic", "heif")
        for (dir in publicImageDirectories()) {
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
                            !PhotoImageProcessor.isStarterSamplePath(file.name) &&
                            PhotoImageProcessor.isValidMediaFile(null, file.name)
                    }
                    .take(150)
                    .forEach { file ->
                        val absPath = file.absolutePath
                        val fileUri = Uri.fromFile(file)
                        if (absPath !in seenPaths &&
                            fileUri.toString() !in seenPaths &&
                            absPath !in deletedLocalPaths
                        ) {
                            val syntheticId = absPath.hashCode().toLong() and 0x7FFFFFFFL
                            if (seenMediaIds.add(syntheticId)) {
                                seenPaths.add(absPath)
                                val folder = file.parentFile?.name?.takeIf { it.isNotEmpty() } ?: "Device Storage"
                                results.add(
                                    DeviceImageRecord(
                                        mediaId = syntheticId,
                                        contentUri = fileUri,
                                        rawFilePath = absPath,
                                        displayName = file.name,
                                        bucketName = folder,
                                        relativePath = file.parentFile?.absolutePath.orEmpty(),
                                        dateAddedMillis = file.lastModified(),
                                        dateTakenMillis = file.lastModified(),
                                        sizeBytes = file.length(),
                                        width = 1080,
                                        height = 1080,
                                        mimeType = "image/${file.extension.lowercase()}"
                                    )
                                )
                            }
                        }
                    }
            } catch (_: Exception) {
            }
        }
    }

    private fun publicImageDirectories(): List<File> = listOfNotNull(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS)
        } else null
    )

    companion object {
        private const val PREFS_NAME = "simple_gallery_prefs"
        private const val KEY_DELETED_LOCAL_PATHS = "deleted_local_photo_paths"

        internal fun normalizeTimestampMillis(
            primary: Long,
            fallback1: Long = 0L,
            fallback2: Long = 0L,
            nowMs: Long = System.currentTimeMillis()
        ): Long = when {
            primary > 1_000_000_000_000L -> primary
            primary > 0L -> primary * 1000L
            fallback1 > 1_000_000_000_000L -> fallback1
            fallback1 > 0L -> fallback1 * 1000L
            fallback2 > 1_000_000_000_000L -> fallback2
            fallback2 > 0L -> fallback2 * 1000L
            else -> nowMs
        }
    }
}
