package com.a4a.g8invoicing.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.a4a.g8invoicing.data.ClientOrIssuerEditable
import com.a4a.g8invoicing.data.DeliveryNoteEditable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.DocumentProductEditable
import com.a4a.g8invoicing.data.ProductEditable
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.icons.IconArrowDropDown
import com.a4a.g8invoicing.ui.shared.keyboardAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.theme.ColorDarkGray
import com.a4a.g8invoicing.ui.theme.textSmall
import com.a4a.g8invoicing.ui.theme.textTitle
import java.math.BigDecimal


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
)
@Composable
fun DeliveryNoteBottomSheet(
    deliveryNote: DeliveryNoteEditable,
    datePickerState: DatePickerState,
    onDismissBottomSheet: () -> Unit,
    onClickForward: (ScreenElement) -> Unit,
    isBottomSheetVisible: Boolean,
    onValueChange: (ScreenElement, Any) -> Unit,
    clients: MutableList<ClientOrIssuerEditable>,
    issuers: MutableList<ClientOrIssuerEditable>,
    products: MutableList<ProductEditable>,
    onClickNewClientOrIssuer: (PersonType) -> Unit = {},
    // onClickNewProduct: () -> Unit = {},
    //onDocumentProductClick: (Int) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    // onProductClick: (Int) -> Unit,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
    currentProductsIds: List<Int>? = null,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    productOnValueChange: (ScreenElement, Any, Int) -> Unit,
    productPlaceCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDoneForm: (DocumentProductEditable, TypeOfProductCreation) -> Unit,
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
                // onDocumentProductClick = onDocumentProductClick,
                onClickDeleteDocumentProduct = onClickDeleteDocumentProduct,
                // onProductClick =  onProductClick,
                onClickNewClientOrIssuer = {
                    if (slideOtherComponent.value == ScreenElement.DOCUMENT_ISSUER) {
                        onClickNewClientOrIssuer(PersonType.Issuer)
                    } else {
                        onClickNewClientOrIssuer(PersonType.Client)
                    }
                },
                //  onClickNewProduct = BetterModalBottomSheet(),
                datePickerState = datePickerState,
                currentClientId = currentClientId,
                currentIssuerId = currentIssuerId,
                currentProductsIds = currentProductsIds,
                productOnValueChange = productOnValueChange,
                productPlaceCursorAtTheEndOfText = productPlaceCursorAtTheEndOfText,
                onClickDoneForm = onClickDoneForm
            )
        }
    }
    // Closing the bottom sheet when going back with system navigation
    BackHandler(enabled = isBottomSheetVisible) {
        onDismissBottomSheet()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlideInNextComponent(
    pageElement: ScreenElement?,
    parameters: Any?,
    onClickBack: () -> Unit,
    onClientOrIssuerClick: (ClientOrIssuerEditable) -> Unit,
    // onDocumentProductClick: (Int) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    //  onProductClick: (Int) -> Unit,
    onClickNewClientOrIssuer: () -> Unit,
    //  onClickNewProduct: () -> Unit,
    datePickerState: DatePickerState,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
    currentProductsIds: List<Int>? = null,
    productOnValueChange: (ScreenElement, Any, Int) -> Unit,
    productPlaceCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDoneForm: (DocumentProductEditable, TypeOfProductCreation) -> Unit,

    ) {
    var isProductListVisible by remember { mutableStateOf(false) }
    var productCreation: TypeOfProductCreation? by remember { mutableStateOf(null) }
    var documentProductId: Int? by remember { mutableStateOf(null) }
    var productId: Int? by remember { mutableStateOf(null) }
    var documentProduct: DocumentProductEditable? by remember {
        mutableStateOf(
            DocumentProductEditable()
        )
    }

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
        val params = parameters as Pair<List<DocumentProductEditable>, List<ProductEditable>>?

        DeliveryNoteBottomSheetDocumentProductList(
            list = params?.first ?: emptyList(),
            onClickBack = onClickBack,
            onClickChooseProduct = { isProductListVisible = true },
            onDocumentProductClick = {
                documentProductId = it
                // documentProduct = params?.first?.firstOrNull { it.id == documentProductId }
                productCreation = TypeOfProductCreation.EDIT_DOCUMENT_PRODUCT
            },
            onClickDeleteDocumentProduct = onClickDeleteDocumentProduct
        )

        if (isProductListVisible) {
            DeliveryNoteBottomSheetProductList(
                list = params?.second ?: emptyList(),
                onClickBack = { isProductListVisible = false },
                onProductClick = {
/*                    productId = it
                    val product = params?.second?.firstOrNull { it.productId == productId }
                    documentProduct = DocumentProductEditable(
                        id = null,
                        name = product?.name ?: TextFieldValue(""),
                        description = product?.description,
                        finalPrice = product?.finalPrice ?: BigDecimal(0),
                        priceWithoutTax = product?.priceWithoutTax ?: BigDecimal(0),
                        taxRate = product?.taxRate ?: BigDecimal(0),
                        quantity = BigDecimal(1),
                        unit = product?.unit,
                        productId = productId
                    )
                    productCreation = TypeOfProductCreation.ADD_DOCUMENT_PRODUCT*/
                },
                onClickNewProduct = {
                    productCreation = TypeOfProductCreation.CREATE_NEW_PRODUCT
                },
                currentProductsIds = currentProductsIds
            )
        }

        if (productCreation != null) {
            SlideUpTheForm(
                productCreation,
                onDismissRequest = {
                    productCreation = null
                    documentProductId = null
                    productId = null
                },
                documentProduct = params?.first?.firstOrNull { it.id == documentProductId },
                productOnValueChange = { screenElement, value ->
                    documentProductId?.let {
                        productOnValueChange(screenElement, value, it)
                    }
                },
                productPlaceCursorAtTheEndOfText = productPlaceCursorAtTheEndOfText,
                onClickDone = {
                    if (documentProductId != null) { // Existing document product being edited
                        documentProduct?.let {
                            onClickDoneForm(it, TypeOfProductCreation.EDIT_DOCUMENT_PRODUCT)
                        }
                    } else if (productId != null) { // New document product (user has clicked on a product)
                        documentProduct?.let {
                            onClickDoneForm(it, TypeOfProductCreation.ADD_DOCUMENT_PRODUCT)
                        }
                    } else {
                        documentProduct?.let {// New product (user has clicked "New product")
                            onClickDoneForm(it, TypeOfProductCreation.CREATE_NEW_PRODUCT)
                        }
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlideUpTheForm(
    productCreation: TypeOfProductCreation?,
    onDismissRequest: () -> Unit,
    onClickDone: () -> Unit,
    productOnValueChange: (ScreenElement, Any) -> Unit,
    productPlaceCursorAtTheEndOfText: (ScreenElement) -> Unit,
    documentProduct: DocumentProductEditable?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest()
        },
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
                            .clickable { onDismissRequest() },
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
                            TypeOfProductCreation.ADD_DOCUMENT_PRODUCT -> stringResource(id = R.string.delivery_note_modal_add_product)
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
                product = documentProduct,
                onValueChange = productOnValueChange,
                placeCursorAtTheEndOfText = productPlaceCursorAtTheEndOfText,
                onClickForward = {}
            )
        }

    }
}

enum class TypeOfProductCreation {
    EDIT_DOCUMENT_PRODUCT,
    ADD_DOCUMENT_PRODUCT,
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
