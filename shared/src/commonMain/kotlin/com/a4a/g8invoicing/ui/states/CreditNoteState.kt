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
    override var originalCompanyId: Long? = null,
    var dueDate: String = "",
    var linkedInvoice: InvoiceState? = null,
    // BT-120 VAT exemption reason — mirror of Invoice.vatExemptionText. An
    // avoir is still a taxable-flow doc under EN16931 so it needs the same
    // exemption wording when the issuer is in franchise en base.
    override var vatExemptionText: TextFieldValue? = null,
    // No payment-means / bank / terms fields on credit notes: an avoir
    // reverses the flow — the seller owes the buyer, not the other way
    // around, so there's nothing for the buyer to pay. Refund paths (RIB
    // for a SEPA reverse transfer) exist but are rare and out of scope
    // for now. Keeping the state minimal avoids the "empty payment box"
    // artefacts that used to render on the PDF.
) : DocumentState()
