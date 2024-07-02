package com.a4a.g8invoicing.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ForwardElement
import com.a4a.g8invoicing.ui.shared.KeyboardOpt
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.DeliveryNoteState

@Composable
fun DeliveryNoteBottomSheetContent(
    deliveryNote: DeliveryNoteState,
    onValueChange: (ScreenElement, Any) -> Unit,
    onClickForward: (ScreenElement) -> Unit, // Clicking on client/issuer/items
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    localFocusManager: FocusManager
) {
    val inputList = listOfNotNull(
        FormInput(
            label = stringResource(id = R.string.document_number_short),
            inputType = TextInput(
                text = deliveryNote.documentNumber
                    ?: TextFieldValue(stringResource(id = R.string.document_default_number)),
                placeholder = stringResource(id = R.string.document_default_number),
                onValueChange = {
                    onValueChange(ScreenElement.DOCUMENT_NUMBER, it)
                },
            ),
            pageElement = ScreenElement.DOCUMENT_NUMBER
        ),
        FormInput(
            label = stringResource(id = R.string.document_date),
            inputType = ForwardElement(
                text = deliveryNote.documentDate
            ),
            pageElement = ScreenElement.DOCUMENT_DATE
        ),
        FormInput(
            label = stringResource(id = R.string.document_issuer),
            inputType = ForwardElement(
                text = deliveryNote.documentIssuer.let {
                    it.name.text + " " + (it.firstName?.text ?: "")
                } ?: "",
            ),
            pageElement = ScreenElement.DOCUMENT_ISSUER
        ),
        FormInput(
            label = stringResource(id = R.string.document_client),
            inputType = ForwardElement(
                text = deliveryNote.client.let {
                    it.name.text + " " + (it.firstName?.text ?: "")
                }
             ,
            ),
            pageElement = ScreenElement.DOCUMENT_CLIENT
        ),
        FormInput(
            label = stringResource(id = R.string.document_order_number),
            inputType = TextInput(
                text = deliveryNote.orderNumber,
                placeholder = stringResource(id = R.string.document_default_order_number),
                onValueChange = {
                    onValueChange(ScreenElement.DOCUMENT_ORDER_NUMBER, it)
                },
            ),
            pageElement = ScreenElement.DOCUMENT_ORDER_NUMBER
        ),
        FormInput(
            label = stringResource(id = R.string.document_line_items),
            inputType = ForwardElement(
                text = ""
            ),
            pageElement = ScreenElement.DOCUMENT_PRODUCTS
        )
    )

    FormUI(
        inputList = inputList,
        keyboard = KeyboardOpt.VALIDATE_INPUT,
        localFocusManager = localFocusManager,
        onClickForward = onClickForward,
        placeCursorAtTheEndOfText = placeCursorAtTheEndOfText
    )
}