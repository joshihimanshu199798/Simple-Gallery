package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Entity(tableName = "little_memories")
data class MemoryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoId: Long? = null,
    val photoFilePath: String,
    val title: String,
    val story: String,
    val mood: String = "Joyful",
    val milestoneBadge: String = "Everyday Magic",
    val peopleTagged: String = "",
    val locationName: String = "Local Keepsake",
    val memoryDate: Long = System.currentTimeMillis(),
    val isTimeCapsule: Boolean = false,
    val unlockTimestamp: Long? = null,
    val isCapsuleOpened: Boolean = false,
    val frameStyle: String = "Classic Polaroid",
    val isPinnedHighlight: Boolean = false
) {
    val formattedDate: String
        get() {
            val fmt = SimpleDateFormat("MMM d, yyyy", Locale.US)
            return fmt.format(Date(memoryDate))
        }

    val dayOfMonthText: String
        get() = SimpleDateFormat("dd", Locale.US).format(Date(memoryDate))

    val monthShortText: String
        get() = SimpleDateFormat("MMM", Locale.US).format(Date(memoryDate)).uppercase(Locale.US)

    val yearText: String
        get() = SimpleDateFormat("yyyy", Locale.US).format(Date(memoryDate))

    val isCurrentlyLockedCapsule: Boolean
        get() = isTimeCapsule &&
            !isCapsuleOpened &&
            (unlockTimestamp != null && unlockTimestamp > System.currentTimeMillis())

    val capsuleCountdownText: String
        get() {
            val target = unlockTimestamp ?: return "Unlocked"
            val diff = target - System.currentTimeMillis()
            if (diff <= 0L) return "Ready to Unlock!"
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
            return if (days > 0) {
                "Unlocks in ${days}d ${hours}h"
            } else {
                val mins = (TimeUnit.MILLISECONDS.toMinutes(diff) % 60).coerceAtLeast(1)
                "Unlocks in ${hours}h ${mins}m"
            }
        }

    val peopleList: List<String>
        get() = peopleTagged.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "memory_milestones")
data class MilestoneReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "Milestone",
    val targetDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val measurementNote: String = "",
    val isCompleted: Boolean = false,
    val linkedPhotoPath: String? = null
) {
    val formattedTargetDate: String
        get() = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(targetDate))

    val relativeDaysText: String
        get() {
            val diff = targetDate - System.currentTimeMillis()
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            return when {
                isCompleted -> "Completed Milestone"
                days > 0 -> "In $days day${if (days == 1L) "" else "s"}"
                days == 0L -> "Today!"
                else -> "${-days}d ago"
            }
        }
}

enum class MemoryMood(
    val label: String,
    val emoji: String,
    val hexColor: String
) {
    JOYFUL("Joyful", "✨", "#F4A261"),
    GRATEFUL("Grateful", "🥰", "#E76F51"),
    MILESTONE("Milestone", "🎉", "#E9C46A"),
    PEACEFUL("Peaceful", "🌿", "#76B09C"),
    ADVENTURE("Adventure", "🏔️", "#2A9D8F"),
    NOSTALGIC("Nostalgic", "💛", "#C9A66B")
}

enum class PolaroidFrameStyle(
    val label: String,
    val borderHex: String,
    val textHex: String,
    val accentHex: String
) {
    CLASSIC_POLAROID("Classic Polaroid", "#FAF6F0", "#1F1A16", "#9C4A1A"),
    WARM_VINTAGE("Warm Vintage", "#F3E5D0", "#2C1D11", "#B5542D"),
    MIDNIGHT_GOLD("Midnight Gold", "#1E1A17", "#F6F1EB", "#F4A261"),
    BOTANICAL_PRESS("Botanical Press", "#E8F0EA", "#132A1E", "#2D6654")
}
