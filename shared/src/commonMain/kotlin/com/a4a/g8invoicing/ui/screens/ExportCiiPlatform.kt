package com.a4a.g8invoicing.ui.screens

import androidx.compose.runtime.Composable
import com.a4a.g8invoicing.ui.states.InvoiceState

/**
 * Platform-specific CII XML export screen — same "black screen with status +
 * file location + email/share buttons" UX as [ExportPdfPlatform], but writes
 * a raw EN 16931 CII XML file via [com.a4a.g8invoicing.facturx.CiiXmlBuilder]
 * instead of a PDF.
 *
 * Called directly from DocumentAddEdit (not plumbed through the NavGraph
 * lambda chain like ExportPdfPlatform), since it doesn't need any wiring
 * from the App level.
 */
@Composable
expect fun ExportCiiPlatform(
    invoice: InvoiceState,
    onDismissRequest: () -> Unit,
)
