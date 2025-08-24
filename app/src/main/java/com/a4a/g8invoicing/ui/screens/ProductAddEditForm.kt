package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.shared.DecimalInput
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ForwardElement
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.theme.ColorDarkGray
import com.a4a.g8invoicing.ui.theme.callForActions
import java.math.BigDecimal

@Composable
fun ProductAddEditForm(
    product: ProductState,
    onValueChange: (ScreenElement, Any, String?) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickForward: (ScreenElement) -> Unit,
    onClickDeletePrice: (priceId: String) -> Unit,
    onClickAddPrice: () -> Unit,
    isLoading: Boolean,
) {
    val localFocusManager = LocalFocusManager.current

    // Hoist stringResource calls
    val productNameLabel = stringResource(id = R.string.product_name)
    val productNamePlaceholder = stringResource(id = R.string.product_name_input)
    val productDescriptionLabel = stringResource(id = R.string.product_description)
    val productDescriptionPlaceholder = stringResource(id = R.string.product_description_input)
    val productUnitLabel = stringResource(id = R.string.product_unit)
    val productUnitPlaceholder = stringResource(id = R.string.product_unit_input)
    val productTaxLabel = stringResource(id = R.string.product_tax)
    val productPriceLabel = stringResource(id = R.string.product_price)
    val productPricePlaceholder = stringResource(id = R.string.product_price_input)

    Column( // This column is scrollable and set background
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray.copy(alpha = 0.4f))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .padding(top = 110.dp, bottom = 60.dp)
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                })
            }
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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
                val inputList = remember(
                    product.name,
                    product.description,
                    product.unit,
                    product.taxRate,
                    product.defaultPriceWithoutTax,
                    product.defaultPriceWithTax
                ) {
                    listOfNotNull(
                        FormInput(
                            label = productNameLabel,
                            inputType = TextInput(
                                text = product.name,
                                placeholder = productNamePlaceholder,
                                onValueChange = {
                                    onValueChange(ScreenElement.PRODUCT_NAME, it, null)
                                }
                            ),
                            pageElement = ScreenElement.PRODUCT_NAME
                        ),
                        FormInput(
                            label = productDescriptionLabel,
                            inputType = TextInput(
                                text = product.description,
                                placeholder = productDescriptionPlaceholder,
                                onValueChange = {
                                    onValueChange(ScreenElement.PRODUCT_DESCRIPTION, it, null)
                                }
                            ),
                            pageElement = ScreenElement.PRODUCT_DESCRIPTION
                        ),
                        FormInput(
                            label = productUnitLabel,
                            inputType = TextInput(
                                text = product.unit,
                                placeholder = productUnitPlaceholder,
                                onValueChange = {
                                    onValueChange(ScreenElement.PRODUCT_UNIT, it, null)
                                }
                            ),
                            pageElement = ScreenElement.PRODUCT_UNIT
                        ),
                        FormInput(
                            label = productTaxLabel,
                            inputType = ForwardElement(
                                text = product.taxRate.let { taxRate ->
                                    if (taxRate == null) {
                                        "-"
                                    } else {
                                        "$taxRate%"
                                    }
                                },
                            ),
                            pageElement = ScreenElement.PRODUCT_TAX_RATE
                        ),
                        FormInput(
                            label = productPriceLabel,
                            inputType = DecimalInput(
                                text = (product.defaultPriceWithoutTax ?: "").toString(),
                                taxRate = product.taxRate,
                                placeholder = productPricePlaceholder,
                                keyboardType = KeyboardType.Decimal,
                                onValueChange = {
                                    onValueChange(
                                        ScreenElement.PRODUCT_DEFAULT_PRICE_WITHOUT_TAX,
                                        it, null
                                    )
                                }
                            ),
                            inputType2 = DecimalInput(
                                text = (product.defaultPriceWithTax ?: "").toString(),
                                placeholder = product.taxRate?.let {
                                    (BigDecimal(3) + BigDecimal(3) * it / BigDecimal(100)).toString()
                                } ?: "",
                                keyboardType = KeyboardType.Decimal,
                                onValueChange = {
                                    onValueChange(
                                        ScreenElement.PRODUCT_DEFAULT_PRICE_WITH_TAX,
                                        it,
                                        null
                                    )
                                }
                            ),
                            pageElement = ScreenElement.PRODUCT_DEFAULT_PRICE_WITHOUT_TAX
                        )
                    )
                }

                // Create the UI with list items
                FormUI(
                    inputList = inputList,
                    localFocusManager = localFocusManager,
                    onClickForward = onClickForward,
                    placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                    errors = product.errors,
                )
            }

            if (product.additionalPrices.isNullOrEmpty()) {
                Spacer(Modifier.padding(bottom = 6.dp))
                // Add price button
                AddPriceButton(
                    onClick = onClickAddPrice,
                    bottomPadding = 16.dp
                )
            } else {
                Spacer(Modifier.padding(bottom = 16.dp))
            }
        }

        // Section pour les prix
        product.additionalPrices?.let { prices ->
            prices.forEachIndexed { index, currentPrice ->
                key(currentPrice.idStr) {
                    Column(
                        modifier = Modifier
                            .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                            .padding(top = 4.dp)
                    ) {

                        val priceInputList = remember(currentPrice, product.taxRate) {
                            listOf(
                                FormInput(
                                    label = productPriceLabel,
                                    inputType = DecimalInput(
                                        text = (currentPrice.priceWithoutTax ?: "").toString(),
                                        taxRate = product.taxRate,
                                        placeholder = productPricePlaceholder,
                                        keyboardType = KeyboardType.Decimal,
                                        onValueChange = {
                                            // Passez l'ID du prix et le bon ScreenElement/PriceScreenElement
                                            onValueChange(
                                                ScreenElement.PRODUCT_OTHER_PRICE_WITHOUT_TAX,
                                                it,
                                                currentPrice.idStr
                                            )
                                        }
                                    ),
                                    inputType2 = DecimalInput(
                                        text = (currentPrice.priceWithTax ?: "").toString(),
                                        placeholder = "Avec taxe", // Adaptez le placeholder
                                        keyboardType = KeyboardType.Decimal,
                                        onValueChange = {
                                            onValueChange(
                                                ScreenElement.PRODUCT_OTHER_PRICE_WITH_TAX,
                                                it,
                                                currentPrice.idStr
                                            )
                                        }
                                    ),
                                    // Adaptez 'pageElement' si nécessaire, ou la logique dans FormUI
                                    pageElement = ScreenElement.PRODUCT_OTHER_PRICE_WITHOUT_TAX // Peut nécessiter une adaptation
                                )
                            )
                        }


                        // Create the UI with list items
                        FormUI(
                            inputList = priceInputList,
                            localFocusManager = localFocusManager,
                            placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                            errors = product.errors,
                        )
                    }

                    // Add buttons to add or delete price
                    Row() {
                        // Show AddPriceButton only for the last item in the list
                        if (index == prices.size - 1) {
                            AddPriceButton(
                                onClick = onClickAddPrice,
                            )
                        }

                        Spacer(Modifier.weight(1F))
                        DeletePriceButton(
                            onClick = { onClickDeletePrice(currentPrice.idStr) })
                    }
                }
            }
        }
    }
}


@Composable
fun AddPriceButton(onClick: () -> Unit, bottomPadding: Dp = 0.dp) {
    Text(
        style = MaterialTheme.typography.callForActions,
        modifier = Modifier
            .padding(start = 4.dp, top = 4.dp, bottom = bottomPadding)
            .clickable(enabled = true) {
                onClick()
            },
        text = AnnotatedString(Strings.get(R.string.product_add_price)),
    )
}

@Composable
fun DeletePriceButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(end = 4.dp, top = 4.dp, bottom = 16.dp)
            .size(14.dp)
    ) {
        Icon(
            modifier = Modifier
                .size(22.dp),
            imageVector = Icons.Outlined.DeleteOutline,
            tint = ColorDarkGray,
            contentDescription = Strings.get(R.string.product_delete_price)
        )
    }
}