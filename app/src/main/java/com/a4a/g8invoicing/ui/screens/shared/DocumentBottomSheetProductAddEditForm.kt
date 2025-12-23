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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.DecimalInput
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ForwardElement
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.DocumentProductState
import java.math.BigDecimal

// It's a bit different from ProductAddEditForm: user can choose the quantity; there's some styling diff;
// and it's not possible to add several prices from here.
@Composable
fun DocumentBottomSheetProductAddEditForm(
    documentProduct: DocumentProductState,
    bottomFormOnValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickForward: (ScreenElement) -> Unit,
    showFullScreenText: (ScreenElement) -> Unit
) {
    val localFocusManager = LocalFocusManager.current

    // Hoist stringResource calls to the composable scope
    val productNameLabel = stringResource(id = R.string.product_name)
    val productNamePlaceholder = stringResource(id = R.string.product_name_input)
    val quantityLabel = stringResource(id = R.string.document_product_quantity)
    val quantityPlaceholder = stringResource(id = R.string.document_product_quantity_input)
    val descriptionLabel = stringResource(id = R.string.product_description)
    val descriptionPlaceholder = stringResource(id = R.string.product_description_input)
    val unitLabel = stringResource(id = R.string.product_unit)
    val unitPlaceholder = stringResource(id = R.string.product_unit_input)
    val taxLabel = stringResource(id = R.string.product_tax)
    val priceLabel = stringResource(id = R.string.product_price)
    val pricePlaceholder = stringResource(id = R.string.product_price_input)


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
                    start = 12.dp,
                    end = 12.dp
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)

        ) {

            // Create the list with all fields
            val inputList = remember(
                documentProduct.name,
                documentProduct.quantity,
                documentProduct.description,
                documentProduct.unit,
                documentProduct.taxRate,
                documentProduct.priceWithoutTax,
                documentProduct.priceWithTax
            ) {
                listOfNotNull(
                    FormInput(
                        label = productNameLabel,
                        inputType = TextInput(
                            text = documentProduct.name,
                            placeholder = productNamePlaceholder,
                            onValueChange = {
                                bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_NAME, it)
                            },
                            displayFullScreenIcon = true
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_NAME
                    ),
                    FormInput(
                        label = quantityLabel,
                        inputType = DecimalInput(
                            text = documentProduct.quantity
                                .toString(),
                            placeholder = quantityPlaceholder,
                            onValueChange = {
                                bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_QUANTITY, it)
                            },
                            keyboardType = KeyboardType.Decimal
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_QUANTITY
                    ),
                    FormInput(
                        label = descriptionLabel,
                        inputType = TextInput(
                            text = documentProduct.description ?: TextFieldValue(""),
                            placeholder = descriptionPlaceholder,
                            onValueChange = {
                                bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION, it)
                            },
                            displayFullScreenIcon = true
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION
                    ),
                    FormInput(
                        label = unitLabel,
                        inputType = TextInput(
                            text = documentProduct.unit ?: TextFieldValue(""),
                            placeholder = unitPlaceholder,
                            onValueChange = {
                                bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_UNIT, it)
                            }
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_UNIT
                    ),
                    FormInput(
                        label = taxLabel,
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
                        label = priceLabel,
                        inputType = DecimalInput(
                            text = (documentProduct.priceWithoutTax ?: "").toString(),
                            taxRate = documentProduct.taxRate,
                            placeholder = pricePlaceholder,
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = {
                                bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_PRICE_WITHOUT_TAX, it)
                            }
                        ),
                        inputType2 = DecimalInput(
                            text = (documentProduct.priceWithTax ?: "").toString(),
                            placeholder = documentProduct.taxRate?.let {
                                (BigDecimal(3) + BigDecimal(3) * it / BigDecimal(100)).toString()
                            } ?: "",
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = {
                                bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_PRICE_WITH_TAX, it)
                            }
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_PRICE_WITHOUT_TAX
                    ),
                )
            }

            // Create the UI with list items
            FormUI(
                inputList = inputList,
                localFocusManager = localFocusManager,
                onClickForward = onClickForward,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                errors = documentProduct.errors,
                onClickExpandFullScreen = {
                    showFullScreenText(it)
                }
            )
        }
    }
}

