package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.screens.shared.getDateFormatter
import com.a4a.g8invoicing.ui.shared.DocumentType
import java.util.Calendar


// This Client is created to manipulate client data,
// without using the ClientSimple.kt generated by sql
// it allows to not specify the client id (that will be auto-incremented)
// and to change properties values (var instead of val)


data class CreditNoteState(
    override var documentType: DocumentType = DocumentType.CREDIT_NOTE,
    override var documentTag: DocumentTag = DocumentTag.UNDEFINED,
    override var documentId: Int? = null,
    override var documentNumber: TextFieldValue = TextFieldValue(Strings.get(R.string.credit_note_default_number)),
    // Have to put a time "12:00:00" because of a bug, if we don't, DatePicker will show 1 day prior to current date
    // https://issuetracker.google.com/issues/281859606
    override var documentDate: String = getDateFormatter().format(Calendar.getInstance().time) +  " 12:00:00",
    override var reference: TextFieldValue? = null,
    override var freeField: TextFieldValue? = null,
    override var documentIssuer: ClientOrIssuerState? = null,
    override var documentClient: ClientOrIssuerState? = null,
    override var documentProducts: List<DocumentProductState>? = null,
    override var documentPrices: DocumentPrices? = null,
    override var currency: TextFieldValue  = TextFieldValue(),
    var paymentStatus: Int = 0,
    var dueDate: String = getDateFormatter().format(getDateInOneMonth().time) +  " 12:00:00",
    override var footerText: TextFieldValue = TextFieldValue(""),
    override var createdDate: String? = null
) : DocumentState()

