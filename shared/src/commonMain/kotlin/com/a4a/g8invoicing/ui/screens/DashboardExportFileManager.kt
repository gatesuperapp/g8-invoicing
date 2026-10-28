package com.a4a.g8invoicing.ui.screens

/**
 * Platform writer + share launcher for the Dashboard "Exporter CSV / PDF"
 * buttons. Mirrors [com.a4a.g8invoicing.facturx.CiiXmlFileManager] but
 * fires the OS share sheet in the same call so the user flow is one tap
 * (file saved → "Envoyer via…" opens straight away).
 *
 * iOS is a stub for now; neither Dashboard tab is wired into the iOS
 * entry point as of 2.2.
 */
expect class DashboardExportFileManager() {
    /**
     * Persist [content] as a UTF-8 CSV in the shareable Downloads folder
     * and open the OS share sheet pre-filled with the resulting file.
     */
    fun exportCsv(fileName: String, content: String)

    /**
     * Persist [bytes] as a PDF in the shareable Downloads folder and open
     * the OS share sheet pre-filled with the resulting file. Zero-length
     * [bytes] is a no-op (iOS stub path).
     */
    fun exportPdf(fileName: String, bytes: ByteArray)
}
