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
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.icons.IconArrowDropDown
import com.a4a.g8invoicing.ui.shared.keyboardAsState
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import icons.IconDone
import java.math.BigDecimal


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryNoteBottomSheet(
    deliveryNote: DeliveryNoteState,
    datePickerState: DatePickerState,
    onDismissBottomSheet: () -> Unit,
    onValueChange: (ScreenElement, Any) -> Unit,
    clients: MutableList<ClientOrIssuerState>,
    issuers: MutableList<ClientOrIssuerState>,
    documentClientUiState: DocumentClientOrIssuerState,
    documentIssuerUiState: DocumentClientOrIssuerState,
    documentProductUiState: DocumentProductState,
    products: MutableList<ProductState>,
    taxRates: List<BigDecimal>,
    onClickProduct: (ProductState) -> Unit,
    onClickClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickDocumentProduct: (DocumentProductState) -> Unit,
    onClickDocumentClientOrIssuer: (DocumentClientOrIssuerState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    onClickDeleteDocumentClientOrIssuer: (Int) -> Unit,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
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

            DocumentBottomSheetSlideInNextComponent(
                pageElement = slideOtherComponent.value,
                parameters = when (slideOtherComponent.value) {
                    ScreenElement.DOCUMENT_ISSUER -> Pair(
                        deliveryNote.documentIssuer,
                        issuers
                    )
                    ScreenElement.DOCUMENT_CLIENT -> Pair(
                        deliveryNote.documentClient,
                        clients
                    )
                    ScreenElement.DOCUMENT_PRODUCTS -> Pair(
                        deliveryNote.documentProducts,
                        products
                    )
                    ScreenElement.DOCUMENT_DATE -> deliveryNote.documentDate
                    else -> {}
                },
                onClickBack = {
                    slideOtherComponent.value = null
                },
                documentClientUiState = documentClientUiState,
                documentIssuerUiState = documentIssuerUiState,
                documentProductUiState = documentProductUiState,
                taxRates = taxRates,
                onClickClientOrIssuer = onClickClientOrIssuer,
                onClickProduct = onClickProduct,
                onClickDocumentClientOrIssuer = onClickDocumentClientOrIssuer,
                onClickDocumentProduct = onClickDocumentProduct,
                onClickDeleteDocumentProduct = onClickDeleteDocumentProduct,
                onClickDeleteDocumentClientOrIssuer = onClickDeleteDocumentClientOrIssuer,
                datePickerState = datePickerState,
                currentClientId = currentClientId,
                currentIssuerId = currentIssuerId,
                bottomFormOnValueChange = bottomFormOnValueChange,
                productPlaceCursorAtTheEndOfText = bottomFormPlaceCursor,
                onClickDoneForm = onClickDoneForm,
                onClickCancelForm = onClickCancelForm,
                onSelectTaxRate = onSelectTaxRate
            )
        }
    }
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
