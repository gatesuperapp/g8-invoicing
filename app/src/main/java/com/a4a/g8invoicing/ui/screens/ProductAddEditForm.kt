package com.a4a.g8invoicing.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
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
import com.a4a.g8invoicing.ui.shared.ListPicker
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.ClientRef
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.theme.ColorDarkGray
import com.a4a.g8invoicing.ui.theme.callForActions
import com.ionspin.kotlin.bignum.decimal.BigDecimal

@Composable
fun ProductAddEditForm(
    product: ProductState,
    onValueChange: (ScreenElement, Any, String?) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickForward: (ScreenElement) -> Unit,
    onClickDeletePrice: (priceId: String) -> Unit,
    onClickAddPrice: () -> Unit,
    onClickSelectClients: (priceId: String) -> Unit,
    onRemoveClient: (priceId: String, clientId: Int) -> Unit,
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
    val productPriceDefaultLabel = stringResource(id = R.string.product_price_default)
    val productPricePlaceholder = stringResource(id = R.string.product_price_input)
    val productPriceClient = stringResource(id = R.string.product_price_client)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .padding(top = 110.dp, bottom = 60.dp)
            .imePadding() // Ceci fait remonter l'écran quand le clavier s'ouvre
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                })
            }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column {
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

                    val hasAdditionalPrices = !product.additionalPrices.isNullOrEmpty()
                    val inputList = remember(
                        product.name,
                        product.description,
                        product.unit,
                        product.taxRate,
                        product.defaultPriceWithoutTax,
                        product.defaultPriceWithTax,
                        hasAdditionalPrices
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
                                            "${taxRate.toPlainString()}%"
                                        }
                                    },
                                ),
                                pageElement = ScreenElement.PRODUCT_TAX_RATE
                            ),
                            FormInput(
                                label = if (hasAdditionalPrices) productPriceDefaultLabel else productPriceLabel,
                                inputType = DecimalInput(
                                    text = product.defaultPriceWithoutTax?.toPlainString() ?: "",
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
                                    text = product.defaultPriceWithTax?.toPlainString() ?: "",
                                    placeholder = product.taxRate?.let {
                                        (BigDecimal.fromInt(3) + BigDecimal.fromInt(3) * it / BigDecimal.fromInt(100)).toPlainString()
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

                    FormUI(
                        inputList = inputList,
                        localFocusManager = localFocusManager,
                        onClickForward = onClickForward,
                        placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                        errors = product.errors
                    )
                }

                // Afficher "Ajouter un prix" seulement s'il n'y a pas de prix additionnels
                if (product.additionalPrices.isNullOrEmpty()) {
                    Spacer(Modifier.padding(bottom = 6.dp))
                    AddPriceButton(
                        onClick = onClickAddPrice,
                        bottomPadding = 16.dp
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator()
            }
        }

        // Section pour les prix additionnels
        product.additionalPrices?.let { prices ->
            prices.forEachIndexed { index, currentPrice ->
                // Espacement entre les blocs de prix additionnels
                if (index > 0) {
                    Spacer(Modifier.padding(bottom = 12.dp))
                } else {
                    Spacer(Modifier.padding(bottom = 16.dp))
                }

                key(currentPrice.idStr) {
                    Column(
                        modifier = Modifier
                            .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                    ) {

                        // 🗑️ Suppression du prix - padding réduit
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            DeletePriceButton {
                                onClickDeletePrice(currentPrice.idStr)
                            }
                        }

                        val priceInputList = remember(
                            currentPrice.clients,
                            currentPrice.priceWithoutTax,
                            currentPrice.priceWithTax,
                            currentPrice.idStr,
                            product.taxRate
                        ) {
                            listOf(
                                FormInput(
                                    label = productPriceLabel,
                                    inputType = DecimalInput(
                                        text = currentPrice.priceWithoutTax?.toPlainString() ?: "",
                                        taxRate = product.taxRate,
                                        placeholder = productPricePlaceholder,
                                        keyboardType = KeyboardType.Decimal,
                                        onValueChange = {
                                            onValueChange(
                                                ScreenElement.PRODUCT_OTHER_PRICE_WITHOUT_TAX,
                                                it,
                                                currentPrice.idStr
                                            )
                                        }
                                    ),
                                    inputType2 = DecimalInput(
                                        text = currentPrice.priceWithTax?.toPlainString() ?: "",
                                        placeholder = "Avec taxe",
                                        keyboardType = KeyboardType.Decimal,
                                        onValueChange = {
                                            onValueChange(
                                                ScreenElement.PRODUCT_OTHER_PRICE_WITH_TAX,
                                                it,
                                                currentPrice.idStr
                                            )
                                        }
                                    ),
                                    pageElement = ScreenElement.PRODUCT_OTHER_PRICE_WITHOUT_TAX
                                ),
                                FormInput(
                                    label = productPriceClient,
                                    inputType = ListPicker(
                                        selectedItems = currentPrice.clients,
                                        onClick = {
                                            onClickSelectClients(currentPrice.idStr)
                                        },
                                        onRemoveItem = {
                                            onRemoveClient(currentPrice.idStr, it)
                                        }
                                    ),
                                    pageElement = ScreenElement.PRODUCT_OTHER_PRICE_CLIENTS,
                                    extraId = currentPrice.idStr
                                )
                            )
                        }

                        Column(
                            modifier = Modifier
                                .offset(y = (-4).dp)
                        ) {
                            FormUI(
                                inputList = priceInputList,
                                localFocusManager = localFocusManager,
                                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                                onClickOpenClientSelection = onClickSelectClients,
                                errors = product.errors
                            )
                        }
                    }
                }
            }

            // Afficher "Ajouter un prix" après tous les prix additionnels
            Spacer(Modifier.padding(bottom = 6.dp))
            AddPriceButton(
                onClick = onClickAddPrice,
                bottomPadding = 16.dp
            )
        }
    }
}

@Composable
fun AddPriceButton(onClick: () -> Unit, bottomPadding: Dp = 0.dp) {
    Box(
        modifier = Modifier
            .padding(start = 4.dp, top = 4.dp, bottom = bottomPadding)
            .background(
                color = Color(0xFFE8E8E8), // Gris clair
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(enabled = true) {
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            style = MaterialTheme.typography.callForActions,
            text = AnnotatedString(Strings.get(R.string.product_add_price)),
        )
    }
}


@Composable
fun DeletePriceButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(end = 4.dp, top = 4.dp)
            .size(14.dp)
    ) {
        Icon(
            modifier = Modifier
                .size(22.dp),
            imageVector = Icons.Outlined.Delete,
            tint = ColorDarkGray,
            contentDescription = Strings.get(R.string.product_delete_price)
        )
    }
}