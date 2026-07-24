package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.ScreenElement
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
    sheetMaxHeight: Dp,
    isSheetFullScreen: Boolean,
    onSheetDragUp: () -> Unit,
    onSheetStepDown: () -> Unit,
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
    onOrderChange: (List<DocumentProductState>) -> Unit,
    showProductType: Boolean = false,
) {
    val density = LocalDensity.current
    val topInsetDp = with(density) { WindowInsets.safeDrawing.getTop(density).toDp() }
    // The sheet Surface is held at the max-visible height (fullscreen state minus
    // the top inset) at all states, so its white background always fills whatever
    // area the sheet occupies on screen and no preview leaks through during the
    // Partial→Expanded animation. The visible-content Column inside then animates
    // between peekHeight and the max to drive the LazyColumn viewport dynamically.
    val sheetMaxContentHeight = sheetMaxHeight - topInsetDp
    val visibleContentHeight by animateDpAsState(
        targetValue = if (isSheetFullScreen) sheetMaxContentHeight else sheetMaxHeight / 2,
        label = "sheet-content-height",
    )
    Box(modifier = Modifier.height(sheetMaxContentHeight)) {
    Column(modifier = Modifier.fillMaxWidth().height(visibleContentHeight)) {
    SheetDragHandle(
        onDragUp = onSheetDragUp,
        onDragDown = onSheetStepDown,
        onTap = onSheetStepDown,
    )
    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
        var isProductListVisible by remember { mutableStateOf(false) }
        var typeOfCreation: DocumentBottomSheetTypeOfForm by remember {
            mutableStateOf(
                DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT
            )
        }

        // Back inside the products picker goes back to the chosen list
        // rather than dismissing the whole sheet.
        PlatformBackHandler(enabled = isProductListVisible) {
            isProductListVisible = false
        }

        val parameters = Pair(
            document.documentProducts,
            products
        )

        val params = parameters as Pair<List<DocumentProductState>?, List<ProductState>?>

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
                clientId = document.documentClient?.originalClientOrIssuerId
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
                onClickCancel = {
                    onClickCancelForm()
                    onShowDocumentForm(false)
                },
                onClickDone = {
                    onClickDoneForm(typeOfCreation)
                },
                onSelectTaxRate = onSelectTaxRate,
                showProductType = showProductType,
            )
        }
    }
    }
    }

}

