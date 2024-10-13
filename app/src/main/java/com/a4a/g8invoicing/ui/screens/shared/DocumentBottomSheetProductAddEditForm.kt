package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.text.input.TextFieldValue
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

// It's a bit of a duplicate of ProductAddEditForm (the quantity has been added & there's some styling diff),
// but needed as in the documents we must use "DocumentProductState" instead of "ProductState"
// so the product is not deleted from the doc when deleted from the product list.
@Composable
fun DocumentProductAddEditForm(
    documentProduct: DocumentProductState,
    bottomFormOnValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickForward: (ScreenElement) -> Unit,
) {
    val localFocusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxHeight(0.5f)
            .background(Color.LightGray.copy(alpha = 0.4f))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .imePadding()
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
                    top = 18.dp,
                    bottom = 18.dp,
                    start = 12.dp,
                    end = 12.dp
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)

        ) {
            var priceWithoutTax: BigDecimal? = null

            documentProduct.let {productState ->
                productState.taxRate?.let {taxRate ->
                    productState.priceWithTax?.let { priceWithTax ->
                        priceWithoutTax = BigDecimal(priceWithTax.toDouble() / (1.0 + taxRate.toDouble() / 100.0))
                    }
                }
            }
            // Create the list with all fields
            val inputList = listOfNotNull(
                FormInput(
                    label = stringResource(id = R.string.product_name),
                    inputType = TextInput(
                        text = documentProduct.name,
                        placeholder = stringResource(id = R.string.product_name_input),
                        onValueChange = {
                            bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_NAME, it)
                        }
                    ),
                    pageElement = ScreenElement.DOCUMENT_PRODUCT_NAME
                ),
                FormInput(
                    label = stringResource(id = R.string.document_product_quantity),
                    inputType = DecimalInput(
                        text = documentProduct.quantity
                            .toString(),
                        placeholder = stringResource(id = R.string.document_product_quantity_input),
                        onValueChange = {
                            bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_QUANTITY, it)
                        },
                        keyboardType = KeyboardType.Decimal
                    ),
                    pageElement = ScreenElement.DOCUMENT_PRODUCT_QUANTITY
                ),
                FormInput(
                    label = stringResource(id = R.string.product_description),
                    inputType = TextInput(
                        text = documentProduct.description ?: TextFieldValue(""),
                        placeholder = stringResource(id = R.string.product_description_input),
                        onValueChange = {
                            bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION, it)
                        }
                    ),
                    pageElement = ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION
                ),
                FormInput(
                    label = stringResource(
                        id = R.string.product_price
                    ),
                    inputType = DecimalInput(
                        text = (documentProduct.priceWithTax?.setScale(2, RoundingMode.HALF_UP) ?: "")
                            .toString(),
                        taxRate = documentProduct.taxRate,
                        placeholder = stringResource(id = R.string.product_price_input),
                        onValueChange = {
                            bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_FINAL_PRICE, it)
                        },
                        keyboardType = KeyboardType.Decimal
                    ),
                    inputType2 = DecimalInput(
                        text = (priceWithoutTax?.setScale(2, RoundingMode.HALF_UP) ?: "")
                            .toString(),
                        placeholder = documentProduct.taxRate?.let {
                            (BigDecimal(3) + BigDecimal(3) * it / BigDecimal(100)).toString()
                        } ?: "",
                        keyboardType = KeyboardType.Decimal
                    ),
                    pageElement = ScreenElement.DOCUMENT_PRODUCT_PRICE
                ),
                FormInput(
                    label = stringResource(id = R.string.product_tax),
                    inputType = ForwardElement(
                        text = documentProduct.taxRate.let { taxRate ->
                            if (taxRate == null) {
                                "-"
                            } else {
                                "$taxRate%"
                            }
                        },
                    ),
                    pageElement = ScreenElement.DOCUMENT_PRODUCT_TAX_RATE
                ),
                FormInput(
                    label = stringResource(id = R.string.product_unit),
                    inputType = TextInput(
                        text = documentProduct.unit ?: TextFieldValue(""),
                        placeholder = stringResource(id = R.string.product_unit_input),
                        onValueChange = {
                            bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_UNIT, it)
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
                errors = documentProduct.errors
            )
        }
    }
}


