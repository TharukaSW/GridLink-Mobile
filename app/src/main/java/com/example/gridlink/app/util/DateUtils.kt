package com.example.gridlink.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    private val displayFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.US)
    private val shortDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeOnlyFormat = SimpleDateFormat("hh:mm a", Locale.US)

    fun formatDisplayDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "N/A"
        return try {
            val cleanStr = if (dateStr.contains(".")) {
                dateStr.substring(0, dateStr.indexOf("."))
            } else {
                dateStr.replace("Z", "")
            }
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(cleanStr)
            date?.let { displayFormat.format(it) } ?: dateStr
        } catch (e: Exception) {
            dateStr
        }
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
            val cleanStr = if (slotIsoString.contains(".")) {
                slotIsoString.substring(0, slotIsoString.indexOf("."))
            } else {
                slotIsoString.replace("Z", "")
            }
            val slotDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(cleanStr) ?: return false
            val nowMillis = System.currentTimeMillis()
            val slotMillis = slotDate.time
            val diffHours = (slotMillis - nowMillis) / (1000.0 * 60 * 60)
            diffHours >= 12.0
        } catch (e: Exception) {
            false
        }
    }
}
