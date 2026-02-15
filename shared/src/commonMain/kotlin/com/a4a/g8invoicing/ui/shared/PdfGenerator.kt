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
    val currency: String,
    val invoicePaid: String,
    val labelSeparator: String,
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
}

/**
 * Shared PDF generator using iText.
 * This contains all the PDF generation logic that's identical on Android and Desktop.
 */
expect class PdfGenerator(strings: PdfStrings, fileManager: PdfFileManager) {
    /**
     * Generate a PDF for the given document.
     * Returns the final file name.
     */
    fun generatePdf(document: DocumentState): String
}

/**
 * Helper to get document type name.
 */
fun getDocumentTypeName(documentType: DocumentType, strings: PdfStrings): String {
    return when (documentType) {
        DocumentType.INVOICE -> strings.invoiceNumber
        DocumentType.DELIVERY_NOTE -> strings.deliveryNoteNumber
        DocumentType.CREDIT_NOTE -> strings.creditNoteNumber
    }
}
