package com.a4a.g8invoicing.ui.screens

import com.a4a.g8invoicing.data.formatAmount
import com.a4a.g8invoicing.ui.states.DashboardClientStatement
import com.a4a.g8invoicing.ui.states.DashboardStatementRow
import com.a4a.g8invoicing.ui.states.DashboardStatementsUiState

/**
 * Flatten the statements tab's accordion into a single CSV, one row per
 * document. Semicolon separator for Excel-FR friendliness; UTF-8 BOM
 * prefix so Excel on Windows picks up the encoding without the user
 * having to "Import" rather than "Open".
 *
 * Columns: Client, Type, Numéro, Date, Montant, Statut.
 */
internal fun buildStatementsCsv(
    state: DashboardStatementsUiState,
    currencyCode: String,
    formatLocale: String?,
): String {
    val sb = StringBuilder()
    sb.append("﻿") // UTF-8 BOM so Excel-Windows auto-detects the encoding.
    sb.append(csvRow("Client", "Email", "Type", "Numéro", "Date", "Montant", "Statut"))
    sb.append('\n')

    state.clients.forEach { client ->
        appendRows(sb, client, client.invoices, "Facture", currencyCode, formatLocale)
        appendRows(sb, client, client.creditNotes, "Avoir", currencyCode, formatLocale)
        appendRows(sb, client, client.deliveryNotes, "Bon de livraison", currencyCode, formatLocale)
        appendRows(sb, client, client.salesOrders, "Bon de commande", currencyCode, formatLocale)
        appendRows(sb, client, client.quotes, "Devis", currencyCode, formatLocale)
    }
    return sb.toString()
}

private fun appendRows(
    sb: StringBuilder,
    client: DashboardClientStatement,
    rows: List<DashboardStatementRow>,
    typeLabel: String,
    currencyCode: String,
    formatLocale: String?,
) {
    rows.forEach { row ->
        sb.append(
            csvRow(
                client.displayName,
                client.email.orEmpty(),
                typeLabel,
                row.number,
                row.date,
                formatAmount(row.totalAmount, currencyCode, formatLocale),
                frenchStatus(row.tagName),
            )
        )
        sb.append('\n')
    }
}

private fun frenchStatus(tag: String?): String = when (tag) {
    "paid" -> "Payée"
    "sent" -> "Envoyée"
    "late" -> "En retard"
    "reminded" -> "Relancée"
    "cancelled" -> "Annulée"
    "draft" -> "Brouillon"
    "invoiced" -> "Facturée"
    "locked" -> "Verrouillée"
    else -> ""
}

/**
 * RFC-4180-ish escaping tuned for FR Excel: `;` separator, fields that
 * contain `;`, `"`, newline or carriage return are wrapped in quotes,
 * inner quotes doubled. Null/blank -> empty cell.
 */
private fun csvRow(vararg cells: String): String =
    cells.joinToString(";") { escape(it) }

private fun escape(s: String): String {
    if (s.isEmpty()) return ""
    val needsQuote = s.any { it == ';' || it == '"' || it == '\n' || it == '\r' }
    return if (!needsQuote) s else "\"" + s.replace("\"", "\"\"") + "\""
}
