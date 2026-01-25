package com.a4a.g8invoicing.ui.shared

import com.a4a.g8invoicing.ui.states.DocumentState

/**
 * iOS stub implementation for PdfFileManager.
 * TODO: Implement with PDFKit when iOS support is added.
 */
actual class PdfFileManager actual constructor() {
    actual fun getTempFilePath(fileName: String): String {
        // TODO: Use NSTemporaryDirectory()
        return ""
    }

    actual fun getFinalFilePath(fileName: String): String {
        // TODO: Use Documents directory
        return ""
    }

    actual fun saveToFinalLocation(tempFilePath: String, finalFileName: String): String {
        // TODO: Implement file copy
        return ""
    }

    actual fun deleteTempFile(filePath: String) {
        // TODO: Implement file deletion
    }

    actual fun openOrShare(filePath: String) {
        // TODO: Use UIActivityViewController for sharing
    }
}

/**
 * iOS stub implementation for PdfGenerator.
 * TODO: Implement with PDFKit when iOS support is added.
 */
actual class PdfGenerator actual constructor(
    private val strings: PdfStrings,
    private val fileManager: PdfFileManager
) {
    actual fun generatePdf(document: DocumentState): String {
        // TODO: Implement PDF generation with PDFKit
        // For now, return empty string to indicate not implemented
        return ""
    }
}
