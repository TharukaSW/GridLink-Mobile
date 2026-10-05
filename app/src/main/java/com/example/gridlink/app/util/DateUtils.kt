package com.example.gridlink.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    // Includes the device UTC offset (e.g. +05:30) so the API converts to UTC correctly
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    private val displayFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.US)
    private val shortDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeOnlyFormat = SimpleDateFormat("hh:mm a", Locale.US)

    private val zoneSuffix = Regex("(Z|[+-]\\d{2}:?\\d{2})$")

    private fun parseServerDate(dateStr: String): Date? {
        val cleanStr = dateStr.trim().replace(Regex("\\.\\d+"), "")
        return if (zoneSuffix.containsMatchIn(cleanStr)) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(cleanStr)
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(cleanStr)
        }
    }

    fun formatDisplayDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "N/A"
        return try {
            val date = parseServerDate(dateStr)
            date?.let { displayFormat.format(it) } ?: dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatDisplayDate(calendar: Calendar): String {
        return displayFormat.format(calendar.time)
    }

    fun toIsoString(calendar: Calendar): String {
        return isoFormat.format(calendar.time)
    }

    /**
     * Checks if the scheduled booking is within the next 7 days (as per assignment specification).
     */
    fun isWithin7Days(bookingCalendar: Calendar): Boolean {
        val now = Calendar.getInstance()
        val maxFuture = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
        }
        return bookingCalendar.after(now) && bookingCalendar.before(maxFuture)
    }

    /**
     * Checks if at least 12 hours notice remains before the slot time.
     * Required for updates and cancellations as per assignment specification.
     */
    fun has12HoursNotice(slotIsoString: String?): Boolean {
        if (slotIsoString.isNullOrBlank()) return false
        return try {
            val slotDate = parseServerDate(slotIsoString) ?: return false
            val nowMillis = System.currentTimeMillis()
            val slotMillis = slotDate.time
            val diffHours = (slotMillis - nowMillis) / (1000.0 * 60 * 60)
            diffHours >= 12.0
        } catch (e: Exception) {
            false
        }
    }
}
