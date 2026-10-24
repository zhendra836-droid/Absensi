package com.example

import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testFormatDuration() {
        assertEquals("0 Menit", DateUtils.formatDuration(0))
        assertEquals("45 Menit", DateUtils.formatDuration(45))
        assertEquals("2 Jam", DateUtils.formatDuration(120))
        assertEquals("8 Jam 30 Menit", DateUtils.formatDuration(510))
    }

    @Test
    fun testOvertimeCalculation() {
        // Create timestamp for 19:00 on any date
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 19)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Work end is 17:00, so overtime is 120 minutes
        val overtime = DateUtils.calculateOvertimeMinutes(
            checkOutTimestamp = cal.timeInMillis,
            workEndTimeStr = "17:00",
            minOvertimeThresholdMinutes = 30
        )
        assertEquals(120, overtime)

        // Work end is 17:00, checkout at 17:15 (under 30 mins threshold -> 0)
        cal.set(Calendar.HOUR_OF_DAY, 17)
        cal.set(Calendar.MINUTE, 15)
        val underThreshold = DateUtils.calculateOvertimeMinutes(
            checkOutTimestamp = cal.timeInMillis,
            workEndTimeStr = "17:00",
            minOvertimeThresholdMinutes = 30
        )
        assertEquals(0, underThreshold)
    }

    @Test
    fun testLateMinutesCalculation() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 8)
        cal.set(Calendar.MINUTE, 25)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Start time is 08:00, check-in at 08:25 -> 25 mins late
        val lateMins = DateUtils.calculateLateMinutes(cal.timeInMillis, "08:00")
        assertEquals(25, lateMins)

        // Check-in at 07:55 -> 0 mins late
        cal.set(Calendar.HOUR_OF_DAY, 7)
        cal.set(Calendar.MINUTE, 55)
        val onTimeMins = DateUtils.calculateLateMinutes(cal.timeInMillis, "08:00")
        assertEquals(0, onTimeMins)
    }
}

