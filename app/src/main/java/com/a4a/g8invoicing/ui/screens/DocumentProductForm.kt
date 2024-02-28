package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.shared.DecimalInput
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ForwardElement
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import java.math.BigDecimal
import java.math.RoundingMode

// Can be Product or DocumentProduct. Names are "Product" to simplify.
@Composable
fun DocumentProductForm(
    product: DocumentProductState?,
    onValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickForward: (ScreenElement) -> Unit,
) {
    val localFocusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth()
            .imePadding()
            .padding(
                top = 30.dp,
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                })
            }

    ) {
        Column(
            // This column adds padding and contains other blocks/columns
            modifier = Modifier
                .padding(12.dp),
            //.fillMaxHeight(0.9f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        )
        {
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
                // Create the list with all fields
                val inputList = listOfNotNull(
                    FormInput(
                        label = stringResource(id = R.string.product_name),
                        inputType = TextInput(
                            text = product?.name,
                            placeholder = stringResource(id = R.string.product_name_input),
                            onValueChange = {
                                onValueChange(ScreenElement.DOCUMENT_PRODUCT_NAME, it)
                            }
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_NAME
                    ),
                    FormInput(
                        label = stringResource(id = R.string.document_product_quantity),
                        inputType = DecimalInput(
                            text = product?.quantity.toString(),
                            placeholder = stringResource(id = R.string.document_product_quantity_input),
                            onValueChange = {
                                onValueChange(ScreenElement.DOCUMENT_PRODUCT_QUANTITY, it)
                            },
                            keyboardType = KeyboardType.Decimal
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_QUANTITY
                    ),
                    FormInput(
                        label = stringResource(id = R.string.product_description),
                        inputType = TextInput(
                            text = product?.description,
                            placeholder = stringResource(id = R.string.product_description_input),
                            onValueChange = {
                                onValueChange(ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION, it)
                            }
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION
                    ),
                    FormInput(
                        label = stringResource(
                            id = R.string.product_price
                        ),
                        inputType = DecimalInput(
                            text = product?.finalPrice?.setScale(2, RoundingMode.HALF_UP)
                                .toString(),
                            taxRate = product?.taxRate,
                            placeholder = stringResource(id = R.string.product_price_input),
                            onValueChange = {
                                val finalPrice = it.toBigDecimalOrNull()
                                if (finalPrice == null) { // Empty input
                                    product?.priceWithoutTax = BigDecimal(0)
                                    product?.finalPrice = BigDecimal(0)
                                } else {
                                    product?.finalPrice = finalPrice
                                    product?.taxRate?.let {
                                        product.priceWithoutTax =
                                            finalPrice - finalPrice * it / BigDecimal(100)
                                    }
                                }
                            },
                            keyboardType = KeyboardType.Decimal
                        ),
                        inputType2 = DecimalInput(
                            text = product?.priceWithoutTax?.setScale(2, RoundingMode.HALF_UP)
                                .toString(),
                            placeholder = product?.taxRate?.let {
                                (BigDecimal(3) + BigDecimal(3) * it / BigDecimal(
                                    100
                                )
                                        ).toString()
                            } ?: "",
                            onValueChange = {
                                val priceWithoutTax = it.toBigDecimalOrNull()
                                if (priceWithoutTax == null) { // Empty input
                                    product?.priceWithoutTax = BigDecimal(0)
                                    product?.finalPrice = BigDecimal(0)
                                } else {
                                    product?.priceWithoutTax = priceWithoutTax
                                    product?.taxRate?.let {
                                        product.finalPrice =
                                            priceWithoutTax + priceWithoutTax * it / BigDecimal(
                                                100
                                            )
                                    }
                                }
                            },
                            keyboardType = KeyboardType.Decimal
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_PRICE
                    ),
                    FormInput(
                        label = stringResource(id = R.string.product_tax),
                        inputType = ForwardElement(
                            text = product?.taxRate?.let { taxRate ->
                                if (taxRate == BigDecimal(0)) {
                                    "-"
                                } else {
                                    "$taxRate%"
                                }
                            } ?: "-",
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_TAX_RATE
                    ),
                    FormInput(
                        label = stringResource(id = R.string.product_unit),
                        inputType = TextInput(
                            text = product?.unit,
                            placeholder = stringResource(id = R.string.product_unit_input),
                            onValueChange = {
                                onValueChange(ScreenElement.PRODUCT_UNIT, it)
                            }
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_UNIT
                    ),
                )

                // Create the UI with list items
                FormUI(
                    inputList = inputList,
                    localFocusManager = localFocusManager,
                    onClickForward = onClickForward,
                    placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                    isFormStateful = false
                )
            }
        }
    }
}


