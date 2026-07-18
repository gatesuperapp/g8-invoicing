package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.models.ProductNature
import com.a4a.g8invoicing.data.models.UnitCodes
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_product_quantity
import com.a4a.g8invoicing.shared.resources.document_product_quantity_input
import com.a4a.g8invoicing.shared.resources.product_description
import com.a4a.g8invoicing.shared.resources.product_description_input
import com.a4a.g8invoicing.shared.resources.product_name
import com.a4a.g8invoicing.shared.resources.product_name_input
import com.a4a.g8invoicing.shared.resources.product_price
import com.a4a.g8invoicing.shared.resources.product_price_input
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
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource

// It's a bit different from ProductAddEditForm: user can choose the quantity; there's some styling diff;
// and it's not possible to add several prices from here.
@Composable
fun DocumentBottomSheetProductAddEditForm(
    documentProduct: DocumentProductState,
    bottomFormOnValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickForward: (ScreenElement) -> Unit,
    showFullScreenText: (ScreenElement) -> Unit,
    showProductType: Boolean = false,
) {
    val localFocusManager = LocalFocusManager.current

    // Hoist stringResource calls to the composable scope
    val productNameLabel = stringResource(Res.string.product_name)
    val productNamePlaceholder = stringResource(Res.string.product_name_input)
    val quantityLabel = stringResource(Res.string.document_product_quantity)
    val quantityPlaceholder = stringResource(Res.string.document_product_quantity_input)
    val descriptionLabel = stringResource(Res.string.product_description)
    val descriptionPlaceholder = stringResource(Res.string.product_description_input)
    val unitLabel = stringResource(Res.string.product_unit)
    val unitPlaceholder = stringResource(Res.string.product_unit_input)
    val productTypeLabel = stringResource(Res.string.product_type_label)
    val productTypeGoodsLabel = stringResource(Res.string.product_type_goods)
    val productTypeServiceLabel = stringResource(Res.string.product_type_service)
    val productTypeInfoTitle = stringResource(Res.string.product_type_info_modal_title)
    val productTypeInfoContent = stringResource(Res.string.product_type_info_modal_content)
    val productTypeInfoDesc = stringResource(Res.string.product_type_info_desc)
    val productUnitCodeLabel = stringResource(Res.string.product_unit_code_label)
    val unitCodeInfoTitle = stringResource(Res.string.product_unit_code_info_modal_title)
    val unitCodeInfoContent = stringResource(Res.string.product_unit_code_info_modal_content)
    val unitCodeInfoDesc = stringResource(Res.string.product_unit_code_info_desc)
    val taxLabel = stringResource(Res.string.product_tax)
    val priceLabel = stringResource(Res.string.product_price)
    val pricePlaceholder = stringResource(Res.string.product_price_input)

    var unitPickerOpen by remember { mutableStateOf(false) }
    var typePickerOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxHeight(0.5f)
            .background(Color.LightGray.copy(alpha = 0.4f))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .imePadding()
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
                documentProduct.unitCode,
                documentProduct.type,
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
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_NAME,
                        isMandatory = true
                    ),
                    FormInput(
                        label = quantityLabel,
                        inputType = DecimalInput(
                            text = documentProduct.quantity
                                .stripTrailingZeros().toPlainString(),
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
                        label = productUnitCodeLabel,
                        inputType = ForwardElement(
                            text = documentProduct.unitCode
                                ?.let { code -> UnitCodes.findByCode(code)?.let { "$code — ${it.labelFr}" } ?: code }
                                ?: "-",
                            isMultiline = false,
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_UNIT_CODE,
                        labelInfoTooltip = LabelInfoTooltip(
                            title = unitCodeInfoTitle,
                            content = unitCodeInfoContent,
                            contentDescription = unitCodeInfoDesc,
                            persistenceKey = "product_unit_code",
                        ),
                    ),
                    // Type produit (BT-151) — masqué quand l'émetteur n'a pas coché
                    // "Ventes hors de mon pays (UE)" côté profil : inutile pour un flow
                    // domestique / hors UE, cf. ProductAddEditViewModel.showProductType.
                    if (showProductType) FormInput(
                        label = productTypeLabel,
                        inputType = ForwardElement(
                            text = when (documentProduct.type) {
                                ProductNature.GOODS -> productTypeGoodsLabel
                                ProductNature.SERVICE -> productTypeServiceLabel
                                null -> "-"
                            },
                            isMultiline = false,
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_TYPE,
                        labelInfoTooltip = LabelInfoTooltip(
                            title = productTypeInfoTitle,
                            content = productTypeInfoContent,
                            contentDescription = productTypeInfoDesc,
                            persistenceKey = "product_type",
                        ),
                    ) else null,
                    FormInput(
                        label = taxLabel,
                        inputType = ForwardElement(
                            text = documentProduct.taxRate.let { taxRate ->
                                if (taxRate == null) {
                                    "-"
                                } else {
                                    "${taxRate.stripTrailingZeros().toPlainString().replace(".", ",")}%"
                                }
                            },
                        ),
                        pageElement = ScreenElement.DOCUMENT_PRODUCT_TAX_RATE
                    ),
                    FormInput(
                        label = priceLabel,
                        inputType = DecimalInput(
                            text = documentProduct.priceWithoutTax?.toPlainString() ?: "",
                            taxRate = documentProduct.taxRate,
                            placeholder = pricePlaceholder,
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = {
                                bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_PRICE_WITHOUT_TAX, it)
                            }
                        ),
                        inputType2 = DecimalInput(
                            text = documentProduct.priceWithTax?.toPlainString() ?: "",
                            placeholder = documentProduct.taxRate?.let {
                                (BigDecimal.fromInt(3) + BigDecimal.fromInt(3) * it / BigDecimal.fromInt(100)).toPlainString()
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
                onClickForward = { element ->
                    when (element) {
                        ScreenElement.DOCUMENT_PRODUCT_UNIT_CODE -> unitPickerOpen = true
                        ScreenElement.DOCUMENT_PRODUCT_TYPE -> typePickerOpen = true
                        else -> onClickForward(element)
                    }
                },
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                errors = documentProduct.errors,
                onClickExpandFullScreen = {
                    showFullScreenText(it)
                }
            )
        }
    }

    if (unitPickerOpen) {
        UnitCodePicker(
            currentCode = documentProduct.unitCode,
            onSelect = { code ->
                bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_UNIT_CODE, code)
                unitPickerOpen = false
            },
            onDismiss = { unitPickerOpen = false },
        )
    }
    if (typePickerOpen) {
        ProductNaturePicker(
            current = documentProduct.type,
            onSelect = { nature ->
                bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_TYPE, nature)
                typePickerOpen = false
            },
            onDismiss = { typePickerOpen = false },
        )
    }
}
