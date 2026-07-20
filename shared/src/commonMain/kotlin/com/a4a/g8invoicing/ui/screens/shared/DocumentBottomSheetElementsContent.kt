package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusManager
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_number_short
import com.a4a.g8invoicing.shared.resources.invoice_default_number
import com.a4a.g8invoicing.shared.resources.document_date
import com.a4a.g8invoicing.shared.resources.document_issuer
import com.a4a.g8invoicing.shared.resources.document_client
import com.a4a.g8invoicing.shared.resources.document_reference
import com.a4a.g8invoicing.shared.resources.document_default_reference
import com.a4a.g8invoicing.shared.resources.document_free_field
import com.a4a.g8invoicing.shared.resources.document_due_date
import com.a4a.g8invoicing.shared.resources.document_footer
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ForwardElement
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import org.jetbrains.compose.resources.stringResource

@Composable
fun DocumentBottomSheetElementsContent(
    document: DocumentState,
    onValueChange: (ScreenElement, Any) -> Unit,
    onClickForward: (ScreenElement) -> Unit, // Clicking on client/issuer/items
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    localFocusManager: FocusManager,
) {

    val inputList = mutableListOf(
        FormInput(
            label = stringResource(Res.string.document_number_short),
            inputType = TextInput(
                text = document.documentNumber,
                placeholder = stringResource(Res.string.invoice_default_number),
                onValueChange = {
                    onValueChange(ScreenElement.DOCUMENT_NUMBER, it)
                },
            ),
            pageElement = ScreenElement.DOCUMENT_NUMBER
        ),
        FormInput(
            label = stringResource(Res.string.document_date),
            inputType = ForwardElement(
                text = document.documentDate.substringBefore(" "),
                displayArrow = false
            ),
            pageElement = ScreenElement.DOCUMENT_DATE
        ),
        FormInput(
            label = stringResource(Res.string.document_issuer),
            inputType = ForwardElement(
                text = document.documentIssuer?.let {
                    it.name.text + (it.firstName?.let { " " + it.text } ?: "")
                } ?: "",
            ),
            pageElement = ScreenElement.DOCUMENT_ISSUER
        ),
        FormInput(
            label = stringResource(Res.string.document_client),
            inputType = ForwardElement(
                text = document.documentClient?.let {
                    it.name.text + (it.firstName?.let { " " + it.text } ?: "")
                } ?: "",
            ),
            pageElement = ScreenElement.DOCUMENT_CLIENT
        ),
        FormInput(
            label = stringResource(Res.string.document_reference),
            inputType = TextInput(
                text = document.reference,
                placeholder = stringResource(Res.string.document_default_reference),
                onValueChange = {
                    onValueChange(ScreenElement.DOCUMENT_REFERENCE, it)
                },
            ),
            pageElement = ScreenElement.DOCUMENT_REFERENCE
        ),
        FormInput(
            label = stringResource(Res.string.document_free_field),
            inputType = TextInput(
                text = document.freeField,
                onValueChange = {
                    onValueChange(ScreenElement.DOCUMENT_FREE_FIELD, it)
                },
            ),
            pageElement = ScreenElement.DOCUMENT_FREE_FIELD
        )

    )

    if (document is InvoiceState) {
        inputList
            .add(
                FormInput(
                    label = stringResource(Res.string.document_due_date),
                    inputType = ForwardElement(
                        text = document.dueDate.substringBefore(" "),
                        displayArrow = false
                    ),
                    pageElement = ScreenElement.DOCUMENT_DUE_DATE
                )
            )
    }
    inputList.add(
        FormInput(
            label = stringResource(Res.string.document_footer),
            inputType = ForwardElement(
                text = document.footerText.text.ifEmpty { " - " },
                isMultiline = false,
                displayArrow = false
            ),
            pageElement = ScreenElement.DOCUMENT_FOOTER
        )
    )


    FormUI(
        inputList = inputList,
        localFocusManager = localFocusManager,
        onClickForward = onClickForward,
        placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
        //keyboard = KeyboardOpt.VALIDATE_INPUT,
    )

}
