package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.DecimalInput
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ForwardElement
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.ProductState
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun ProductAddEditForm(
    product: ProductState,
    onValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickForward: (ScreenElement) -> Unit,
) {
    val localFocusManager = LocalFocusManager.current

    Column( // This column is scrollable and set background
        modifier = Modifier
            .background(Color.LightGray.copy(alpha = 0.4f))
            .padding(12.dp)
            .padding(top = 80.dp)
            .imePadding()
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                .fillMaxWidth()
                .padding(
                    top = 6.dp,
                    bottom = 6.dp
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            var priceWithoutTax: BigDecimal
            product.let {
                priceWithoutTax =
                    it.priceWithTax - it.priceWithTax * it.taxRate / BigDecimal(100)
            }
            // Create the list with all fields
            val inputList = listOfNotNull(
                FormInput(
                    label = stringResource(id = R.string.product_name),
                    inputType = TextInput(
                        text = product.name,
                        placeholder = stringResource(id = R.string.product_name_input),
                        onValueChange = {
                            onValueChange(ScreenElement.PRODUCT_NAME, it)
                        }
                    ),
                    pageElement = ScreenElement.PRODUCT_NAME
                ),
                FormInput(
                    label = stringResource(id = R.string.product_description),
                    inputType = TextInput(
                        text = product.description,
                        placeholder = stringResource(id = R.string.product_description_input),
                        onValueChange = {
                            onValueChange(ScreenElement.PRODUCT_DESCRIPTION, it)
                        }
                    ),
                    pageElement = ScreenElement.PRODUCT_DESCRIPTION
                ),
                FormInput(
                    label = stringResource(
                        id = R.string.product_price
                    ),
                    inputType = DecimalInput(
                        text = product.priceWithTax.setScale(2, RoundingMode.HALF_UP)
                            .toString(),
                        taxRate = product.taxRate,
                        placeholder = stringResource(id = R.string.product_price_input),
                        onValueChange = {
                            onValueChange(ScreenElement.PRODUCT_FINAL_PRICE, it)
                        },
                        keyboardType = KeyboardType.Decimal
                    ),
                    inputType2 = DecimalInput(
                        text = priceWithoutTax.setScale(2, RoundingMode.HALF_UP).toString(),
                        placeholder = product.taxRate.let {
                            (BigDecimal(3) + BigDecimal(3) * it / BigDecimal(100)).toString()
                        },
                        keyboardType = KeyboardType.Decimal
                    ),
                    pageElement = ScreenElement.PRODUCT_PRICE
                ),
                FormInput(
                    label = stringResource(id = R.string.product_tax),
                    inputType = ForwardElement(
                        text = product.taxRate.let { taxRate ->
                            if (taxRate == BigDecimal(0)) {
                                "-"
                            } else {
                                "$taxRate%"
                            }
                        } ?: "-",
                    ),
                    pageElement = ScreenElement.PRODUCT_TAX_RATE
                ),
                FormInput(
                    label = stringResource(id = R.string.product_unit),
                    inputType = TextInput(
                        text = product.unit,
                        placeholder = stringResource(id = R.string.product_unit_input),
                        onValueChange = {
                            onValueChange(ScreenElement.PRODUCT_UNIT, it)
                        }
                    ),
                    pageElement = ScreenElement.PRODUCT_UNIT
                ),
            )

            // Create the UI with list items
            FormUI(
                inputList = inputList,
                localFocusManager = localFocusManager,
                onClickForward = onClickForward,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText
            )
        }
    }
}