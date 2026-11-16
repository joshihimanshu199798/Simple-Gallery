package com.example

import com.example.data.GrowthAgeUtils
import com.example.data.PhotoEntity
import com.example.data.formatBytes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `read app name and verify gallery utilities`() {
        assertEquals("1.50 MB", formatBytes(1_572_864L))
    }

    @Test
    fun `verify photo fileName and date formatting properties for search`() {
        val samplePhoto = PhotoEntity(
            id = 42L,
            title = "Golden Hour Cliffs",
            filePath = "/storage/emulated/0/Pictures/IMG_20261015_Sunset.jpg",
            albumName = "Travel",
            dateTaken = 1760551200000L,
            sizeBytes = 2_450_000L,
            width = 4000,
            height = 3000
        )
        assertEquals("IMG_20261015_Sunset.jpg", samplePhoto.fileName)
        assertNotNull(samplePhoto.isoDateText)
        assertNotNull(samplePhoto.shortDateText)
        assertNotNull(samplePhoto.dateHeader)
    }

    @Test
    fun `verify growth age label and chapter resolution`() {
        val cal = java.util.Calendar.getInstance().apply {
            set(2024, java.util.Calendar.JANUARY, 1, 12, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val dob = cal.timeInMillis
        val dayMs = 86_400_000L

        assertEquals("Day 1", GrowthAgeUtils.calculateAgeLabel(dob, dob + 0L))
        assertEquals("Newborn • Day 1", GrowthAgeUtils.resolveAgeChapter(dob, dob + 3 * dayMs))

        val plus3m = (cal.clone() as java.util.Calendar).apply { add(java.util.Calendar.MONTH, 3) }.timeInMillis
        assertEquals("3m", GrowthAgeUtils.calculateAgeLabel(dob, plus3m))
        assertEquals("3 Months", GrowthAgeUtils.resolveAgeChapter(dob, plus3m))

        val plus6m = (cal.clone() as java.util.Calendar).apply { add(java.util.Calendar.MONTH, 6) }.timeInMillis
        assertEquals("6m", GrowthAgeUtils.calculateAgeLabel(dob, plus6m))
        assertEquals("6 Months", GrowthAgeUtils.resolveAgeChapter(dob, plus6m))

        val plus1y = (cal.clone() as java.util.Calendar).apply { add(java.util.Calendar.YEAR, 1) }.timeInMillis
        assertEquals("1st Birthday", GrowthAgeUtils.resolveAgeChapter(dob, plus1y))

        val plus1y2m = (cal.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.YEAR, 1)
            add(java.util.Calendar.MONTH, 2)
        }.timeInMillis
        assertEquals("1y 2m", GrowthAgeUtils.calculateAgeLabel(dob, plus1y2m))
        assertEquals("1 Year+", GrowthAgeUtils.resolveAgeChapter(dob, plus1y2m))
    }

    @Test
    fun `verify smart category matching filters`() {
        val videoPhoto = PhotoEntity(
            title = "Birthday Reel",
            filePath = "/tmp/vid.mp4",
            albumName = "Family",
            tags = "Family, Portrait",
            milestoneSticker = "1st Birthday",
            isVideo = true,
            durationMs = 14_000L,
            dateTaken = System.currentTimeMillis() - 5 * 86_400_000L,
            sizeBytes = 5_000_000L,
            width = 1920,
            height = 1080
        )
        assertTrue(videoPhoto.matchesCategory("All Photos"))
        assertTrue(videoPhoto.matchesCategory("Videos"))
        assertTrue(videoPhoto.matchesCategory("1st Birthday"))
        assertTrue(videoPhoto.matchesCategory("Last Month"))
        assertFalse(videoPhoto.matchesCategory("Screenshots"))
        assertFalse(videoPhoto.matchesCategory("Receipts"))
    }
}
