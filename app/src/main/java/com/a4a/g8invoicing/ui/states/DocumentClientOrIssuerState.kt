package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType

// The equivalent of ClientOrIssuerState, but used to save the client & issuer
// linked to the document, so they're not deleted from the document
// when deleted from the client/issuer list.
data class DocumentClientOrIssuerState(
    var id: Int? = null,
    var type: ClientOrIssuerType? = null,
    var firstName: TextFieldValue? = null,
    var name: TextFieldValue = TextFieldValue(""),
    var addresses: List<AddressState>? = null,
    var phone: TextFieldValue? = null,
    var email: TextFieldValue? = null,
    var notes: TextFieldValue? = null,
    var companyId1Label: TextFieldValue? = TextFieldValue(Strings.get(R.string.document_default_issuer_company_label1)),
    var companyId1Number: TextFieldValue? = null,
    var companyId2Label: TextFieldValue? = TextFieldValue(Strings.get(R.string.document_default_issuer_company_label2)),
    var companyId2Number: TextFieldValue? = null,
    var errors: MutableList<Pair<ScreenElement, String?>> = mutableListOf(),
)

