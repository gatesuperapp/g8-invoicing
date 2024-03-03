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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.a4a.g8invoicing.data.ClientOrIssuerEditable
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryNoteBottomSheet(
    deliveryNote: DeliveryNoteState,
    datePickerState: DatePickerState,
    onDismissBottomSheet: () -> Unit,
    onValueChange: (ScreenElement, Any) -> Unit,
    clients: MutableList<ClientOrIssuerEditable>,
    issuers: MutableList<ClientOrIssuerEditable>,
    products: MutableList<ProductState>,
    onClickNewClientOrIssuer: (PersonType) -> Unit = {},
    onProductClick: (ProductState) -> Unit,
    documentProductUiState: DocumentProductState,
    onNewProductClick: () -> Unit = {},
    onDocumentProductClick: (DocumentProductState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
    currentProductsIds: List<Int>? = null,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    documentProductOnValueChange: (ScreenElement, Any) -> Unit,
    productPlaceCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDoneForm: (TypeOfProductCreation) -> Unit,
    onClickCancelForm: () -> Unit,
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
                                if (keyboard.name == "Opened") {
                                    keyboardController?.hide()
                                } else {
                                    onDismissBottomSheet()
                                }
                            }) {
                        Icon(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(30.dp)
                                .align(alignment = Alignment.CenterEnd),
                            imageVector = IconArrowDropDown,
                            contentDescription = "Close"
                        )
                    }
                }
                Row {
                    DeliveryNoteBottomSheetContent(
                        deliveryNote = deliveryNote,
                        onValueChange = onValueChange,
                        onClickForward = {
                            keyboardController?.hide()
                            slideOtherComponent.value = it
                        },
                        placeCursorAtTheEndOfText = placeCursorAtTheEndOfText
                    )
                }
            }

            SlideInNextComponent(
                pageElement = slideOtherComponent.value,
                parameters = when (slideOtherComponent.value) {
                    ScreenElement.DOCUMENT_ISSUER -> issuers
                    ScreenElement.DOCUMENT_CLIENT -> clients
                    ScreenElement.DOCUMENT_PRODUCTS -> Pair(deliveryNote.documentProducts, products)
                    ScreenElement.DOCUMENT_DATE -> deliveryNote.deliveryDate
                    else -> {}
                },
                onClickBack = {
                    slideOtherComponent.value = null
                },
                onClientOrIssuerClick = {
                    // Must select it, display it
                    // in the document & go back to previous screen
                    onValueChange(slideOtherComponent.value!!, it)
                    slideOtherComponent.value = null
                },
                onProductClick = onProductClick,
                documentProductUiState = documentProductUiState,
                onNewProductClick = onNewProductClick,
                onDocumentProductClick = onDocumentProductClick,
                onClickDeleteDocumentProduct = onClickDeleteDocumentProduct,
                onClickNewClientOrIssuer = {
                    if (slideOtherComponent.value == ScreenElement.DOCUMENT_ISSUER) {
                        onClickNewClientOrIssuer(PersonType.Issuer)
                    } else {
                        onClickNewClientOrIssuer(PersonType.Client)
                    }
                },
                datePickerState = datePickerState,
                currentClientId = currentClientId,
                currentIssuerId = currentIssuerId,
                currentProductsIds = currentProductsIds,
                documentProductOnValueChange = documentProductOnValueChange,
                productPlaceCursorAtTheEndOfText = productPlaceCursorAtTheEndOfText,
                onClickDoneForm = onClickDoneForm,
                onClickCancelForm = onClickCancelForm
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
    onClientOrIssuerClick: (ClientOrIssuerEditable) -> Unit,
    onProductClick: (ProductState) -> Unit,
    documentProductUiState: DocumentProductState,
    onNewProductClick: () -> Unit,
    onDocumentProductClick: (DocumentProductState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    onClickNewClientOrIssuer: () -> Unit,
    datePickerState: DatePickerState,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
    currentProductsIds: List<Int>? = null,
    documentProductOnValueChange: (ScreenElement, Any) -> Unit,
    productPlaceCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDoneForm: (TypeOfProductCreation) -> Unit,
    onClickCancelForm: () -> Unit,

    ) {
    var isProductListVisible by remember { mutableStateOf(false) }
    var typeOfCreation: TypeOfProductCreation by remember { mutableStateOf(TypeOfProductCreation.ADD_PRODUCT) }
    var isDocumentFormVisible by remember { mutableStateOf(false) }
    var productId: Int? by remember { mutableStateOf(null) }
    // var documentProduct: DocumentProductState by remember { mutableStateOf(DocumentProductState()) }

    if (pageElement == ScreenElement.DOCUMENT_CLIENT || pageElement == ScreenElement.DOCUMENT_ISSUER) {
        DeliveryNoteBottomSheetClientOrIssuerList(
            list = parameters?.let { it as List<ClientOrIssuerEditable> } ?: emptyList(),
            pageElement = pageElement,
            onClickBack = onClickBack,
            onClientOrIssuerClick = onClientOrIssuerClick,
            onClickNewClientOrIssuer = onClickNewClientOrIssuer,
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
        val params = parameters as Pair<List<DocumentProductState>, List<ProductState>>?

        DeliveryNoteBottomSheetDocumentProductList(
            list = params?.first ?: emptyList(),
            onClickBack = onClickBack,
            onClickChooseProduct = { isProductListVisible = true },
            onDocumentProductClick = onDocumentProductClick
            /*       {
                       documentProduct = it
                       typeOfCreation = TypeOfProductCreation.EDIT_DOCUMENT_PRODUCT
                       isDocumentFormVisible = true
                   }*/,
            onClickDeleteDocumentProduct = onClickDeleteDocumentProduct
        )

        if (isProductListVisible) {
            DeliveryNoteBottomSheetProductList(
                list = params?.second ?: emptyList(),
                onClickBack = { isProductListVisible = false },
                onProductClick = {
                    onProductClick(it) // Update the ProductAddEditViewModel with the chosen product
                    // so we open bottom document form with the chosen product
                    typeOfCreation = TypeOfProductCreation.ADD_PRODUCT
                    isDocumentFormVisible = true
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(TimeUnit.MILLISECONDS.toMillis(500))
                        // Waits for the bottom form to be opened,
                        // so previous screen change is in background
                        isProductListVisible = false
                    }

                },
                onClickNewProduct = {
                    onNewProductClick()
                    typeOfCreation = TypeOfProductCreation.CREATE_NEW_PRODUCT
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
                productOnValueChange = { screenElement, value ->
                    documentProductOnValueChange(screenElement, value)
                },
                productPlaceCursorAtTheEndOfText = productPlaceCursorAtTheEndOfText,
                onClickCancel = { // Re-initialize
                    isDocumentFormVisible = false
                    onClickCancelForm()
                },
                onClickDone = {
                    onClickDoneForm(typeOfCreation)
                    isDocumentFormVisible = false
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlideUpTheForm(
    productCreation: TypeOfProductCreation?,
    onClickCancel: () -> Unit,
    onClickDone: () -> Unit,
    productOnValueChange: (ScreenElement, Any) -> Unit,
    productPlaceCursorAtTheEndOfText: (ScreenElement) -> Unit,
    documentProduct: DocumentProductState?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

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
                            .clickable { onClickCancel() },
                        style = MaterialTheme.typography.textSmall,
                        text = stringResource(id = R.string.delivery_note_modal_new_product_cancel)
                    )
                    Text(
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.TopCenter),
                        style = MaterialTheme.typography.textTitle,
                        text = when (productCreation) {
                            TypeOfProductCreation.EDIT_DOCUMENT_PRODUCT -> stringResource(id = R.string.delivery_note_modal_edit_product)
                            TypeOfProductCreation.ADD_PRODUCT -> stringResource(id = R.string.delivery_note_modal_add_product)
                            TypeOfProductCreation.CREATE_NEW_PRODUCT -> stringResource(id = R.string.delivery_note_modal_new_product)
                            else -> ""
                        }
                    )
                    Text(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .align(Alignment.TopEnd)
                            .clickable { onClickDone() },
                        style = MaterialTheme.typography.textSmall,
                        text = stringResource(id = R.string.delivery_note_modal_new_product_save)
                    )
                }
            }
            DocumentProductForm(
                documentProduct = documentProduct,
                onValueChange = productOnValueChange,
                placeCursorAtTheEndOfText = productPlaceCursorAtTheEndOfText,
                onClickForward = {}
            )
        }

    }
}

enum class TypeOfProductCreation {
    EDIT_DOCUMENT_PRODUCT,
    ADD_PRODUCT,
    CREATE_NEW_PRODUCT
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
