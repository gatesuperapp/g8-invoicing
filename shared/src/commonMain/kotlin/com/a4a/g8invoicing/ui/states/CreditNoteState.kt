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
    var linkedInvoice: InvoiceState? = null
) : DocumentState()
