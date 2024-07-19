package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.math.BigDecimal

@Composable
fun FormUI(
    inputList: List<FormInput>,
    keyboard: KeyboardOpt = KeyboardOpt.GO_TO_NEXT_INPUT,
    localFocusManager: FocusManager,
    onClickForward: (ScreenElement) -> Unit = {},
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit = {},
    errors: MutableList<Pair<ScreenElement, String?>>? = null
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

    //  Remember which input element is focused, ex:  inputsAndFocused = listOf(
    //            Pair(PageElement.DELIVERY_NOTE_NUMBER, Boolean),
    //            Pair(PageElement.ORDER_NUMBER, Boolean)
    //        )
    val inputsWithBoolean: MutableList<Pair<ScreenElement, Boolean>> = mutableListOf()
    textFieldsInputs.forEach {
        inputsWithBoolean.add(
            Pair(it, false) // unfocused by default
        )
    }
    val focusStates: MutableList<Pair<ScreenElement, Boolean>> = remember { inputsWithBoolean }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                bottom = 14.dp
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                })
            }
    ) {
        inputList.forEach { item ->
            // Keyboard actions and options
            val isLastInput = item == inputList.last()
            val imeAction = if (isLastInput || keyboard == KeyboardOpt.VALIDATE_INPUT) {
                ImeAction.Done
            } else {
                ImeAction.Next
            }
            val formActions = if (!isLastInput) {
                KeyboardActions(
                    onNext = {
                        localFocusManager.moveFocus(FocusDirection.Down)
                    }
                )
            } else {
                KeyboardActions(
                    onGo = {
                        localFocusManager.clearFocus()
                    }
                )
            }

            PageElementCreator(
                item = item,
                isLastInput = item == inputList.last(),
                imeAction = imeAction,
                onClickForward = onClickForward,
                formActions = formActions,
                focusRequester = focusRequesters.firstOrNull { it.first == item.pageElement }?.second,
                onClickRow = {
                    if (focusStates.any { it.second == true }) { // If any input was focused, we clear all
                        focusManager.clearFocus()
                        clearFocusForAllRows(focusStates)
                    } else { // Else, we focus the cursor in the text input of the clicked row
                        if (item.inputType is TextInput) {
                            placeCursorAtTheEndOfText(item.pageElement)
                        }
                        focusRequesters.firstOrNull { it.first == item.pageElement }?.second?.requestFocus()
                        saveFocusForTheRow(focusStates, item.pageElement)
                    }
                },
                errorMessage = errors?.firstOrNull { it.first == item.pageElement }?.second
            )
        }
    }
}

fun clearFocusForAllRows(list: MutableList<Pair<ScreenElement, Boolean>>) {
    list.forEachIndexed { index, pair ->
        list.set(index, Pair(pair.first, false))
    }
}

fun saveFocusForTheRow(list: MutableList<Pair<ScreenElement, Boolean>>, element: ScreenElement?) {
    element?.let { pageElement ->
        list.set(list.indexOfFirst { it.first == pageElement }, Pair(pageElement, true))
    }
}

@Composable
fun PageElementCreator(
    item: FormInput,
    isLastInput: Boolean,
    imeAction: ImeAction,
    formActions: KeyboardActions,
    focusRequester: FocusRequester?,
    onClickRow: () -> Unit,
    onClickForward: (ScreenElement) -> Unit,
    errorMessage: String?
) {

    RowWithLabelAndInput(
        formInput = item,
        imeAction = imeAction,
        formActions = formActions,
        focusRequester = focusRequester,
        onClickRow = onClickRow,
        onClickForward = onClickForward,
        errorMessage = errorMessage
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
    errorMessage: String?
) {
    // for the ripple on the row
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.Top, // For label to stay on top when multiline input
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.Black, bounded = true),
                onClick = {
                    when (formInput.inputType) {
                        is ForwardElement -> {
                            onClickForward(formInput.pageElement)
                        }

                        else -> onClickRow()
                    }
                }
            )
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = 14.dp
            )
    ) {
        // Label

        when (formInput.label) {
            is String -> Text(
                modifier = Modifier
                    .fillMaxWidth(0.4f),
                text = formInput.label,
                fontWeight = FontWeight.SemiBold
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
                )
            }

            is DecimalInput -> if (formInput.inputType2 is DecimalInput) {
                FormInputCreatorDoublePrice(
                    textInput1 = formInput.inputType,
                    textInput2 = formInput.inputType2,
                    taxRate = (formInput.inputType).taxRate,
                    keyboardOption = imeAction,
                    formActions = formActions,
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
                ForwardInputCreatorGoForward(
                    formInput.inputType
                )
        }
    }
}

class FormInput(
    val label: Any,
    val inputType: Any,
    val inputType2: Any? = null, // Used for DoubleInputCreator
    val pageElement: ScreenElement,
)

class TextInput(
    val text: TextFieldValue? = null,
    val placeholder: String? = null,
    val onValueChange: (TextFieldValue) -> Unit = {},
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