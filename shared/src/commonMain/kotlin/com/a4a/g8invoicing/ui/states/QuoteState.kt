package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.shared.DocumentType

data class QuoteState(
    override var documentType: DocumentType = DocumentType.QUOTE,
    override var documentTag: DocumentTag = DocumentTag.DRAFT,
    override var documentId: Int? = null,
    override var documentNumber: TextFieldValue = TextFieldValue("D-001"),
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
    override var fontFamily: String? = null,
    // See InvoiceState.retentions. A devis previews the final billed amount
    // so it needs the same retention lines the corresponding invoice will
    // carry — otherwise the client-facing total on the quote wouldn't match
    // the eventual facture for a retention-eligible issuer.
    var retentions: List<RetentionState> = emptyList(),
    // Payment fields — parity with InvoiceState (see there for the full doc
    // on each one). A devis carries the same payment context as the future
    // invoice: the client needs to see how they're expected to pay before
    // signing, and the quote-to-invoice conversion copies the fields
    // straight across.
    var paymentMeansSelections: Set<String>? = null,
    var paymentMeansOtherChecked: Boolean = false,
    var paymentMeansSegments: List<com.a4a.g8invoicing.data.models.PaymentLabelSegment> = emptyList(),
    var paymentMeansHidden: Boolean = false,
    var paymentBankHidden: Boolean = false,
    var paymentBankSegments: List<com.a4a.g8invoicing.data.models.PaymentBankSegment> = emptyList(),
    var paymentTermsRecoveryFees: TextFieldValue = TextFieldValue(),
    var paymentTermsLateFees: TextFieldValue = TextFieldValue(),
    var paymentTermsDiscount: TextFieldValue = TextFieldValue(),
    // BT-120 VAT exemption reason — same semantics as InvoiceState. Set
    // when the issuer is in franchise en base so the mention appears
    // under the totals block on the devis preview + PDF.
    override var vatExemptionText: TextFieldValue? = null,
) : DocumentState()
