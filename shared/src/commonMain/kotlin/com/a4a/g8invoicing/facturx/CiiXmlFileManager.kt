package com.a4a.g8invoicing.facturx

/**
 * Platform-specific writer for CII XML files. Mirrors the pattern of
 * [com.a4a.g8invoicing.ui.shared.PdfFileManager] but with an
 * `application/xml` MIME type so:
 *  - Android's MediaStore stores the file with the right extension and
 *    the OS "open with…" picker offers XML-aware apps.
 *  - iOS Files / share sheet picks the correct UTI.
 *
 * Kept separate from PdfFileManager instead of parameterising the MIME so
 * the PDF path stays byte-identical and we don't risk regressions there.
 *
 * Writing and sharing are split so the export screen can show its own
 * "file saved" confirmation and let the user decide whether to email /
 * share — same UX as the PDF flow.
 */
expect class CiiXmlFileManager() {
    /**
     * Persist [xml] as a UTF-8 encoded file named [fileName] in a
     * shareable location (Downloads/g8 on Android, user Downloads on
     * Desktop, Documents folder on iOS). Returns the final file name
     * (the platform may have suffixed it on collision, e.g.
     * `invoice.xml` → `invoice (1).xml`).
     */
    fun writeXml(fileName: String, xml: String): String
}
