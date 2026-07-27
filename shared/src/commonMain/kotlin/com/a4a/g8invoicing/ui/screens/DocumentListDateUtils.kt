package com.a4a.g8invoicing.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

// Small helpers for the document-list row layout: day-countdown pill, hourglass
// icon variant, month grouping header. Kept out of DocumentListItem so the row
// composable stays focused on layout and the maths + labels can be reused in
// the sticky-header sort inside DocumentListContent.

private val monthsFr = arrayOf(
    "JANVIER", "FÉVRIER", "MARS", "AVRIL", "MAI", "JUIN",
    "JUILLET", "AOÛT", "SEPTEMBRE", "OCTOBRE", "NOVEMBRE", "DÉCEMBRE",
)

private fun parseDateOnly(dateString: String): LocalDate? {
    val datePart = dateString.split(" ").firstOrNull() ?: return null
    return try {
        if (datePart.contains("-")) {
            // yyyy-MM-dd
            val parts = datePart.split("-")
            LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        } else {
            // dd/MM/yyyy
            val parts = datePart.split("/")
            LocalDate(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
        }
    } catch (_: Exception) {
        null
    }
}

private fun today(): LocalDate =
    kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

// "dd/MM/yyyy" or "yyyy-MM-dd" → "dd/MM" (drops the year).
fun dateWithoutYear(dateString: String): String {
    val parsed = parseDateOnly(dateString) ?: return dateString.split(" ").first()
    val d = parsed.dayOfMonth.toString().padStart(2, '0')
    val m = parsed.monthNumber.toString().padStart(2, '0')
    return "$d/$m"
}

// Days between today and `dueDateString`. Positive = future, 0 = today,
// negative = past. Null when the date can't be parsed.
fun daysUntilDueDate(dueDateString: String?): Int? {
    val due = dueDateString?.let(::parseDateOnly) ?: return null
    return today().daysUntil(due)
}

// "J-30" for 30 days out, "J-0" for today, "J+3" for 3 days late.
fun formatDayCountdown(days: Int): String =
    if (days >= 0) "J-$days" else "J+${-days}"

// Sand-at-top when there's still plenty of time; empty (transitioning) inside
// the last 15 days; sand-at-bottom once the due date has passed.
fun hourglassFor(days: Int): ImageVector = when {
    days > 15 -> Icons.Outlined.HourglassTop
    days >= 0 -> Icons.Outlined.HourglassEmpty
    else -> Icons.Outlined.HourglassBottom
}

// Sort key for grouping by (year, month) descending: "2026-03" > "2026-02".
fun monthKey(dateString: String): String {
    val d = parseDateOnly(dateString) ?: return "0000-00"
    return "${d.year}-${d.monthNumber.toString().padStart(2, '0')}"
}

// Localised uppercase month + year, e.g. "MARS 2026". FR-only for now — mirror
// arrays for EN/DE/ES land here when we add proper i18n.
fun monthLabel(monthKey: String): String {
    val parts = monthKey.split("-")
    if (parts.size < 2) return monthKey
    val monthIdx = (parts[1].toIntOrNull() ?: return monthKey) - 1
    if (monthIdx !in monthsFr.indices) return monthKey
    return "${monthsFr[monthIdx]} ${parts[0]}"
}
