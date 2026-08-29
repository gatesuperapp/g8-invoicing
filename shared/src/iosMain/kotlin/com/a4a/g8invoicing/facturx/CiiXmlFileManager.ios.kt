package com.a4a.g8invoicing.facturx

/**
 * iOS stub — TODO: NSString.writeToURL into NSTemporaryDirectory then
 * hand the URL to the export screen so it can present a
 * UIActivityViewController with the `.xml` UTType.
 *
 * Left as a no-op for now (same pattern as PdfFileManager.ios). The
 * CiiXmlBuilder itself already runs on iOS via commonMain — only the
 * file-manager plumbing is missing.
 */
actual class CiiXmlFileManager actual constructor() {
    actual fun writeXml(fileName: String, xml: String): String {
        // TODO(ios): NSString.writeToURL to NSTemporaryDirectory
        return fileName
    }
}
