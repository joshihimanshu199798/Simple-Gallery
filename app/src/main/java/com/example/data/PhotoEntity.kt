package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val albumName: String,
    val dateTaken: Long,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val mimeType: String = "image/jpeg",
    val cameraModel: String = "Pixel 9 Pro",
    val aperture: String = "f/1.7",
    val shutterSpeed: String = "1/500s",
    val iso: String = "ISO 100",
    val focalLength: String = "24mm",
    val locationTag: String = "Local Device",
    val tags: String = "Photography",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val isVaultLocked: Boolean = false,
    val isTrashed: Boolean = false,
    val trashedAt: Long? = null,
    val appliedFilter: String = "Original",
    val dominantHex: String = "#F4A261",
    val milestoneSticker: String = "",
    val isVideo: Boolean = false,
    val durationMs: Long = 0L
) {
    @delegate:androidx.room.Ignore
    val formattedSize: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        formatBytes(sizeBytes)
    }

    @delegate:androidx.room.Ignore
    val megapixels: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val mp = (width.toLong() * height.toLong()) / 1_000_000.0
        if (mp >= 0.1) String.format(Locale.US, "%.1f MP", mp) else "${width}×${height}"
    }

    @delegate:androidx.room.Ignore
    val resolutionText: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        "$width × $height"
    }

    @delegate:androidx.room.Ignore
    val exifSummary: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        "$aperture • $shutterSpeed • $iso • $focalLength"
    }

    @delegate:androidx.room.Ignore
    val tagList: List<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        if (tags.isBlank()) emptyList()
        else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    @delegate:androidx.room.Ignore
    val dateHeader: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        DATE_HEADER_FORMAT.get()!!.format(Date(dateTaken))
    }

    @delegate:androidx.room.Ignore
    val shortDateText: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SHORT_DATE_FORMAT.get()!!.format(Date(dateTaken))
    }

    @delegate:androidx.room.Ignore
    val fullTimestampText: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FULL_TIMESTAMP_FORMAT.get()!!.format(Date(dateTaken))
    }

    @delegate:androidx.room.Ignore
    val fileName: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        filePath.substringAfterLast('/')
    }

    @delegate:androidx.room.Ignore
    val isoDateText: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ISO_DATE_FORMAT.get()!!.format(Date(dateTaken))
    }

    @delegate:androidx.room.Ignore
    val monthYearText: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        MONTH_YEAR_FORMAT.get()!!.format(Date(dateTaken))
    }

    @delegate:androidx.room.Ignore
    val shortMonthYearText: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SHORT_MONTH_YEAR_FORMAT.get()!!.format(Date(dateTaken))
    }

    @delegate:androidx.room.Ignore
    val formattedDuration: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val totalSec = (durationMs / 1000L).coerceAtLeast(1L)
        val mins = totalSec / 60L
        val secs = totalSec % 60L
        String.format(Locale.US, "%d:%02d", mins, secs)
    }

    fun relativeDateGroup(nowMs: Long = System.currentTimeMillis()): String {
        val calNow = NOW_CALENDAR.get()!!.apply { timeInMillis = nowMs }
        val calPhoto = PHOTO_CALENDAR.get()!!.apply { timeInMillis = dateTaken }

        val sameYear = calNow.get(java.util.Calendar.YEAR) == calPhoto.get(java.util.Calendar.YEAR)
        val dayDiff = if (sameYear) {
            calNow.get(java.util.Calendar.DAY_OF_YEAR) - calPhoto.get(java.util.Calendar.DAY_OF_YEAR)
        } else {
            ((nowMs - dateTaken) / 86_400_000L).toInt()
        }

        return when {
            dayDiff <= 0 && sameYear -> "Today"
            dayDiff == 1 && sameYear -> "Yesterday"
            dayDiff in 2..6 -> "This Week"
            sameYear && calNow.get(java.util.Calendar.MONTH) == calPhoto.get(java.util.Calendar.MONTH) -> "Earlier This Month"
            else -> monthYearText
        }
    }

    companion object {
        private val DATE_HEADER_FORMAT = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue() = SimpleDateFormat("EEE, MMM d, yyyy", Locale.US)
        }
        private val SHORT_DATE_FORMAT = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue() = SimpleDateFormat("MMM d", Locale.US)
        }
        private val FULL_TIMESTAMP_FORMAT = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue() = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
        }
        private val ISO_DATE_FORMAT = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue() = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        }
        private val MONTH_YEAR_FORMAT = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue() = SimpleDateFormat("MMMM yyyy", Locale.US)
        }
        private val SHORT_MONTH_YEAR_FORMAT = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue() = SimpleDateFormat("MMM yyyy", Locale.US)
        }
        private val NOW_CALENDAR = object : ThreadLocal<java.util.Calendar>() {
            override fun initialValue(): java.util.Calendar = java.util.Calendar.getInstance()
        }
        private val PHOTO_CALENDAR = object : ThreadLocal<java.util.Calendar>() {
            override fun initialValue(): java.util.Calendar = java.util.Calendar.getInstance()
        }
    }

    fun isScreenshot(): Boolean {
        return albumName.contains("Screen", ignoreCase = true) ||
            title.contains("Screenshot", ignoreCase = true) ||
            fileName.contains("SCREEN", ignoreCase = true) ||
            filePath.contains("/Screenshots/", ignoreCase = true) ||
            tagList.any { it.contains("Screenshot", ignoreCase = true) }
    }

    fun matchesCategory(category: String): Boolean {
        val clean = category.trim()
        if (clean.equals("All", ignoreCase = true) || clean.equals("All Photos", ignoreCase = true)) return true
        if (clean.equals("Favorites", ignoreCase = true)) return isFavorite
        if (clean.equals("Edited", ignoreCase = true)) {
            return appliedFilter != "Original" ||
                albumName.equals("Edited Studio", ignoreCase = true) ||
                tagList.any {
                    it.equals("Edited", ignoreCase = true) ||
                        it.equals("Cropped", ignoreCase = true) ||
                        it.equals("Framed", ignoreCase = true) ||
                        it.equals("Watermarked", ignoreCase = true)
                }
        }
        if (albumName.equals(clean, ignoreCase = true)) return true
        if (tagList.any { it.equals(clean, ignoreCase = true) }) return true

        // Smart semantic category matching
        return when (clean.lowercase(Locale.US)) {
            "camera" -> !isScreenshot() && !isVideo && (
                albumName.equals("Camera", ignoreCase = true) ||
                    albumName.equals("Camera Roll", ignoreCase = true) ||
                    albumName.contains("DCIM", ignoreCase = true) ||
                    fileName.startsWith("PXL_", ignoreCase = true) ||
                    fileName.startsWith("IMG_", ignoreCase = true) ||
                    fileName.startsWith("DSC", ignoreCase = true) ||
                    filePath.contains("/DCIM/", ignoreCase = true) ||
                    filePath.contains("/Camera/", ignoreCase = true) ||
                    tagList.any { it.equals("Camera", ignoreCase = true) || it.equals("DCIM", ignoreCase = true) }
            )
            "screenshots", "screenshot" -> isScreenshot()
            "videos", "video" -> isVideo ||
                mimeType.startsWith("video/", ignoreCase = true) ||
                albumName.contains("Video", ignoreCase = true) ||
                tagList.any { it.contains("Video", ignoreCase = true) || it.contains("Clip", ignoreCase = true) || it.contains("Reel", ignoreCase = true) }
            "receipts", "receipt" -> albumName.contains("Receipt", ignoreCase = true) ||
                albumName.contains("Document", ignoreCase = true) ||
                title.contains("Receipt", ignoreCase = true) ||
                title.contains("Invoice", ignoreCase = true) ||
                tagList.any { it.contains("Receipt", ignoreCase = true) || it.contains("Document", ignoreCase = true) || it.contains("Scan", ignoreCase = true) }
            "selfies", "selfie" -> tagList.any { it.contains("Selfie", ignoreCase = true) || it.contains("Portrait", ignoreCase = true) } ||
                title.contains("Selfie", ignoreCase = true) ||
                focalLength.contains("Front", ignoreCase = true)
            "1st birthday" -> milestoneSticker.equals("1st Birthday", ignoreCase = true) ||
                title.contains("Birthday", ignoreCase = true) ||
                tagList.any { it.contains("Birthday", ignoreCase = true) }
            "last month" -> {
                val thirtyDaysAgo = System.currentTimeMillis() - 35L * 86_400_000L
                dateTaken >= thirtyDaysAgo
            }
            "nature" -> albumName.contains("Nature", ignoreCase = true) ||
                albumName.contains("Landscape", ignoreCase = true) ||
                tagList.any {
                    it.contains("Nature", ignoreCase = true) ||
                        it.contains("Landscape", ignoreCase = true) ||
                        it.contains("Forest", ignoreCase = true) ||
                        it.contains("Botanical", ignoreCase = true) ||
                        it.contains("Macro", ignoreCase = true) ||
                        it.contains("Mountains", ignoreCase = true)
                }
            "travel" -> albumName.contains("Travel", ignoreCase = true) ||
                albumName.contains("Landscape", ignoreCase = true) ||
                tagList.any {
                    it.contains("Travel", ignoreCase = true) ||
                        it.contains("Ocean", ignoreCase = true) ||
                        it.contains("Coastal", ignoreCase = true) ||
                        it.contains("Aerial", ignoreCase = true)
                }
            "family", "portraits" -> albumName.contains("Family", ignoreCase = true) ||
                albumName.contains("Memories", ignoreCase = true) ||
                albumName.contains("Portrait", ignoreCase = true) ||
                tagList.any {
                    it.contains("Family", ignoreCase = true) ||
                        it.contains("Portrait", ignoreCase = true) ||
                        it.contains("Park", ignoreCase = true) ||
                        it.contains("People", ignoreCase = true)
                }
            "architecture" -> albumName.contains("Architecture", ignoreCase = true) ||
                tagList.any {
                    it.contains("Architecture", ignoreCase = true) ||
                        it.contains("Minimal", ignoreCase = true) ||
                        it.contains("Symmetry", ignoreCase = true) ||
                        it.contains("Spiral", ignoreCase = true)
                }
            "night", "street", "night & street" -> albumName.contains("Street", ignoreCase = true) ||
                albumName.contains("Night", ignoreCase = true) ||
                tagList.any {
                    it.contains("Night", ignoreCase = true) ||
                        it.contains("Street", ignoreCase = true) ||
                        it.contains("Cyberpunk", ignoreCase = true) ||
                        it.contains("Bokeh", ignoreCase = true)
                }
            else -> albumName.contains(clean, ignoreCase = true) ||
                tags.contains(clean, ignoreCase = true) ||
                milestoneSticker.contains(clean, ignoreCase = true)
        }
    }
}

@Entity(tableName = "custom_albums")
data class CustomAlbumEntity(
    @PrimaryKey val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val accentHex: String = "#F4A261"
)

data class AlbumSummary(
    val name: String,
    val description: String,
    val photoCount: Int,
    val coverFilePath: String?,
    val totalBytes: Long,
    val latestTimestamp: Long
) {
    val formattedTotalSize: String
        get() = formatBytes(totalBytes)
}

enum class SortOption(val label: String) {
    DATE_DESC("Date: Newest First"),
    DATE_ASC("Date: Oldest First"),
    NAME_ASC("Name (A–Z)"),
    NAME_DESC("Name (Z–A)"),
    SIZE_DESC("Size: Largest First"),
    SIZE_ASC("Size: Smallest First"),
    RESOLUTION_DESC("Highest Resolution")
}

enum class AlbumSortOption(val label: String) {
    DATE_DESC("Date: Newest First"),
    DATE_ASC("Date: Oldest First"),
    NAME_ASC("Name (A–Z)"),
    NAME_DESC("Name (Z–A)"),
    SIZE_DESC("Size: Largest Total Size"),
    SIZE_ASC("Size: Smallest Total Size"),
    COUNT_DESC("Size: Most Photos")
}

enum class GridDensity(val columns: Int, val label: String) {
    TWO(2, "2 Cols"),
    THREE(3, "3 Cols"),
    FOUR(4, "4 Cols"),
    FIVE(5, "5 Cols"),
    LIST(1, "List")
}

enum class PhotoFilterPreset(
    val displayName: String,
    val subtitle: String
) {
    ORIGINAL("Original", "Raw color balance"),
    WARM_AMBER("Golden Hour", "Warm amber & sunlit tones"),
    CLASSIC_BW("Silver Halide", "High-dynamic B&W monochrome"),
    KODAK_SEPIA("Archival Sepia", "Warm vintage darkroom print"),
    CYBER_CYAN("Nordic Slate", "Cool cyan shadows & crisp highlights"),
    HIGH_CONTRAST("Street Punch", "Deep blacks & vivid saturation"),
    FADE_MATTE("Editorial Matte", "Lifted shadows & soft film grain feel"),
    EMERALD("Botanical", "Rich foliage & natural greens")
}

enum class CropAspectRatio(val label: String, val ratio: Float?) {
    ORIGINAL("Freeform", null),
    SQUARE("1:1 Square", 1f),
    PORTRAIT_4_5("4:5 Portrait", 4f / 5f),
    LANDSCAPE_4_3("4:3 Classic", 4f / 3f),
    CINEMA_16_9("16:9 Cinema", 16f / 9f),
    PORTRAIT_3_4("3:4 Vertical", 3f / 4f),
    STORY_9_16("9:16 Story", 9f / 16f)
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val kb = bytes / 1024.0
    return if (kb < 1024.0) {
        "${kb.roundToInt().coerceAtLeast(1)} KB"
    } else {
        String.format(Locale.US, "%.2f MB", kb / 1024.0)
    }
}
