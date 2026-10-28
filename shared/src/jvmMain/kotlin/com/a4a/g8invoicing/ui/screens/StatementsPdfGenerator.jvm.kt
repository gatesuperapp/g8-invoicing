package com.a4a.g8invoicing.ui.screens

import com.a4a.g8invoicing.data.formatAmount
import com.a4a.g8invoicing.ui.states.DashboardClientStatement
import com.a4a.g8invoicing.ui.states.DashboardStatementRow
import com.a4a.g8invoicing.ui.states.DashboardStatementsUiState
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.ByteArrayOutputStream

actual fun writeStatementsPdfBytes(
    state: DashboardStatementsUiState,
    currencyCode: String,
    formatLocale: String?,
    exportDateLabel: String,
): ByteArray {
    val output = ByteArrayOutputStream()
    val writer = PdfWriter(output)
    val pdfDoc = PdfDocument(writer)
    val doc = Document(pdfDoc, PageSize.A4).apply {
        setMargins(36f, 36f, 36f, 36f)
    }

    val regular = PdfFontFactory.createFont(StandardFonts.HELVETICA)
    val bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
    val subtleGrey = DeviceRgb(120, 120, 120)
    val headerGrey = DeviceRgb(245, 245, 245)
    val hairline = DeviceRgb(220, 220, 220)

    doc.setFontSize(9.5f)
    doc.setFont(regular)

    // --- Report title ---------------------------------------------------
    doc.add(
        Paragraph("Relevé clients")
            .setFont(bold)
            .setFontSize(18f)
    )
    doc.add(
        Paragraph(exportDateLabel)
            .setFontColor(subtleGrey)
            .setFontSize(9f)
            .setMarginBottom(18f)
    )

    // --- Per-client sections --------------------------------------------
    if (state.clients.isEmpty()) {
        doc.add(
            Paragraph("Aucun client avec un document.")
                .setFontColor(subtleGrey)
        )
    } else {
        state.clients.forEachIndexed { index, client ->
            if (index > 0) doc.add(Paragraph(" ").setFontSize(4f))
            renderClient(doc, client, currencyCode, formatLocale, bold, headerGrey, hairline, subtleGrey)
        }
    }

    doc.close()
    writer.close()
    pdfDoc.close()
    return output.toByteArray()
}

private fun renderClient(
    doc: Document,
    client: DashboardClientStatement,
    currencyCode: String,
    formatLocale: String?,
    bold: com.itextpdf.kernel.font.PdfFont,
    headerBg: DeviceRgb,
    hairline: DeviceRgb,
    subtle: DeviceRgb,
) {
    // Client header line.
    doc.add(
        Paragraph(client.displayName)
            .setFont(bold)
            .setFontSize(12.5f)
            .setMarginTop(14f)
            .setMarginBottom(if (client.email.isNullOrBlank()) 2f else 0f)
    )
    if (!client.email.isNullOrBlank()) {
        doc.add(
            Paragraph(client.email)
                .setFontColor(subtle)
                .setFontSize(8.5f)
                .setMarginBottom(2f)
        )
    }
    val summary = buildList {
        if (client.invoices.isNotEmpty()) {
            add("${client.invoices.size} ${if (client.invoices.size > 1) "factures" else "facture"}")
        }
        if (client.creditNotes.isNotEmpty()) {
            add("${client.creditNotes.size} ${if (client.creditNotes.size > 1) "avoirs" else "avoir"}")
        }
        if (client.deliveryNotes.isNotEmpty()) add("${client.deliveryNotes.size} BL")
        if (client.salesOrders.isNotEmpty()) add("${client.salesOrders.size} BC")
        if (client.quotes.isNotEmpty()) add("${client.quotes.size} devis")
    }.joinToString(" • ")
    if (summary.isNotEmpty()) {
        doc.add(
            Paragraph(summary)
                .setFontColor(subtle)
                .setFontSize(8.5f)
                .setMarginBottom(6f)
        )
    }

    // 5-column table: Type | N° | Date | Montant | Statut.
    // Weights picked by eye for an A4 portrait page — amount right-aligned.
    val table = Table(UnitValue.createPercentArray(floatArrayOf(16f, 20f, 14f, 20f, 15f)))
        .useAllAvailableWidth()
        .setMarginBottom(10f)
    val headerBorder = SolidBorder(hairline, 0.5f)
    listOf("Type", "Numéro", "Date", "Montant", "Statut").forEachIndexed { i, title ->
        val cell = Cell().add(Paragraph(title).setFont(bold).setFontSize(9f))
            .setBackgroundColor(headerBg)
            .setBorder(headerBorder)
            .setPadding(5f)
        if (i == 3) cell.setTextAlignment(TextAlignment.RIGHT)
        table.addHeaderCell(cell)
    }
    appendTypeRows(table, client.invoices, "Facture", currencyCode, formatLocale, hairline)
    appendTypeRows(table, client.creditNotes, "Avoir", currencyCode, formatLocale, hairline)
    appendTypeRows(table, client.deliveryNotes, "Bon de livraison", currencyCode, formatLocale, hairline)
    appendTypeRows(table, client.salesOrders, "Bon de commande", currencyCode, formatLocale, hairline)
    appendTypeRows(table, client.quotes, "Devis", currencyCode, formatLocale, hairline)
    doc.add(table)
}

private fun appendTypeRows(
    table: Table,
    rows: List<DashboardStatementRow>,
    typeLabel: String,
    currencyCode: String,
    formatLocale: String?,
    hairline: DeviceRgb,
) {
    val rowBorder = SolidBorder(hairline, 0.3f)
    rows.forEach { row ->
        table.addCell(cell(typeLabel, rowBorder))
        table.addCell(cell(row.number, rowBorder))
        table.addCell(cell(row.date, rowBorder))
        table.addCell(
            cell(formatAmount(row.totalAmount, currencyCode, formatLocale), rowBorder)
                .setTextAlignment(TextAlignment.RIGHT)
        )
        table.addCell(cell(frenchStatusLabel(row.tagName), rowBorder))
    }
}

private fun cell(text: String, border: Border): Cell =
    Cell().add(Paragraph(text).setFontSize(9f))
        .setBorder(border)
        .setPadding(5f)

private fun frenchStatusLabel(tag: String?): String = when (tag) {
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
