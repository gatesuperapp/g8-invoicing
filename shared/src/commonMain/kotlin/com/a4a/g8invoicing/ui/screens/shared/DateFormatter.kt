package com.a4a.g8invoicing.ui.screens.shared

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

// Date formatting utilities for KMP
// TODO: Use kotlinx-datetime consistently across the app

fun formatDate(millis: Long, pattern: String = "dd/MM/yyyy HH:mm:ss"): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    return buildString {
        append(localDateTime.dayOfMonth.toString().padStart(2, '0'))
        append("/")
        append(localDateTime.monthNumber.toString().padStart(2, '0'))
        append("/")
        append(localDateTime.year)
        if (pattern.contains("HH")) {
            append(" ")
            append(localDateTime.hour.toString().padStart(2, '0'))
            append(":")
            append(localDateTime.minute.toString().padStart(2, '0'))
            append(":")
            append(localDateTime.second.toString().padStart(2, '0'))
        }
    }
}

fun parseDate(dateString: String, pattern: String = "dd/MM/yyyy HH:mm:ss"): Long? {
    return try {
        val datePart = dateString.split(" ")[0]

        // Detect format: yyyy-MM-dd or dd/MM/yyyy
        val (year, month, day) = if (datePart.contains("-")) {
            // yyyy-MM-dd format
            val parts = datePart.split("-")
            if (parts.size < 3) return null
            Triple(
                parts[0].toIntOrNull() ?: return null,
                parts[1].toIntOrNull() ?: return null,
                parts[2].toIntOrNull() ?: return null
            )
        } else {
            // dd/MM/yyyy format
            val parts = datePart.split("/")
            if (parts.size < 3) return null
            Triple(
                parts[2].toIntOrNull() ?: return null,
                parts[1].toIntOrNull() ?: return null,
                parts[0].toIntOrNull() ?: return null
            )
        }

        var hour = 0
        var minute = 0
        var second = 0

        if (dateString.contains(" ") && dateString.split(" ").size > 1) {
            val timeParts = dateString.split(" ")[1].split(":")
            hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
            minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            second = timeParts.getOrNull(2)?.toIntOrNull() ?: 0
        }

        val localDateTime = LocalDateTime(year, month, day, hour, minute, second)
        val instant = localDateTime.toInstant(TimeZone.currentSystemDefault())
        instant.toEpochMilliseconds()
    } catch (e: Exception) {
        null
    }
}

fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
