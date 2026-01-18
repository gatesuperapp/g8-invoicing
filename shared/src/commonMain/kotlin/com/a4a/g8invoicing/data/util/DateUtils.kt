package com.a4a.g8invoicing.data.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * KMP date utilities using kotlinx-datetime
 */
object DateUtils {

    /**
     * Get current date formatted as dd/MM/yyyy
     */
    fun getCurrentDateFormatted(): String {
        val now = Clock.System.now()
        val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return formatDate(localDate)
    }

    /**
     * Get date N days from now formatted as dd/MM/yyyy
     */
    fun getDatePlusDaysFormatted(days: Int): String {
        val now = Clock.System.now()
        val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val futureDate = localDate.plus(days, DateTimeUnit.DAY)
        return formatDate(futureDate)
    }

    /**
     * Format LocalDate as dd/MM/yyyy
     */
    fun formatDate(date: LocalDate): String {
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = date.monthNumber.toString().padStart(2, '0')
        val year = date.year.toString()
        return "$day/$month/$year"
    }

    /**
     * Format current datetime as yyyy-MM-dd HH:mm:ss for database timestamps
     */
    fun getCurrentTimestamp(): String {
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val date = localDateTime.date
        val time = localDateTime.time

        val year = date.year.toString()
        val month = date.monthNumber.toString().padStart(2, '0')
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val hour = time.hour.toString().padStart(2, '0')
        val minute = time.minute.toString().padStart(2, '0')
        val second = time.second.toString().padStart(2, '0')

        return "$year-$month-$day $hour:$minute:$second"
    }

    /**
     * Parse date string in dd/MM/yyyy format to LocalDate
     * Returns null if parsing fails
     */
    fun parseDate(dateString: String): LocalDate? {
        return try {
            if (dateString.isBlank()) return null
            val parts = dateString.take(10).split("/")
            if (parts.size != 3) return null
            val day = parts[0].toIntOrNull() ?: return null
            val month = parts[1].toIntOrNull() ?: return null
            val year = parts[2].toIntOrNull() ?: return null
            LocalDate(year, month, day)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if date string represents a date before today
     */
    fun isDateBeforeToday(dateString: String): Boolean {
        val date = parseDate(dateString) ?: return false
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return date < today
    }

    /**
     * Get current time in milliseconds
     */
    fun currentTimeMillis(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }
}
