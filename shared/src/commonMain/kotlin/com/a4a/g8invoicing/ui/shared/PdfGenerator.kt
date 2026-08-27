package com.a4a.g8invoicing.ui.shared

import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.setScale
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.screens.shared.getLinkedDeliveryNotes
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.DocumentTotalPrices
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.ionspin.kotlin.bignum.decimal.RoundingMode

/**
 * Strings needed for PDF generation.
 * These are passed from the UI layer which has access to Compose Resources.
 */
data class PdfStrings(
    val invoiceNumber: String,
    val deliveryNoteNumber: String,
    val creditNoteNumber: String,
    val quoteNumber: String,
    val documentDate: String,
    val documentReference: String,
    val tableDescription: String,
    val tableQuantity: String,
    val tableUnit: String,
    val tableTaxRate: String,
    val tableUnitPrice: String,
    val tableTotalPrice: String,
    val totalWithoutTax: String,
    val totalWithTax: String,
    val tax: String,
    val dueDate: String,
    val invoicePaid: String,
    val labelSeparator: String,
    val addressedTo: String,
    val companyId1Label: String,
    val companyId2Label: String,
    val companyId3Label: String,
    val otherLines: String,
    // Parameterised label rendered under the top date when the document was
    // created with showCurrencyAndAutoTaxColumn=true and the currency isn't EUR. The
    // %1$s placeholder receives the ISO code — never translated.
    val currencyNoticeLabel: String,
    // Payment means block (BT-81) mode labels keyed by chip identity (enum
    // name like "TRANSFER", "PAYPAL"). Chip identity, not UN/CEFACT code,
    // because PayPal + Stripe share code 68. Resolved at construction time
    // from Compose Resources so PDF generation stays synchronous. The block's
    // prefix is not stored here — it's a per-document user label on
    // Invoice/CreditNote state (paymentMeansLabel), rendered as
    // "<userLabel> : <joined modes>".
    val paymentMeansLabels: Map<String, String>,
    // Label prefix for the bank identifier line — "IBAN :" for IBAN countries,
    // localised "N° de compte :" for non-IBAN countries (US, AU, NZ, ZA…).
    val bankAccountIbanLabel: String,
    val bankAccountGenericLabel: String,
    // Fallback header for the greyed payment box on documents that don't carry
    // a due date (credit notes). Invoices override it with "<dueDate label>
    // <date>" built from the frozen dueDate string above.
    val paymentSectionTitle: String,
)

/**
 * Platform-specific PDF file operations.
 */
expect class PdfFileManager() {
    /**
     * Get the temporary file path for PDF generation.
     */
    fun getTempFilePath(fileName: String): String

    /**
     * Get the final output path for the PDF.
     */
    fun getFinalFilePath(fileName: String): String

    /**
     * Move/copy the temp file to the final location (e.g., Downloads on Android).
     * Returns the final file path.
     */
    fun saveToFinalLocation(tempFilePath: String, finalFileName: String): String

    /**
     * Delete a temporary file.
     */
    fun deleteTempFile(filePath: String)

    /**
     * Open the folder containing the PDF (desktop) or get URI for sharing (Android).
     */
    fun openOrShare(filePath: String)

    /**
     * Load raw bytes for a bundled asset (e.g. embedded helvetica.ttf). Returns
     * null when the asset isn't available on the current platform.
     */
    fun loadAssetBytes(assetName: String): ByteArray?

    /**
     * Enumerate absolute paths of system font files usable by iText. The PDF
     * generator registers these in a FontProvider so any user-typed character
     * (currency symbols like ৳ ֏ ₽, or exotic chars in a footer/product name)
     * falls back to whichever system font covers it. Returns an empty list on
     * platforms without a discoverable system font directory.
     */
    fun listSystemFontFiles(): List<String>
}

/**
 * Shared PDF generator using iText.
 * This contains all the PDF generation logic that's identical on Android and Desktop.
 */
expect class PdfGenerator(strings: PdfStrings, fileManager: PdfFileManager) {
    /**
     * Generate a PDF for the given document. The watermark, if any, is read from
     * [DocumentState.watermarkText] (frozen at document creation).
     * Returns the final file name.
     */
    fun generatePdf(document: DocumentState): String

    /**
     * Generate a Factur-X 1.0 PDF (EN 16931 CII payload embedded in a
     * regular PDF with AFRelationship=Data and file name `factur-x.xml`).
     * The [xmlBytes] should be the UTF-8 encoded CII XML from
     * [com.a4a.g8invoicing.facturx.CiiXmlBuilder]. Returns the final file
     * name.
     */
    fun generateFacturX(document: DocumentState, xmlBytes: ByteArray): String
}

/**
 * Helper to get document type name.
 */
fun getDocumentTypeName(documentType: DocumentType, strings: PdfStrings): String {
    return when (documentType) {
        DocumentType.INVOICE -> strings.invoiceNumber
        DocumentType.DELIVERY_NOTE -> strings.deliveryNoteNumber
        DocumentType.CREDIT_NOTE -> strings.creditNoteNumber
        DocumentType.QUOTE -> strings.quoteNumber
    }
}
