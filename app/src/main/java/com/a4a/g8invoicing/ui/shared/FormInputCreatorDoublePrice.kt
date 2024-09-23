package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import java.math.BigDecimal
import java.text.DecimalFormat

// Used in forms with a product or documentProduct
// for changing alternatively Price & Price including tax
@Composable
fun FormInputCreatorDoublePrice(
    textInput1: DecimalInput,
    textInput2: DecimalInput,
    taxRate: BigDecimal? = null,
    keyboardOption: ImeAction,
    formActions: KeyboardActions,
    focusRequester: FocusRequester?,
) {
    // Stateful : mutable values are remembered here - avoids recomposing all form
    var text1 by remember { mutableStateOf(textInput1.text) }
    var text2 by remember { mutableStateOf(textInput2.text) }

    // Rounds after 2 numbers after decimal (2,15 instead of 2,14678)
    val formatWithTwoDecimals = DecimalFormat("#.##")

    // Rules to format the display, as Compose don't do it alone :
    // for instance, allow the input of only 1 separator (2,15 and not 2,15.23,23)
    val decimalFormatter = DecimalFormatter()

    val keyboardController = LocalSoftwareKeyboardController.current
    var customModifier = Modifier
        .onFocusChanged {
            if (it.isFocused) {
                keyboardController?.show()
            }
        }
        .fillMaxWidth()
        .padding(bottom = 3.dp)

    focusRequester?.let {
        customModifier = customModifier.then(Modifier.focusRequester(focusRequester))
    }

    fun updateFieldsWithCalculatedValues(newValue: String, isFirstField: Boolean) {
        taxRate?.let { tax ->
            if (isFirstField) {
                text2 = newValue.toBigDecimalOrNull()?.let {
                    formatWithTwoDecimals.format(it - it * tax / BigDecimal(100)).toString()
                }?.toString() ?: ""
            } else {
                text1 = newValue.toBigDecimalOrNull()?.let {
                    formatWithTwoDecimals.format(it + it * tax / BigDecimal(100)).toString()
                }?.toString() ?: ""
            }
        }
    }

    // If the product has a tax rate, we display price with tax & price without tax
    if (taxRate != null) {
        Column(
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxWidth(0.4f),
            // horizontalAlignment = Alignment.End
        ) {
            Text(
                modifier = Modifier
                    .padding(bottom = 3.dp),
                text = stringResource(id = R.string.product_price_with_tax),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.product_price_without_tax),
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    Column() {
        // First basic text field
        // If no tax rate has been chosen:
        //      it's the only displayed field & it displays the price
        // If a tax rate has been chosen :
        //      it displays price WITH tax (ex: TTC = 4.4)

        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {

            BasicTextField(
                maxLines = 1,
                modifier = customModifier,
                value = text1?.replace(".", ",") ?: "",
                onValueChange = {
                    text1 = decimalFormatter.cleanup(it)
                    updateFieldsWithCalculatedValues(it.replace(",", "."), true)
                    textInput1.onValueChange(it)
                },
                textStyle = LocalTextStyle.current,
                keyboardOptions = KeyboardOptions(
                    imeAction = keyboardOption,
                    keyboardType = textInput1.keyboardType
                ),
                keyboardActions = formActions,


                ) { innerTextField ->
                val interactionSource = remember { MutableInteractionSource() }
                FormInputDefaultStyle(
                    text1,
                    innerTextField,
                    textInput1.placeholder,
                    interactionSource
                )
            }

            // Second basic text field
            // If no tax rate has been chosen:
            // Not displayed
            // If a tax rate has been chosen :
            // it's the first field and it displays price WITHOUT tax (HT = 4)
            if (taxRate != null) {
                BasicTextField(
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = text2?.replace(".", ",") ?: "",
                    onValueChange = {
                        text2 = decimalFormatter.cleanup(it)
                        updateFieldsWithCalculatedValues(it.replace(",", "."), false)
                        textInput1.onValueChange(text1 ?: "")
                    },
                    textStyle = LocalTextStyle.current,
                    keyboardOptions = KeyboardOptions(
                        imeAction = keyboardOption,
                        keyboardType = textInput2.keyboardType
                    ),
                    keyboardActions = formActions
                ) { innerTextField ->
                    val interactionSource = remember { MutableInteractionSource() }
                    FormInputDefaultStyle(
                        text1,
                        innerTextField,
                        textInput2.placeholder,
                        interactionSource
                    )
                }
            }
        }
    }
}