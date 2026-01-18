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
import com.a4a.g8invoicing.data.util.calculatePriceWithTax
import com.a4a.g8invoicing.data.util.calculatePriceWithoutTax
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import java.text.DecimalFormat

// Used in forms with a product or documentProduct
// for changing alternatively Price & Price including tax
@Composable
fun FormInputCreatorDoublePrice(
    priceWithoutTaxInput: DecimalInput,
    priceWithTaxInput: DecimalInput,
    taxRate: BigDecimal? = null,
    keyboardOption: ImeAction,
    formActions: KeyboardActions,
    focusRequester: FocusRequester?,
) {
    // Stateful : mutable values are remembered here - avoids recomposing all form
    // La clé permet de synchroniser le state interne avec la prop quand elle change
    var textPriceWithoutTax by remember(priceWithoutTaxInput.text) { mutableStateOf(priceWithoutTaxInput.text) }
    var textPriceWithTax by remember(priceWithTaxInput.text) { mutableStateOf(priceWithTaxInput.text) }

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
                textPriceWithTax = newValue.replace(",", ".").toDoubleOrNull()?.let {
                    val value = it / (1.0 + tax.doubleValue(false) / 100.0)
                    formatWithTwoDecimals.format(value).toString()
                }?.toString() ?: ""
            } else {
                textPriceWithoutTax = newValue.replace(",", ".").toDoubleOrNull()?.let { price ->
                    val priceDecimal = BigDecimal.fromDouble(price)
                    formatWithTwoDecimals.format((priceDecimal + priceDecimal * tax / BigDecimal.fromInt(100)).doubleValue(false)).toString()
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
                text = stringResource(id = R.string.product_price_without_tax),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.product_price_with_tax),
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    Column() {
        // First basic text field
        // If no tax rate has been chosen:
        //      it's the only displayed field & it displays the price
        // If a tax rate has been chosen :
        //      it displays price WITHOUT tax (ex: TTC = 4)

        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {

            BasicTextField(
                maxLines = 1,
                modifier = customModifier,
                value = textPriceWithoutTax?.replace(".", ",") ?: "",
                onValueChange = { newValue ->
                    val cleanedValue = decimalFormatter.cleanup(newValue)
                    textPriceWithoutTax = cleanedValue

                    if (cleanedValue.isBlank()) { // Si le champ est vide
                        textPriceWithTax = "" // Vider aussi text2
                    } else {
                        val newPrice = cleanedValue.replace(",", ".").toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }
                        if (newPrice != null && taxRate != null) {
                            textPriceWithTax = calculatePriceWithTax(newPrice, taxRate).toPlainString()
                        } else if (taxRate == null) {
                            // Si pas de taxe, text2 ne devrait pas être affecté ou devrait rester vide
                            // ou vous pouvez le laisser tel quel si text2 n'est pas affiché quand taxRate est null
                        }
                    }

                    priceWithoutTaxInput.onValueChange(textPriceWithoutTax.toString())
                    priceWithTaxInput.onValueChange(textPriceWithTax.toString())
                },
                textStyle = LocalTextStyle.current,
                keyboardOptions = KeyboardOptions(
                    imeAction = keyboardOption,
                    keyboardType = priceWithoutTaxInput.keyboardType
                ),
                keyboardActions = formActions,
            ) { innerTextField ->
                val interactionSource = remember { MutableInteractionSource() }
                FormInputDefaultStyle(
                    textPriceWithoutTax,
                    innerTextField,
                    priceWithoutTaxInput.placeholder,
                    interactionSource
                )
            }

            // Second basic text field
            // If no tax rate has been chosen:
            // Not displayed
            // If a tax rate has been chosen :
            //     it displays price WITH tax (HT = 4.4)
            if (taxRate != null) {
                BasicTextField(
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = textPriceWithTax?.replace(".", ",") ?: "",
                    onValueChange = { newValue ->
                        val cleanedValue = decimalFormatter.cleanup(newValue)
                        textPriceWithTax = cleanedValue // Toujours mettre à jour text2

                        if (cleanedValue.isBlank()) { // Si le champ est vide
                            textPriceWithoutTax = "" // Vider aussi text1
                        } else {
                            val newPrice = cleanedValue.replace(",", ".").toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }
                            if (newPrice != null) { // taxRate est forcément non nul ici à cause du `if (taxRate != null)` externe
                                textPriceWithoutTax = calculatePriceWithoutTax(newPrice, taxRate).toPlainString()
                            }
                        }
                        priceWithoutTaxInput.onValueChange(textPriceWithoutTax ?: "")
                        priceWithTaxInput.onValueChange(textPriceWithTax ?: "")
                    },
                    textStyle = LocalTextStyle.current,
                    keyboardOptions = KeyboardOptions(
                        imeAction = keyboardOption,
                        keyboardType = priceWithTaxInput.keyboardType
                    ),
                    keyboardActions = formActions
                ) { innerTextField ->
                    val interactionSource = remember { MutableInteractionSource() }
                    FormInputDefaultStyle(
                        textPriceWithoutTax,
                        innerTextField,
                        priceWithTaxInput.placeholder,
                        interactionSource
                    )
                }
            }
        }
    }
}