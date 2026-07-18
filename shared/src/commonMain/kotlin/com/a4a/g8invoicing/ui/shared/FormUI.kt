package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.states.ClientRef
import com.a4a.g8invoicing.ui.theme.inputLabel
import com.ionspin.kotlin.bignum.decimal.BigDecimal

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


    // Row Y bounds tracked by each wrapper Box below; used by
    // absorbAndDispatchTap to focus the closest field on an unconsumed tap.
    // In release APKs, .clickable on Row / wrapper Column silently fails to
    // fire, so tap-to-focus has to be routed via a single pointerInput at this
    // level with explicit Y dispatch — see absorbAndDispatchTap for the why.
    val rowYRanges = remember { mutableStateListOf<Triple<ScreenElement, Int, Int>>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                bottom = 8.dp
            )
            .absorbAndDispatchTap("formui") { pos ->
                val y = pos.y.toInt()
                val exact = rowYRanges.firstOrNull { y in it.second..it.third }
                val hit = exact ?: rowYRanges.minByOrNull {
                    minOf(kotlin.math.abs(y - it.second), kotlin.math.abs(y - it.third))
                }
                if (hit != null) {
                    val hitElement = hit.first
                    val input = inputList.firstOrNull { it.pageElement == hitElement }
                    if (input?.inputType is TextInput) {
                        placeCursorAtTheEndOfText(hitElement)
                    }
                    focusRequesters.firstOrNull { it.first == hitElement }?.second?.requestFocus()
                }
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

            Box(modifier = Modifier.onGloballyPositioned { coords ->
                val bounds = coords.boundsInParent()
                val top = bounds.top.toInt()
                val bottom = bounds.bottom.toInt()
                val existing = rowYRanges.indexOfFirst { it.first == input.pageElement }
                if (existing >= 0) {
                    rowYRanges[existing] = Triple(input.pageElement, top, bottom)
                } else {
                    rowYRanges.add(Triple(input.pageElement, top, bottom))
                }
            }) {
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
    Column {
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
            is String -> {
                if (formInput.labelInfoTooltip != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (formInput.isMandatory) "${formInput.label} *" else formInput.label,
                            style = MaterialTheme.typography.inputLabel,
                        )
                        Spacer(Modifier.weight(1f))
                        // Pastille "?" alignée au bord droit de la colonne label — les
                        // lignes Code unité / Type ont ainsi leur pastille en colonne.
                        // Se supprime après lecture (persist via Settings au close du modal).
                        InfoTooltipButton(
                            title = formInput.labelInfoTooltip.title,
                            content = formInput.labelInfoTooltip.content,
                            contentDescription = formInput.labelInfoTooltip.contentDescription,
                            persistenceKey = formInput.labelInfoTooltip.persistenceKey,
                        )
                    }
                } else {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .padding(end = 12.dp),
                        text = if (formInput.isMandatory) "${formInput.label} *" else formInput.label,
                        style = MaterialTheme.typography.inputLabel
                    )
                }
            }

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

            // Used for email list with chips
            is EmailListInput ->
                FormInputCreatorEmailList(
                    input = formInput.inputType,
                    focusRequester = focusRequester,
                )
        }
    }
}

class FormInput(
    val label: Any,
    val inputType: Any,
    val inputType2: Any? = null, // Used for DoubleInputCreator
    val pageElement: ScreenElement,
    val extraId: String? = null,
    val isMandatory: Boolean = false,
    // Optional info tooltip (small ⓘ icon) rendered right after the label. Meant for
    // fields the user might not intuit — e.g. Factur-X metadata that never appears on
    // the PDF. Kept opt-in so most FormInputs stay clean.
    val labelInfoTooltip: LabelInfoTooltip? = null,
)

data class LabelInfoTooltip(
    val title: String,
    val content: String,
    val contentDescription: String,
    // When non-null, the ⓘ disappears permanently after the user has read + dismissed
    // the modal (persisted via Settings). Same key = same one-time discoverability aid.
    val persistenceKey: String? = null,
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
