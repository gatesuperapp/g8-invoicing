package com.a4a.g8invoicing.ui.screens

import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_list_due_in_day
import com.a4a.g8invoicing.shared.resources.document_list_due_in_days
import com.a4a.g8invoicing.shared.resources.document_list_overdue_day
import com.a4a.g8invoicing.shared.resources.document_list_overdue_days
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource

// Small helpers for the document-list row layout: day-countdown text, month
// grouping header. Kept out of DocumentListItem so the row composable stays
// focused on layout and the maths + labels can be reused in the month sort
// inside DocumentListContent.

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

// Picks the singular / plural, future / overdue variant of the countdown
// string. Caller passes the absolute day count to the resource formatter.
fun countdownStringFor(days: Int): StringResource = when {
    days < 0 && days == -1 -> Res.string.document_list_overdue_day
    days < 0 -> Res.string.document_list_overdue_days
    days == 1 -> Res.string.document_list_due_in_day
    else -> Res.string.document_list_due_in_days
}

// Sort key for grouping by (year, month) descending: "2026-03" > "2026-02".
fun monthKey(dateString: String): String {
    val d = parseDateOnly(dateString) ?: return "0000-00"
    return "${d.year}-${d.monthNumber.toString().padStart(2, '0')}"
}

// Localised uppercase month + year, e.g. "MARS 2026". FR-only for now —
// mirror arrays for EN/DE/ES land here when we add proper i18n.
fun monthLabel(monthKey: String): String {
    val parts = monthKey.split("-")
    if (parts.size < 2) return monthKey
    val monthIdx = (parts[1].toIntOrNull() ?: return monthKey) - 1
    if (monthIdx !in monthsFr.indices) return monthKey
    return "${monthsFr[monthIdx]} ${parts[0]}"
}
