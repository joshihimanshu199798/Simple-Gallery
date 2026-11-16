package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import kotlin.math.max

@Entity(tableName = "person_clusters")
data class PersonClusterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val avatarPhotoPath: String = "",
    val seedColorHex: String = "#C84B28",
    val isHidden: Boolean = false,
    val isPet: Boolean = false,
    val mergedIntoPersonId: Long? = null,
    val hasGrowthTimeline: Boolean = false,
    val childDisplayName: String = "",
    val childDobMs: Long? = null
) {
    val displayTitle: String
        get() = when {
            childDisplayName.isNotBlank() && hasGrowthTimeline -> childDisplayName
            name.isNotBlank() -> name
            isPet -> "Pet"
            else -> "Add name"
        }

    val isNamed: Boolean
        get() = name.isNotBlank() || childDisplayName.isNotBlank()
}

@Entity(tableName = "photo_face_tags")
data class PhotoFaceTagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoId: Long,
    val personId: Long,
    val leftFrac: Float = 0.28f,
    val topFrac: Float = 0.18f,
    val rightFrac: Float = 0.72f,
    val bottomFrac: Float = 0.62f
)

object GrowthAgeUtils {
    val AGE_CHAPTER_ORDER: List<String> = listOf(
        "Newborn • Day 1",
        "3 Months",
        "6 Months",
        "1st Birthday",
        "1 Year+"
    )

    val MILESTONE_STICKER_OPTIONS: List<String> = listOf(
        "First Smile",
        "First Steps",
        "1st Birthday"
    )

    fun calculateAgeLabel(dobMs: Long, photoDateMs: Long): String {
        if (photoDateMs <= dobMs) return "Day 1"
        val dobCal = Calendar.getInstance().apply { timeInMillis = dobMs }
        val photoCal = Calendar.getInstance().apply { timeInMillis = photoDateMs }

        var years = photoCal.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR)
        var months = photoCal.get(Calendar.MONTH) - dobCal.get(Calendar.MONTH)
        var days = photoCal.get(Calendar.DAY_OF_MONTH) - dobCal.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months -= 1
            val prevMonth = (photoCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (months < 0) {
            years -= 1
            months += 12
        }
        years = max(0, years)
        months = max(0, months)
        days = max(0, days)

        return when {
            years == 0 && months == 0 && days <= 3 -> "Day 1"
            years == 0 && months == 0 -> "${days}d"
            years == 0 -> if (days > 0) "${months}m ${days}d" else "${months}m"
            months == 0 -> "${years}y"
            else -> "${years}y ${months}m"
        }
    }

    fun calculateHeaderAge(dobMs: Long, nowMs: Long = System.currentTimeMillis()): String {
        if (nowMs <= dobMs) return "Newborn • Day 1"
        val dobCal = Calendar.getInstance().apply { timeInMillis = dobMs }
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMs }

        var years = nowCal.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR)
        var months = nowCal.get(Calendar.MONTH) - dobCal.get(Calendar.MONTH)
        var days = nowCal.get(Calendar.DAY_OF_MONTH) - dobCal.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months -= 1
            val prevMonth = (nowCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (months < 0) {
            years -= 1
            months += 12
        }
        years = max(0, years)
        months = max(0, months)
        days = max(0, days)

        return when {
            years == 0 && months == 0 -> "$days Day${if (days == 1) "" else "s"} Old"
            years == 0 -> "$months Month${if (months == 1) "" else "s"} Old"
            months == 0 -> "$years Year${if (years == 1) "" else "s"} Old"
            else -> "$years Year${if (years == 1) "" else "s"}, $months Month${if (months == 1) "" else "s"} Old"
        }
    }

    fun resolveAgeChapter(dobMs: Long, photoDateMs: Long): String {
        val diffDays = ((photoDateMs - dobMs) / 86_400_000L).coerceAtLeast(0L)
        return when {
            diffDays < 60L -> "Newborn • Day 1"
            diffDays < 150L -> "3 Months"
            diffDays < 330L -> "6 Months"
            diffDays <= 410L -> "1st Birthday"
            else -> "1 Year+"
        }
    }
}
