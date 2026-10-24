package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val localeId = Locale.forLanguageTag("id-ID")

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", localeId)
    val shortDisplayDateFormat = SimpleDateFormat("dd MMM yyyy", localeId)
    val timeFormat = SimpleDateFormat("HH:mm:ss", localeId)
    val shortTimeFormat = SimpleDateFormat("HH:mm", localeId)
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", localeId)
    val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    fun getTodayDateString(): String {
        return dateFormat.format(Date())
    }

    fun getCurrentMonthKey(): String {
        return monthKeyFormat.format(Date())
    }

    fun formatDisplayDate(date: Date = Date()): String {
        return displayDateFormat.format(date)
    }

    fun formatShortDate(timestamp: Long): String {
        return shortDisplayDateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) return "--:--"
        return shortTimeFormat.format(Date(timestamp))
    }

    fun formatTimeWithSeconds(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatDuration(minutes: Int): String {
        if (minutes <= 0) return "0 Menit"
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 && mins > 0 -> "$hours Jam $mins Menit"
            hours > 0 -> "$hours Jam"
            else -> "$mins Menit"
        }
    }

    fun formatCurrencyIdr(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(localeId)
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    /**
     * Calculates if check-in is late compared to workStartTime (e.g. "08:00")
     * Returns minutes late (0 if on time or early)
     */
    fun calculateLateMinutes(checkInTimestamp: Long, workStartTimeStr: String): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = checkInTimestamp

        val parts = workStartTimeStr.split(":")
        val startHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val startMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val scheduledCal = Calendar.getInstance()
        scheduledCal.timeInMillis = checkInTimestamp
        scheduledCal.set(Calendar.HOUR_OF_DAY, startHour)
        scheduledCal.set(Calendar.MINUTE, startMinute)
        scheduledCal.set(Calendar.SECOND, 0)
        scheduledCal.set(Calendar.MILLISECOND, 0)

        val diffMillis = checkInTimestamp - scheduledCal.timeInMillis
        return if (diffMillis > 0) {
            (diffMillis / (60 * 1000)).toInt()
        } else {
            0
        }
    }

    /**
     * Calculates overtime minutes if check-out is after workEndTime (e.g. "17:00")
     * plus overtime minimum threshold (e.g. 30 mins)
     */
    fun calculateOvertimeMinutes(
        checkOutTimestamp: Long,
        workEndTimeStr: String,
        minOvertimeThresholdMinutes: Int = 30
    ): Int {
        val parts = workEndTimeStr.split(":")
        val endHour = parts.getOrNull(0)?.toIntOrNull() ?: 17
        val endMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val scheduledEndCal = Calendar.getInstance()
        scheduledEndCal.timeInMillis = checkOutTimestamp
        scheduledEndCal.set(Calendar.HOUR_OF_DAY, endHour)
        scheduledEndCal.set(Calendar.MINUTE, endMinute)
        scheduledEndCal.set(Calendar.SECOND, 0)
        scheduledEndCal.set(Calendar.MILLISECOND, 0)

        val diffMillis = checkOutTimestamp - scheduledEndCal.timeInMillis
        if (diffMillis <= 0) return 0

        val totalPastMinutes = (diffMillis / (60 * 1000)).toInt()
        return if (totalPastMinutes >= minOvertimeThresholdMinutes) {
            totalPastMinutes
        } else {
            0
        }
    }
}
