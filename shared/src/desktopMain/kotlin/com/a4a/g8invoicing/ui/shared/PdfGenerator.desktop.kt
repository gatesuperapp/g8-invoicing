package com.a4a.g8invoicing.ui.shared

import com.a4a.g8invoicing.ui.states.DocumentState
import java.io.File

/**
 * Desktop implementation of PdfFileManager.
 * Saves PDFs to ~/Documents/g8 folder.
 */
actual class PdfFileManager actual constructor() {
    private val outputDir = File(System.getProperty("user.home"), "Documents/g8")

    init {
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
    }

    actual fun getTempFilePath(fileName: String): String {
        return File(outputDir, fileName).absolutePath
    }

    actual fun getFinalFilePath(fileName: String): String {
        return File(outputDir, fileName).absolutePath
    }

    actual fun saveToFinalLocation(tempFilePath: String, finalFileName: String): String {
        // On desktop, files are already in the final location
        return getFinalFilePath(finalFileName)
    }

    actual fun deleteTempFile(filePath: String) {
        File(filePath).delete()
    }

    actual fun openOrShare(filePath: String) {
        try {
            val os = System.getProperty("os.name").lowercase()
            when {
                os.contains("mac") -> Runtime.getRuntime().exec(arrayOf("open", outputDir.absolutePath))
                os.contains("win") -> Runtime.getRuntime().exec(arrayOf("explorer", outputDir.absolutePath))
                else -> Runtime.getRuntime().exec(arrayOf("xdg-open", outputDir.absolutePath))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * Desktop implementation of PdfGenerator.
 * Delegates to shared PdfGeneratorImpl in jvmMain.
 */
actual class PdfGenerator actual constructor(
    private val strings: PdfStrings,
    private val fileManager: PdfFileManager
) {
    private val impl = PdfGeneratorImpl(strings, fileManager)

    actual fun generatePdf(document: DocumentState): String {
        return impl.generatePdf(document)
    }
}
