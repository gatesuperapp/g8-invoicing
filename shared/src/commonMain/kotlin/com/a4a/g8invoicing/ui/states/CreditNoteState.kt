package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.shared.DocumentType

data class CreditNoteState(
    override var documentType: DocumentType = DocumentType.CREDIT_NOTE,
    override var documentTag: DocumentTag = DocumentTag.DRAFT,
    override var documentId: Int? = null,
    override var documentNumber: TextFieldValue = TextFieldValue("A-001"),
    override var documentDate: String = "",
    override var reference: TextFieldValue? = null,
    override var freeField: TextFieldValue? = null,
    override var documentIssuer: ClientOrIssuerState? = null,
    override var documentClient: ClientOrIssuerState? = null,
    override var documentProducts: List<DocumentProductState>? = null,
    override var documentTotalPrices: DocumentTotalPrices? = null,
    override var currency: TextFieldValue = TextFieldValue(),
    override var footerText: TextFieldValue = TextFieldValue(),
    override var createdDate: String? = null,
    override var watermarkText: String? = null,
    override var labelsSnapshot: String? = null,
    override var showCurrencyAndAutoTaxColumn: Boolean = false,
    override var formatLocale: String? = null,
    var dueDate: String = "",
    var linkedInvoice: InvoiceState? = null,
    // See InvoiceState.paymentMeansSelections — same semantics on credit notes since
    // they are Factur-X payment documents too (type-code 381 for regular avoir, 384
    // for corrective).
    var paymentMeansSelections: Set<String>? = null,
    // See InvoiceState.paymentMeansOtherChecked.
    var paymentMeansOtherChecked: Boolean = false,
    // See InvoiceState.paymentMeansSegments.
    var paymentMeansSegments: List<com.a4a.g8invoicing.data.models.PaymentLabelSegment> = emptyList(),
    // See InvoiceState.paymentMeansHidden.
    var paymentMeansHidden: Boolean = false,
    // See InvoiceState.paymentBankHidden.
    var paymentBankHidden: Boolean = false,
    // See InvoiceState.paymentBankSegments.
    var paymentBankSegments: List<com.a4a.g8invoicing.data.models.PaymentBankSegment> = emptyList(),
) : DocumentState()
