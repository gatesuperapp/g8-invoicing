package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.data.ClientOrIssuerEditable
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.navigation.BottomBarEdition
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.navigation.actionComponents
import com.a4a.g8invoicing.ui.navigation.actionDone
import com.a4a.g8invoicing.ui.navigation.actionStyle
import com.a4a.g8invoicing.ui.shared.ScreenElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun DeliveryNoteAddEdit(
    navController: NavController,
    deliveryNote: DeliveryNoteState,
    clients: MutableList<ClientOrIssuerEditable>,
    issuers: MutableList<ClientOrIssuerEditable>,
    products: MutableList<ProductState>,
    isNewDeliveryNote: Boolean,
    onClickDone: (Boolean) -> Unit,
    onClickBack: () -> Unit,
    onValueChange: (ScreenElement, Any) -> Unit, // OUT : update ui state with user input
    onClickNewClientOrIssuer: (PersonType) -> Unit,
    onProductClick: (ProductState) -> Unit,
    documentProductUiState: DocumentProductState,
    onNewProductClick: () -> Unit,
    onDocumentProductClick: (DocumentProductState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    documentProductOnValueChange: (ScreenElement, Any) -> Unit,
    documentProductPlaceCursor: (ScreenElement) -> Unit,
    onClickDoneForm: (TypeOfProductCreation) -> Unit,
    onClickCancelForm: () -> Unit,
) {
    val localFocusManager = LocalFocusManager.current

    /*    // The state is hoisted here & shared between the template & the bottom sheet
        var deliveryNote by remember { mutableStateOf(deliveryNoteUiState) }*/

    // We use BottomSheetScaffold to open a bottom sheet modal
    // (We could use ModalBottomSheet but there are issues with overlapping system navigation)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
            skipHiddenState = false
        )
    )
    val scope = rememberCoroutineScope()

    // Keyboard: if it was open and the user swipes down the bottom sheet:
    // close the keyboard (if we close keyboard before sheet, there is a weird effect)
    val keyboardController = LocalSoftwareKeyboardController.current
    val isBottomSheetOpened = scaffoldState.bottomSheetState.isVisible
    if (!isBottomSheetOpened) {
        keyboardController?.hide()
    }

    // Date picker & formatter
    val selectedDate = System.currentTimeMillis()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate,
        initialDisplayMode = DisplayMode.Picker
    )
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
    deliveryNote.deliveryDate =
        datePickerState.selectedDateMillis?.let { formatter.format(Date(it)) }

    BottomSheetScaffold(
        sheetSwipeEnabled = false,
        // The bottom sheet is still swipable though because it contains a lazy column
        // It's due to this bug https://issuetracker.google.com/issues/215403277
        sheetDragHandle = null, // Disable drag handle
        sheetShape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp
        ),// Remove rounded corners (must be a better way..)
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            DeliveryNoteBottomSheet(
                deliveryNote = deliveryNote,
                datePickerState = datePickerState,
                onDismissBottomSheet = {
                    hideBottomSheet(scope, scaffoldState, keyboardController)
                },
                isBottomSheetVisible = isBottomSheetOpened,
                clients = clients,
                issuers = issuers,
                products = products,
                onValueChange = onValueChange,
                onClickNewClientOrIssuer = onClickNewClientOrIssuer,
                onProductClick = onProductClick,
                documentProductUiState = documentProductUiState,
                onNewProductClick = onNewProductClick,
                onDocumentProductClick = onDocumentProductClick,
                onClickDeleteDocumentProduct = onClickDeleteDocumentProduct,
                currentClientId = deliveryNote.client?.id,
                currentIssuerId = deliveryNote.issuer?.id,
                currentProductsIds = deliveryNote.documentProducts?.mapNotNull { it.productId },
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                documentProductOnValueChange = documentProductOnValueChange,
                productPlaceCursorAtTheEndOfText = documentProductPlaceCursor,
                onClickDoneForm = onClickDoneForm,
                onClickCancelForm = onClickCancelForm,
            )
        },
        sheetShadowElevation = 30.dp
    )
    {
        // As it's not possible to have a bottom bar inside a BottomSheetScaffold,
        // as a temporary solution, we use Scaffold inside BottomSheetScaffold
        Scaffold(
            topBar = {
                DeliveryNoteAddEditTopBar(
                    isNewDeliveryNote = isNewDeliveryNote,
                    navController = navController,
                    onClickDone = { isNewDeliveryNote ->
                        onClickDone(isNewDeliveryNote)
                    },
                    onClickBackArrow = onClickBack
                )
            },
            bottomBar = {
                DeliveryNoteAddEditBottomBar(
                    onClickComponents = {
                        expandBottomSheet(scope, scaffoldState)
                    },
                    onClickStyle = {
                        expandBottomSheet(scope, scaffoldState)
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .background(Color.LightGray.copy(alpha = 0.4f))
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )
                    .padding(20.dp) // Adds 20dp to inner padding
            ) {
                var selectedItem: ScreenElement? by remember { mutableStateOf(null) }

                DeliveryNoteBasicTemplate(
                    uiState = deliveryNote,
                    onClickDeliveryNoteNumber = {
                        expandBottomSheet(scope, scaffoldState)
                        selectedItem = ScreenElement.DOCUMENT_NUMBER
                    },
                    onClickDate = {
                        expandBottomSheet(scope, scaffoldState)
                        selectedItem = ScreenElement.DOCUMENT_DATE
                    },
                    onClickIssuer = {
                        expandBottomSheet(scope, scaffoldState)
                        selectedItem = ScreenElement.DOCUMENT_ISSUER
                    },
                    onClickClient = {
                        expandBottomSheet(scope, scaffoldState)
                        selectedItem = ScreenElement.DOCUMENT_CLIENT
                    },
                    onClickOrderNumber = {
                        expandBottomSheet(scope, scaffoldState)
                        selectedItem = ScreenElement.DOCUMENT_ORDER_NUMBER
                    },
                    onClickDocumentProducts = {
                        expandBottomSheet(scope, scaffoldState)
                        selectedItem = ScreenElement.DOCUMENT_PRODUCTS
                    },
                    selectedItem
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun expandBottomSheet(scope: CoroutineScope, scaffoldState: BottomSheetScaffoldState) {
    scope.launch { scaffoldState.bottomSheetState.expand() }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
private fun hideBottomSheet(
    scope: CoroutineScope,
    scaffoldState: BottomSheetScaffoldState,
    keyboardController: SoftwareKeyboardController?,
) {
    scope.launch {
        keyboardController?.let {// If the keyboard was open, must hide it
            it.hide()
            delay(200L) // Small delay because it's janky without it,
            // added here because we can't access bottomsheet ".animate" (it's not public)
        }
        scaffoldState.bottomSheetState.hide()
    }
}

@Composable
private fun DeliveryNoteAddEditTopBar(
    isNewDeliveryNote: Boolean,
    navController: NavController,
    onClickDone: (Boolean) -> Unit,
    onClickBackArrow: () -> Unit,
) {
    TopBar(
        title = null,
        actionDone(
            onClick = { onClickDone(isNewDeliveryNote) }
        ),
        navController = navController,
        onClickBackArrow = onClickBackArrow
    )
}

@Composable
private fun DeliveryNoteAddEditBottomBar(
    onClickComponents: () -> Unit,
    onClickStyle: () -> Unit,
) {
    BottomBarEdition(
        actions = arrayOf(
            actionComponents(onClickComponents),
            actionStyle(onClickStyle)
        )
    )
}
