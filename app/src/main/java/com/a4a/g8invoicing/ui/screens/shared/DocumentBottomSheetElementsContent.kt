package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ForwardElement
import com.a4a.g8invoicing.ui.shared.KeyboardOpt
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState

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
            label = stringResource(id = R.string.document_number_short),
            inputType = TextInput(
                text = document.documentNumber,
                placeholder = stringResource(id = R.string.invoice_default_number),
                onValueChange = {
                    onValueChange(ScreenElement.DOCUMENT_NUMBER, it)
                },
            ),
            pageElement = ScreenElement.DOCUMENT_NUMBER
        ),
        FormInput(
            label = stringResource(id = R.string.document_date),
            inputType = ForwardElement(
                text = document.documentDate.substringBefore(" "),
                displayArrow = false
            ),
            pageElement = ScreenElement.DOCUMENT_DATE
        ),
        FormInput(
            label = stringResource(id = R.string.document_issuer),
            inputType = ForwardElement(
                text = document.documentIssuer?.let {
                    (it.firstName?.let { it.text + " " } ?: "") + it.name.text
                } ?: "",
            ),
            pageElement = ScreenElement.DOCUMENT_ISSUER
        ),
        FormInput(
            label = stringResource(id = R.string.document_client),
            inputType = ForwardElement(
                text = document.documentClient?.let {
                    (it.firstName?.let { it.text + " " } ?: "") + it.name.text
                } ?: "",
            ),
            pageElement = ScreenElement.DOCUMENT_CLIENT
        ),
        FormInput(
            label = stringResource(id = R.string.document_reference),
            inputType = TextInput(
                text = document.reference,
                placeholder = stringResource(id = R.string.document_default_reference),
                onValueChange = {
                    onValueChange(ScreenElement.DOCUMENT_REFERENCE, it)
                },
            ),
            pageElement = ScreenElement.DOCUMENT_REFERENCE
        ),
        FormInput(
            label = stringResource(id = R.string.document_free_field),
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
                    label = stringResource(id = R.string.document_due_date),
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
            label = stringResource(id = R.string.document_footer),
            inputType = ForwardElement(
                text = document.footerText.text.ifEmpty { " - " },
                isMultiline = false,
                displayArrow = false
            ),
            pageElement = ScreenElement.DOCUMENT_FOOTER
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                bottom = 8.dp
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                })
            }
    ) {

        FormUI(
            inputList = inputList,
            keyboard = KeyboardOpt.VALIDATE_INPUT,
            localFocusManager = localFocusManager,
            onClickForward = onClickForward,
            placeCursorAtTheEndOfText = placeCursorAtTheEndOfText
        )

    }
}


