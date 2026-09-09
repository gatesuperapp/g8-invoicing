package com.a4a.g8invoicing.ui.screens

import androidx.compose.runtime.Composable
import com.a4a.g8invoicing.ui.states.InvoiceState

/**
 * iOS stub — mirrors [ExportPdfPlatform.ios] no-op until the CII file
 * manager + UIActivityViewController plumbing lands.
 */
@Composable
actual fun ExportCiiPlatform(
    invoice: InvoiceState,
    onDismissRequest: () -> Unit,
) {
    // TODO(ios): black-screen UI + write via CiiXmlFileManager + share sheet
    onDismissRequest()
}
