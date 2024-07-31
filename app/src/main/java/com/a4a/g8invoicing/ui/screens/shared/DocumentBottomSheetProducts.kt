package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.icons.IconArrowDropDown
import com.a4a.g8invoicing.ui.shared.keyboardAsState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import icons.IconDone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


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
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    localFocusManager: FocusManager,
    showDocumentForm: Boolean = false,
    onShowDocumentForm: (Boolean) -> Unit,
) {
    Column(
        // We add this column to be able to apply "fillMaxHeight" to the components that slide in
        // If we don't constrain the parent (=this column) width, components that slide in
        // fill the screen full height
    ) {
        val slideOtherComponent: MutableState<ScreenElement?> = remember { mutableStateOf(null) }
        val keyboardController = LocalSoftwareKeyboardController.current
        val keyboard by keyboardAsState()
        var isProductListVisible by remember { mutableStateOf(false) }
        var typeOfCreation: DocumentBottomSheetTypeOfForm by remember {
            mutableStateOf(
                DocumentBottomSheetTypeOfForm.ADD_PRODUCT
            )
        }

        val parameters = Pair(
            document.documentProducts,
            products
        )

        val params = parameters as Pair<List<DocumentProductState>?, List<ProductState>?>

        Box(
            modifier = Modifier.background(Color.Transparent)
        ) {
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
                                if (keyboard.name == "Opened") {
                                    keyboardController?.hide()
                                } else { // Hides bottom sheet
                                    onDismissBottomSheet()
                                }
                            }) {
                        if (keyboard.name == "Opened") {
                            Icon(
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(20.dp)
                                    .align(alignment = Alignment.CenterEnd),
                                imageVector = IconDone,
                                contentDescription = "Close keyboard"
                            )
                        } else { // Hides bottom sheet
                            Icon(
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(30.dp)
                                    .align(alignment = Alignment.CenterEnd),
                                imageVector = IconArrowDropDown,
                                contentDescription = "Close bottom sheet"
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    keyboardController?.hide()
                    slideOtherComponent.value = ScreenElement.DOCUMENT_PRODUCT
                }
            }

            DocumentBottomSheetDocumentProductList(
                list = params.first ?: emptyList(),
                onClickNew = {
                    onClickNewProduct()
                    typeOfCreation = DocumentBottomSheetTypeOfForm.NEW_PRODUCT
                    onShowDocumentForm(true)
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(TimeUnit.MILLISECONDS.toMillis(500))
                        isProductListVisible = false
                    }
                },
                onClickChooseExisting = {
                    isProductListVisible = true },
                onClickDocumentProduct = {
                    onClickDocumentProduct(it)
                    typeOfCreation = DocumentBottomSheetTypeOfForm.EDIT_PRODUCT
                    onShowDocumentForm(true)
                },
                onClickDelete = onClickDeleteDocumentProduct
            )

            if (isProductListVisible) {
                DocumentBottomSheetProductList(
                    list = params.second ?: emptyList(),
                    onClickBack = { isProductListVisible = false },
                    onProductClick = {
                        onClickProduct(it) // Update the ProductAddEditViewModel with the chosen product
                        // so we open bottom document form with the chosen product
                        typeOfCreation = DocumentBottomSheetTypeOfForm.ADD_PRODUCT
                        onShowDocumentForm(true)
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(TimeUnit.MILLISECONDS.toMillis(500))
                            // Waits for the bottom form to be opened,
                            // so previous screen change is in background
                            isProductListVisible = false
                        }
                    }
                )
            }

            if (showDocumentForm) {
                DocumentBottomSheetFormModal(
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
}