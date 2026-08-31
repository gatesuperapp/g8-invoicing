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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.states.RetentionState
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
    onSheetCollapseToPartial: () -> Unit,
    documentProductUiState: DocumentProductState,
    products: MutableList<ProductState>,
    taxRates: List<BigDecimal>,
    onClickProduct: (ProductState) -> Unit,
    onClickNewProduct: () -> Unit,
    onClickDocumentProduct: (DocumentProductState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm, syncToMaster: Boolean) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    showDocumentForm: Boolean = false,
    onShowDocumentForm: (Boolean) -> Unit,
    onOrderChange: (List<DocumentProductState>) -> Unit,
    showProductType: Boolean = false,
    hideLinkedSourceHeaders: Boolean = false,
    onToggleHideLinkedSourceHeaders: (() -> Unit)? = null,
    // Retentions: only Invoice + CreditNote wire non-noop callbacks (delivery
    // notes and quotes don't carry withholding).
    onSaveRetention: (Int, RetentionState) -> Unit = { _, _ -> },
    onToggleRetentionHidden: (Int) -> Unit = {},
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
    // Overscroll hook mirrors DocumentBottomSheetTextElements: at partial height,
    // hitting the bottom of the list and pulling further up expands the sheet; at
    // full height, pulling down at the top of the list steps down one notch to
    // partial. Never chains all the way to Hidden — full close still requires an
    // explicit gesture on the drag handle.
    //
    // Sign convention (matches the text sheet, verified empirically): available.y
    // carries the raw pointer delta. Finger DOWN = positive y, finger UP = negative y.
    val overscrollScope = rememberCoroutineScope()
    val overscrollConnection = remember(isSheetFullScreen) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                if (!isSheetFullScreen && available.y < 0f) {
                    overscrollScope.launch { onSheetDragUp() }
                    return available
                }
                if (isSheetFullScreen && available.y > 0f) {
                    overscrollScope.launch { onSheetCollapseToPartial() }
                    return available
                }
                return Offset.Zero
            }
        }
    }
    Box(modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .nestedScroll(overscrollConnection)
    ) {
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

        val retentions = when (document) {
            is InvoiceState -> document.retentions
            is CreditNoteState -> document.retentions
            else -> emptyList()
        }
        // Which retention row is being edited (null when the form is not in
        // EDIT_RETENTION mode). Tap on a row sets this + flips the shared
        // DocumentBottomSheetForm to EDIT_RETENTION.
        var editingRetentionIndex: Int? by remember { mutableStateOf(null) }
        val editingRetention = editingRetentionIndex?.let { retentions.getOrNull(it) }

        // List of selected products
        DocumentBottomSheetProductsChosen(
            list = params.first ?: emptyList(),
            onClickChooseExisting = {
                isProductListVisible = true
            },
            onClickDocumentProduct = {
                onClickDocumentProduct(it)
                typeOfCreation = DocumentBottomSheetTypeOfForm.EDIT_PRODUCT
                onShowDocumentForm(true)
            },
            onClickDelete = onClickDeleteDocumentProduct,
            onOrderChange = onOrderChange,
            hideLinkedSourceHeaders = hideLinkedSourceHeaders,
            onToggleHideLinkedSourceHeaders = onToggleHideLinkedSourceHeaders,
            retentions = retentions,
            onClickRetention = { idx ->
                editingRetentionIndex = idx
                typeOfCreation = DocumentBottomSheetTypeOfForm.EDIT_RETENTION
                onShowDocumentForm(true)
            },
            onToggleRetentionHidden = onToggleRetentionHidden,
        )
        // List of all products to chose from
        if (isProductListVisible) {
            ProductPickerBottomSheet(
                products = params.second ?: emptyList(),
                clientId = document.documentClient?.originalClientOrIssuerId,
                onDismiss = { isProductListVisible = false },
                onSelect = {
                    onClickProduct(it)
                    typeOfCreation = DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT
                    isProductListVisible = false
                    onShowDocumentForm(true)
                },
                onClickNew = {
                    typeOfCreation = DocumentBottomSheetTypeOfForm.NEW_PRODUCT
                    onShowDocumentForm(true)
                    onClickNewProduct()
                },
            )
        }
        // Add new product / edit chosen product / edit retention
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
                    editingRetentionIndex = null
                },
                onClickDone = { syncToMaster ->
                    onClickDoneForm(typeOfCreation, syncToMaster)
                    isProductListVisible = false
                },
                onSelectTaxRate = onSelectTaxRate,
                showProductType = showProductType,
                retention = editingRetention,
                onRetentionSave = { updated ->
                    editingRetentionIndex?.let { idx ->
                        onSaveRetention(idx, updated)
                    }
                    editingRetentionIndex = null
                    onShowDocumentForm(false)
                },
            )
        }
    }
    }
    }

}

