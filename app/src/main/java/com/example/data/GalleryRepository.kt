package com.example.data

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GalleryRepository(
    private val context: Context,
    private val photoDao: PhotoDao
) {
    private val syncMutex = Mutex()
    val visiblePhotosFlow: Flow<List<PhotoEntity>> = photoDao.observeVisiblePhotos().map { list ->
        list.distinctBy { photo -> "${photo.fileName}:${photo.sizeBytes}" }
    }.flowOn(Dispatchers.Default)
    val favoritePhotosFlow: Flow<List<PhotoEntity>> = photoDao.observeFavoritePhotos().map { list ->
        list.distinctBy { photo -> "${photo.fileName}:${photo.sizeBytes}" }
    }.flowOn(Dispatchers.Default)
    val vaultPhotosFlow: Flow<List<PhotoEntity>> = photoDao.observeVaultPhotos()
    val trashedPhotosFlow: Flow<List<PhotoEntity>> = photoDao.observeTrashedPhotos()
    val customAlbumsFlow: Flow<List<CustomAlbumEntity>> = photoDao.observeCustomAlbums()
    val memoriesFlow: Flow<List<MemoryEntryEntity>> = photoDao.observeAllMemories()
    val milestonesFlow: Flow<List<MilestoneReminderEntity>> = photoDao.observeAllMilestones()
    val visiblePersonsFlow: Flow<List<PersonClusterEntity>> = photoDao.observeVisiblePersons()
    val faceTagsFlow: Flow<List<PhotoFaceTagEntity>> = photoDao.observeAllFaceTags()

    val albumsSummaryFlow: Flow<List<AlbumSummary>> = combine(
        visiblePhotosFlow,
        customAlbumsFlow
    ) { photos, customAlbums ->
        val grouped = photos.groupBy { it.albumName }
        val allAlbumNames = (grouped.keys + customAlbums.map { it.name }).distinct()
        val customDescriptions = customAlbums.associateBy { it.name }

        allAlbumNames.map { name ->
            val albumPhotos = grouped[name].orEmpty().sortedByDescending { it.dateTaken }
            val latest = albumPhotos.firstOrNull()
            AlbumSummary(
                name = name,
                description = customDescriptions[name]?.description?.ifBlank {
                    defaultAlbumDescription(name)
                } ?: defaultAlbumDescription(name),
                photoCount = albumPhotos.size,
                coverFilePath = latest?.filePath,
                totalBytes = albumPhotos.sumOf { it.sizeBytes },
                latestTimestamp = latest?.dateTaken
                    ?: customDescriptions[name]?.createdAt
                    ?: 0L
            )
        }.sortedWith(
            compareByDescending<AlbumSummary> { it.photoCount }
                .thenByDescending { it.latestTimestamp }
        )
    }.flowOn(Dispatchers.Default)

    private fun defaultAlbumDescription(name: String): String = when (name) {
        "Family" -> "Warm gatherings, park strolls & candid family portraits"
        "Travel" -> "Ocean bays, coastal horizons & destination journeys"
        "Landscapes" -> "Golden hour peaks, coastal horizons & nature vistas"
        "Architecture" -> "Minimalist geometry, light shafts & urban structure"
        "Nature & Macro" -> "Botanical close-ups, foliage & organic textures"
        "Street & Night" -> "After-hours neon, rain reflections & city stories"
        "Camera Roll" -> "Captured shots & imported local device photographs"
        "Edited Studio" -> "Darkroom filtered & cropped master prints"
        "Keepsakes" -> "Printed Polaroid keepsakes, milestones & family diary cards"
        else -> "Curated local photo collection"
    }

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("simple_gallery_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("default_albums_seeded_v2", false)) {
            val defaultAlbums = listOf(
                CustomAlbumEntity("Camera Roll", "Captured shots & imported personal device photographs"),
                CustomAlbumEntity("Family", "Warm gatherings, park strolls & candid family portraits"),
                CustomAlbumEntity("Travel", "Ocean bays, coastal horizons & destination journeys"),
                CustomAlbumEntity("Keepsakes", "Printed Polaroid keepsakes, milestones & family diary cards"),
                CustomAlbumEntity("Landscapes", "Golden hour peaks, coastal horizons & nature vistas"),
                CustomAlbumEntity("Architecture", "Minimalist geometry, light shafts & urban structure"),
                CustomAlbumEntity("Nature & Macro", "Botanical close-ups, foliage & organic textures"),
                CustomAlbumEntity("Street & Night", "After-hours neon, rain reflections & city stories"),
                CustomAlbumEntity("Edited Studio", "Darkroom filtered & cropped master prints")
            )
            photoDao.deleteCustomAlbum("Little Memories")
            defaultAlbums.forEach { photoDao.insertCustomAlbum(it) }
            val keepDemoSamples = prefs.getBoolean("keep_demo_samples_explicitly", false)
            if (!keepDemoSamples) {
                clearDefaultSamplePhotos()
            }
            prefs.edit().putBoolean("default_albums_seeded_v2", true).apply()
        }
    }

    /**
     * Triggers Android's MediaScanner on common public photo directories so newly added
     * camera shots, downloads, or screenshots are indexed in MediaStore.
     */
    private fun triggerMediaScannerOnPublicFolders() {
        try {
            val publicDirs = listOfNotNull(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS)
                } else null
            ).filter { it.exists() }.map { it.absolutePath }.toTypedArray()

            if (publicDirs.isNotEmpty()) {
                MediaScannerConnection.scanFile(context, publicDirs, null, null)
            }
        } catch (_: Exception) {
        }
    }

    private fun extractMediaIdFromPathOrUri(pathOrUri: String): Long? {
        if (pathOrUri.startsWith("content://")) {
            return pathOrUri.substringAfterLast('/').toLongOrNull()
        }
        val fileName = pathOrUri.substringAfterLast('/')
        if (fileName.startsWith("MS_")) {
            return fileName.removePrefix("MS_").substringBefore("_").toLongOrNull()
        }
        return null
    }

    /**
     * Synchronizes device photos from MediaStore into the Room database.
     * - On initial sync (or explicit forceRescan), performs a progressive scan that flushes the
     *   first 24 photos immediately (<50ms) so the initial viewport renders right away, then inserts
     *   remaining photos in large 300-row batches.
     * - On subsequent app opens (`!forceRescan` when photos are already cached), performs an
     *   ultra-fast delta query (`DATE_ADDED >= lastSyncSec OR DATE_MODIFIED >= lastSyncSec`)
     *   so cached photos appear in 0ms without re-fetching all photos from start.
     */
    suspend fun syncDeviceMediaStorePhotos(forceRescan: Boolean = false): Int =
        withContext(Dispatchers.IO) {
            syncMutex.withLock {
                if (forceRescan) {
                    triggerMediaScannerOnPublicFolders()
                }

                val resolver = context.contentResolver
                val prefs = context.getSharedPreferences("simple_gallery_prefs", Context.MODE_PRIVATE)
                val deletedLocalPaths = prefs.getStringSet("deleted_local_photo_paths", emptySet()) ?: emptySet()
                val lastSyncSec = prefs.getLong("last_mediastore_sync_sec", 0L)
                val nowSec = System.currentTimeMillis() / 1000L

                val existingPhotos = photoDao.getAllPhotosOnce()
                val isDeltaSync = !forceRescan && existingPhotos.isNotEmpty() && lastSyncSec > 0L

                // Only run full deduplication on initial sync or explicit rescan
                val activePhotos = if (!isDeltaSync && existingPhotos.size > 1) {
                    val seenKeys = HashMap<String, PhotoEntity>(existingPhotos.size)
                    val duplicateIdsToDelete = mutableListOf<Long>()
                    for (photo in existingPhotos) {
                        val canonicalKey = "${photo.fileName}:${photo.sizeBytes}"
                        val existing = seenKeys[canonicalKey]
                        if (existing != null) {
                            if (photo.filePath.startsWith("content://") && !existing.filePath.startsWith("content://")) {
                                duplicateIdsToDelete.add(existing.id)
                                seenKeys[canonicalKey] = photo
                            } else {
                                duplicateIdsToDelete.add(photo.id)
                            }
                        } else {
                            seenKeys[canonicalKey] = photo
                        }
                    }
                    if (duplicateIdsToDelete.isNotEmpty()) {
                        photoDao.deletePhotosByIds(duplicateIdsToDelete)
                    }
                    seenKeys.values
                } else {
                    existingPhotos
                }

                val existingPaths = HashSet<String>(activePhotos.size * 2)
                val existingByMediaId = HashMap<Long, PhotoEntity>(activePhotos.size)
                val existingByRawPath = HashMap<String, PhotoEntity>()

                for (photo in activePhotos) {
                    existingPaths.add(photo.filePath)
                    extractMediaIdFromPathOrUri(photo.filePath)?.let { id ->
                        existingByMediaId[id] = photo
                    }
                    if (!photo.filePath.startsWith("content://")) {
                        existingByRawPath[photo.filePath] = photo
                    }
                }

                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.DATE_MODIFIED,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.DATA
                )

                val queryUris = if (isDeltaSync) {
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

                val deltaThresholdSec = (lastSyncSec - 60L).coerceAtLeast(0L).toString()
                val selection = if (isDeltaSync) {
                    "${MediaStore.Images.Media.DATE_ADDED} >= ? OR ${MediaStore.Images.Media.DATE_MODIFIED} >= ?"
                } else {
                    null
                }
                val selectionArgs = if (isDeltaSync) {
                    arrayOf(deltaThresholdSec, deltaThresholdSec)
                } else {
                    null
                }

                var totalAddedOrRepaired = 0
                var firstBatchFlushed = existingPhotos.isNotEmpty()
                val pendingInsertBatch = ArrayList<PhotoEntity>(320)
                val discoveredAlbums = HashSet<String>()
                val seenMediaIds = HashSet<Long>()
                val seenFileKeys = HashSet<String>()

                for (queryUri in queryUris) {
                    val cursor = try {
                        resolver.query(
                            queryUri,
                            projection,
                            selection,
                            selectionArgs,
                            "${MediaStore.Images.Media.DATE_ADDED} DESC"
                        )
                    } catch (_: Exception) {
                        try {
                            resolver.query(
                                queryUri,
                                arrayOf(
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
                                ),
                                selection,
                                selectionArgs,
                                null
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

                        while (c.moveToNext()) {
                            val mediaId = if (!c.isNull(idCol)) c.getLong(idCol) else continue
                            if (!seenMediaIds.add(mediaId)) continue

                            val contentUriString = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                mediaId
                            ).toString()

                            val rawDataPath = if (dataCol >= 0 && !c.isNull(dataCol)) {
                                c.getString(dataCol).orEmpty()
                            } else {
                                ""
                            }

                            val displayName = if (nameCol >= 0 && !c.isNull(nameCol)) {
                                c.getString(nameCol) ?: "IMG_$mediaId.jpg"
                            } else {
                                rawDataPath.substringAfterLast('/').ifBlank { "IMG_$mediaId.jpg" }
                            }

                            // Only skip the 10 synthetic starter demo files, NEVER real user photos
                            if (PhotoImageProcessor.isStarterSamplePath(displayName)) continue

                            // Skip photos that the user explicitly deleted from local storage
                            if (contentUriString in deletedLocalPaths ||
                                "media_id:$mediaId" in deletedLocalPaths ||
                                (rawDataPath.isNotEmpty() && rawDataPath in deletedLocalPaths)
                            ) {
                                continue
                            }

                            val existingMatch = existingByMediaId[mediaId]
                                ?: (if (rawDataPath.isNotEmpty()) existingByRawPath[rawDataPath] else null)

                            if (existingMatch != null) {
                                if (existingMatch.filePath != contentUriString) {
                                    photoDao.updatePhoto(
                                        existingMatch.copy(
                                            filePath = contentUriString,
                                            isTrashed = if (forceRescan) false else existingMatch.isTrashed
                                        )
                                    )
                                    totalAddedOrRepaired++
                                } else if (forceRescan && existingMatch.isTrashed) {
                                    photoDao.updatePhoto(existingMatch.copy(isTrashed = false, trashedAt = null))
                                    totalAddedOrRepaired++
                                }
                                continue
                            }

                            if (contentUriString in existingPaths) continue

                            val bucketName = if (bucketCol >= 0 && !c.isNull(bucketCol)) {
                                c.getString(bucketCol)?.trim()?.takeIf { it.isNotEmpty() } ?: "Camera Roll"
                            } else {
                                rawDataPath.substringBeforeLast('/', "")
                                    .substringAfterLast('/')
                                    .takeIf { it.isNotEmpty() } ?: "Camera Roll"
                            }

                            val rawDateTaken = if (dateTakenCol >= 0 && !c.isNull(dateTakenCol)) c.getLong(dateTakenCol) else 0L
                            val rawDateAdded = if (dateAddedCol >= 0 && !c.isNull(dateAddedCol)) c.getLong(dateAddedCol) else 0L
                            val rawDateMod = if (dateModCol >= 0 && !c.isNull(dateModCol)) c.getLong(dateModCol) else 0L

                            val dateTakenMs = when {
                                rawDateTaken > 1_000_000_000_000L -> rawDateTaken
                                rawDateTaken > 0L -> rawDateTaken * 1000L
                                rawDateAdded > 1_000_000_000_000L -> rawDateAdded
                                rawDateAdded > 0L -> rawDateAdded * 1000L
                                rawDateMod > 1_000_000_000_000L -> rawDateMod
                                rawDateMod > 0L -> rawDateMod * 1000L
                                else -> System.currentTimeMillis()
                            }

                            val w = (if (widthCol >= 0 && !c.isNull(widthCol)) c.getInt(widthCol) else 0)
                                .takeIf { it > 0 } ?: 1080
                            val h = (if (heightCol >= 0 && !c.isNull(heightCol)) c.getInt(heightCol) else 0)
                                .takeIf { it > 0 } ?: 1080
                            val sizeBytes = (if (sizeCol >= 0 && !c.isNull(sizeCol)) c.getLong(sizeCol) else 0L)
                                .takeIf { it > 0L } ?: 250_000L
                            val mimeType = (if (mimeCol >= 0 && !c.isNull(mimeCol)) c.getString(mimeCol) else null)
                                ?: "image/jpeg"

                            val cleanTitle = displayName.substringBeforeLast(".")
                                .replace("_", " ")
                                .replace("-", " ")
                                .ifBlank { "Photo $mediaId" }

                            val autoTags = buildList {
                                add(bucketName)
                                add("Device")
                                when {
                                    bucketName.contains("Screen", ignoreCase = true) -> add("Architecture")
                                    bucketName.contains("Camera", ignoreCase = true) ||
                                        bucketName.contains("DCIM", ignoreCase = true) -> add("Portraits")
                                    bucketName.contains("Download", ignoreCase = true) -> add("Travel")
                                    bucketName.contains("WhatsApp", ignoreCase = true) ||
                                        bucketName.contains("Family", ignoreCase = true) -> add("Family")
                                    bucketName.contains("Nature", ignoreCase = true) ||
                                        bucketName.contains("Park", ignoreCase = true) -> add("Nature")
                                }
                            }.distinct().joinToString(",")

                            discoveredAlbums.add(bucketName)
                            existingPaths.add(contentUriString)
                            seenFileKeys.add("$displayName:$sizeBytes")
                            pendingInsertBatch.add(
                                PhotoEntity(
                                    title = cleanTitle,
                                    filePath = contentUriString,
                                    albumName = bucketName,
                                    dateTaken = dateTakenMs,
                                    width = w,
                                    height = h,
                                    sizeBytes = sizeBytes,
                                    mimeType = mimeType,
                                    cameraModel = "Device Camera",
                                    locationTag = bucketName,
                                    tags = autoTags
                                )
                            )
                            totalAddedOrRepaired++

                            // Flush the first 24 photos immediately so the first screen appears in <50ms,
                            // then flush in large 300-item chunks to avoid excessive Room Flow emissions
                            val batchLimit = if (!firstBatchFlushed) 24 else 300
                            if (pendingInsertBatch.size >= batchLimit) {
                                firstBatchFlushed = true
                                photoDao.insertPhotos(pendingInsertBatch.toList())
                                pendingInsertBatch.clear()
                            }
                        }
                    }
                }

                // Fallback directory walk ONLY when MediaStore returned zero items and DB is empty
                if (!isDeltaSync && seenMediaIds.isEmpty() && existingPhotos.isEmpty()) {
                    val publicDirsToScan = listOfNotNull(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS)
                        } else null
                    )
                    val supportedExtensions = setOf("jpg", "jpeg", "png", "webp", "heic", "heif")
                    val alreadyTrackedRawPaths = existingByRawPath.keys.toHashSet()

                    for (rootDir in publicDirsToScan) {
                        if (!rootDir.exists() || !rootDir.canRead()) continue
                        try {
                            rootDir.walkTopDown()
                                .maxDepth(3)
                                .filter { file ->
                                    file.isFile &&
                                        file.canRead() &&
                                        file.length() > 0L &&
                                        !file.name.startsWith(".") &&
                                        file.extension.lowercase() in supportedExtensions &&
                                        !PhotoImageProcessor.isStarterSamplePath(file.name)
                                }
                                .take(200)
                                .forEach { file ->
                                    val absPath = file.absolutePath
                                    val fileName = file.name
                                    val fileSize = file.length()
                                    val fileKey = "$fileName:$fileSize"
                                    if (absPath !in existingPaths &&
                                        absPath !in alreadyTrackedRawPaths &&
                                        absPath !in deletedLocalPaths &&
                                        fileKey !in seenFileKeys
                                    ) {
                                        val folderName = file.parentFile?.name?.takeIf { it.isNotEmpty() } ?: "Camera Roll"
                                        val fileUriString = Uri.fromFile(file).toString()
                                        if (fileUriString !in existingPaths && fileUriString !in deletedLocalPaths) {
                                            discoveredAlbums.add(folderName)
                                            existingPaths.add(absPath)
                                            pendingInsertBatch.add(
                                                PhotoEntity(
                                                    title = file.nameWithoutExtension.replace("_", " ").ifBlank { "Device Photo" },
                                                    filePath = absPath,
                                                    albumName = folderName,
                                                    dateTaken = file.lastModified().takeIf { it > 0L } ?: System.currentTimeMillis(),
                                                    width = 1080,
                                                    height = 1080,
                                                    sizeBytes = fileSize,
                                                    mimeType = "image/${file.extension.lowercase()}",
                                                    cameraModel = "Device Storage",
                                                    locationTag = folderName,
                                                    tags = "$folderName,Device"
                                                )
                                            )
                                            totalAddedOrRepaired++
                                        }
                                    }
                                }
                        } catch (_: Exception) {
                        }
                    }
                }

                if (pendingInsertBatch.isNotEmpty()) {
                    photoDao.insertPhotos(pendingInsertBatch.toList())
                    pendingInsertBatch.clear()
                }

                for (albumName in discoveredAlbums) {
                    photoDao.insertCustomAlbum(
                        CustomAlbumEntity(
                            name = albumName,
                            description = "Automatically synced from device folder '$albumName'"
                        )
                    )
                }

                prefs.edit().putLong("last_mediastore_sync_sec", nowSec).apply()
                totalAddedOrRepaired
            }
        }

    suspend fun clearDefaultSamplePhotos(): Int = withContext(Dispatchers.IO) {
        val allPhotos = photoDao.getAllPhotosOnce()
        val starterPhotos = allPhotos.filter { PhotoImageProcessor.isStarterSamplePath(it.filePath) }
        for (sample in starterPhotos) {
            val file = File(sample.filePath)
            if (file.exists()) {
                file.delete()
            }
        }
        if (starterPhotos.isNotEmpty()) {
            photoDao.deleteFaceTagsForPhotos(starterPhotos.map { it.id })
            photoDao.deletePhotosByIds(starterPhotos.map { it.id })
        }
        val remaining = photoDao.getVisiblePhotosOnce()
        if (remaining.isEmpty()) {
            photoDao.deleteAllFaceTags()
            photoDao.deleteAllPersons()
        }
        val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
        galleryDir.listFiles()?.forEach { file ->
            if (file.name in PhotoImageProcessor.STARTER_SAMPLE_FILENAMES) {
                file.delete()
            }
        }
        context.getSharedPreferences("simple_gallery_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("keep_demo_samples_explicitly", false)
            .apply()
        starterPhotos.size
    }

    suspend fun loadSampleDemoPhotos(): Int = withContext(Dispatchers.IO) {
        val starterPhotos = PhotoImageProcessor.generateStarterLibraryIfNeeded(context)
        val existingPaths = photoDao.getAllPhotosOnce().map { it.filePath }.toSet()
        val missingStarters = starterPhotos.filter { it.filePath !in existingPaths }
        if (missingStarters.isNotEmpty()) {
            photoDao.insertPhotos(missingStarters)
        }
        seedDemoPeopleAndGrowthTimeline()
        context.getSharedPreferences("simple_gallery_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("keep_demo_samples_explicitly", true)
            .apply()
        missingStarters.size
    }

    private suspend fun seedDemoPeopleAndGrowthTimeline() {
        val visible = photoDao.getVisiblePhotosOnce()
        val byFileName = visible.associateBy { it.filePath.substringAfterLast('/') }
        val mayaNewborn = byFileName["IMG_2026_MAYA_NEWBORN.jpg"]
        val maya3m = byFileName["IMG_2026_MAYA_3M_SMILE.jpg"]
        val maya6m = byFileName["IMG_2026_MAYA_6M_SITTING.jpg"]
        val maya1y = byFileName["IMG_2026_MAYA_1ST_BIRTHDAY.jpg"]
        val mayaSteps = byFileName["IMG_2026_MAYA_FIRST_STEPS.jpg"]
        val familyWalk = byFileName["IMG_2026_FAMILY_SUNSET_WALK.jpg"]
        val liamPhoto = byFileName["IMG_2026_LIAM_COFFEE.jpg"]
        val parkBurst = byFileName["IMG_2026_PARK_STROLL_BURST.jpg"]
        val rexPhoto = byFileName["IMG_2026_REX_PARK.jpg"]

        val existingPersons = photoDao.getAllPersonsOnce()
        if (existingPersons.isEmpty() && maya1y != null) {
            val now = System.currentTimeMillis()
            val mayaDob = now - 395L * 86_400_000L
            val mayaId = photoDao.insertPerson(
                PersonClusterEntity(
                    name = "Maya",
                    avatarPhotoPath = maya1y.filePath,
                    seedColorHex = "#C84B28",
                    hasGrowthTimeline = true,
                    childDisplayName = "Maya",
                    childDobMs = mayaDob
                )
            )
            val liamId = photoDao.insertPerson(
                PersonClusterEntity(
                    name = "Liam",
                    avatarPhotoPath = (liamPhoto ?: familyWalk ?: maya1y).filePath,
                    seedColorHex = "#D9822B"
                )
            )
            val rexId = photoDao.insertPerson(
                PersonClusterEntity(
                    name = "Rex",
                    avatarPhotoPath = (rexPhoto ?: maya6m ?: maya1y).filePath,
                    seedColorHex = "#4A7C59",
                    isPet = true
                )
            )
            val unnamedId = photoDao.insertPerson(
                PersonClusterEntity(
                    name = "",
                    avatarPhotoPath = (parkBurst ?: familyWalk ?: maya1y).filePath,
                    seedColorHex = "#6B625B"
                )
            )

            val tags = mutableListOf<PhotoFaceTagEntity>()
            listOfNotNull(mayaNewborn, maya3m, maya6m, maya1y, mayaSteps).forEach { p ->
                tags.add(PhotoFaceTagEntity(photoId = p.id, personId = mayaId, leftFrac = 0.30f, topFrac = 0.18f, rightFrac = 0.70f, bottomFrac = 0.56f))
            }
            if (familyWalk != null) {
                tags.add(PhotoFaceTagEntity(photoId = familyWalk.id, personId = mayaId, leftFrac = 0.44f, topFrac = 0.50f, rightFrac = 0.56f, bottomFrac = 0.74f))
                tags.add(PhotoFaceTagEntity(photoId = familyWalk.id, personId = liamId, leftFrac = 0.28f, topFrac = 0.44f, rightFrac = 0.41f, bottomFrac = 0.72f))
            }
            if (liamPhoto != null) {
                tags.add(PhotoFaceTagEntity(photoId = liamPhoto.id, personId = liamId, leftFrac = 0.31f, topFrac = 0.19f, rightFrac = 0.69f, bottomFrac = 0.57f))
            }
            if (rexPhoto != null) {
                tags.add(PhotoFaceTagEntity(photoId = rexPhoto.id, personId = rexId, leftFrac = 0.32f, topFrac = 0.24f, rightFrac = 0.68f, bottomFrac = 0.60f))
            }
            if (parkBurst != null) {
                tags.add(PhotoFaceTagEntity(photoId = parkBurst.id, personId = unnamedId, leftFrac = 0.36f, topFrac = 0.24f, rightFrac = 0.64f, bottomFrac = 0.54f))
            }
            if (tags.isNotEmpty()) {
                photoDao.insertFaceTags(tags)
            }
        }
    }

    suspend fun renamePerson(personId: Long, newName: String) {
        val clean = newName.trim()
        val existing = photoDao.getPersonById(personId) ?: return
        photoDao.updatePerson(
            existing.copy(
                name = clean,
                childDisplayName = if (existing.hasGrowthTimeline && clean.isNotEmpty()) clean else existing.childDisplayName
            )
        )
    }

    suspend fun hidePerson(personId: Long) {
        photoDao.setPersonHidden(personId, true)
    }

    suspend fun mergePersons(sourcePersonId: Long, targetPersonId: Long) {
        if (sourcePersonId == targetPersonId) return
        photoDao.reassignFaceTags(sourcePersonId, targetPersonId)
        photoDao.markPersonMerged(sourcePersonId, targetPersonId)
    }

    suspend fun setupGrowthTimeline(personId: Long, childName: String, dobMs: Long) {
        val clean = childName.trim().ifEmpty { "Little One" }
        photoDao.setupGrowthTimeline(personId, clean, dobMs)
    }

    suspend fun updatePhotoMilestoneSticker(photoId: Long, sticker: String) {
        photoDao.updateMilestoneSticker(photoId, sticker)
    }

    suspend fun createExifStrippedShareFile(photo: PhotoEntity): File? {
        return PhotoImageProcessor.exportExifStrippedFile(context, photo)
    }

    suspend fun createCustomAlbum(name: String, description: String) {
        val cleanName = name.trim()
        if (cleanName.isNotEmpty()) {
            photoDao.insertCustomAlbum(
                CustomAlbumEntity(
                    name = cleanName,
                    description = description.trim().ifEmpty { "Custom local photo album" }
                )
            )
        }
    }

    suspend fun saveMemoryEntry(memory: MemoryEntryEntity): Long {
        return photoDao.insertMemory(memory)
    }

    suspend fun deleteMemoryEntry(id: Long) {
        photoDao.deleteMemoryById(id)
    }

    suspend fun toggleMemoryPinned(memory: MemoryEntryEntity) {
        photoDao.setMemoryPinned(memory.id, !memory.isPinnedHighlight)
    }

    suspend fun unlockTimeCapsule(memoryId: Long) {
        photoDao.unlockTimeCapsule(memoryId)
    }

    private fun matchedStarterDate(
        matchedStarter: PhotoEntity?,
        index: Int,
        now: Long,
        fallback: Long
    ): Long = matchedStarter?.dateTaken ?: when (index) {
        0 -> now - 1 * 3_600_000L
        1 -> now - 3 * 3_600_000L
        2 -> now - 5 * 3_600_000L
        3, 4, 5 -> now - 86_400_000L - (index * 3_600_000L)
        6, 7 -> now - 3 * 86_400_000L
        else -> fallback
    }

    suspend fun exportMemoryAsPolaroidToGallery(memory: MemoryEntryEntity): PhotoEntity? {
        val rendered = PhotoImageProcessor.renderPolaroidPostcardFile(context, memory) ?: return null
        val (file, width, height) = rendered
        photoDao.insertCustomAlbum(
            CustomAlbumEntity("Keepsakes", "Printed Polaroid keepsakes, milestones & family diary cards")
        )
        val entity = PhotoEntity(
            title = "Polaroid • ${memory.title}",
            filePath = file.absolutePath,
            albumName = "Keepsakes",
            dateTaken = System.currentTimeMillis(),
            width = width,
            height = height,
            sizeBytes = file.length(),
            cameraModel = "Keepsake Studio",
            aperture = memory.mood,
            shutterSpeed = memory.milestoneBadge,
            iso = memory.frameStyle,
            focalLength = "Postcard",
            locationTag = memory.locationName,
            tags = "Keepsakes,Polaroid,${memory.mood},${memory.milestoneBadge}",
            notes = memory.story,
            isFavorite = true
        )
        val newId = photoDao.insertPhoto(entity)
        return entity.copy(id = newId)
    }

    suspend fun createBentoCollage(
        photoPaths: List<String>,
        collageTitle: String,
        layoutMode: Int,
        backgroundColorHex: String
    ): PhotoEntity? {
        val rendered = PhotoImageProcessor.renderBentoCollageFile(
            context = context,
            photoPaths = photoPaths,
            collageTitle = collageTitle,
            layoutMode = layoutMode,
            bgHex = backgroundColorHex
        ) ?: return null
        val (file, width, height) = rendered
        photoDao.insertCustomAlbum(
            CustomAlbumEntity("Collage Studio", "Multi-photo Bento & Scrapbook collages")
        )
        val layoutLabel = when (layoutMode) {
            0 -> "Split 2-Up"
            1 -> "Bento 3-Up"
            else -> "Quad 4-Grid"
        }
        val cleanTitle = collageTitle.trim().ifEmpty { "Bento Story Collage" }
        val entity = PhotoEntity(
            title = cleanTitle,
            filePath = file.absolutePath,
            albumName = "Collage Studio",
            dateTaken = System.currentTimeMillis(),
            width = width,
            height = height,
            sizeBytes = file.length(),
            cameraModel = "Bento Collage Studio",
            aperture = layoutLabel,
            shutterSpeed = "${photoPaths.size} Photos",
            iso = backgroundColorHex,
            focalLength = "Collage",
            locationTag = "Local Studio",
            tags = "Collage,Bento,Edited,$layoutLabel",
            notes = "Created in Bento Collage Studio with ${photoPaths.size} photos ($layoutLabel).",
            isFavorite = true
        )
        val newId = photoDao.insertPhoto(entity)
        return entity.copy(id = newId)
    }

    suspend fun createWatermarkedPhoto(
        original: PhotoEntity,
        watermarkText: String,
        includeDateAndExif: Boolean
    ): PhotoEntity? {
        val rendered = PhotoImageProcessor.renderWatermarkedPhotoFile(
            context = context,
            photo = original,
            watermarkText = watermarkText,
            includeDateAndExif = includeDateAndExif
        ) ?: return null
        val (file, width, height) = rendered
        photoDao.insertCustomAlbum(
            CustomAlbumEntity("Edited Studio", "Darkroom filtered, watermarked & cropped master prints")
        )
        val entity = original.copy(
            id = 0,
            title = "${original.title} (Stamped)",
            filePath = file.absolutePath,
            albumName = "Edited Studio",
            dateTaken = System.currentTimeMillis(),
            width = width,
            height = height,
            sizeBytes = file.length(),
            tags = (original.tagList + listOf("Watermarked", "Edited")).distinct().joinToString(","),
            notes = "Watermarked with '${watermarkText.ifBlank { "Studio Archive" }}'"
        )
        val newId = photoDao.insertPhoto(entity)
        return entity.copy(id = newId)
    }

    suspend fun shrinkPhoto(
        original: PhotoEntity,
        scaleFactor: Float,
        jpegQuality: Int,
        replaceOriginal: Boolean
    ): PhotoEntity? {
        val rendered = PhotoImageProcessor.shrinkAndOptimizePhotoFile(
            context = context,
            photo = original,
            scaleFactor = scaleFactor,
            jpegQuality = jpegQuality,
            replaceOriginal = replaceOriginal
        ) ?: return null
        val (file, width, height) = rendered
        val newSize = file.length()
        return if (replaceOriginal) {
            val updated = original.copy(
                filePath = file.absolutePath,
                width = width,
                height = height,
                sizeBytes = newSize,
                tags = (original.tagList + "Shrunk").distinct().joinToString(",")
            )
            photoDao.updatePhoto(updated)
            updated
        } else {
            photoDao.insertCustomAlbum(
                CustomAlbumEntity("Edited Studio", "Darkroom filtered, watermarked & optimized master prints")
            )
            val copy = original.copy(
                id = 0,
                title = "${original.title} (Optimized)",
                filePath = file.absolutePath,
                albumName = "Edited Studio",
                dateTaken = System.currentTimeMillis(),
                width = width,
                height = height,
                sizeBytes = newSize,
                tags = (original.tagList + listOf("Optimized", "Edited")).distinct().joinToString(",")
            )
            val newId = photoDao.insertPhoto(copy)
            copy.copy(id = newId)
        }
    }

    suspend fun createFramedPhoto(
        original: PhotoEntity,
        frameStyleName: String,
        showExifSignature: Boolean,
        customBadgeText: String = ""
    ): PhotoEntity? {
        val rendered = PhotoImageProcessor.renderFramedPhotoFile(
            context = context,
            photo = original,
            frameStyleName = frameStyleName,
            showExifSignature = showExifSignature,
            customBadgeText = customBadgeText
        ) ?: return null
        val (file, width, height) = rendered
        photoDao.insertCustomAlbum(
            CustomAlbumEntity("Edited Studio", "Darkroom filtered, framed & cropped master prints")
        )
        val framedEntity = original.copy(
            id = 0,
            title = "${original.title} ($frameStyleName)",
            filePath = file.absolutePath,
            albumName = "Edited Studio",
            dateTaken = System.currentTimeMillis(),
            width = width,
            height = height,
            sizeBytes = file.length(),
            appliedFilter = frameStyleName,
            tags = (original.tagList + listOf("Framed", frameStyleName, "Edited")).distinct().joinToString(",")
        )
        val newId = photoDao.insertPhoto(framedEntity)
        return framedEntity.copy(id = newId)
    }

    suspend fun saveMilestoneReminder(milestone: MilestoneReminderEntity): Long {
        return photoDao.insertMilestone(milestone)
    }

    suspend fun toggleMilestoneCompleted(milestone: MilestoneReminderEntity) {
        photoDao.setMilestoneCompleted(milestone.id, !milestone.isCompleted)
    }

    suspend fun deleteMilestone(id: Long) {
        photoDao.deleteMilestoneById(id)
    }

    suspend fun importPhotosFromUris(uris: List<Uri>, targetAlbum: String = "Camera Roll"): List<Long> =
        withContext(Dispatchers.IO) {
            val importedIds = mutableListOf<Long>()
            val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
            val resolver = context.contentResolver

            for (uri in uris) {
                try {
                    var displayName = "Imported_${System.currentTimeMillis()}.jpg"
                    resolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx >= 0 && cursor.moveToFirst()) {
                            displayName = cursor.getString(nameIdx) ?: displayName
                        }
                    }
                    val safeFileName = "IMP_${System.currentTimeMillis()}_${
                        displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                    }"
                    val destFile = File(galleryDir, safeFileName)
                    resolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (destFile.exists() && destFile.length() > 0L) {
                        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeFile(destFile.absolutePath, opts)
                        val w = opts.outWidth.coerceAtLeast(800)
                        val h = opts.outHeight.coerceAtLeast(600)
                        val cleanTitle = displayName.substringBeforeLast(".")
                            .replace("_", " ")
                            .replace("-", " ")
                            .ifBlank { "Imported Photo" }

                        val exif = try {
                            android.media.ExifInterface(destFile.absolutePath)
                        } catch (_: Exception) {
                            null
                        }
                        val make = exif?.getAttribute(android.media.ExifInterface.TAG_MAKE)?.trim().orEmpty()
                        val model = exif?.getAttribute(android.media.ExifInterface.TAG_MODEL)?.trim().orEmpty()
                        val cameraName = when {
                            make.isNotEmpty() && model.isNotEmpty() && !model.startsWith(make, ignoreCase = true) -> "$make $model"
                            model.isNotEmpty() -> model
                            make.isNotEmpty() -> make
                            else -> "Device Import"
                        }
                        val fNum = exif?.getAttribute(android.media.ExifInterface.TAG_F_NUMBER)?.trim().orEmpty()
                        val apertureVal = if (fNum.isNotEmpty()) {
                            if (fNum.startsWith("f/", ignoreCase = true)) fNum else "f/$fNum"
                        } else "f/1.8"
                        val expTime = exif?.getAttribute(android.media.ExifInterface.TAG_EXPOSURE_TIME)?.trim().orEmpty()
                        val shutterVal = if (expTime.isNotEmpty()) {
                            val sec = expTime.toDoubleOrNull()
                            if (sec != null && sec > 0.0 && sec < 1.0) {
                                "1/${(1.0 / sec).toInt().coerceAtLeast(1)}s"
                            } else if (expTime.endsWith("s")) {
                                expTime
                            } else {
                                "${expTime}s"
                            }
                        } else "1/250s"
                        val isoAttr = exif?.getAttribute(android.media.ExifInterface.TAG_ISO_SPEED_RATINGS)?.trim().orEmpty()
                        val isoVal = if (isoAttr.isNotEmpty()) {
                            if (isoAttr.startsWith("ISO", ignoreCase = true)) isoAttr else "ISO $isoAttr"
                        } else "ISO 100"
                        val focalAttr = exif?.getAttribute(android.media.ExifInterface.TAG_FOCAL_LENGTH)?.trim().orEmpty()
                        val focalVal = if (focalAttr.isNotEmpty()) {
                            if (focalAttr.contains("/")) {
                                val parts = focalAttr.split("/")
                                val num = parts.getOrNull(0)?.toDoubleOrNull() ?: 26.0
                                val den = (parts.getOrNull(1)?.toDoubleOrNull() ?: 1.0).coerceAtLeast(1.0)
                                "${(num / den).toInt()}mm"
                            } else if (focalAttr.endsWith("mm")) {
                                focalAttr
                            } else {
                                "${focalAttr}mm"
                            }
                        } else "26mm"

                        val newId = photoDao.insertPhoto(
                            PhotoEntity(
                                title = cleanTitle,
                                filePath = destFile.absolutePath,
                                albumName = targetAlbum,
                                dateTaken = System.currentTimeMillis(),
                                width = w,
                                height = h,
                                sizeBytes = destFile.length(),
                                mimeType = opts.outMimeType ?: "image/jpeg",
                                cameraModel = cameraName,
                                aperture = apertureVal,
                                shutterSpeed = shutterVal,
                                iso = isoVal,
                                focalLength = focalVal,
                                locationTag = "Local Library",
                                tags = "Imported,$targetAlbum"
                            )
                        )
                        importedIds.add(newId)
                    }
                } catch (_: Exception) {
                    // Continue importing remaining URIs safely
                }
            }
            importedIds
        }

    suspend fun saveCapturedBitmap(bitmap: Bitmap, targetAlbum: String = "Camera Roll"): PhotoEntity =
        withContext(Dispatchers.IO) {
            val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val destFile = File(galleryDir, "CAP_$stamp.jpg")
            FileOutputStream(destFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 94, out)
            }
            val entity = PhotoEntity(
                title = "Camera Capture $stamp",
                filePath = destFile.absolutePath,
                albumName = targetAlbum,
                dateTaken = System.currentTimeMillis(),
                width = bitmap.width.coerceAtLeast(640),
                height = bitmap.height.coerceAtLeast(480),
                sizeBytes = destFile.length(),
                cameraModel = "Device Camera",
                aperture = "f/1.8",
                shutterSpeed = "1/250s",
                iso = "ISO 125",
                focalLength = "24mm",
                locationTag = "Camera Capture",
                tags = "Camera,Captured,$targetAlbum"
            )
            val newId = photoDao.insertPhoto(entity)
            entity.copy(id = newId)
        }

    suspend fun applyStudioEdits(
        original: PhotoEntity,
        preset: PhotoFilterPreset,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        rotationDegrees: Int,
        flipHorizontal: Boolean,
        flipVertical: Boolean,
        cropRatio: CropAspectRatio,
        saveAsCopy: Boolean,
        customCropBounds: android.graphics.RectF? = null
    ): PhotoEntity? {
        val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
        val isContentUri = original.filePath.startsWith("content://")
        val originalFile = File(original.filePath)
        val isInsideAppStorage = !isContentUri && originalFile.absolutePath.startsWith(galleryDir.absolutePath)
        val safeBaseName = original.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "photo" }
        val targetFile = if (saveAsCopy || !isInsideAppStorage) {
            File(galleryDir, "EDIT_${System.currentTimeMillis()}_${safeBaseName}.jpg")
        } else {
            originalFile
        }

        val dimensions = PhotoImageProcessor.renderAndSaveEditedPhoto(
            sourceFilePath = original.filePath,
            targetFile = targetFile,
            preset = preset,
            brightness = brightness,
            contrast = contrast,
            saturation = saturation,
            rotationDegrees = rotationDegrees,
            flipHorizontal = flipHorizontal,
            flipVertical = flipVertical,
            cropRatio = cropRatio,
            customCropBounds = customCropBounds,
            context = context
        ) ?: return null

        val (newW, newH, newSize) = dimensions
        val isCropped = cropRatio != CropAspectRatio.ORIGINAL ||
            (customCropBounds != null &&
                (customCropBounds.left > 0.01f ||
                    customCropBounds.top > 0.01f ||
                    customCropBounds.right < 0.99f ||
                    customCropBounds.bottom < 0.99f))
        val extraTags = buildList {
            add("Edited")
            if (preset != PhotoFilterPreset.ORIGINAL) add(preset.displayName)
            if (isCropped) add("Cropped")
        }
        val updatedTags = (original.tagList + extraTags)
            .distinct()
            .joinToString(",")

        val suffixLabel = when {
            isCropped && preset == PhotoFilterPreset.ORIGINAL -> "Cropped"
            else -> preset.displayName
        }

        return if (saveAsCopy) {
            photoDao.insertCustomAlbum(
                CustomAlbumEntity("Edited Studio", "Darkroom filtered & cropped master prints")
            )
            val copyEntity = original.copy(
                id = 0,
                title = "${original.title} ($suffixLabel)",
                filePath = targetFile.absolutePath,
                albumName = "Edited Studio",
                dateTaken = System.currentTimeMillis(),
                width = newW,
                height = newH,
                sizeBytes = newSize,
                tags = updatedTags,
                appliedFilter = suffixLabel
            )
            val id = photoDao.insertPhoto(copyEntity)
            copyEntity.copy(id = id)
        } else {
            val updatedEntity = original.copy(
                filePath = targetFile.absolutePath,
                width = newW,
                height = newH,
                sizeBytes = newSize,
                dateTaken = System.currentTimeMillis(),
                tags = updatedTags,
                appliedFilter = suffixLabel
            )
            photoDao.updatePhoto(updatedEntity)
            updatedEntity
        }
    }

    suspend fun applyAdvancedStudioEdits(
        original: PhotoEntity,
        cropRatio: CropAspectRatio,
        customCropRect: RectF?,
        rotationDegrees: Int,
        flipH: Boolean,
        flipV: Boolean,
        exposure: Float,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        highlights: Float,
        shadows: Float,
        temperature: Float,
        tint: Float,
        vignette: Float,
        grain: Float,
        filterMatrix: FloatArray?,
        filterIntensity: Float,
        skinSmoothing: Float,
        saveAsCopy: Boolean
    ): PhotoEntity? = withContext(Dispatchers.IO) {
        val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
        val safeBaseName = original.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "master" }
        val targetFile = File(galleryDir, "STUDIO_${System.currentTimeMillis()}_${safeBaseName}.jpg")

        val success = com.example.ui.studio.StudioImageEngine.processAndExportPhoto(
            context = context,
            sourcePathOrUri = original.filePath,
            targetFile = targetFile,
            cropRatio = cropRatio,
            customCropRect = customCropRect,
            rotationDegrees = rotationDegrees,
            flipHorizontal = flipH,
            flipVertical = flipV,
            exposure = exposure,
            brightness = brightness,
            contrast = contrast,
            saturation = saturation,
            highlights = highlights,
            shadows = shadows,
            temperature = temperature,
            tint = tint,
            vignette = vignette,
            grain = grain,
            filterMatrix = filterMatrix,
            filterIntensity = filterIntensity,
            skinSmoothing = skinSmoothing
        )
        if (!success) return@withContext null

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(targetFile.absolutePath, options)
        val w = options.outWidth.takeIf { it > 0 } ?: original.width
        val h = options.outHeight.takeIf { it > 0 } ?: original.height
        val sz = targetFile.length()

        photoDao.insertCustomAlbum(
            CustomAlbumEntity("Studio Prints", "Master darkroom prints & creative edits")
        )

        val updatedTags = (original.tagList + listOf("Studio", "Edited")).distinct().joinToString(",")
        val entity = if (saveAsCopy) {
            original.copy(
                id = 0,
                title = "${original.title} (Studio)",
                filePath = targetFile.absolutePath,
                albumName = "Studio Prints",
                dateTaken = System.currentTimeMillis(),
                width = w,
                height = h,
                sizeBytes = sz,
                tags = updatedTags,
                appliedFilter = "Studio Master"
            )
        } else {
            original.copy(
                filePath = targetFile.absolutePath,
                width = w,
                height = h,
                sizeBytes = sz,
                dateTaken = System.currentTimeMillis(),
                tags = updatedTags,
                appliedFilter = "Studio Master"
            )
        }

        if (saveAsCopy) {
            val newId = photoDao.insertPhoto(entity)
            entity.copy(id = newId)
        } else {
            photoDao.updatePhoto(entity)
            entity
        }
    }

    suspend fun compressPhotoBatch(
        targets: List<PhotoEntity>,
        format: String,
        quality: Int,
        scalePercent: Int,
        preserveExif: Boolean
    ): List<PhotoEntity> = withContext(Dispatchers.IO) {
        val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
        val ext = format.lowercase()
        val results = mutableListOf<PhotoEntity>()

        photoDao.insertCustomAlbum(
            CustomAlbumEntity("Compressed", "Optimized lightweight copies")
        )

        for (photo in targets) {
            val safeName = photo.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "photo" }
            val targetFile = File(galleryDir, "OPT_${System.currentTimeMillis()}_${safeName}.$ext")
            val (success, _, newBytes) = com.example.ui.studio.StudioImageEngine.compressAndOptimize(
                context = context,
                sourcePath = photo.filePath,
                targetFile = targetFile,
                format = format,
                quality = quality,
                scalePercent = scalePercent,
                preserveExif = preserveExif
            )
            if (success) {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(targetFile.absolutePath, opts)
                val w = opts.outWidth.takeIf { it > 0 } ?: photo.width
                val h = opts.outHeight.takeIf { it > 0 } ?: photo.height

                val newEntity = photo.copy(
                    id = 0,
                    title = "${photo.title} ($quality%)",
                    filePath = targetFile.absolutePath,
                    albumName = "Compressed",
                    dateTaken = System.currentTimeMillis(),
                    width = w,
                    height = h,
                    sizeBytes = newBytes,
                    tags = (photo.tagList + listOf("Compressed", format)).distinct().joinToString(",")
                )
                val id = photoDao.insertPhoto(newEntity)
                results.add(newEntity.copy(id = id))
            }
        }
        results
    }

    suspend fun exportAiCutout(photo: PhotoEntity): PhotoEntity? = withContext(Dispatchers.IO) {
        val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
        val safeName = photo.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "cutout" }
        val targetFile = File(galleryDir, "CUTOUT_${System.currentTimeMillis()}_${safeName}.png")

        val success = com.example.ui.studio.StudioImageEngine.createTransparentCutout(
            context = context,
            sourcePath = photo.filePath,
            targetFile = targetFile
        )
        if (!success) return@withContext null

        photoDao.insertCustomAlbum(
            CustomAlbumEntity("AI Cutouts", "Transparent PNG subject cutouts")
        )
        val entity = photo.copy(
            id = 0,
            title = "${photo.title} (Cutout)",
            filePath = targetFile.absolutePath,
            albumName = "AI Cutouts",
            dateTaken = System.currentTimeMillis(),
            mimeType = "image/png",
            sizeBytes = targetFile.length(),
            tags = (photo.tagList + listOf("AI", "Cutout", "PNG")).distinct().joinToString(",")
        )
        val id = photoDao.insertPhoto(entity)
        entity.copy(id = id)
    }

    suspend fun exportAiInpaint(photo: PhotoEntity, maskRects: List<RectF>): PhotoEntity? = withContext(Dispatchers.IO) {
        val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
        val safeName = photo.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "inpaint" }
        val targetFile = File(galleryDir, "INPAINT_${System.currentTimeMillis()}_${safeName}.jpg")

        val success = com.example.ui.studio.StudioImageEngine.inpaintObjectRemoval(
            context = context,
            sourcePath = photo.filePath,
            targetFile = targetFile,
            maskRects = maskRects
        )
        if (!success || !targetFile.exists()) return@withContext null

        photoDao.insertCustomAlbum(
            CustomAlbumEntity("AI Object Removal", "Distraction-free AI inpainted photos")
        )
        val entity = photo.copy(
            id = 0,
            title = "${photo.title} (Object Removed)",
            filePath = targetFile.absolutePath,
            albumName = "AI Object Removal",
            dateTaken = System.currentTimeMillis(),
            mimeType = "image/jpeg",
            sizeBytes = targetFile.length(),
            tags = (photo.tagList + listOf("AI", "Inpaint", "Clean")).distinct().joinToString(",")
        )
        val id = photoDao.insertPhoto(entity)
        entity.copy(id = id)
    }

    suspend fun exportAiBgReplace(photo: PhotoEntity, bgStyle: String = "Studio Gradient"): PhotoEntity? = withContext(Dispatchers.IO) {
        val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
        val safeName = photo.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "bg_swap" }
        val targetFile = File(galleryDir, "BG_${System.currentTimeMillis()}_${safeName}.jpg")

        val success = com.example.ui.studio.StudioImageEngine.replaceBackground(
            context = context,
            sourcePath = photo.filePath,
            targetFile = targetFile,
            style = bgStyle
        )
        if (!success || !targetFile.exists()) return@withContext null

        photoDao.insertCustomAlbum(
            CustomAlbumEntity("AI Backgrounds", "Pro studio backdrops & custom backgrounds")
        )
        val entity = photo.copy(
            id = 0,
            title = "${photo.title} (Studio BG)",
            filePath = targetFile.absolutePath,
            albumName = "AI Backgrounds",
            dateTaken = System.currentTimeMillis(),
            mimeType = "image/jpeg",
            sizeBytes = targetFile.length(),
            tags = (photo.tagList + listOf("AI", "Background", bgStyle)).distinct().joinToString(",")
        )
        val id = photoDao.insertPhoto(entity)
        entity.copy(id = id)
    }

    suspend fun exportAiRestoration(photo: PhotoEntity): PhotoEntity? = withContext(Dispatchers.IO) {
        val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
        val safeName = photo.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "restored" }
        val targetFile = File(galleryDir, "RESTORED_${System.currentTimeMillis()}_${safeName}.jpg")

        val success = com.example.ui.studio.StudioImageEngine.restoreOldPhoto(
            context = context,
            sourcePath = photo.filePath,
            targetFile = targetFile
        )
        if (!success || !targetFile.exists()) return@withContext null

        photoDao.insertCustomAlbum(
            CustomAlbumEntity("Restored Photos", "AI dynamic range & color revived restorations")
        )
        val entity = photo.copy(
            id = 0,
            title = "${photo.title} (Restored)",
            filePath = targetFile.absolutePath,
            albumName = "Restored Photos",
            dateTaken = System.currentTimeMillis(),
            mimeType = "image/jpeg",
            sizeBytes = targetFile.length(),
            tags = (photo.tagList + listOf("AI", "Restored", "Revived")).distinct().joinToString(",")
        )
        val id = photoDao.insertPhoto(entity)
        entity.copy(id = id)
    }

    suspend fun exportAiSuperRes(photo: PhotoEntity): PhotoEntity? = withContext(Dispatchers.IO) {
        val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
        val safeName = photo.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "upscaled" }
        val targetFile = File(galleryDir, "UPSCALED_${System.currentTimeMillis()}_${safeName}.jpg")

        val success = com.example.ui.studio.StudioImageEngine.superResolutionUpscale(
            context = context,
            sourcePath = photo.filePath,
            targetFile = targetFile,
            scaleFactor = 2.0f
        )
        if (!success || !targetFile.exists()) return@withContext null

        photoDao.insertCustomAlbum(
            CustomAlbumEntity("AI Upscaled", "2x super-resolution crisp detail prints")
        )
        val entity = photo.copy(
            id = 0,
            title = "${photo.title} (2x Crisp)",
            filePath = targetFile.absolutePath,
            albumName = "AI Upscaled",
            dateTaken = System.currentTimeMillis(),
            mimeType = "image/jpeg",
            width = (photo.width * 2).coerceAtLeast(1200),
            height = (photo.height * 2).coerceAtLeast(1200),
            sizeBytes = targetFile.length(),
            tags = (photo.tagList + listOf("AI", "Upscaled", "SuperRes")).distinct().joinToString(",")
        )
        val id = photoDao.insertPhoto(entity)
        entity.copy(id = id)
    }

    suspend fun createAdvancedCollage(
        photoPaths: List<String>,
        layoutMode: com.example.ui.studio.CollageLayoutMode,
        aspectRatio: CropAspectRatio,
        spacingDp: Float,
        cornerRadiusDp: Float,
        backgroundColorHex: String
    ): PhotoEntity? = withContext(Dispatchers.IO) {
        val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
        val targetFile = File(galleryDir, "COLLAGE_${System.currentTimeMillis()}.jpg")
        val bgInt = try {
            android.graphics.Color.parseColor(backgroundColorHex)
        } catch (_: Exception) {
            android.graphics.Color.parseColor("#141418")
        }
        val success = com.example.ui.studio.StudioImageEngine.createMultiPhotoCollage(
            context = context,
            photoPaths = photoPaths,
            targetFile = targetFile,
            layoutMode = layoutMode,
            aspectRatio = aspectRatio,
            spacingDp = spacingDp,
            cornerRadiusDp = cornerRadiusDp,
            backgroundColorInt = bgInt
        )
        if (!success || !targetFile.exists()) return@withContext null

        photoDao.insertCustomAlbum(
            CustomAlbumEntity("Collages", "Creative Studio Photo Collages")
        )
        val entity = PhotoEntity(
            title = "Collage ${System.currentTimeMillis() % 10000}",
            filePath = targetFile.absolutePath,
            albumName = "Collages",
            tags = "Collage,Studio,Creative",
            mimeType = "image/jpeg",
            dateTaken = System.currentTimeMillis(),
            sizeBytes = targetFile.length(),
            width = 2048,
            height = 2048
        )
        val id = photoDao.insertPhoto(entity)
        entity.copy(id = id)
    }

    suspend fun exportRawDevelopedPhoto(
        photo: PhotoEntity,
        kelvinTemp: Int,
        tint: Float,
        exposureEv: Float,
        highlightRecovery: Float,
        shadowRecovery: Float,
        chromaDenoise: Float
    ): PhotoEntity? = withContext(Dispatchers.IO) {
        val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
        val safeName = photo.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "raw" }
        val targetFile = File(galleryDir, "RAW_DEV_${System.currentTimeMillis()}_${safeName}.jpg")

        val tempShift = ((kelvinTemp - 5500) / 80f).coerceIn(-50f, 50f)
        val success = com.example.ui.studio.StudioImageEngine.processAndExportPhoto(
            context = context,
            sourcePathOrUri = photo.filePath,
            targetFile = targetFile,
            exposure = exposureEv,
            temperature = tempShift,
            tint = tint,
            highlights = highlightRecovery,
            shadows = shadowRecovery
        )
        if (!success || !targetFile.exists()) return@withContext null

        photoDao.insertCustomAlbum(
            CustomAlbumEntity("RAW Develop", "Developed RAW/DNG master photographs")
        )
        val entity = photo.copy(
            id = 0,
            title = "${photo.title} (Dev)",
            filePath = targetFile.absolutePath,
            albumName = "RAW Develop",
            mimeType = "image/jpeg",
            dateTaken = System.currentTimeMillis(),
            sizeBytes = targetFile.length(),
            tags = (photo.tagList + listOf("RAW", "Developed")).distinct().joinToString(",")
        )
        val id = photoDao.insertPhoto(entity)
        entity.copy(id = id)
    }

    suspend fun executeBatchStudioAction(
        targets: List<PhotoEntity>,
        mode: com.example.ui.studio.BatchActionMode,
        presetId: String?,
        cropRatio: CropAspectRatio = CropAspectRatio.ORIGINAL,
        filterIntensity: Float = 1f,
        rotationDegrees: Int = 0
    ): Int = withContext(Dispatchers.IO) {
        var processed = 0
        val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
        when (mode) {
            com.example.ui.studio.BatchActionMode.APPLY_FILTER,
            com.example.ui.studio.BatchActionMode.BATCH_EDIT -> {
                val filter: com.example.ui.studio.StudioFilterItem? = if (presetId != null && presetId != "none") {
                    com.example.ui.studio.CinematicFilterCatalog.findFilter(presetId)
                } else null
                for (photo in targets) {
                    val safeName = photo.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "batch" }
                    val targetFile = File(galleryDir, "BATCH_${System.currentTimeMillis()}_${safeName}.jpg")
                    val ok = com.example.ui.studio.StudioImageEngine.processAndExportPhoto(
                        context = context,
                        sourcePathOrUri = photo.filePath,
                        targetFile = targetFile,
                        cropRatio = cropRatio,
                        rotationDegrees = rotationDegrees,
                        filterMatrix = filter?.matrixArray,
                        filterIntensity = filterIntensity
                    )
                    if (ok && targetFile.exists()) {
                        val tagsList = photo.tagList.toMutableList()
                        tagsList.add("Batch")
                        if (filter != null) tagsList.add(filter.name)
                        if (cropRatio != CropAspectRatio.ORIGINAL) tagsList.add(cropRatio.label)
                        val entity = photo.copy(
                            id = 0,
                            title = "${photo.title} (Batch)",
                            filePath = targetFile.absolutePath,
                            albumName = "Batch Edits",
                            dateTaken = System.currentTimeMillis(),
                            sizeBytes = targetFile.length(),
                            tags = tagsList.distinct().joinToString(",")
                        )
                        photoDao.insertPhoto(entity)
                        processed++
                    }
                }
            }
            com.example.ui.studio.BatchActionMode.COMPRESS -> {
                val results = compressPhotoBatch(targets, "WebP", 80, 85, true)
                processed = results.size
            }
            com.example.ui.studio.BatchActionMode.RESIZE_50 -> {
                val results = compressPhotoBatch(targets, "JPEG", 90, 50, true)
                processed = results.size
            }
            com.example.ui.studio.BatchActionMode.CONVERT_JPEG -> {
                val results = compressPhotoBatch(targets, "JPEG", 94, 100, true)
                processed = results.size
            }
        }
        processed
    }

    suspend fun updateMetadata(
        photo: PhotoEntity,
        newTitle: String,
        newAlbum: String,
        newTags: String,
        newNotes: String,
        newLocation: String,
        newCameraModel: String = photo.cameraModel,
        newAperture: String = photo.aperture,
        newShutterSpeed: String = photo.shutterSpeed,
        newIso: String = photo.iso,
        newFocalLength: String = photo.focalLength
    ) {
        val cleanAlbum = newAlbum.trim().ifEmpty { photo.albumName }
        photoDao.insertCustomAlbum(CustomAlbumEntity(cleanAlbum))
        photoDao.updatePhoto(
            photo.copy(
                title = newTitle.trim().ifEmpty { photo.title },
                albumName = cleanAlbum,
                tags = newTags.trim().ifEmpty { "Photography" },
                notes = newNotes.trim(),
                locationTag = newLocation.trim().ifEmpty { "Local Device" },
                cameraModel = newCameraModel.trim().ifEmpty { photo.cameraModel },
                aperture = newAperture.trim().ifEmpty { photo.aperture },
                shutterSpeed = newShutterSpeed.trim().ifEmpty { photo.shutterSpeed },
                iso = newIso.trim().ifEmpty { photo.iso },
                focalLength = newFocalLength.trim().ifEmpty { photo.focalLength }
            )
        )
    }

    suspend fun assignPhotoCategory(photo: PhotoEntity, categoryName: String) {
        val cleanCategory = categoryName.trim().ifEmpty { return }
        photoDao.insertCustomAlbum(CustomAlbumEntity(cleanCategory, "$cleanCategory photo category"))
        val latest = photoDao.getPhotoById(photo.id) ?: photo
        val mergedTags = (latest.tagList + cleanCategory).distinct().joinToString(",")
        photoDao.updatePhoto(
            latest.copy(
                albumName = cleanCategory,
                tags = mergedTags
            )
        )
    }

    suspend fun toggleFavorite(photo: PhotoEntity): Boolean {
        val latest = photoDao.getPhotoById(photo.id)
        val currentFav = latest?.isFavorite ?: photo.isFavorite
        val nextFav = !currentFav
        photoDao.setFavorite(photo.id, nextFav)
        return nextFav
    }

    suspend fun setFavoriteBatch(ids: List<Long>, favorite: Boolean) {
        photoDao.setFavoriteBatch(ids, favorite)
    }

    suspend fun movePhotosToAlbum(ids: List<Long>, albumName: String) {
        photoDao.insertCustomAlbum(CustomAlbumEntity(albumName))
        photoDao.movePhotosToAlbum(ids, albumName)
    }

    suspend fun copyPhotosToAlbum(ids: List<Long>, targetAlbum: String) =
        withContext(Dispatchers.IO) {
            photoDao.insertCustomAlbum(CustomAlbumEntity(targetAlbum))
            val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
            val originals = photoDao.getPhotosByIds(ids)
            for (photo in originals) {
                val safeBaseName = photo.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "photo" }
                val dest = File(galleryDir, "CPY_${System.currentTimeMillis()}_${safeBaseName}.jpg")
                val copied = PhotoImageProcessor.copyPathOrUriToFile(context, photo.filePath, dest)
                photoDao.insertPhoto(
                    photo.copy(
                        id = 0,
                        filePath = if (copied && dest.exists()) dest.absolutePath else photo.filePath,
                        albumName = targetAlbum,
                        dateTaken = System.currentTimeMillis()
                    )
                )
            }
        }

    suspend fun setVaultLockedBatch(ids: List<Long>, locked: Boolean) {
        photoDao.setVaultLockedBatch(ids, locked)
    }

    suspend fun moveToTrashBatch(ids: List<Long>) {
        photoDao.moveToTrashBatch(ids, System.currentTimeMillis())
    }

    suspend fun restoreFromTrashBatch(ids: List<Long>) {
        photoDao.restoreFromTrashBatch(ids)
    }

    suspend fun permanentlyDeleteBatch(ids: List<Long>): Int = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext 0
        val prefs = context.getSharedPreferences("simple_gallery_prefs", Context.MODE_PRIVATE)
        val deletedLocalPaths = (prefs.getStringSet("deleted_local_photo_paths", emptySet()) ?: emptySet())
            .toMutableSet()
        val photos = photoDao.getPhotosByIds(ids)
        for (photo in photos) {
            deletedLocalPaths.add(photo.filePath)
            extractMediaIdFromPathOrUri(photo.filePath)?.let { mediaId ->
                deletedLocalPaths.add("media_id:$mediaId")
            }
            if (photo.filePath.startsWith("content://")) {
                runCatching {
                    context.contentResolver.delete(Uri.parse(photo.filePath), null, null)
                }
            } else {
                val file = File(photo.filePath)
                deletedLocalPaths.add(Uri.fromFile(file).toString())
                runCatching {
                    if (file.exists()) {
                        file.delete()
                    }
                }
                runCatching {
                    context.contentResolver.delete(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "${MediaStore.Images.Media.DATA} = ?",
                        arrayOf(file.absolutePath)
                    )
                }
            }
        }
        prefs.edit().putStringSet("deleted_local_photo_paths", deletedLocalPaths).apply()
        photoDao.deletePhotosByIds(ids)
        photos.size.coerceAtLeast(ids.size)
    }

    suspend fun duplicatePhoto(photo: PhotoEntity): PhotoEntity? = withContext(Dispatchers.IO) {
        val galleryDir = PhotoImageProcessor.getGalleryDirectory(context)
        val safeBaseName = photo.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "photo" }
        val dest = File(galleryDir, "DUP_${System.currentTimeMillis()}_${safeBaseName}.jpg")
        val copied = PhotoImageProcessor.copyPathOrUriToFile(context, photo.filePath, dest)
        val copyEntity = photo.copy(
            id = 0,
            title = "${photo.title} (Copy)",
            filePath = if (copied && dest.exists()) dest.absolutePath else photo.filePath,
            dateTaken = System.currentTimeMillis()
        )
        val newId = photoDao.insertPhoto(copyEntity)
        copyEntity.copy(id = newId)
    }

    suspend fun batchShrinkPhotos(
        ids: List<Long>,
        scaleFactor: Float = 0.75f,
        jpegQuality: Int = 80
    ): Int = withContext(Dispatchers.IO) {
        val photos = photoDao.getPhotosByIds(ids)
        var shrunkCount = 0
        for (photo in photos) {
            val saved = shrinkPhoto(
                original = photo,
                scaleFactor = scaleFactor,
                jpegQuality = jpegQuality,
                replaceOriginal = false
            )
            if (saved != null) {
                shrunkCount++
            }
        }
        shrunkCount
    }

    suspend fun emptyTrash(): Int = withContext(Dispatchers.IO) {
        val trashed = photoDao.getTrashedPhotosOnce()
        val ids = trashed.map { it.id }
        if (ids.isNotEmpty()) {
            permanentlyDeleteBatch(ids)
        }
        ids.size
    }
}

