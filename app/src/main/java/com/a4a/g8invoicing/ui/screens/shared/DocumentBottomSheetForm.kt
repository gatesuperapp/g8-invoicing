package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerAddEditForm
import com.a4a.g8invoicing.ui.screens.ProductTaxRatesContent
import com.a4a.g8invoicing.ui.shared.ModalBottomSheetFork
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.rememberModalBottomSheetState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.theme.callForActionsDisabled
import com.a4a.g8invoicing.ui.theme.callForActionsViolet

import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import com.a4a.g8invoicing.ui.viewmodels.ProductType
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentBottomSheetForm(
    typeOfCreation: DocumentBottomSheetTypeOfForm?,
    documentClientUiState: ClientOrIssuerState = ClientOrIssuerState(),
    documentIssuerUiState: ClientOrIssuerState = ClientOrIssuerState(),
    documentProduct: DocumentProductState = DocumentProductState(),
    taxRates: List<BigDecimal>? = null,
    onClickCancel: () -> Unit,
    onClickDone: () -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    onClickDeleteAddress: (ClientOrIssuerType) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden })
    var isTaxSelectionVisible by remember { mutableStateOf(false) }
    var fullScreenElementToShow: MutableState<ScreenElement?> = remember { mutableStateOf(null) }
    val screenElement by remember { mutableStateOf(ScreenElement.DOCUMENT_PRODUCT_NAME) }

    ModalBottomSheetFork(
        onDismissRequest = onClickCancel,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = if (typeOfCreation?.name
                                .toString()
                                .contains(ClientOrIssuerType.CLIENT.name)
                        )
                            60.dp else 40.dp, end = 30.dp, start = 30.dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        style = MaterialTheme.typography.callForActionsViolet,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 20.dp)
                            .clickable {
                                if (isTaxSelectionVisible) {
                                    isTaxSelectionVisible = false
                                } else onClickCancel()
                            },
                        text = if (!isTaxSelectionVisible) {
                            stringResource(id = R.string.document_modal_product_cancel)
                        } else stringResource(id = R.string.document_modal_product_back)
                    )
                    Text(
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.TopCenter),
                        style = MaterialTheme.typography.titleMedium,
                        text = when (typeOfCreation) {
                            DocumentBottomSheetTypeOfForm.NEW_CLIENT -> stringResource(id = R.string.document_modal_new_client)
                            DocumentBottomSheetTypeOfForm.NEW_ISSUER -> stringResource(id = R.string.document_modal_new_issuer)
                            DocumentBottomSheetTypeOfForm.NEW_PRODUCT -> stringResource(id = R.string.document_modal_new_product)
                            DocumentBottomSheetTypeOfForm.ADD_EXISTING_CLIENT -> stringResource(id = R.string.document_modal_edit_client)
                            DocumentBottomSheetTypeOfForm.ADD_EXISTING_ISSUER -> stringResource(id = R.string.document_modal_edit_issuer)
                            DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT -> stringResource(id = R.string.document_modal_add_product)
                            DocumentBottomSheetTypeOfForm.EDIT_CLIENT -> stringResource(id = R.string.document_modal_edit_client)
                            DocumentBottomSheetTypeOfForm.EDIT_ISSUER -> stringResource(id = R.string.document_modal_edit_issuer)
                            DocumentBottomSheetTypeOfForm.EDIT_PRODUCT -> stringResource(id = R.string.document_modal_edit_product)
                            else -> ""
                        }
                    )

                    val requiredFieldsAreFilled: Boolean =
                        if (typeOfCreation?.name.toString().contains(ClientOrIssuerType.CLIENT.name)
                            && documentClientUiState.name.text.isNotEmpty()
                        ) {
                            true
                        } else if (typeOfCreation?.name.toString()
                                .contains(ClientOrIssuerType.ISSUER.name)
                            && documentIssuerUiState.name.text.isNotEmpty()
                        ) {
                            true
                        } else if (typeOfCreation?.name.toString()
                                .contains(ProductType.PRODUCT.name)
                            && documentProduct.name.text.isNotEmpty()
                            && documentProduct.quantity != BigDecimal(0)
                        ) {
                            true
                        } else false

                    var customModifier = Modifier
                        .padding(top = 20.dp, bottom = 20.dp)
                        .align(Alignment.TopEnd)
                    customModifier = if (requiredFieldsAreFilled)
                        customModifier.then(
                            Modifier
                                .clickable { onClickDone() }
                        ) else customModifier

                    Text(
                        style = if (requiredFieldsAreFilled) MaterialTheme.typography.callForActionsViolet
                        else MaterialTheme.typography.callForActionsDisabled,
                        modifier = customModifier,
                        text = if (!isTaxSelectionVisible) {
                            stringResource(id = R.string.document_modal_product_save)
                        } else ""
                    )
                }
            }
            if (typeOfCreation?.name.toString().contains(ClientOrIssuerType.CLIENT.name)) {
                ClientOrIssuerAddEditForm(
                    clientOrIssuerUiState = documentClientUiState,
                    typeOfCreation = typeOfCreation,
                    onValueChange = { screenElement, value ->
                        bottomFormOnValueChange(
                            screenElement,
                            value,
                            ClientOrIssuerType.DOCUMENT_CLIENT
                        )
                    },
                    placeCursorAtTheEndOfText = {
                        bottomFormPlaceCursor(it, ClientOrIssuerType.DOCUMENT_CLIENT)
                    },
                    isInBottomSheetModal = true,
                    onClickDeleteAddress = {
                        onClickDeleteAddress(ClientOrIssuerType.DOCUMENT_CLIENT)
                    }
                )
            } else if (typeOfCreation?.name.toString().contains(ClientOrIssuerType.ISSUER.name)) {
                ClientOrIssuerAddEditForm(
                    clientOrIssuerUiState = documentIssuerUiState,
                    typeOfCreation = typeOfCreation,
                    onValueChange = { screenElement, value ->
                        bottomFormOnValueChange(
                            screenElement,
                            value,
                            ClientOrIssuerType.DOCUMENT_ISSUER
                        )
                    },
                    placeCursorAtTheEndOfText = {
                        bottomFormPlaceCursor(it, ClientOrIssuerType.DOCUMENT_ISSUER)
                    },
                    isInBottomSheetModal = true,
                    onClickDeleteAddress = {
                        onClickDeleteAddress(ClientOrIssuerType.DOCUMENT_ISSUER)
                    }
                )
            } else {
                if (!isTaxSelectionVisible) {
                    DocumentBottomSheetProductAddEditForm(
                        documentProduct = documentProduct,
                        bottomFormOnValueChange = { screenElement, value ->
                            bottomFormOnValueChange(screenElement, value, null)
                        },
                        placeCursorAtTheEndOfText = {
                            bottomFormPlaceCursor(it, null)
                        },
                        onClickForward = {
                            isTaxSelectionVisible = true
                        },
                        showFullScreenText = {
                            fullScreenElementToShow.value = it
                        }
                    )
                } else {
                    DocumentBottomSheetTaxSelection(
                        taxRates = taxRates,
                        currentTaxRate = documentProduct.taxRate,
                        onSelectTaxRate = {
                            isTaxSelectionVisible = false
                            onSelectTaxRate(it)
                        }
                    )
                }
            }
        }
    }


    if (fullScreenElementToShow.value != null) {
        var text by remember { mutableStateOf(TextFieldValue("")) }

        Surface(
            modifier = Modifier.background(Color.White)
        ) // disable click on background component
        {
            fullScreenElementToShow.value?.let { fullScreenElement ->
                DocumentBottomSheetFormSimple(
                    onClickCancel = { fullScreenElementToShow.value = null },
                    onClickDone = {
                        bottomFormOnValueChange(it, text, null)
                        fullScreenElementToShow.value = null
                    },
                    bottomSheetTitle = if (fullScreenElement == ScreenElement.DOCUMENT_PRODUCT_NAME)
                        Strings.get(R.string.product_name2)
                    else Strings.get(R.string.product_description),
                    content = {
                        DocumentBottomSheetLargeText(
                            initialText = if (fullScreenElement == ScreenElement.DOCUMENT_PRODUCT_NAME)
                                documentProduct.name
                            else documentProduct.description
                                ?: TextFieldValue(""),
                            onValueChange = {
                                text = it
                            }
                        )
                    },
                    screenElement = fullScreenElement
                )
            }
        }
    }
}


@Composable
fun DocumentBottomSheetTaxSelection(
    taxRates: List<BigDecimal>?,
    currentTaxRate: BigDecimal?,
    onSelectTaxRate: (BigDecimal?) -> Unit,
) {
    ProductTaxRatesContent(
        taxRates,
        currentTaxRate,
        onSelectTaxRate,
        true
    )
}
