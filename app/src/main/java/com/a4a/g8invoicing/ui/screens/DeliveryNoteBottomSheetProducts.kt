package com.a4a.g8invoicing.ui.screens

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
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
import icons.IconDone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryNoteBottomSheetProducts(
    deliveryNote: DeliveryNoteState,
    onDismissBottomSheet: () -> Unit,
    onValueChange: (ScreenElement, Any) -> Unit,
    documentProductUiState: DocumentProductState,
    products: MutableList<ProductState>,
    taxRates: List<BigDecimal>,
    onClickProduct: (ProductState) -> Unit,
    onClickDocumentProduct: (DocumentProductState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    localFocusManager: FocusManager,
) {
    Column(
        // We add this column to be able to apply "fillMaxHeight" to the components that slide in
        // If we don't constrain the parent (=this column) width, components that slide in
        // fill the screen full height
        modifier = Modifier
        //  .fillMaxHeight(0.5f)
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
        var isDocumentFormVisible by remember { mutableStateOf(false) }

        val parameters = Pair(
            deliveryNote.documentProducts,
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
                    slideOtherComponent.value = ScreenElement.DOCUMENT_PRODUCTS
                   /* DeliveryNoteBottomSheetItemsContent(
                        onClickForward = {
                            keyboardController?.hide()
                            slideOtherComponent.value = it
                        },
                        placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                        localFocusManager = localFocusManager
                    )*/
                }
            }

            DeliveryNoteBottomSheetDocumentProductList(
                list = params.first ?: emptyList(),
                onClickNew = {
                    typeOfCreation = DocumentBottomSheetTypeOfForm.NEW_PRODUCT
                    isDocumentFormVisible = true
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(TimeUnit.MILLISECONDS.toMillis(500))
                        isProductListVisible = false
                    }
                },
                onClickChooseExisting = { isProductListVisible = true },
                onClickDocumentProduct = {
                    onClickDocumentProduct(it)
                    typeOfCreation = DocumentBottomSheetTypeOfForm.EDIT_PRODUCT
                    isDocumentFormVisible = true
                    /*CoroutineScope(Dispatchers.IO).launch {
                        delay(TimeUnit.MILLISECONDS.toMillis(500))
                        isProductListVisible = false
                    }*/
                },
                onClickDelete = onClickDeleteDocumentProduct
            )

            if (isProductListVisible) {
                DeliveryNoteBottomSheetProductList(
                    list = params.second ?: emptyList(),
                    onClickBack = { isProductListVisible = false },
                    onProductClick = {
                        onClickProduct(it) // Update the ProductAddEditViewModel with the chosen product
                        // so we open bottom document form with the chosen product
                        typeOfCreation = DocumentBottomSheetTypeOfForm.ADD_PRODUCT
                        isDocumentFormVisible = true
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(TimeUnit.MILLISECONDS.toMillis(500))
                            // Waits for the bottom form to be opened,
                            // so previous screen change is in background
                            isProductListVisible = false
                        }
                    }
                )
            }

            if (isDocumentFormVisible) {
                DocumentBottomSheetFormModal(
                    typeOfCreation = typeOfCreation,
                    documentProduct = documentProductUiState,
                    taxRates = taxRates,
                    bottomFormOnValueChange = bottomFormOnValueChange,
                    placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                    onClickCancel = { // Re-initialize
                        isDocumentFormVisible = false
                        onClickCancelForm()
                    },
                    onClickDone = {
                        onClickDoneForm(typeOfCreation)
                        isDocumentFormVisible = false
                    },
                    onSelectTaxRate = onSelectTaxRate
                )
            }
        }
    }
}