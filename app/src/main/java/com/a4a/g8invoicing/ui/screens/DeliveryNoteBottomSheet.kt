package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.a4a.g8invoicing.data.ClientOrIssuerState
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
import androidx.compose.ui.res.stringResource
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.theme.ColorDarkGray
import com.a4a.g8invoicing.ui.theme.textSmall
import com.a4a.g8invoicing.ui.theme.textTitle
import icons.IconDone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryNoteBottomSheet(
    deliveryNote: DeliveryNoteState,
    datePickerState: DatePickerState,
    onDismissBottomSheet: () -> Unit,
    onValueChange: (ScreenElement, Any) -> Unit,
    clients: MutableList<ClientOrIssuerState>,
    issuers: MutableList<ClientOrIssuerState>,
    products: MutableList<ProductState>,
    taxRates: List<BigDecimal>,
    onProductClick: (ProductState) -> Unit,
    documentProductUiState: DocumentProductState,
    onDocumentProductClick: (DocumentProductState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
    currentProductsIds: List<Int>? = null,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any) -> Unit,
    bottomFormPlaceCursor: (ScreenElement) -> Unit,
    onClickDoneForm: (TypeOfBottomSheetForm) -> Unit,
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
                    DeliveryNoteBottomSheetContent(
                        deliveryNote = deliveryNote,
                        onValueChange = onValueChange,
                        onClickForward = {
                            keyboardController?.hide()
                            slideOtherComponent.value = it
                        },
                        placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                        localFocusManager = localFocusManager
                    )
                }
            }

            SlideInNextComponent(
                pageElement = slideOtherComponent.value,
                parameters = when (slideOtherComponent.value) {
                    ScreenElement.DOCUMENT_ISSUER -> issuers
                    ScreenElement.DOCUMENT_CLIENT -> clients
                    ScreenElement.DOCUMENT_PRODUCTS -> listOf(
                        deliveryNote.documentProducts,
                        products,
                        taxRates
                    )

                    ScreenElement.DOCUMENT_DATE -> deliveryNote.deliveryDate
                    else -> {}
                },
                onClickBack = {
                    slideOtherComponent.value = null
                },
                onClientOrIssuerClick = {
                    // Select it, display it in the document
                    //  & go back to previous screen
                    onValueChange(slideOtherComponent.value!!, it)
                    slideOtherComponent.value = null
                },
                onProductClick = onProductClick,
                documentProductUiState = documentProductUiState,
                onDocumentProductClick = onDocumentProductClick,
                onClickDeleteDocumentProduct = onClickDeleteDocumentProduct,
                datePickerState = datePickerState,
                currentClientId = currentClientId,
                currentIssuerId = currentIssuerId,
                currentProductsIds = currentProductsIds,
                bottomFormOnValueChange = bottomFormOnValueChange,
                productPlaceCursorAtTheEndOfText = bottomFormPlaceCursor,
                onClickDoneForm = onClickDoneForm,
                onClickCancelForm = onClickCancelForm,
                onSelectTaxRate = onSelectTaxRate
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlideInNextComponent(
    pageElement: ScreenElement?,
    parameters: Any?,
    onClickBack: () -> Unit,
    onClientOrIssuerClick: (ClientOrIssuerState) -> Unit,
    onProductClick: (ProductState) -> Unit,
    documentProductUiState: DocumentProductState,
    onDocumentProductClick: (DocumentProductState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    datePickerState: DatePickerState,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
    currentProductsIds: List<Int>? = null,
    bottomFormOnValueChange: (ScreenElement, Any) -> Unit,
    productPlaceCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDoneForm: (TypeOfBottomSheetForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
) {
    var isProductListVisible by remember { mutableStateOf(false) }
    var typeOfCreation: TypeOfBottomSheetForm by remember { mutableStateOf(TypeOfBottomSheetForm.ADD_PRODUCT) }
    var isDocumentFormVisible by remember { mutableStateOf(false) }

    if (pageElement == ScreenElement.DOCUMENT_CLIENT || pageElement == ScreenElement.DOCUMENT_ISSUER) {
        DeliveryNoteBottomSheetClientOrIssuerList(
            list = parameters?.let { it as List<ClientOrIssuerState> } ?: emptyList(),
            pageElement = pageElement,
            onClickBack = onClickBack,
            onClientOrIssuerClick = {
                onClientOrIssuerClick(it)
                typeOfCreation = if (pageElement == ScreenElement.DOCUMENT_CLIENT) {
                    TypeOfBottomSheetForm.ADD_CLIENT
                } else TypeOfBottomSheetForm.ADD_ISSUER

            },
            onClickNewClientOrIssuer = {
                typeOfCreation = if (pageElement == ScreenElement.DOCUMENT_CLIENT) {
                    TypeOfBottomSheetForm.NEW_CLIENT
                } else TypeOfBottomSheetForm.NEW_ISSUER
                isDocumentFormVisible = true
            },
            currentClientId = currentClientId,
            currentIssuerId = currentIssuerId
        )
    }

    if (pageElement == ScreenElement.DOCUMENT_DATE) {
        DeliveryNoteBottomSheetDatePicker(
            initialDate = parameters?.let { it as String } ?: "",
            datePickerState = datePickerState,
            onClickBack = onClickBack,
        )
    }

    if (pageElement == ScreenElement.DOCUMENT_PRODUCTS) {
        val params = parameters as List<List<Any>?>

        DeliveryNoteBottomSheetDocumentProductList(
            list = params.first() as List<DocumentProductState>? ?: emptyList(),
            onClickBack = onClickBack,
            onClickChooseProduct = { isProductListVisible = true },
            onDocumentProductClick = {
                onDocumentProductClick(it)
                typeOfCreation = TypeOfBottomSheetForm.EDIT_DOCUMENT_PRODUCT
                isDocumentFormVisible = true
                CoroutineScope(Dispatchers.IO).launch {
                    delay(TimeUnit.MILLISECONDS.toMillis(500))
                    isProductListVisible = false
                }
            },
            onClickDeleteDocumentProduct = onClickDeleteDocumentProduct
        )

        if (isProductListVisible) {
            DeliveryNoteBottomSheetProductList(
                list = params[1] as List<ProductState>? ?: emptyList(),
                onClickBack = { isProductListVisible = false },
                onProductClick = {
                    onProductClick(it) // Update the ProductAddEditViewModel with the chosen product
                    // so we open bottom document form with the chosen product
                    typeOfCreation = TypeOfBottomSheetForm.ADD_PRODUCT
                    isDocumentFormVisible = true
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(TimeUnit.MILLISECONDS.toMillis(500))
                        // Waits for the bottom form to be opened,
                        // so previous screen change is in background
                        isProductListVisible = false
                    }
                },
                onClickNewProduct = {
                    typeOfCreation = TypeOfBottomSheetForm.NEW_PRODUCT
                    isDocumentFormVisible = true
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(TimeUnit.MILLISECONDS.toMillis(500))
                        isProductListVisible = false
                    }
                },
                currentProductsIds = currentProductsIds
            )
        }

        if (isDocumentFormVisible) {
            SlideUpTheForm(
                typeOfCreation,
                documentProduct = documentProductUiState,
                taxRates = params[2] as List<BigDecimal>,
                bottomFormOnValueChange = { screenElement, value ->
                    bottomFormOnValueChange(screenElement, value)
                },
                productPlaceCursorAtTheEndOfText = productPlaceCursorAtTheEndOfText,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlideUpTheForm(
    typeOfElement: TypeOfBottomSheetForm?,
    documentProduct: DocumentProductState,
    taxRates: List<BigDecimal>,
    onClickCancel: () -> Unit,
    onClickDone: () -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any) -> Unit,
    productPlaceCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    var isTaxSelectionVisible by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onClickCancel,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(modifier = Modifier.padding(bottom = bottomPadding)) {
            Row(
                modifier = Modifier
                    .padding(top = 60.dp, end = 30.dp, start = 30.dp)
                    .bottomBorder(1.dp, ColorDarkGray)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 20.dp)
                            .clickable {
                                if (isTaxSelectionVisible) {
                                    isTaxSelectionVisible = false
                                } else onClickCancel()
                            },
                        style = MaterialTheme.typography.textSmall,
                        text = if (!isTaxSelectionVisible) {
                            stringResource(id = R.string.delivery_note_modal_product_cancel)
                        } else stringResource(id = R.string.delivery_note_modal_product_tva_back)
                    )
                    Text(
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.TopCenter),
                        style = MaterialTheme.typography.textTitle,
                        text = when (typeOfElement) {
                            TypeOfBottomSheetForm.NEW_ISSUER -> stringResource(id = R.string.delivery_note_modal_new_issuer)
                            TypeOfBottomSheetForm.NEW_CLIENT -> stringResource(id = R.string.delivery_note_modal_new_client)
                            TypeOfBottomSheetForm.EDIT_DOCUMENT_PRODUCT -> stringResource(id = R.string.delivery_note_modal_edit_product)
                            TypeOfBottomSheetForm.ADD_PRODUCT -> stringResource(id = R.string.delivery_note_modal_add_product)
                            TypeOfBottomSheetForm.NEW_PRODUCT -> stringResource(id = R.string.delivery_note_modal_new_product)
                            else -> ""
                        }
                    )
                    Text(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .align(Alignment.TopEnd)
                            .clickable { onClickDone() },
                        style = MaterialTheme.typography.textSmall,
                        text = if (!isTaxSelectionVisible) {
                            stringResource(id = R.string.delivery_note_modal_product_save)
                        } else ""
                    )
                }
            }
            if (typeOfElement == TypeOfBottomSheetForm.NEW_CLIENT || typeOfElement == TypeOfBottomSheetForm.NEW_ISSUER) {
                ClientOrIssuerAddEditForm(
                    clientOrIssuer = ClientOrIssuerState(),
                    onValueChange = bottomFormOnValueChange,
                    placeCursorAtTheEndOfText = productPlaceCursorAtTheEndOfText,
                    isDisplayedInBottomSheet = true

                )
            } else {
                if (!isTaxSelectionVisible) {
                    DocumentProductAddEditForm(
                        documentProduct = documentProduct,
                        bottomFormOnValueChange = bottomFormOnValueChange,
                        placeCursorAtTheEndOfText = productPlaceCursorAtTheEndOfText,
                        onClickForward = {
                            isTaxSelectionVisible = true
                        }
                    )
                } else {
                    OpenTaxSelection(
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
}

@Composable
fun OpenTaxSelection(
    taxRates: List<BigDecimal>,
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

enum class TypeOfBottomSheetForm {
    NEW_CLIENT,
    ADD_CLIENT,
    NEW_ISSUER,
    ADD_ISSUER,
    EDIT_DOCUMENT_PRODUCT,
    ADD_PRODUCT,
    NEW_PRODUCT
}

//TODO animation: slide elements from right to left on open, and left to right on close
/*AnimatedContent(
    targetState = pageElement,
    transitionSpec = {
        ContentTransform(
            targetContentEnter = slideInHorizontally(
                animationSpec = tween(
                    durationMillis= 14000,
                    easing = LinearEasing
                )
            ),
            initialContentExit = slideOutHorizontally(
                animationSpec = tween(
                    durationMillis= 14000,
                    easing = LinearEasing
                )
            ),
            sizeTransform = SizeTransform(sizeAnimationSpec = { _, _ -> tween() })
        )
    }, label = ""
) { targetState ->
    if (targetState == PageElement.CLIENT) {
        DeliveryNoteBottomSheetList(
            // clients = parameters?.let { it as List<ClientOrIssuerEditable> } ?: emptyList(),
            onClickBack = onClickBack,
            onItemClick = onItemClick
        )
    }
}*/


/*
if (pageElement == PageElement.CLIENT) {
    // Animated visibility can contain only one
    //composable, unless the exit won't work (that's why the if(pageElement..) is outside)
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it }, // it = fullWidth
            animationSpec = tween(
                easing = LinearEasing
            )
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(
                easing = LinearEasing
            )
        )
    ) {
        DeliveryNoteBottomSheetList(
            clients = parameters?.let { it as List<ClientOrIssuerEditable> } ?: emptyList(),
            onClickBack = onClickBack,
            onItemClick = onItemClick
        )
    }
}
if (pageElement == PageElement.DATE) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it }, // it = fullWidth
            animationSpec = tween(
                easing = LinearEasing
            )
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(
                easing = LinearEasing
            )
        )
    ) {
        DeliveryNoteBottomSheetDatePicker(
            initialDate = parameters?.let { it as String } ?: "",
            datePickerState = datePickerState,
            onClickBack = onClickBack,
        )
    }
}
*/
