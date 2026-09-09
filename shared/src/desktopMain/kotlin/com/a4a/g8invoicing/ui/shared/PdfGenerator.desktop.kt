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

    actual fun loadAssetBytes(assetName: String): ByteArray? {
        // Desktop reads embedded assets from the jvmMain classpath.
        return try {
            this::class.java.classLoader?.getResourceAsStream(assetName)?.use { it.readBytes() }
        } catch (_: Throwable) {
            null
        }
    }

    actual fun listSystemFontFiles(): List<String> {
        // Iterate the OS-specific system font dirs; iText's FontProvider will
        // skip anything it can't parse. Kept coarse: users on desktop rarely
        // hit exotic scripts, and the Android side is where this matters most.
        val roots = when {
            System.getProperty("os.name").lowercase().contains("mac") ->
                listOf("/System/Library/Fonts", "/Library/Fonts", "${System.getProperty("user.home")}/Library/Fonts")
            System.getProperty("os.name").lowercase().contains("win") ->
                listOf("${System.getenv("WINDIR") ?: "C:\\Windows"}\\Fonts")
            else -> listOf("/usr/share/fonts", "/usr/local/share/fonts", "${System.getProperty("user.home")}/.fonts")
        }
        return roots.flatMap { root ->
            try {
                File(root).walkTopDown().filter {
                    it.isFile && it.canRead() && (
                        it.name.endsWith(".ttf", true) ||
                        it.name.endsWith(".otf", true) ||
                        it.name.endsWith(".ttc", true)
                    )
                }.map { it.absolutePath }.toList()
            } catch (_: Throwable) {
                emptyList()
            }
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
    private val imageStorage = ImageStorage()
    private val impl = PdfGeneratorImpl(strings, fileManager, imageStorage)

    actual fun generatePdf(document: DocumentState): String {
        return impl.generatePdf(document)
    }

    actual fun generateFacturX(document: DocumentState, xmlBytes: ByteArray): String {
        return impl.generateFacturX(document, xmlBytes)
    }
}
