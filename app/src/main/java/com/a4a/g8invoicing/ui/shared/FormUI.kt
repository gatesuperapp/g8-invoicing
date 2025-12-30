package com.a4a.g8invoicing.ui.shared

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.states.ClientRef
import com.a4a.g8invoicing.ui.theme.inputLabel
import java.math.BigDecimal
import kotlin.collections.filter

@Composable
fun FormUI(
    inputList: List<FormInput>,
    keyboard: KeyboardOpt = KeyboardOpt.GO_TO_NEXT_INPUT,
    localFocusManager: FocusManager,
    onClickForward: (ScreenElement) -> Unit = {},
    onClickOpenClientSelection: (String) -> Unit = {},
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit = {},
    errors: MutableList<Pair<ScreenElement, String?>>? = null,
    onClickExpandFullScreen: (ScreenElement) -> Unit = {}, // Used to expand product description field
) {
    // handle focus
    val focusManager = LocalFocusManager.current

    val textFieldsInputs: List<ScreenElement> = inputList.filter {
        true
    }.map {
        it.pageElement
    }

    // Create a focus requester for each input element, ex: inputsAndFocusRequesters = listOf(
    //            Pair(PageElement.DELIVERY_NOTE_NUMBER, FocusRequester()),
    //            Pair(PageElement.ORDER_NUMBER, FocusRequester())
    //        )
    val inputsWithFocusRequester: MutableList<Pair<ScreenElement, FocusRequester>> = mutableListOf()
    textFieldsInputs.forEach {
        inputsWithFocusRequester.add(
            Pair(it, FocusRequester())
        )
    }
    val focusRequesters: List<Pair<ScreenElement, FocusRequester>> =
        remember { inputsWithFocusRequester }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                bottom = 8.dp
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        inputList.forEach { input ->
            // Keyboard actions and options
            val isLastInput = input == inputList.last()
            val imeAction = if (isLastInput || keyboard == KeyboardOpt.VALIDATE_INPUT) {
                ImeAction.Done
            } else {
                ImeAction.Next
            }
            val formActions = if (!isLastInput) {
                KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                )
            } else {
                KeyboardActions(
                    onGo = {
                        focusManager.clearFocus()
                    }
                )
            }

            PageElementCreator(
                input = input,
                isLastInput = input == inputList.last(),
                imeAction = imeAction,
                onClickForward = onClickForward,
                onClickOpenClientSelection = onClickOpenClientSelection,
                formActions = formActions,
                focusRequester = focusRequesters.firstOrNull { it.first == input.pageElement }?.second,
                onClickRow = {
                    if (input.inputType is TextInput) {
                        placeCursorAtTheEndOfText(input.pageElement)
                    }
                    focusRequesters.firstOrNull { it.first == input.pageElement }?.second?.requestFocus()
                },
                errorMessage = errors?.firstOrNull { it.first == input.pageElement }?.second,
                onClickExpandFullScreen = {
                    onClickExpandFullScreen(input.pageElement)
                }, // Used to expand product description field,
                clearFocusForAllRows = {
                    focusManager.clearFocus(force = true)

                }
            )
        }
    }
}

@Composable
fun PageElementCreator(
    input: FormInput,
    isLastInput: Boolean,
    imeAction: ImeAction,
    formActions: KeyboardActions,
    focusRequester: FocusRequester?,
    onClickRow: () -> Unit,
    onClickForward: (ScreenElement) -> Unit,
    onClickOpenClientSelection: (String) -> Unit,
    errorMessage: String?,
    onClickExpandFullScreen: () -> Unit, // Used to expand product description field
    clearFocusForAllRows: () -> Unit,
) {

    RowWithLabelAndInput(
        formInput = input,
        imeAction = imeAction,
        formActions = formActions,
        focusRequester = focusRequester,
        onClickRow = onClickRow,
        onClickForward = onClickForward,
        onClickOpenClientSelection = onClickOpenClientSelection,
        errorMessage = errorMessage,
        onClickExpandFullScreen = onClickExpandFullScreen,
        clearFocusForAllRows = clearFocusForAllRows
    )

    if (!isLastInput) {
        Separator()
    }
}

@Composable
fun RowWithLabelAndInput(
    formInput: FormInput,
    imeAction: ImeAction,
    formActions: KeyboardActions,
    focusRequester: FocusRequester?,
    onClickRow: () -> Unit,
    onClickForward: (ScreenElement) -> Unit,
    onClickOpenClientSelection: (String) -> Unit,
    errorMessage: String?,
    onClickExpandFullScreen: () -> Unit, // Used to expand product description field
    clearFocusForAllRows: () -> Unit,
) {
    // for the ripple on the row
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.Top, // For label to stay on top when multiline input
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.Black, bounded = true),
                onClick = {
                    when (formInput.inputType) {
                        is ForwardElement -> {
                            clearFocusForAllRows()
                            onClickForward(formInput.pageElement)
                        }
                        is ListPicker -> {
                            formInput.extraId?.let {
                                onClickOpenClientSelection(it)
                            }
                        }
                        else -> onClickRow()
                    }
                }
            )
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = if (formInput.pageElement == ScreenElement.DOCUMENT_PRODUCT_NAME
                    || formInput.pageElement == ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION
                ) 0.dp else 16.dp,
                top = 14.dp,
                bottom = if (formInput.pageElement == ScreenElement.PRODUCT_OTHER_PRICE_CLIENTS) 4.dp else 14.dp
            )
    ) {
        // Label
        when (formInput.label) {
            is String -> Text(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .padding(end = 3.dp),
                text = formInput.label,
                style = MaterialTheme.typography.inputLabel
            )

            is TextInput -> FormInputCreatorText(
                input = formInput.label,
                keyboardOption = imeAction,
                formActions = formActions,
                focusRequester = focusRequester,
                errorMessage = errorMessage,
                isEditableLabel = true
            )
        }

        when (formInput.inputType) {
            is TextInput -> {
                FormInputCreatorText(
                    input = formInput.inputType,
                    keyboardOption = imeAction,
                    formActions = formActions,
                    focusRequester = focusRequester,
                    errorMessage = errorMessage,
                    onClickExpandFullScreen = onClickExpandFullScreen
                )
            }

            is DecimalInput -> if (formInput.inputType2 is DecimalInput) {
                FormInputCreatorDoublePrice(
                    priceWithoutTaxInput = formInput.inputType,
                    priceWithTaxInput = formInput.inputType2,
                    taxRate = (formInput.inputType).taxRate,
                    keyboardOption = imeAction,
                    formActions = formActions,
                    focusRequester = focusRequester,
                )
            } else {
                FormInputCreatorDecimal(
                    input = formInput.inputType,
                    keyboardOption = imeAction,
                    formActions = formActions,
                    focusRequester = focusRequester,
                )
            }

            is ForwardElement ->
                FormInputCreatorGoForward(
                    forwardInput = formInput.inputType
                )

            // Used for additional prices, to add client(s)
            is ListPicker ->
                FormInputCreatorListPicker(
                    input = formInput.inputType
                )
        }
    }
}

class FormInput(
    val label: Any,
    val inputType: Any,
    val inputType2: Any? = null, // Used for DoubleInputCreator
    val pageElement: ScreenElement,
    val extraId: String? = null
)

class TextInput(
    val text: TextFieldValue? = null,
    val placeholder: String? = null,
    val onValueChange: (TextFieldValue) -> Unit = {},
    val keyboardType: KeyboardType = KeyboardType.Text,
    val displayFullScreenIcon: Boolean = false,
)

class DecimalInput(
    val text: String? = null,
    val taxRate: BigDecimal? = null,
    val placeholder: String,
    val onValueChange: (String) -> Unit = {},
    val keyboardType: KeyboardType = KeyboardType.Decimal,
)

class ForwardElement(
    val text: String,
    val isMultiline: Boolean = true,
    val displayArrow: Boolean = true,
)

data class ListPicker(
    val selectedItems: List<ClientRef>,
    val onClick: (() -> Unit)? = null,
    val onRemoveItem: ((Int) -> Unit)? = null
)
/*
class DateInput @OptIn(ExperimentalMaterial3Api::class) constructor(
    val text: String? = null,
    val datePickerState: DatePickerState,
    )*/

enum class KeyboardOpt {
    VALIDATE_INPUT,
    GO_TO_NEXT_INPUT
}
