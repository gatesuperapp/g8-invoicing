package com.a4a.g8invoicing.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.data.DeliveryNoteEditable
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ForwardElement
import com.a4a.g8invoicing.ui.shared.KeyboardOpt
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput

@Composable
fun DeliveryNoteBottomSheetContent(
    deliveryNote: DeliveryNoteEditable,
    onValueChange: (ScreenElement, Any) -> Unit,
    onClickForward: (ScreenElement) -> Unit, // Clicking on client/issuer/items
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
) {
    val localFocusManager = LocalFocusManager.current

    val inputList = listOfNotNull(
        FormInput(
            label = stringResource(id = R.string.delivery_note_number_short),
            inputType = TextInput(
                text = deliveryNote.number
                    ?: TextFieldValue(stringResource(id = R.string.delivery_note_default_number)),
                placeholder = stringResource(id = R.string.delivery_note_default_number), //unused
                onValueChange = {
                    onValueChange(ScreenElement.DOCUMENT_NUMBER, it)
                },
            ),
            pageElement = ScreenElement.DOCUMENT_NUMBER
        ),
        FormInput(
            label = stringResource(id = R.string.delivery_note_date),
            inputType = ForwardElement(
                text = deliveryNote.deliveryDate
                    ?: stringResource(id = R.string.delivery_note_default_date)
            ),
            pageElement = ScreenElement.DOCUMENT_DATE
        ),
        FormInput(
            label = stringResource(id = R.string.delivery_note_issuer),
            inputType = ForwardElement(
                text = deliveryNote.issuer?.let {
                    it.name.text + " " + it.firstName?.text
                }
                    ?: (stringResource(id = R.string.delivery_note_default_issuer_firstName) + " " + stringResource(id = R.string.delivery_note_default_issuer_name)),
            ),
            pageElement = ScreenElement.DOCUMENT_ISSUER
        ),
        FormInput(
            label = stringResource(id = R.string.delivery_note_client),
            inputType = ForwardElement(
                text = deliveryNote.client?.let {
                    it.name.text + " " + it.firstName?.text
                }
                    ?: (stringResource(id = R.string.delivery_note_default_client_firstName) + " " + stringResource(id = R.string.delivery_note_default_client_name)),
            ),
            pageElement = ScreenElement.DOCUMENT_CLIENT
        ),
        FormInput(
            label = stringResource(id = R.string.delivery_note_order_number),
            inputType = TextInput(
                text = deliveryNote.orderNumber
                    ?: TextFieldValue(stringResource(id = R.string.delivery_note_default_order_number)),
                placeholder = stringResource(id = R.string.delivery_note_default_order_number), //unused
                onValueChange = {
                    onValueChange(ScreenElement.DOCUMENT_ORDER_NUMBER, it)
                },
            ),
            pageElement = ScreenElement.DOCUMENT_ORDER_NUMBER
        ),
        FormInput(
            label = stringResource(id = R.string.delivery_note_line_items),
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