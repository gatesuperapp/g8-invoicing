package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.ui.shared.Keyboard
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.keyboardAsState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.ProductState
import com.ionspin.kotlin.bignum.decimal.BigDecimal

// Bottom sheet with "New product" and "Choose in list" buttons
// And the list of chosen products
@Composable
fun DocumentBottomSheetProducts(
    document: DocumentState,
    onDismissBottomSheet: () -> Unit,
    documentProductUiState: DocumentProductState,
    products: MutableList<ProductState>,
    taxRates: List<BigDecimal>,
    onClickProduct: (ProductState) -> Unit,
    onClickNewProduct: () -> Unit,
    onClickDocumentProduct: (DocumentProductState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    showDocumentForm: Boolean = false,
    onShowDocumentForm: (Boolean) -> Unit,
    onOrderChange: (List<DocumentProductState>) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboard by keyboardAsState()

    Box(
        // We add this column to be able to apply "fillMaxHeight" to the components that slide in
        // If we don't constrain the parent (=this column) width, components that slide in
        // fill the screen full height
    ) {
        val slideOtherComponent: MutableState<ScreenElement?> = remember { mutableStateOf(null) }
        var isProductListVisible by remember { mutableStateOf(false) }
        var typeOfCreation: DocumentBottomSheetTypeOfForm by remember {
            mutableStateOf(
                DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT
            )
        }

        val parameters = Pair(
            document.documentProducts,
            products
        )

        val params = parameters as Pair<List<DocumentProductState>?, List<ProductState>?>

        Column(
            modifier = Modifier
                .fillMaxHeight(0.5f)
        ) {
            Row(
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .width(70.dp)
                        .clickable {
                            // Hides keyboard if it was opened
                            if (keyboard == Keyboard.Opened) {
                                keyboardController?.hide()
                            } else { // Hides bottom sheet
                                onDismissBottomSheet()
                            }
                        }) {
                    Icon(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(30.dp)
                            .align(alignment = Alignment.CenterEnd),
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = "Close bottom sheet"
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                //    keyboardController?.hide()
                slideOtherComponent.value = ScreenElement.DOCUMENT_PRODUCT
            }
        }

        // List of selected products
        DocumentBottomSheetProductsChosen(
            list = params.first ?: emptyList(),
            onClickNew = {
                typeOfCreation = DocumentBottomSheetTypeOfForm.NEW_PRODUCT
                onShowDocumentForm(true)
                onClickNewProduct()
            },
            onClickChooseExisting = {
                isProductListVisible = true
            },
            onClickDocumentProduct = {
                onClickDocumentProduct(it)
                typeOfCreation = DocumentBottomSheetTypeOfForm.EDIT_PRODUCT
                onShowDocumentForm(true)
            },
            onClickDelete = onClickDeleteDocumentProduct,
            isClientOrIssuerListEmpty = parameters.second.isEmpty(),
            onOrderChange = onOrderChange
        )
        // List of all products to chose from
        if (isProductListVisible) {
            DocumentBottomSheetProductsAvailable(
                list = params.second ?: emptyList(),
                onClickBack = { isProductListVisible = false },
                onProductClick = {
                    onClickProduct(it)
                    typeOfCreation = DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT
                    isProductListVisible = false
                    onShowDocumentForm(true)
                },
                clientId = document.documentClient?.originalClientId
            )
        }
        // Add new product or edit chosen product
        if (showDocumentForm) {
            DocumentBottomSheetForm(
                typeOfCreation = typeOfCreation,
                documentProduct = documentProductUiState,
                taxRates = taxRates,
                bottomFormOnValueChange = bottomFormOnValueChange,
                bottomFormPlaceCursor = bottomFormPlaceCursor,
                onClickCancel = { // Re-initialize
                    onClickCancelForm()
                    onShowDocumentForm(false)
                },
                onClickDone = {
                    onClickDoneForm(typeOfCreation)
                },
                onSelectTaxRate = onSelectTaxRate
            )
        }
    }

}
