package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object PhotoImageProcessor {

    val STARTER_SAMPLE_FILENAMES: Set<String> = setOf(
        "IMG_2026_FAMILY_SUNSET_WALK.jpg",
        "IMG_2026_PARK_STROLL_BURST.jpg",
        "IMG_2026_GOLDEN_OCEAN_LIGHT.jpg",
        "IMG_2026_MISTY_FOREST_CANOPY.jpg",
        "IMG_2026_ALPINE_DAWN.jpg",
        "IMG_2026_COURTYARD_ARC.jpg",
        "IMG_2026_MONSTERA_DEW.jpg",
        "IMG_2026_VOLCANIC_TIDE.jpg",
        "IMG_2026_SHINJUKU_RAIN.jpg",
        "IMG_2026_SPIRAL_ATRIUM.jpg",
        "IMG_2026_MAYA_NEWBORN.jpg",
        "IMG_2026_MAYA_3M_SMILE.jpg",
        "IMG_2026_MAYA_6M_SITTING.jpg",
        "IMG_2026_MAYA_1ST_BIRTHDAY.jpg",
        "IMG_2026_MAYA_FIRST_STEPS.jpg",
        "IMG_2026_LIAM_COFFEE.jpg",
        "IMG_2026_REX_PARK.jpg",
        "IMG_2026_SCREEN_FLIGHT.jpg",
        "IMG_2026_RECEIPT_CAFE.jpg",
        "IMG_2026_VIDEO_SUNSET_REEL.jpg"
    )

    fun isStarterSamplePath(pathOrUri: String): Boolean {
        if (pathOrUri.isBlank()) return false
        val name = pathOrUri.substringAfterLast('/')
        return name in STARTER_SAMPLE_FILENAMES
    }

    fun resolveCoilModel(pathOrUri: String): Any {
        return if (
            pathOrUri.startsWith("content://") ||
            pathOrUri.startsWith("file://") ||
            pathOrUri.startsWith("android.resource://")
        ) {
            Uri.parse(pathOrUri)
        } else {
            File(pathOrUri)
        }
    }

    fun canDisplayPhotoPath(pathOrUri: String): Boolean {
        if (pathOrUri.isBlank()) return false
        if (
            pathOrUri.startsWith("content://") ||
            pathOrUri.startsWith("file://") ||
            pathOrUri.startsWith("android.resource://")
        ) {
            return true
        }
        return File(pathOrUri).exists()
    }

    fun decodeBitmapFromPathOrUri(
        context: Context?,
        pathOrUri: String,
        options: BitmapFactory.Options? = null
    ): Bitmap? {
        if (pathOrUri.isBlank()) return null
        try {
            if (
                pathOrUri.startsWith("content://") ||
                pathOrUri.startsWith("file://") ||
                pathOrUri.startsWith("android.resource://")
            ) {
                val resolver = context?.contentResolver ?: return null
                val uri = Uri.parse(pathOrUri)
                return resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
            }
            val file = File(pathOrUri)
            if (file.exists()) {
                val decoded = BitmapFactory.decodeFile(file.absolutePath, options)
                if (decoded != null || options?.inJustDecodeBounds == true) {
                    return decoded
                }
            }
            if (context != null && file.exists()) {
                return context.contentResolver.openInputStream(Uri.fromFile(file))?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
            }
        } catch (_: Exception) {
        }
        return null
    }

    fun readExifFromPathOrUri(context: Context?, pathOrUri: String): ExifInterface? {
        if (pathOrUri.isBlank()) return null
        return try {
            if (
                pathOrUri.startsWith("content://") ||
                pathOrUri.startsWith("file://") ||
                pathOrUri.startsWith("android.resource://")
            ) {
                context?.contentResolver?.openInputStream(Uri.parse(pathOrUri))?.use { stream ->
                    ExifInterface(stream)
                }
            } else {
                val file = File(pathOrUri)
                if (file.exists()) ExifInterface(file.absolutePath) else null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun copyPathOrUriToFile(context: Context, pathOrUri: String, destFile: File): Boolean {
        return try {
            if (
                pathOrUri.startsWith("content://") ||
                pathOrUri.startsWith("file://") ||
                pathOrUri.startsWith("android.resource://")
            ) {
                context.contentResolver.openInputStream(Uri.parse(pathOrUri))?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                destFile.exists() && destFile.length() > 0L
            } else {
                val src = File(pathOrUri)
                if (src.exists()) {
                    src.copyTo(destFile, overwrite = true)
                    destFile.exists() && destFile.length() > 0L
                } else {
                    false
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    fun getGalleryDirectory(context: Context): File {
        val dir = File(context.filesDir, "gallery_photos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun computeLuminanceHistogram(
        filePath: String,
        bins: Int = 28,
        context: Context? = null
    ): List<Float> =
        withContext(Dispatchers.IO) {
            val options = BitmapFactory.Options().apply { inSampleSize = 4 }
            val bmp = decodeBitmapFromPathOrUri(context, filePath, options)
                ?: return@withContext List(bins) { 0.2f }
            val counts = IntArray(bins)
            val width = bmp.width
            val height = bmp.height
            val stepX = (width / 40).coerceAtLeast(1)
            val stepY = (height / 40).coerceAtLeast(1)
            var y = 0
            while (y < height) {
                var x = 0
                while (x < width) {
                    val pixel = bmp.getPixel(x, y)
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)
                    val lum = (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)
                    val bin = ((lum / 256f) * bins).toInt().coerceIn(0, bins - 1)
                    counts[bin]++
                    x += stepX
                }
                y += stepY
            }
            bmp.recycle()
            val maxCount = (counts.maxOrNull() ?: 1).coerceAtLeast(1)
            counts.map { (it.toFloat() / maxCount).coerceIn(0.05f, 1f) }
        }

    fun buildCombinedColorMatrix(
        preset: PhotoFilterPreset,
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): ColorMatrix {
        val matrix = ColorMatrix()

        // 1. Saturation
        val satMatrix = ColorMatrix().apply { setSaturation(saturation) }
        matrix.postConcat(satMatrix)

        // 2. Preset Color Matrix
        val presetMatrix = when (preset) {
            PhotoFilterPreset.ORIGINAL -> ColorMatrix()
            PhotoFilterPreset.WARM_AMBER -> ColorMatrix(
                floatArrayOf(
                    1.14f, 0.05f, 0f, 0f, 12f,
                    0.02f, 1.03f, 0f, 0f, 6f,
                    0f, 0f, 0.86f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            PhotoFilterPreset.CLASSIC_BW -> ColorMatrix().apply {
                setSaturation(0f)
                postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            1.22f, 0f, 0f, 0f, -18f,
                            0f, 1.22f, 0f, 0f, -18f,
                            0f, 0f, 1.22f, 0f, -18f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
            }
            PhotoFilterPreset.KODAK_SEPIA -> ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 10f,
                    0.349f, 0.686f, 0.168f, 0f, 5f,
                    0.272f, 0.534f, 0.131f, 0f, -8f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            PhotoFilterPreset.CYBER_CYAN -> ColorMatrix(
                floatArrayOf(
                    0.88f, 0f, 0.08f, 0f, -8f,
                    0f, 1.06f, 0.08f, 0f, 6f,
                    0.05f, 0.10f, 1.20f, 0f, 18f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            PhotoFilterPreset.HIGH_CONTRAST -> ColorMatrix(
                floatArrayOf(
                    1.32f, 0f, 0f, 0f, -34f,
                    0f, 1.32f, 0f, 0f, -34f,
                    0f, 0f, 1.32f, 0f, -34f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            PhotoFilterPreset.FADE_MATTE -> ColorMatrix(
                floatArrayOf(
                    0.90f, 0.04f, 0.04f, 0f, 24f,
                    0.04f, 0.90f, 0.04f, 0f, 22f,
                    0.04f, 0.04f, 0.88f, 0f, 26f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            PhotoFilterPreset.EMERALD -> ColorMatrix(
                floatArrayOf(
                    0.92f, 0.06f, 0f, 0f, -6f,
                    0.04f, 1.16f, 0.04f, 0f, 8f,
                    0f, 0.06f, 0.96f, 0f, 2f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        matrix.postConcat(presetMatrix)

        // 3. Contrast & Brightness
        val translate = (1f - contrast) * 128f + (brightness * 255f)
        val cbMatrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        matrix.postConcat(cbMatrix)
        return matrix
    }

    suspend fun renderAndSaveEditedPhoto(
        sourceFilePath: String,
        targetFile: File,
        preset: PhotoFilterPreset,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        rotationDegrees: Int,
        flipHorizontal: Boolean,
        flipVertical: Boolean,
        cropRatio: CropAspectRatio,
        customCropBounds: RectF? = null,
        context: Context? = null
    ): Triple<Int, Int, Long>? = withContext(Dispatchers.IO) {
        val srcBitmap = decodeBitmapFromPathOrUri(context, sourceFilePath) ?: return@withContext null

        // Step 1: Rotate & Flip via Matrix
        val geoMatrix = Matrix()
        val sx = if (flipHorizontal) -1f else 1f
        val sy = if (flipVertical) -1f else 1f
        if (flipHorizontal || flipVertical) {
            geoMatrix.postScale(sx, sy, srcBitmap.width / 2f, srcBitmap.height / 2f)
        }
        if (rotationDegrees % 360 != 0) {
            geoMatrix.postRotate(rotationDegrees.toFloat())
        }

        val transformedBitmap = Bitmap.createBitmap(
            srcBitmap,
            0,
            0,
            srcBitmap.width,
            srcBitmap.height,
            geoMatrix,
            true
        )

        // Step 2: Apply Custom Interactive Crop Bounds OR Aspect Ratio Crop
        val hasCustomBounds = customCropBounds != null &&
            (customCropBounds.left > 0.01f ||
                customCropBounds.top > 0.01f ||
                customCropBounds.right < 0.99f ||
                customCropBounds.bottom < 0.99f)

        val croppedBitmap = when {
            hasCustomBounds && customCropBounds != null -> {
                val bmpW = transformedBitmap.width
                val bmpH = transformedBitmap.height
                val leftPx = (customCropBounds.left.coerceIn(0f, 0.9f) * bmpW).toInt()
                val topPx = (customCropBounds.top.coerceIn(0f, 0.9f) * bmpH).toInt()
                val rightPx = (customCropBounds.right.coerceIn(0.1f, 1f) * bmpW).toInt()
                val bottomPx = (customCropBounds.bottom.coerceIn(0.1f, 1f) * bmpH).toInt()
                val safeW = (rightPx - leftPx).coerceIn(16, bmpW - leftPx)
                val safeH = (bottomPx - topPx).coerceIn(16, bmpH - topPx)
                Bitmap.createBitmap(transformedBitmap, leftPx, topPx, safeW, safeH)
            }
            cropRatio.ratio != null -> {
                val targetRatio = cropRatio.ratio
                val currentW = transformedBitmap.width.toFloat()
                val currentH = transformedBitmap.height.toFloat()
                val currentRatio = currentW / currentH
                val (cropW, cropH) = if (currentRatio > targetRatio) {
                    (currentH * targetRatio).toInt() to currentH.toInt()
                } else {
                    currentW.toInt() to (currentW / targetRatio).toInt()
                }
                val safeW = cropW.coerceIn(1, transformedBitmap.width)
                val safeH = cropH.coerceIn(1, transformedBitmap.height)
                val offsetX = ((transformedBitmap.width - safeW) / 2).coerceAtLeast(0)
                val offsetY = ((transformedBitmap.height - safeH) / 2).coerceAtLeast(0)
                Bitmap.createBitmap(transformedBitmap, offsetX, offsetY, safeW, safeH)
            }
            else -> transformedBitmap
        }

        // Step 3: Apply ColorMatrix filter
        val finalBitmap = Bitmap.createBitmap(
            croppedBitmap.width,
            croppedBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(finalBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(
                buildCombinedColorMatrix(preset, brightness, contrast, saturation)
            )
        }
        canvas.drawBitmap(croppedBitmap, 0f, 0f, paint)

        FileOutputStream(targetFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        val resultWidth = finalBitmap.width
        val resultHeight = finalBitmap.height
        val resultSize = targetFile.length()

        if (srcBitmap != transformedBitmap) srcBitmap.recycle()
        if (transformedBitmap != croppedBitmap) transformedBitmap.recycle()
        if (croppedBitmap != finalBitmap) croppedBitmap.recycle()
        finalBitmap.recycle()

        Triple(resultWidth, resultHeight, resultSize)
    }

    suspend fun generateStarterLibraryIfNeeded(context: Context): List<PhotoEntity> =
        withContext(Dispatchers.IO) {
            val galleryDir = getGalleryDirectory(context)
            val now = System.currentTimeMillis()
            val hourMs = 3_600_000L
            val dayMs = 86_400_000L

            data class StarterSpec(
                val fileName: String,
                val title: String,
                val album: String,
                val width: Int,
                val height: Int,
                val ageMs: Long,
                val camera: String,
                val aperture: String,
                val shutter: String,
                val iso: String,
                val focal: String,
                val location: String,
                val tags: String,
                val notes: String,
                val isFavorite: Boolean,
                val dominantHex: String,
                val styleIndex: Int,
                val milestoneSticker: String = "",
                val isVideo: Boolean = false,
                val durationMs: Long = 0L
            )

            val specs = listOf(
                StarterSpec(
                    fileName = "IMG_2026_FAMILY_SUNSET_WALK.jpg",
                    title = "Sunny autumn afternoon walk in the park with everyone smiling.",
                    album = "Family",
                    width = 1080,
                    height = 720,
                    ageMs = 1 * hourMs, // Today
                    camera = "Pixel 9 Pro",
                    aperture = "f/1.7",
                    shutter = "1/800s",
                    iso = "ISO 64",
                    focal = "24mm",
                    location = "Lakeside Sunset Park",
                    tags = "Family,Portraits,Golden Hour,Park,Highlight",
                    notes = "Sunny autumn afternoon walk in the park with everyone smiling hand-in-hand.",
                    isFavorite = true,
                    dominantHex = "#F59E0B",
                    styleIndex = 8
                ),
                StarterSpec(
                    fileName = "IMG_2026_PARK_STROLL_BURST.jpg",
                    title = "Burst shot of our park stroll.",
                    album = "Family",
                    width = 960,
                    height = 720,
                    ageMs = 2 * hourMs, // Today
                    camera = "Pixel 9 Pro",
                    aperture = "f/1.7",
                    shutter = "1/1000s",
                    iso = "ISO 80",
                    focal = "24mm",
                    location = "Lakeside Sunset Park",
                    tags = "Family,Portraits,Burst,Sunset,Park",
                    notes = "Candid burst capture as the sun dipped below the cloud bank.",
                    isFavorite = false,
                    dominantHex = "#EA580C",
                    styleIndex = 9
                ),
                StarterSpec(
                    fileName = "IMG_2026_GOLDEN_OCEAN_LIGHT.jpg",
                    title = "Golden light reflecting on the turquoise ocean bay.",
                    album = "Travel",
                    width = 960,
                    height = 720,
                    ageMs = 4 * hourMs, // Today
                    camera = "Leica Q3",
                    aperture = "f/2.8",
                    shutter = "1/640s",
                    iso = "ISO 100",
                    focal = "28mm",
                    location = "Coral Bay Horizon",
                    tags = "Travel,Ocean,Golden Hour,Coastal",
                    notes = "Soft pastel clouds and warm golden rays shimmering over turquoise water.",
                    isFavorite = true,
                    dominantHex = "#0EA5E9",
                    styleIndex = 10
                ),
                StarterSpec(
                    fileName = "IMG_2026_MISTY_FOREST_CANOPY.jpg",
                    title = "Morning mist drifting through the emerald forest canopy.",
                    album = "Nature & Macro",
                    width = 960,
                    height = 720,
                    ageMs = 1 * dayMs + 2 * hourMs, // Yesterday
                    camera = "Sony A7C II",
                    aperture = "f/2.0",
                    shutter = "1/320s",
                    iso = "ISO 160",
                    focal = "35mm",
                    location = "Redwood Mist Trail",
                    tags = "Nature,Forest,Mist,Morning",
                    notes = "Ethereal fog and soft orb of morning sunlight filtering through ancient trees.",
                    isFavorite = true,
                    dominantHex = "#2E6F40",
                    styleIndex = 11
                ),
                StarterSpec(
                    fileName = "IMG_2026_ALPINE_DAWN.jpg",
                    title = "Dolomites Golden Reflection",
                    album = "Landscapes",
                    width = 960,
                    height = 720,
                    ageMs = 1 * dayMs + 4 * hourMs, // Yesterday
                    camera = "Pixel 9 Pro",
                    aperture = "f/1.7",
                    shutter = "1/640s",
                    iso = "ISO 64",
                    focal = "24mm",
                    location = "Lago di Braies, Italy",
                    tags = "Nature,Landscape,Golden Hour,Mountains,RAW",
                    notes = "First light catching the limestone peaks over still glacial waters.",
                    isFavorite = true,
                    dominantHex = "#F4A261",
                    styleIndex = 0
                ),
                StarterSpec(
                    fileName = "IMG_2026_COURTYARD_ARC.jpg",
                    title = "Terracotta Sunbeams Pavilion",
                    album = "Architecture",
                    width = 800,
                    height = 960,
                    ageMs = 1 * dayMs + 6 * hourMs, // Yesterday
                    camera = "Leica Q3",
                    aperture = "f/2.8",
                    shutter = "1/1250s",
                    iso = "ISO 100",
                    focal = "28mm",
                    location = "Kyoto Design Museum",
                    tags = "Architecture,Minimal,Shadows,Warm",
                    notes = "Afternoon sun carving crisp geometric arches across travertine stone.",
                    isFavorite = true,
                    dominantHex = "#E76F51",
                    styleIndex = 1
                ),
                StarterSpec(
                    fileName = "IMG_2026_MONSTERA_DEW.jpg",
                    title = "Emerald Fern & Morning Dew",
                    album = "Nature & Macro",
                    width = 960,
                    height = 720,
                    ageMs = 3 * dayMs, // This Week
                    camera = "Pixel 9 Pro",
                    aperture = "f/2.0",
                    shutter = "1/320s",
                    iso = "ISO 80",
                    focal = "50mm Macro",
                    location = "Cloud Forest Conservatory",
                    tags = "Nature,Macro,Botanical,Green",
                    notes = "Refraction of sunrise inside crystal dewdrops on tropical fern fronds.",
                    isFavorite = false,
                    dominantHex = "#76B09C",
                    styleIndex = 2
                ),
                StarterSpec(
                    fileName = "IMG_2026_VOLCANIC_TIDE.jpg",
                    title = "Reynisfjara Turquoise Tide",
                    album = "Travel",
                    width = 960,
                    height = 640,
                    ageMs = 4 * dayMs, // This Week
                    camera = "DJI Mavic 3 Pro",
                    aperture = "f/2.8",
                    shutter = "1/250s",
                    iso = "ISO 100",
                    focal = "24mm Aerial",
                    location = "Vík, Southern Iceland",
                    tags = "Travel,Landscape,Coastal,Aerial",
                    notes = "Top-down perspective where white Atlantic surf meets black basalt sand.",
                    isFavorite = true,
                    dominantHex = "#2A9D8F",
                    styleIndex = 3
                ),
                StarterSpec(
                    fileName = "IMG_2026_SHINJUKU_RAIN.jpg",
                    title = "Neon Alley Rain Reflections",
                    album = "Street & Night",
                    width = 720,
                    height = 960,
                    ageMs = 10 * dayMs, // Earlier This Month
                    camera = "Sony A7C II",
                    aperture = "f/1.4",
                    shutter = "1/125s",
                    iso = "ISO 800",
                    focal = "35mm",
                    location = "Shinjuku, Tokyo",
                    tags = "Night,Street,Cyberpunk,Bokeh",
                    notes = "Amber lanterns and cyan neon signs mirrored on wet asphalt after midnight.",
                    isFavorite = false,
                    dominantHex = "#E9C46A",
                    styleIndex = 4
                ),
                StarterSpec(
                    fileName = "IMG_2026_SPIRAL_ATRIUM.jpg",
                    title = "Oculus Spiral Staircase",
                    album = "Architecture",
                    width = 840,
                    height = 840,
                    ageMs = 14 * dayMs, // Earlier This Month
                    camera = "Leica Q3",
                    aperture = "f/4.0",
                    shutter = "1/400s",
                    iso = "ISO 200",
                    focal = "28mm",
                    location = "Vatican Museums, Rome",
                    tags = "Architecture,Symmetry,Spiral,Monochrome",
                    notes = "Double-helix bronze and marble stairwell viewed from the top oculus.",
                    isFavorite = true,
                    dominantHex = "#C9A66B",
                    styleIndex = 6
                ),
                StarterSpec(
                    fileName = "IMG_2026_MAYA_NEWBORN.jpg",
                    title = "Maya Welcome Home • Day 1",
                    album = "Family",
                    width = 900,
                    height = 900,
                    ageMs = 395L * dayMs,
                    camera = "Leica Q3",
                    aperture = "f/2.0",
                    shutter = "1/160s",
                    iso = "ISO 250",
                    focal = "28mm",
                    location = "Home Nursery",
                    tags = "Family,Portraits,Newborn,Maya",
                    notes = "First quiet afternoon wrapped in soft knit blankets.",
                    isFavorite = true,
                    dominantHex = "#D9822B",
                    styleIndex = 12
                ),
                StarterSpec(
                    fileName = "IMG_2026_MAYA_3M_SMILE.jpg",
                    title = "Maya Morning Giggles",
                    album = "Family",
                    width = 900,
                    height = 900,
                    ageMs = 305L * dayMs,
                    camera = "Pixel 9 Pro",
                    aperture = "f/1.7",
                    shutter = "1/320s",
                    iso = "ISO 125",
                    focal = "24mm",
                    location = "Sunlit Living Room",
                    tags = "Family,Portraits,Maya,Selfies",
                    notes = "Biggest morning smile right after nap time.",
                    isFavorite = true,
                    dominantHex = "#C84B28",
                    styleIndex = 13,
                    milestoneSticker = "First Smile"
                ),
                StarterSpec(
                    fileName = "IMG_2026_MAYA_6M_SITTING.jpg",
                    title = "Maya Picnic Blanket",
                    album = "Family",
                    width = 920,
                    height = 840,
                    ageMs = 200L * dayMs,
                    camera = "Sony A7C II",
                    aperture = "f/1.8",
                    shutter = "1/500s",
                    iso = "ISO 100",
                    focal = "35mm",
                    location = "Botanical Lawn",
                    tags = "Family,Portraits,Nature,Maya",
                    notes = "Sitting up all on her own on the checkered quilt.",
                    isFavorite = true,
                    dominantHex = "#4A7C59",
                    styleIndex = 14
                ),
                StarterSpec(
                    fileName = "IMG_2026_MAYA_1ST_BIRTHDAY.jpg",
                    title = "Maya 1st Birthday Smash Cake",
                    album = "Keepsakes",
                    width = 960,
                    height = 960,
                    ageMs = 30L * dayMs,
                    camera = "Leica Q3",
                    aperture = "f/2.0",
                    shutter = "1/400s",
                    iso = "ISO 160",
                    focal = "28mm",
                    location = "Backyard Garden",
                    tags = "Family,Portraits,1st Birthday,Keepsakes,Maya",
                    notes = "One candle glowing above strawberry cream cake.",
                    isFavorite = true,
                    dominantHex = "#C84B28",
                    styleIndex = 15,
                    milestoneSticker = "1st Birthday"
                ),
                StarterSpec(
                    fileName = "IMG_2026_MAYA_FIRST_STEPS.jpg",
                    title = "Maya First Steps in the Garden",
                    album = "Family",
                    width = 900,
                    height = 1000,
                    ageMs = 8L * dayMs,
                    camera = "Pixel 9 Pro",
                    aperture = "f/1.7",
                    shutter = "1/1000s",
                    iso = "ISO 80",
                    focal = "24mm",
                    location = "Patio Lawn",
                    tags = "Family,Portraits,Maya,First Steps",
                    notes = "Wobbly joyful steps toward dad across warm afternoon grass.",
                    isFavorite = true,
                    dominantHex = "#4A7C59",
                    styleIndex = 14,
                    milestoneSticker = "First Steps"
                ),
                StarterSpec(
                    fileName = "IMG_2026_LIAM_COFFEE.jpg",
                    title = "Liam Espresso Morning",
                    album = "Family",
                    width = 860,
                    height = 960,
                    ageMs = 6L * dayMs,
                    camera = "Sony A7C II",
                    aperture = "f/2.0",
                    shutter = "1/250s",
                    iso = "ISO 320",
                    focal = "50mm",
                    location = "Roastery Patio",
                    tags = "Family,Portraits,Selfies,Liam",
                    notes = "Warm morning pour-over before the weekend market.",
                    isFavorite = false,
                    dominantHex = "#8A5A44",
                    styleIndex = 16
                ),
                StarterSpec(
                    fileName = "IMG_2026_REX_PARK.jpg",
                    title = "Rex Golden Retriever Fetch",
                    album = "Family",
                    width = 960,
                    height = 760,
                    ageMs = 5L * dayMs,
                    camera = "Pixel 9 Pro",
                    aperture = "f/1.7",
                    shutter = "1/1250s",
                    iso = "ISO 100",
                    focal = "24mm",
                    location = "Cedar Meadow",
                    tags = "Family,Nature,Pet,Rex",
                    notes = "Bounding through tall clover after a tennis ball.",
                    isFavorite = true,
                    dominantHex = "#D9822B",
                    styleIndex = 17
                ),
                StarterSpec(
                    fileName = "IMG_2026_SCREEN_FLIGHT.jpg",
                    title = "Screenshot Boarding Pass SFO-KIX",
                    album = "Screenshots",
                    width = 720,
                    height = 1120,
                    ageMs = 2L * dayMs,
                    camera = "System Capture",
                    aperture = "f/2.0",
                    shutter = "Screen",
                    iso = "sRGB",
                    focal = "UI 1080p",
                    location = "Device Screenshots",
                    tags = "Screenshots,Travel,UI",
                    notes = "Saved mobile itinerary QR pass for Kyoto trip.",
                    isFavorite = false,
                    dominantHex = "#2A6F7B",
                    styleIndex = 18
                ),
                StarterSpec(
                    fileName = "IMG_2026_RECEIPT_CAFE.jpg",
                    title = "Bakery & Film Lab Receipt",
                    album = "Receipts",
                    width = 760,
                    height = 980,
                    ageMs = 3L * dayMs,
                    camera = "Document Scan",
                    aperture = "f/2.2",
                    shutter = "1/120s",
                    iso = "ISO 200",
                    focal = "26mm",
                    location = "Downtown Film Lab",
                    tags = "Receipts,Document,Scan",
                    notes = "Portra 400 C-41 develop & scan itemized receipt.",
                    isFavorite = false,
                    dominantHex = "#D6CFC7",
                    styleIndex = 19
                ),
                StarterSpec(
                    fileName = "IMG_2026_VIDEO_SUNSET_REEL.jpg",
                    title = "Coastal Sunset Drone Clip",
                    album = "Travel",
                    width = 1080,
                    height = 720,
                    ageMs = 12L * hourMs,
                    camera = "DJI Mavic 3 Pro",
                    aperture = "f/2.8",
                    shutter = "1/60s 4K",
                    iso = "ISO 100",
                    focal = "24mm",
                    location = "Big Sur Cliffs",
                    tags = "Videos,Travel,Coastal,Reel",
                    notes = "4K 60fps golden hour coastal flyover clip.",
                    isFavorite = true,
                    dominantHex = "#C84B28",
                    styleIndex = 10,
                    isVideo = true,
                    durationMs = 24_000L
                )
            )

            specs.map { spec ->
                val targetFile = File(galleryDir, spec.fileName)
                if (!targetFile.exists() || targetFile.length() == 0L) {
                    renderArtisticSampleFile(targetFile, spec.width, spec.height, spec.styleIndex)
                }
                PhotoEntity(
                    title = spec.title,
                    filePath = targetFile.absolutePath,
                    albumName = spec.album,
                    dateTaken = now - spec.ageMs,
                    width = spec.width,
                    height = spec.height,
                    sizeBytes = targetFile.length().coerceAtLeast(85_000L),
                    mimeType = if (spec.isVideo) "video/mp4" else "image/jpeg",
                    cameraModel = spec.camera,
                    aperture = spec.aperture,
                    shutterSpeed = spec.shutter,
                    iso = spec.iso,
                    focalLength = spec.focal,
                    locationTag = spec.location,
                    tags = spec.tags,
                    notes = spec.notes,
                    isFavorite = spec.isFavorite,
                    dominantHex = spec.dominantHex,
                    milestoneSticker = spec.milestoneSticker,
                    isVideo = spec.isVideo,
                    durationMs = spec.durationMs
                )
            }
        }

    private fun renderArtisticSampleFile(file: File, width: Int, height: Int, styleIndex: Int) {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val w = width.toFloat()
        val h = height.toFloat()

        when (styleIndex) {
            0 -> {
                // Golden hour alpine mountain & glacial reflection
                paint.shader = LinearGradient(
                    0f, 0f, 0f, h * 0.62f,
                    intArrayOf(
                        Color.parseColor("#1B263B"),
                        Color.parseColor("#774936"),
                        Color.parseColor("#F4A261"),
                        Color.parseColor("#FFE5B4")
                    ),
                    floatArrayOf(0f, 0.42f, 0.78f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h * 0.62f, paint)

                // Sun glow
                paint.shader = RadialGradient(
                    w * 0.68f, h * 0.34f, w * 0.28f,
                    intArrayOf(
                        Color.parseColor("#FFF8E7"),
                        Color.parseColor("#CCF4A261"),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.45f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(w * 0.68f, h * 0.34f, w * 0.28f, paint)

                // Distant mountain range
                val farPeak = Path().apply {
                    moveTo(0f, h * 0.62f)
                    lineTo(w * 0.18f, h * 0.34f)
                    lineTo(w * 0.36f, h * 0.48f)
                    lineTo(w * 0.56f, h * 0.22f)
                    lineTo(w * 0.77f, h * 0.42f)
                    lineTo(w * 0.92f, h * 0.29f)
                    lineTo(w, h * 0.46f)
                    lineTo(w, h * 0.62f)
                    close()
                }
                paint.shader = LinearGradient(
                    0f, h * 0.22f, 0f, h * 0.62f,
                    Color.parseColor("#3D2C2E"),
                    Color.parseColor("#1D1821"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(farPeak, paint)

                // Glacial lake reflection
                paint.shader = LinearGradient(
                    0f, h * 0.62f, 0f, h,
                    intArrayOf(
                        Color.parseColor("#E09F67"),
                        Color.parseColor("#2A6F7B"),
                        Color.parseColor("#112A36")
                    ),
                    floatArrayOf(0f, 0.45f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, h * 0.62f, w, h, paint)

                // Water shimmer lines
                paint.shader = null
                paint.color = Color.parseColor("#66FFE3B3")
                for (i in 0..18) {
                    val ry = h * 0.64f + i * (h * 0.018f)
                    val rw = w * (0.32f - i * 0.012f).coerceAtLeast(0.06f)
                    canvas.drawRoundRect(
                        RectF(w * 0.68f - rw / 2f, ry, w * 0.68f + rw / 2f, ry + 3f),
                        2f, 2f, paint
                    )
                }
            }
            1 -> {
                // Architectural sunbeams & terracotta courtyard
                paint.shader = LinearGradient(
                    0f, 0f, w, h,
                    Color.parseColor("#D97F54"),
                    Color.parseColor("#6E3524"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                // Diagonal shadow wall
                val shadowPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w * 0.65f, 0f)
                    lineTo(w * 0.22f, h)
                    lineTo(0f, h)
                    close()
                }
                paint.shader = LinearGradient(
                    0f, 0f, w * 0.65f, h,
                    Color.parseColor("#2B1810"),
                    Color.parseColor("#47271B"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(shadowPath, paint)

                // Archway opening
                paint.shader = LinearGradient(
                    w * 0.35f, h * 0.2f, w * 0.85f, h * 0.9f,
                    Color.parseColor("#FCEADE"),
                    Color.parseColor("#F4A261"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(
                    RectF(w * 0.34f, h * 0.22f, w * 0.82f, h * 0.92f),
                    w * 0.24f, w * 0.24f, paint
                )

                // Inner architectural colonnade shadow
                paint.shader = null
                paint.color = Color.parseColor("#381D14")
                canvas.drawRect(w * 0.34f, h * 0.76f, w * 0.82f, h * 0.92f, paint)
            }
            2 -> {
                // Botanical emerald macro with glowing bokeh
                paint.shader = RadialGradient(
                    w * 0.75f, h * 0.25f, w * 0.85f,
                    intArrayOf(
                        Color.parseColor("#52B788"),
                        Color.parseColor("#1B4332"),
                        Color.parseColor("#081C15")
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                // Golden bokeh circles
                paint.shader = null
                val bokehPositions = listOf(
                    Triple(0.78f, 0.22f, 0.11f),
                    Triple(0.64f, 0.35f, 0.07f),
                    Triple(0.86f, 0.44f, 0.09f),
                    Triple(0.24f, 0.18f, 0.06f),
                    Triple(0.45f, 0.75f, 0.08f)
                )
                for ((bx, by, br) in bokehPositions) {
                    paint.color = Color.parseColor("#44F4A261")
                    canvas.drawCircle(w * bx, h * by, w * br, paint)
                    paint.color = Color.parseColor("#66FFE8C2")
                    canvas.drawCircle(w * bx, h * by, w * br * 0.55f, paint)
                }

                // Stylized leaf fronds & dewdrops
                paint.color = Color.parseColor("#2D6A4F")
                for (i in 0..6) {
                    val angle = -25f + i * 11f
                    canvas.save()
                    canvas.rotate(angle, w * 0.1f, h * 0.9f)
                    canvas.drawOval(RectF(w * 0.1f, h * 0.35f, w * 0.75f, h * 0.52f), paint)
                    canvas.restore()
                }
                paint.color = Color.parseColor("#CCFFFFFF")
                canvas.drawCircle(w * 0.42f, h * 0.48f, 14f, paint)
                canvas.drawCircle(w * 0.54f, h * 0.56f, 10f, paint)
                canvas.drawCircle(w * 0.35f, h * 0.62f, 12f, paint)
            }
            3 -> {
                // Coastal turquoise waves & volcanic black sand
                paint.shader = LinearGradient(
                    0f, 0f, w, h,
                    intArrayOf(
                        Color.parseColor("#005F73"),
                        Color.parseColor("#0A9396"),
                        Color.parseColor("#94D2BD"),
                        Color.parseColor("#1D1E22")
                    ),
                    floatArrayOf(0f, 0.38f, 0.58f, 0.72f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                // White surf foam curves
                paint.shader = null
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 18f
                paint.color = Color.parseColor("#DDFFFFFF")
                val wavePath = Path().apply {
                    moveTo(0f, h * 0.68f)
                    cubicTo(w * 0.28f, h * 0.52f, w * 0.58f, h * 0.74f, w, h * 0.44f)
                }
                canvas.drawPath(wavePath, paint)
                paint.strokeWidth = 8f
                paint.color = Color.parseColor("#88E0FBFC")
                val wavePath2 = Path().apply {
                    moveTo(0f, h * 0.58f)
                    cubicTo(w * 0.32f, h * 0.42f, w * 0.62f, h * 0.64f, w, h * 0.34f)
                }
                canvas.drawPath(wavePath2, paint)
                paint.style = Paint.Style.FILL
            }
            4 -> {
                // Neon Shinjuku night rain reflections
                paint.shader = LinearGradient(
                    0f, 0f, 0f, h,
                    intArrayOf(
                        Color.parseColor("#0B0914"),
                        Color.parseColor("#1A142E"),
                        Color.parseColor("#121824")
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                val neonColors = listOf("#FF595E", "#FFCA3A", "#00BBF9", "#F15BB5", "#F4A261")
                paint.shader = null
                for (i in 0..14) {
                    val c = Color.parseColor(neonColors[i % neonColors.size])
                    val cx = w * (0.12f + (i * 0.062f) % 0.8f)
                    val cy = h * (0.18f + (i * 0.09f) % 0.42f)
                    paint.color = c
                    paint.alpha = 180
                    canvas.drawRoundRect(RectF(cx, cy, cx + 28f, cy + 90f), 8f, 8f, paint)
                    // Wet pavement reflection
                    paint.alpha = 95
                    canvas.drawRoundRect(
                        RectF(cx - 10f, h - cy * 0.65f, cx + 38f, h - cy * 0.65f + 45f),
                        12f, 12f, paint
                    )
                }
                paint.alpha = 255
            }
            5 -> {
                // Desert dunes under starlight
                paint.shader = LinearGradient(
                    0f, 0f, 0f, h * 0.55f,
                    Color.parseColor("#0D1B2A"),
                    Color.parseColor("#415A77"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h * 0.55f, paint)

                // Stars
                paint.shader = null
                paint.color = Color.parseColor("#EEFFF8E7")
                for (i in 1..45) {
                    val sx = ((i * 73) % 100) / 100f * w
                    val sy = ((i * 41) % 100) / 100f * (h * 0.45f)
                    canvas.drawCircle(sx, sy, if (i % 5 == 0) 3.2f else 1.8f, paint)
                }

                // Lit sand dune side
                val duneLit = Path().apply {
                    moveTo(0f, h * 0.58f)
                    cubicTo(w * 0.3f, h * 0.42f, w * 0.65f, h * 0.62f, w, h * 0.46f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                paint.shader = LinearGradient(
                    0f, h * 0.45f, w, h,
                    Color.parseColor("#E09F5A"),
                    Color.parseColor("#8A4F26"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(duneLit, paint)
            }
            6 -> {
                // Spiral architectural oculus
                paint.shader = RadialGradient(
                    w * 0.5f, h * 0.5f, min(w, h) * 0.6f,
                    intArrayOf(
                        Color.parseColor("#FFF8EB"),
                        Color.parseColor("#D4A373"),
                        Color.parseColor("#4A3728"),
                        Color.parseColor("#1B140E")
                    ),
                    floatArrayOf(0f, 0.28f, 0.68f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                paint.shader = null
                paint.style = Paint.Style.STROKE
                for (ring in 1..9) {
                    val radius = (min(w, h) * 0.055f) * ring
                    paint.strokeWidth = (4f + ring * 1.5f)
                    paint.color = if (ring % 2 == 0) {
                        Color.parseColor("#AA261C15")
                    } else {
                        Color.parseColor("#AAF4A261")
                    }
                    canvas.drawCircle(w * 0.5f + ring * 4f, h * 0.5f - ring * 3f, radius, paint)
                }
                paint.style = Paint.Style.FILL
            }
            8 -> {
                // Family sunset walk along golden water horizon (Hero card 1)
                paint.shader = LinearGradient(
                    0f, 0f, 0f, h * 0.68f,
                    intArrayOf(
                        Color.parseColor("#4A6D8C"),
                        Color.parseColor("#B88B6A"),
                        Color.parseColor("#F59E0B"),
                        Color.parseColor("#FEF08A")
                    ),
                    floatArrayOf(0f, 0.35f, 0.72f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h * 0.68f, paint)

                // Sunburst at center horizon
                paint.shader = RadialGradient(
                    w * 0.52f, h * 0.62f, w * 0.36f,
                    intArrayOf(
                        Color.parseColor("#FFFFFF"),
                        Color.parseColor("#CCFBBF24"),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.4f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(w * 0.52f, h * 0.62f, w * 0.36f, paint)

                // Sunset cloud bands
                paint.shader = null
                paint.color = Color.parseColor("#55475569")
                canvas.drawOval(RectF(w * 0.58f, h * 0.08f, w * 0.96f, h * 0.24f), paint)
                canvas.drawOval(RectF(w * 0.05f, h * 0.14f, w * 0.42f, h * 0.28f), paint)

                // Shimmering water & dark shore
                paint.shader = LinearGradient(
                    0f, h * 0.65f, 0f, h,
                    intArrayOf(
                        Color.parseColor("#F59E0B"),
                        Color.parseColor("#334155"),
                        Color.parseColor("#0F172A")
                    ),
                    floatArrayOf(0f, 0.35f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, h * 0.65f, w, h, paint)

                // Silhouettes of family & friends walking hand-in-hand across the horizon
                paint.shader = null
                paint.color = Color.parseColor("#111827")
                val peopleX = listOf(0.14f, 0.24f, 0.34f, 0.42f, 0.50f, 0.58f, 0.66f, 0.75f, 0.85f)
                peopleX.forEachIndexed { idx, px ->
                    val personH = if (idx % 3 == 1) h * 0.16f else h * 0.21f
                    val baseY = h * 0.78f
                    val topY = baseY - personH
                    // Head
                    canvas.drawCircle(w * px, topY, w * 0.018f, paint)
                    // Torso
                    canvas.drawRoundRect(
                        RectF(w * (px - 0.024f), topY + w * 0.02f, w * (px + 0.024f), baseY),
                        10f, 10f, paint
                    )
                }
            }
            9 -> {
                // Burst shot of park stroll sunset clouds
                paint.shader = LinearGradient(
                    0f, 0f, 0f, h,
                    intArrayOf(
                        Color.parseColor("#3B5978"),
                        Color.parseColor("#C27D54"),
                        Color.parseColor("#F97316"),
                        Color.parseColor("#1E293B")
                    ),
                    floatArrayOf(0f, 0.45f, 0.72f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                paint.shader = RadialGradient(
                    w * 0.5f, h * 0.68f, w * 0.35f,
                    intArrayOf(
                        Color.parseColor("#FFFBEB"),
                        Color.parseColor("#AAFB923C"),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(w * 0.5f, h * 0.68f, w * 0.35f, paint)

                // Dramatic cloud textures
                paint.shader = null
                paint.color = Color.parseColor("#66334155")
                for (i in 0..5) {
                    val cx = w * (0.1f + i * 0.16f)
                    val cy = h * (0.18f + (i % 3) * 0.1f)
                    canvas.drawOval(RectF(cx - 90f, cy - 28f, cx + 110f, cy + 28f), paint)
                }
            }
            10 -> {
                // Golden light reflecting on turquoise ocean bay
                paint.shader = LinearGradient(
                    0f, 0f, 0f, h * 0.54f,
                    intArrayOf(
                        Color.parseColor("#7DD3FC"),
                        Color.parseColor("#FDE68A"),
                        Color.parseColor("#FDBA74")
                    ),
                    floatArrayOf(0f, 0.6f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h * 0.54f, paint)

                // Turquoise ocean below horizon
                paint.shader = LinearGradient(
                    0f, h * 0.54f, 0f, h,
                    intArrayOf(
                        Color.parseColor("#2DD4BF"),
                        Color.parseColor("#0D9488"),
                        Color.parseColor("#D6C5B3")
                    ),
                    floatArrayOf(0f, 0.62f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, h * 0.54f, w, h, paint)

                // Sun reflection column on water
                paint.shader = RadialGradient(
                    w * 0.38f, h * 0.54f, w * 0.32f,
                    intArrayOf(
                        Color.parseColor("#FFFFFF"),
                        Color.parseColor("#BBFDE047"),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.45f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(w * 0.38f, h * 0.54f, w * 0.32f, paint)
            }
            11 -> {
                // Misty emerald forest canopy with glowing sun orb
                paint.shader = LinearGradient(
                    0f, 0f, 0f, h,
                    intArrayOf(
                        Color.parseColor("#3A4F41"),
                        Color.parseColor("#6B8E78"),
                        Color.parseColor("#A3B899"),
                        Color.parseColor("#4D7C0F")
                    ),
                    floatArrayOf(0f, 0.45f, 0.78f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                // Forest tree trunks & canopy branches
                paint.shader = null
                paint.color = Color.parseColor("#29382E")
                paint.strokeWidth = 14f
                for (i in 0..8) {
                    val tx = w * (0.08f + i * 0.11f)
                    canvas.drawLine(tx, h, tx + (i % 3 - 1) * 35f, 0f, paint)
                }

                // Glowing morning sun orb at top center
                paint.color = Color.parseColor("#FFFBEB")
                canvas.drawCircle(w * 0.48f, h * 0.12f, 18f, paint)
            }
            12, 13, 14 -> {
                // Warm Editorial Child Portrait Illustrations (Newborn / 3M / 6M+)
                val bgTop = when (styleIndex) {
                    12 -> "#F8EDE3"
                    13 -> "#FCE8D5"
                    else -> "#E8F0E5"
                }
                val bgBot = when (styleIndex) {
                    12 -> "#E3C5AD"
                    13 -> "#E6B89C"
                    else -> "#B8D0B0"
                }
                paint.shader = LinearGradient(
                    0f, 0f, 0f, h,
                    Color.parseColor(bgTop),
                    Color.parseColor(bgBot),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                // Sunlit bokeh halo behind child portrait
                paint.shader = RadialGradient(
                    w * 0.5f, h * 0.38f, w * 0.42f,
                    intArrayOf(
                        Color.parseColor("#FFFFFAF2"),
                        Color.parseColor("#88F4A261"),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(w * 0.5f, h * 0.38f, w * 0.42f, paint)
                paint.shader = null

                // Warm terracotta/amber outfit silhouette
                paint.color = when (styleIndex) {
                    12 -> Color.parseColor("#D9822B")
                    13 -> Color.parseColor("#C84B28")
                    else -> Color.parseColor("#4A7C59")
                }
                canvas.drawRoundRect(
                    RectF(w * 0.24f, h * 0.56f, w * 0.76f, h * 1.05f),
                    w * 0.22f, w * 0.22f, paint
                )
                // Soft warm portrait head + rosy cheeks + smile
                paint.color = Color.parseColor("#F5D0B5")
                canvas.drawCircle(w * 0.5f, h * 0.38f, w * 0.17f, paint)
                paint.color = Color.parseColor("#5C3A28")
                canvas.drawArc(
                    RectF(w * 0.33f, h * 0.20f, w * 0.67f, h * 0.40f),
                    180f, 180f, true, paint
                )
                paint.color = Color.parseColor("#1F1C1A")
                canvas.drawCircle(w * 0.44f, h * 0.38f, w * 0.015f, paint)
                canvas.drawCircle(w * 0.56f, h * 0.38f, w * 0.015f, paint)
                paint.color = Color.parseColor("#44C84B28")
                canvas.drawCircle(w * 0.40f, h * 0.42f, w * 0.028f, paint)
                canvas.drawCircle(w * 0.60f, h * 0.42f, w * 0.028f, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 6f
                paint.color = Color.parseColor("#8C3B2B")
                canvas.drawArc(
                    RectF(w * 0.44f, h * 0.39f, w * 0.56f, h * 0.47f),
                    15f, 150f, false, paint
                )
                paint.style = Paint.Style.FILL
            }
            15 -> {
                // 1st Birthday Celebration & Cake Scene
                paint.shader = LinearGradient(
                    0f, 0f, 0f, h,
                    Color.parseColor("#FFF6EC"),
                    Color.parseColor("#F4D5B8"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.shader = null
                // Festive confetti dots
                val confetti = listOf("#C84B28", "#D9822B", "#4A7C59", "#E76F51")
                for (i in 0..22) {
                    paint.color = Color.parseColor(confetti[i % confetti.size])
                    val cx = w * (0.08f + ((i * 19) % 84) / 100f)
                    val cy = h * (0.08f + ((i * 29) % 40) / 100f)
                    canvas.drawCircle(cx, cy, 9f, paint)
                }
                // Portrait head behind birthday cake
                paint.color = Color.parseColor("#F5D0B5")
                canvas.drawCircle(w * 0.5f, h * 0.36f, w * 0.16f, paint)
                paint.color = Color.parseColor("#5C3A28")
                canvas.drawArc(RectF(w * 0.34f, h * 0.18f, w * 0.66f, h * 0.38f), 180f, 180f, true, paint)
                paint.color = Color.parseColor("#1F1C1A")
                canvas.drawCircle(w * 0.45f, h * 0.36f, 10f, paint)
                canvas.drawCircle(w * 0.55f, h * 0.36f, 10f, paint)
                // Cake + candle
                paint.color = Color.parseColor("#FFFFFF")
                canvas.drawRoundRect(RectF(w * 0.34f, h * 0.66f, w * 0.66f, h * 0.86f), 20f, 20f, paint)
                paint.color = Color.parseColor("#C84B28")
                canvas.drawRoundRect(RectF(w * 0.34f, h * 0.66f, w * 0.66f, h * 0.72f), 16f, 16f, paint)
                canvas.drawRect(RectF(w * 0.49f, h * 0.55f, w * 0.51f, h * 0.66f), paint)
                paint.color = Color.parseColor("#F59E0B")
                canvas.drawCircle(w * 0.5f, h * 0.53f, 14f, paint)
            }
            16 -> {
                // Liam Editorial Portrait
                paint.shader = LinearGradient(
                    0f, 0f, w, h,
                    Color.parseColor("#EFE5DC"),
                    Color.parseColor("#C9B7A6"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.shader = null
                paint.color = Color.parseColor("#2B2623")
                canvas.drawRoundRect(RectF(w * 0.22f, h * 0.56f, w * 0.78f, h * 1.05f), 120f, 120f, paint)
                paint.color = Color.parseColor("#ECC8AF")
                canvas.drawCircle(w * 0.5f, h * 0.38f, w * 0.165f, paint)
                paint.color = Color.parseColor("#2B211B")
                canvas.drawArc(RectF(w * 0.33f, h * 0.20f, w * 0.67f, h * 0.39f), 180f, 180f, true, paint)
                canvas.drawCircle(w * 0.44f, h * 0.38f, 9f, paint)
                canvas.drawCircle(w * 0.56f, h * 0.38f, 9f, paint)
            }
            17 -> {
                // Rex Golden Retriever on Green Meadow
                paint.shader = LinearGradient(
                    0f, 0f, 0f, h,
                    Color.parseColor("#D8E8D4"),
                    Color.parseColor("#4A7C59"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.shader = null
                // Dog ears & warm golden coat
                paint.color = Color.parseColor("#B46924")
                canvas.drawOval(RectF(w * 0.30f, h * 0.28f, w * 0.42f, h * 0.54f), paint)
                canvas.drawOval(RectF(w * 0.58f, h * 0.28f, w * 0.70f, h * 0.54f), paint)
                paint.color = Color.parseColor("#E09F5A")
                canvas.drawCircle(w * 0.5f, h * 0.42f, w * 0.16f, paint)
                canvas.drawRoundRect(RectF(w * 0.34f, h * 0.56f, w * 0.66f, h * 0.95f), 80f, 80f, paint)
                paint.color = Color.parseColor("#1F1C1A")
                canvas.drawCircle(w * 0.45f, h * 0.40f, 10f, paint)
                canvas.drawCircle(w * 0.55f, h * 0.40f, 10f, paint)
                canvas.drawOval(RectF(w * 0.46f, h * 0.45f, w * 0.54f, h * 0.50f), paint)
            }
            18 -> {
                // Clean Editorial Flight Boarding Pass Screenshot
                paint.color = Color.parseColor("#FAF8F5")
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.color = Color.parseColor("#1F1C1A")
                canvas.drawRoundRect(RectF(w * 0.08f, h * 0.08f, w * 0.92f, h * 0.92f), 36f, 36f, paint)
                paint.color = Color.parseColor("#C84B28")
                canvas.drawRoundRect(RectF(w * 0.08f, h * 0.08f, w * 0.92f, h * 0.24f), 36f, 36f, paint)
                paint.color = Color.WHITE
                paint.textSize = 48f
                paint.isFakeBoldText = true
                canvas.drawText("SFO  →  KIX", w * 0.16f, h * 0.18f, paint)
                paint.color = Color.parseColor("#FAF8F5")
                canvas.drawRoundRect(RectF(w * 0.25f, h * 0.48f, w * 0.75f, h * 0.78f), 22f, 22f, paint)
                paint.color = Color.parseColor("#1F1C1A")
                for (r in 0..5) {
                    for (c in 0..5) {
                        if ((r + c) % 2 == 0) {
                            canvas.drawRect(
                                RectF(
                                    w * 0.32f + c * 42f,
                                    h * 0.54f + r * 42f,
                                    w * 0.32f + c * 42f + 30f,
                                    h * 0.54f + r * 42f + 30f
                                ),
                                paint
                            )
                        }
                    }
                }
            }
            19 -> {
                // Document / Receipt Scan Flat-lay
                paint.color = Color.parseColor("#E6DFD5")
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.color = Color.parseColor("#FFFFFF")
                canvas.drawRoundRect(RectF(w * 0.16f, h * 0.08f, w * 0.84f, h * 0.92f), 14f, 14f, paint)
                paint.color = Color.parseColor("#1F1C1A")
                paint.textSize = 36f
                paint.isFakeBoldText = true
                canvas.drawText("FILM LAB RECEIPT", w * 0.24f, h * 0.18f, paint)
                paint.isFakeBoldText = false
                paint.color = Color.parseColor("#6B625B")
                for (line in 0..6) {
                    val ly = h * (0.28f + line * 0.075f)
                    canvas.drawRoundRect(RectF(w * 0.24f, ly, w * 0.76f, ly + 10f), 4f, 4f, paint)
                }
                paint.color = Color.parseColor("#C84B28")
                paint.textSize = 42f
                paint.isFakeBoldText = true
                canvas.drawText("TOTAL  $28.50", w * 0.24f, h * 0.83f, paint)
            }
            else -> {
                // Warm studio rim-light portrait study
                paint.shader = RadialGradient(
                    w * 0.52f, h * 0.42f, w * 0.6f,
                    intArrayOf(
                        Color.parseColor("#F4A261"),
                        Color.parseColor("#9E3B24"),
                        Color.parseColor("#16110E")
                    ),
                    floatArrayOf(0f, 0.48f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                // Silhouette head & shoulders
                paint.shader = null
                paint.color = Color.parseColor("#1C1411")
                canvas.drawCircle(w * 0.5f, h * 0.42f, w * 0.16f, paint)
                canvas.drawRoundRect(
                    RectF(w * 0.24f, h * 0.58f, w * 0.76f, h),
                    w * 0.22f, w * 0.22f, paint
                )
            }
        }

        // Subtle photographic film grain / vignette border
        paint.shader = RadialGradient(
            w / 2f, h / 2f, maxOf(w, h) * 0.72f,
            intArrayOf(Color.TRANSPARENT, Color.parseColor("#55000000")),
            floatArrayOf(0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)

        FileOutputStream(file).use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        bmp.recycle()
    }

    suspend fun renderPolaroidPostcardFile(
        context: Context,
        memory: MemoryEntryEntity
    ): Triple<File, Int, Int>? = withContext(Dispatchers.IO) {
        val srcBmp = decodeBitmapFromPathOrUri(context, memory.photoFilePath)
        val cardWidth = 900
        val cardHeight = 1140
        val cardBmp = Bitmap.createBitmap(cardWidth, cardHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(cardBmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val style = PolaroidFrameStyle.entries.firstOrNull {
            it.label.equals(memory.frameStyle, ignoreCase = true)
        } ?: PolaroidFrameStyle.CLASSIC_POLAROID

        // Card paper background
        paint.color = Color.parseColor(style.borderHex)
        canvas.drawRect(0f, 0f, cardWidth.toFloat(), cardHeight.toFloat(), paint)

        // Subtle inner border accent
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.parseColor(style.accentHex)
        canvas.drawRoundRect(
            RectF(24f, 24f, cardWidth - 24f, cardHeight - 24f),
            18f, 18f, paint
        )
        paint.style = Paint.Style.FILL

        // Photo window (square-ish Polaroid frame)
        val photoRect = RectF(56f, 56f, cardWidth - 56f, 820f)
        if (srcBmp != null) {
            canvas.drawBitmap(srcBmp, null, photoRect, paint)
            srcBmp.recycle()
        } else {
            paint.color = Color.parseColor("#322C26")
            canvas.drawRect(photoRect, paint)
        }

        // Mood & Milestone Pill at top-right of photo
        paint.color = Color.parseColor(style.accentHex)
        canvas.drawRoundRect(
            RectF(cardWidth - 320f, 82f, cardWidth - 82f, 134f),
            26f, 26f, paint
        )
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText(
            "${memory.mood.uppercase()} • ${memory.milestoneBadge.take(12)}",
            cardWidth - 300f,
            116f,
            paint
        )

        // Title in bottom Polaroid margin
        paint.color = Color.parseColor(style.textHex)
        paint.textSize = 40f
        paint.isFakeBoldText = true
        canvas.drawText(memory.title.take(32), 60f, 890f, paint)

        // Story snippet (up to 2 lines)
        paint.isFakeBoldText = false
        paint.textSize = 27f
        val cleanStory = memory.story.replace("\n", " ").trim()
        val line1 = cleanStory.take(48)
        val line2 = if (cleanStory.length > 48) cleanStory.drop(48).take(48) + "…" else ""
        canvas.drawText(line1, 60f, 942f, paint)
        if (line2.isNotEmpty()) {
            canvas.drawText(line2, 60f, 980f, paint)
        }

        // Footer metadata stamp
        paint.color = Color.parseColor(style.accentHex)
        paint.textSize = 24f
        paint.isFakeBoldText = true
        val peoplePart = if (memory.peopleTagged.isNotBlank()) " • With ${memory.peopleTagged}" else ""
        val footer = "${memory.formattedDate} • ${memory.locationName}$peoplePart"
        canvas.drawText(footer.take(54), 60f, 1070f, paint)

        val galleryDir = getGalleryDirectory(context)
        val outFile = File(galleryDir, "POLAROID_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { out ->
            cardBmp.compress(Bitmap.CompressFormat.JPEG, 94, out)
        }
        cardBmp.recycle()
        Triple(outFile, cardWidth, cardHeight)
    }

    suspend fun renderBentoCollageFile(
        context: Context,
        photoPaths: List<String>,
        collageTitle: String,
        layoutMode: Int, // 0 = 2-Split, 1 = 3-Bento Hero, 2 = 4-Quad Grid
        bgHex: String
    ): Triple<File, Int, Int>? = withContext(Dispatchers.IO) {
        if (photoPaths.isEmpty()) return@withContext null
        val outWidth = 1080
        val outHeight = 1200
        val bmp = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = try {
            Color.parseColor(bgHex)
        } catch (_: Exception) {
            Color.parseColor("#FDF7FA")
        }
        canvas.drawRect(0f, 0f, outWidth.toFloat(), outHeight.toFloat(), paint)

        val pad = 32f
        val gap = 20f
        val gridBottom = outHeight - 130f
        val availW = outWidth - pad * 2
        val availH = gridBottom - pad

        val rects: List<RectF> = when (layoutMode) {
            0 -> {
                // 2-Photo Vertical Split or Stack
                val halfH = (availH - gap) / 2f
                listOf(
                    RectF(pad, pad, pad + availW, pad + halfH),
                    RectF(pad, pad + halfH + gap, pad + availW, pad + availH)
                )
            }
            1 -> {
                // 3-Photo Bento Showcase (1 wide top hero + 2 bottom side-by-side)
                val topH = availH * 0.56f
                val botH = availH - topH - gap
                val halfW = (availW - gap) / 2f
                listOf(
                    RectF(pad, pad, pad + availW, pad + topH),
                    RectF(pad, pad + topH + gap, pad + halfW, pad + availH),
                    RectF(pad + halfW + gap, pad + topH + gap, pad + availW, pad + availH)
                )
            }
            else -> {
                // 4-Quad Grid
                val halfW = (availW - gap) / 2f
                val halfH = (availH - gap) / 2f
                listOf(
                    RectF(pad, pad, pad + halfW, pad + halfH),
                    RectF(pad + halfW + gap, pad, pad + availW, pad + halfH),
                    RectF(pad, pad + halfH + gap, pad + halfW, pad + availH),
                    RectF(pad + halfW + gap, pad + halfH + gap, pad + availW, pad + availH)
                )
            }
        }

        rects.forEachIndexed { idx, rect ->
            val path = photoPaths[idx % photoPaths.size]
            val src = decodeBitmapFromPathOrUri(context, path)
            if (src != null) {
                canvas.save()
                val clipPath = Path().apply {
                    addRoundRect(rect, 28f, 28f, Path.Direction.CW)
                }
                canvas.clipPath(clipPath)
                canvas.drawBitmap(src, null, rect, paint)
                canvas.restore()
                src.recycle()
            }
        }

        // Collage Footer Title & Stamp
        val isDarkBg = bgHex.startsWith("#1") || bgHex.startsWith("#0")
        paint.color = if (isDarkBg) Color.WHITE else Color.parseColor("#1E1815")
        paint.textSize = 36f
        paint.isFakeBoldText = true
        canvas.drawText(
            collageTitle.ifBlank { "Bento Archive Story" }.take(34),
            pad,
            outHeight - 54f,
            paint
        )

        val galleryDir = getGalleryDirectory(context)
        val outFile = File(galleryDir, "COLLAGE_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 94, out)
        }
        bmp.recycle()
        Triple(outFile, outWidth, outHeight)
    }

    suspend fun renderWatermarkedPhotoFile(
        context: Context,
        photo: PhotoEntity,
        watermarkText: String,
        includeDateAndExif: Boolean
    ): Triple<File, Int, Int>? = withContext(Dispatchers.IO) {
        val srcBmp = decodeBitmapFromPathOrUri(context, photo.filePath) ?: return@withContext null
        val mutableBmp = srcBmp.copy(Bitmap.Config.ARGB_8888, true)
        srcBmp.recycle()

        val canvas = Canvas(mutableBmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val w = mutableBmp.width.toFloat()
        val h = mutableBmp.height.toFloat()

        // Translucent dark badge at bottom-right
        val barH = (h * 0.11f).coerceAtLeast(64f)
        paint.color = Color.parseColor("#AA12100E")
        canvas.drawRoundRect(
            RectF(24f, h - barH - 20f, w - 24f, h - 20f),
            18f, 18f, paint
        )

        paint.color = Color.parseColor("#F59E0B")
        paint.textSize = (barH * 0.36f).coerceIn(20f, 34f)
        paint.isFakeBoldText = true
        val mainStamp = watermarkText.ifBlank { "© Simple Gallery • ${photo.title}" }
        canvas.drawText(mainStamp.take(40), 44f, h - barH * 0.52f - 16f, paint)

        if (includeDateAndExif) {
            paint.color = Color.WHITE
            paint.textSize = (barH * 0.26f).coerceIn(16f, 24f)
            paint.isFakeBoldText = false
            val subStamp = "${photo.fullTimestampText} • ${photo.cameraModel} (${photo.exifSummary})"
            canvas.drawText(subStamp.take(56), 44f, h - 34f, paint)
        }

        val galleryDir = getGalleryDirectory(context)
        val outFile = File(galleryDir, "STAMP_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { out ->
            mutableBmp.compress(Bitmap.CompressFormat.JPEG, 94, out)
        }
        val outW = mutableBmp.width
        val outH = mutableBmp.height
        mutableBmp.recycle()
        Triple(outFile, outW, outH)
    }

    suspend fun shrinkAndOptimizePhotoFile(
        context: Context,
        photo: PhotoEntity,
        scaleFactor: Float,
        jpegQuality: Int,
        replaceOriginal: Boolean
    ): Triple<File, Int, Int>? = withContext(Dispatchers.IO) {
        val srcBmp = decodeBitmapFromPathOrUri(context, photo.filePath) ?: return@withContext null
        val targetW = (srcBmp.width * scaleFactor).toInt().coerceAtLeast(320)
        val targetH = (srcBmp.height * scaleFactor).toInt().coerceAtLeast(240)
        val scaledBmp = if (targetW != srcBmp.width || targetH != srcBmp.height) {
            Bitmap.createScaledBitmap(srcBmp, targetW, targetH, true)
        } else {
            srcBmp
        }

        val galleryDir = getGalleryDirectory(context)
        val isContentUri = photo.filePath.startsWith("content://")
        val originalFile = File(photo.filePath)
        val isInsideAppStorage = !isContentUri && originalFile.absolutePath.startsWith(galleryDir.absolutePath)
        val safeBaseName = photo.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "photo" }
        val outFile = if (replaceOriginal && isInsideAppStorage) {
            originalFile
        } else {
            File(galleryDir, "SHRINK_${System.currentTimeMillis()}_${safeBaseName}.jpg")
        }

        FileOutputStream(outFile).use { out ->
            scaledBmp.compress(Bitmap.CompressFormat.JPEG, jpegQuality.coerceIn(30, 95), out)
        }

        val finalW = scaledBmp.width
        val finalH = scaledBmp.height
        if (scaledBmp != srcBmp) scaledBmp.recycle()
        srcBmp.recycle()
        Triple(outFile, finalW, finalH)
    }

    suspend fun renderFramedPhotoFile(
        context: Context,
        photo: PhotoEntity,
        frameStyleName: String,
        showExifSignature: Boolean,
        customBadgeText: String = ""
    ): Triple<File, Int, Int>? = withContext(Dispatchers.IO) {
        val srcBmp = decodeBitmapFromPathOrUri(context, photo.filePath) ?: return@withContext null
        val pad = (min(srcBmp.width, srcBmp.height) * 0.11f).coerceAtLeast(52f)
        val hasFooter = showExifSignature || customBadgeText.isNotBlank()
        val bottomExtra = if (hasFooter) pad * 1.15f else pad * 0.25f
        val outW = (srcBmp.width + pad * 2f).toInt()
        val outH = (srcBmp.height + pad * 2f + bottomExtra).toInt()

        val framedBmp = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(framedBmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val (bgHex, borderHex, textHex) = when (frameStyleName) {
            "35mm Film" -> Triple("#111111", "#D9822B", "#F5F5F5")
            "Gold Gallery" -> Triple("#181410", "#D4AF37", "#FDE68A")
            "Obsidian Mat" -> Triple("#1F1C1A", "#C84B28", "#FAF8F5")
            "Editorial Cream" -> Triple("#FAF8F5", "#C84B28", "#1F1C1A")
            else -> Triple("#FFFFFF", "#E6DFD5", "#1F1C1A") // Museum White Mat
        }

        paint.color = Color.parseColor(bgHex)
        canvas.drawRect(0f, 0f, outW.toFloat(), outH.toFloat(), paint)

        // Outer decorative frame border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.color = Color.parseColor(borderHex)
        canvas.drawRoundRect(
            RectF(pad * 0.42f, pad * 0.42f, outW - pad * 0.42f, outH - pad * 0.42f),
            16f, 16f, paint
        )
        paint.style = Paint.Style.FILL

        // 35mm film sprocket holes if 35mm Film style
        if (frameStyleName == "35mm Film") {
            paint.color = Color.parseColor("#2A2A2A")
            val holeCount = 10
            val stepY = outH.toFloat() / (holeCount + 1)
            for (i in 1..holeCount) {
                val cy = i * stepY
                canvas.drawRoundRect(RectF(14f, cy - 12f, pad * 0.72f, cy + 12f), 4f, 4f, paint)
                canvas.drawRoundRect(RectF(outW - pad * 0.72f, cy - 12f, outW - 14f, cy + 12f), 4f, 4f, paint)
            }
        }

        val photoRect = RectF(pad, pad, outW - pad, pad + srcBmp.height)
        canvas.drawBitmap(srcBmp, null, photoRect, paint)
        srcBmp.recycle()

        if (hasFooter) {
            paint.color = Color.parseColor(textHex)
            paint.textSize = (pad * 0.40f).coerceIn(22f, 38f)
            paint.isFakeBoldText = true
            val headerTitle = if (customBadgeText.isNotBlank()) {
                "${photo.title.take(26)} • $customBadgeText"
            } else {
                photo.title.take(36)
            }
            canvas.drawText(
                headerTitle,
                pad,
                pad + photoRect.height() + pad * 0.68f,
                paint
            )
            paint.isFakeBoldText = false
            paint.textSize = (pad * 0.28f).coerceIn(16f, 26f)
            paint.color = Color.parseColor(borderHex)
            val footerLine = if (showExifSignature) {
                "${photo.cameraModel} • ${photo.exifSummary} • ${photo.locationTag}"
            } else {
                "$customBadgeText • ${photo.shortDateText}"
            }
            canvas.drawText(
                footerLine.take(58),
                pad,
                pad + photoRect.height() + pad * 1.15f,
                paint
            )
        }

        val galleryDir = getGalleryDirectory(context)
        val outFile = File(galleryDir, "FRAME_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { out ->
            framedBmp.compress(Bitmap.CompressFormat.JPEG, 94, out)
        }
        framedBmp.recycle()
        Triple(outFile, outW, outH)
    }

    /**
     * Re-encodes the photograph cleanly into cache without any EXIF/GPS metadata
     * so users can strip location and camera details before sharing.
     */
    suspend fun exportExifStrippedFile(context: Context, photo: PhotoEntity): File? =
        withContext(Dispatchers.IO) {
            try {
                val bmp = decodeBitmapFromPathOrUri(context, photo.filePath) ?: return@withContext null
                val shareDir = File(context.cacheDir, "shared_clean").apply { mkdirs() }
                val cleanName = photo.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "photo" }
                val outFile = File(shareDir, "CLEAN_${System.currentTimeMillis()}_${cleanName.take(24)}.jpg")
                FileOutputStream(outFile).use { out ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 94, out)
                }
                bmp.recycle()
                if (outFile.exists() && outFile.length() > 0L) outFile else null
            } catch (_: Exception) {
                null
            }
        }
}
