package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.product_add_price
import com.a4a.g8invoicing.shared.resources.product_delete_price
import com.a4a.g8invoicing.shared.resources.product_description
import com.a4a.g8invoicing.shared.resources.product_description_input
import com.a4a.g8invoicing.shared.resources.product_name
import com.a4a.g8invoicing.shared.resources.product_name_input
import com.a4a.g8invoicing.shared.resources.product_price
import com.a4a.g8invoicing.shared.resources.product_price_client
import com.a4a.g8invoicing.shared.resources.product_price_default
import com.a4a.g8invoicing.shared.resources.product_price_input
import com.a4a.g8invoicing.data.models.ProductNature
import com.a4a.g8invoicing.data.models.UnitCodes
import com.a4a.g8invoicing.ui.screens.shared.ProductNaturePicker
import com.a4a.g8invoicing.ui.screens.shared.UnitCodePicker
import com.a4a.g8invoicing.shared.resources.product_tax
import com.a4a.g8invoicing.shared.resources.product_type_goods
import com.a4a.g8invoicing.shared.resources.product_type_info_desc
import com.a4a.g8invoicing.shared.resources.product_type_info_modal_content
import com.a4a.g8invoicing.shared.resources.product_type_info_modal_title
import com.a4a.g8invoicing.shared.resources.product_type_label
import com.a4a.g8invoicing.shared.resources.product_type_service
import com.a4a.g8invoicing.shared.resources.product_unit
import com.a4a.g8invoicing.shared.resources.product_unit_code_info_desc
import com.a4a.g8invoicing.shared.resources.product_unit_code_info_modal_content
import com.a4a.g8invoicing.shared.resources.product_unit_code_info_modal_title
import com.a4a.g8invoicing.shared.resources.product_unit_code_label
import com.a4a.g8invoicing.shared.resources.product_unit_input
import com.a4a.g8invoicing.ui.shared.DecimalInput
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ForwardElement
import com.a4a.g8invoicing.ui.shared.LabelInfoTooltip
import com.a4a.g8invoicing.ui.shared.dismissKeyboardOnUnconsumedTap
import com.a4a.g8invoicing.ui.shared.ListPicker
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.theme.ColorDarkGray
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.theme.callForActions
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource

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
    // 1.8: no per-product company scoping yet, so the Type row can't be gated on
    // a specific issuer's intraEuSales flag. Default false = row stays hidden on
    // the master form. Wired end-to-end (VM → NavGraph → form) so 1.9 only needs
    // to push a real value here once the active-company concept exists.
    showProductType: Boolean = false,
) {
    val localFocusManager = LocalFocusManager.current
    var unitPickerOpen by remember { mutableStateOf(false) }
    var typePickerOpen by remember { mutableStateOf(false) }

    // Hoist stringResource calls
    val productNameLabel = stringResource(Res.string.product_name)
    val productNamePlaceholder = stringResource(Res.string.product_name_input)
    val productDescriptionLabel = stringResource(Res.string.product_description)
    val productDescriptionPlaceholder = stringResource(Res.string.product_description_input)
    val productUnitLabel = stringResource(Res.string.product_unit)
    val productUnitPlaceholder = stringResource(Res.string.product_unit_input)
    val productTypeLabel = stringResource(Res.string.product_type_label)
    val productTypeServiceLabel = stringResource(Res.string.product_type_service)
    val productTypeGoodsLabel = stringResource(Res.string.product_type_goods)
    val productTypeInfoTitle = stringResource(Res.string.product_type_info_modal_title)
    val productTypeInfoContent = stringResource(Res.string.product_type_info_modal_content)
    val productTypeInfoDesc = stringResource(Res.string.product_type_info_desc)
    val productUnitCodeLabel = stringResource(Res.string.product_unit_code_label)
    val unitCodeInfoTitle = stringResource(Res.string.product_unit_code_info_modal_title)
    val unitCodeInfoContent = stringResource(Res.string.product_unit_code_info_modal_content)
    val unitCodeInfoDesc = stringResource(Res.string.product_unit_code_info_desc)
    val productTaxLabel = stringResource(Res.string.product_tax)
    val productPriceLabel = stringResource(Res.string.product_price)
    val productPriceDefaultLabel = stringResource(Res.string.product_price_default)
    val productPricePlaceholder = stringResource(Res.string.product_price_input)
    val productPriceClient = stringResource(Res.string.product_price_client)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .verticalScroll(rememberScrollState())
            .dismissKeyboardOnUnconsumedTap()
            .padding(12.dp)
            .padding(top = 110.dp, bottom = 60.dp)
            .imePadding() // Ceci fait remonter l'écran quand le clavier s'ouvre
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
                        product.unitCode,
                        product.type,
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
                                pageElement = ScreenElement.PRODUCT_NAME,
                                isMandatory = true
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
                            // Code unité UNECE (BT-130) — ouverture bottom-sheet catégorisée.
                            // Sync bi-directionnelle text↔code géré côté ViewModel : taper "heure"
                            // en unit auto-match HUR ; picker → prérempli "heure" côté unit.
                            FormInput(
                                label = productUnitCodeLabel,
                                inputType = ForwardElement(
                                    text = product.unitCode
                                        ?.let { code -> UnitCodes.findByCode(code)?.let { "$code — ${it.labelFr}" } ?: code }
                                        ?: "-",
                                    isMultiline = false,
                                ),
                                pageElement = ScreenElement.PRODUCT_UNIT_CODE,
                                labelInfoTooltip = LabelInfoTooltip(
                                    title = unitCodeInfoTitle,
                                    content = unitCodeInfoContent,
                                    contentDescription = unitCodeInfoDesc,
                                    persistenceKey = "product_unit_code",
                                ),
                            ),
                            // Product.type (BT-151 SERVICE/GOODS). Row hidden in 1.8
                            // (showProductType defaults to false — see param comment):
                            // no per-product company scoping means we can't decide
                            // which issuer's intraEuSales gates visibility here. Wired
                            // end-to-end so 1.9 only flips the source of the flag.
                            if (showProductType) FormInput(
                                label = productTypeLabel,
                                inputType = ForwardElement(
                                    text = when (product.type) {
                                        ProductNature.GOODS -> productTypeGoodsLabel
                                        ProductNature.SERVICE -> productTypeServiceLabel
                                        null -> "-"
                                    },
                                    isMultiline = false,
                                ),
                                pageElement = ScreenElement.PRODUCT_TYPE,
                                labelInfoTooltip = LabelInfoTooltip(
                                    title = productTypeInfoTitle,
                                    content = productTypeInfoContent,
                                    contentDescription = productTypeInfoDesc,
                                    persistenceKey = "product_type",
                                ),
                            ) else null,
                            FormInput(
                                label = productTaxLabel,
                                inputType = ForwardElement(
                                    text = product.taxRate.let { taxRate ->
                                        if (taxRate == null) {
                                            "-"
                                        } else {
                                            "${taxRate.stripTrailingZeros().toPlainString().replace(".", ",")}%"
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
                        onClickForward = { element ->
                            when (element) {
                                ScreenElement.PRODUCT_UNIT_CODE -> unitPickerOpen = true
                                ScreenElement.PRODUCT_TYPE -> typePickerOpen = true
                                else -> onClickForward(element)
                            }
                        },
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
                            modifier = Modifier.fillMaxWidth(),
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

    if (unitPickerOpen) {
        UnitCodePicker(
            currentCode = product.unitCode,
            onSelect = { code ->
                onValueChange(ScreenElement.PRODUCT_UNIT_CODE, code, null)
                unitPickerOpen = false
            },
            onDismiss = { unitPickerOpen = false },
        )
    }
    if (typePickerOpen) {
        ProductNaturePicker(
            current = product.type,
            onSelect = { nature ->
                onValueChange(ScreenElement.PRODUCT_TYPE, nature, null)
                typePickerOpen = false
            },
            onDismiss = { typePickerOpen = false },
        )
    }
}

@Composable
fun AddPriceButton(onClick: () -> Unit, bottomPadding: Dp = 0.dp) {
    val addPriceText = stringResource(Res.string.product_add_price)
    Box(
        modifier = Modifier
            .padding(start = 4.dp, top = 4.dp, bottom = bottomPadding)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(enabled = true) {
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            style = MaterialTheme.typography.callForActions,
            color = ColorVioletLink,
            text = AnnotatedString(addPriceText),
        )
    }
}


@Composable
fun DeletePriceButton(onClick: () -> Unit) {
    val deletePriceText = stringResource(Res.string.product_delete_price)
    Box(
        modifier = Modifier
            .offset(x = 8.dp, y = (-8).dp)
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = Icons.Outlined.Delete,
            tint = ColorDarkGray,
            contentDescription = deletePriceText
        )
    }
}
