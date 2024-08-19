package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.shared.DocumentType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


// This Client is created to manipulate client data,
// without using the ClientSimple.kt generated by sql
// it allows to not specify the client id (that will be auto-incremented)
// and to change properties values (var instead of val)
data class InvoiceState(
    override var documentType: DocumentType = DocumentType.INVOICE,
    override var documentId: Int? = null,
    override var documentNumber: TextFieldValue = TextFieldValue(Strings.get(R.string.document_default_number)),
    // Have to put a time "12:00:00" because of a bug, if we don't, DatePicker will show 1 day prior to current date
    // https://issuetracker.google.com/issues/281859606
    override var documentDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(Calendar.getInstance().time) +  " 12:00:00",
    override var orderNumber: TextFieldValue? = null,
    override var documentIssuer: DocumentClientOrIssuerState? = null,
    override var documentClient: DocumentClientOrIssuerState? = null,
    override var documentProducts: List<DocumentProductState>? = null,
    override var documentPrices: DocumentPrices? = null,
    override var currency: TextFieldValue  = TextFieldValue(),
    var dueDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(Calendar.getInstance().time) +  " 12:00:00",
    var footerText: TextFieldValue = TextFieldValue(Strings.get(R.string.document_default_footer)),
) : DocumentState()

