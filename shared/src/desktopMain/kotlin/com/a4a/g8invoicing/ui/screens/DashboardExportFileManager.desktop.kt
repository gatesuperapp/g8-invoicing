package com.a4a.g8invoicing.ui.screens

import java.awt.Desktop
import java.io.File

/**
 * Desktop implementation — writes the CSV to ~/Documents/g8 next to the
 * PDF/XML exports, then asks the OS to reveal the containing folder so
 * the user can grab the file and attach it wherever they want.
 */
actual class DashboardExportFileManager actual constructor() {
    private val outputDir = File(System.getProperty("user.home"), "Documents/g8").apply {
        if (!exists()) mkdirs()
    }

    actual fun exportCsv(fileName: String, content: String) {
        val outFile = File(outputDir, fileName)
        outFile.writeText(content, Charsets.UTF_8)
        openFolderBestEffort()
    }

    actual fun exportPdf(fileName: String, bytes: ByteArray) {
        if (bytes.isEmpty()) return
        val outFile = File(outputDir, fileName)
        outFile.writeBytes(bytes)
        openFolderBestEffort()
    }

    private fun openFolderBestEffort() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(outputDir)
            }
        } catch (_: Throwable) {
            // Open-folder is a nice-to-have; the file is saved either way.
        }
    }
}
