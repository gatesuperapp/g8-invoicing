package com.a4a.g8invoicing.ui.screens

/**
 * iOS stub — the Dashboard isn't surfaced on the iOS entry point as of
 * 2.2 so these calls are inert. Replace with NSFileManager + a
 * UIActivityViewController launch when iOS gains Dashboard access.
 */
actual class DashboardExportFileManager actual constructor() {
    actual fun exportCsv(fileName: String, content: String) {
        // no-op
    }

    actual fun exportPdf(fileName: String, bytes: ByteArray) {
        // no-op
    }
}
