package com.a4a.g8invoicing.facturx

import java.io.File

/**
 * Desktop implementation — writes the XML alongside the existing PDF
 * exports in `~/Documents/g8`. The export screen opens the containing
 * folder itself once the user hits "Share" / "Show file", same as the
 * PDF path.
 */
actual class CiiXmlFileManager actual constructor() {
    private val outputDir = File(System.getProperty("user.home"), "Documents/g8").apply {
        if (!exists()) mkdirs()
    }

    actual fun writeXml(fileName: String, xml: String): String {
        val outFile = File(outputDir, fileName)
        outFile.writeText(xml, Charsets.UTF_8)
        return outFile.name
    }
}
