package com.a4a.g8invoicing.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.data.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.navigation.BottomBarEdition
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.navigation.actionComponents
import com.a4a.g8invoicing.ui.navigation.actionExport
import com.a4a.g8invoicing.ui.navigation.actionStyle
import com.a4a.g8invoicing.ui.shared.ScreenElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryNoteAddEdit(
    navController: NavController,
    deliveryNote: DeliveryNoteState,
    clientList: MutableList<ClientOrIssuerState>,
    issuerList: MutableList<ClientOrIssuerState>,
    clientUiState: ClientOrIssuerState,
    issuerUiState: ClientOrIssuerState,
    taxRates: List<BigDecimal>,
    products: MutableList<ProductState>,
    isNewDeliveryNote: Boolean,
    onClickShare: () -> Unit,
    onClickBack: () -> Unit,
    onValueChange: (ScreenElement, Any) -> Unit, // OUT : update ui state with user input
    onProductClick: (ProductState) -> Unit,
    documentProductUiState: DocumentProductState,
    onDocumentProductClick: (DocumentProductState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement) -> Unit,
    onClickDoneForm: (TypeOfBottomSheetForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit
) {
    // We use BottomSheetScaffold to open a bottom sheet modal
    // (We could use ModalBottomSheet but there are issues with overlapping system navigation)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
            skipHiddenState = false
        )
    )
    val scope = rememberCoroutineScope()

/*    // Keyboard: if it was open and the user swipes down the bottom sheet:
    // close the keyboard (if we close keyboard before sheet, there is a weird effect)
    val keyboardController = LocalSoftwareKeyboardController.current
    if (!scaffoldState.bottomSheetState.isVisible) {
        keyboardController?.hide()
    }
    println("ssss" + scaffoldState.bottomSheetState.currentValue)*/

    // Handling native navigation back action
    BackHandler {
        println("ssssAA" + scaffoldState.bottomSheetState.currentValue)
        // We check on bottomSheetState == "Expanded" and not on "bottomSheetState.isVisible"
        // Because of a bug: even when the bottomSheet is hidden, its state is "PartiallyExpanded"
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
           hideBottomSheet(scope, scaffoldState)
        } else {
            onClickBack()
        }
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
                    hideBottomSheet(scope, scaffoldState)
                },
                clients = clientList,
                issuers = issuerList,
                clientUiState = clientUiState,
                issuerUiState = issuerUiState,
                products = products,
                taxRates = taxRates,
                onValueChange = onValueChange,
                onProductClick = onProductClick,
                documentProductUiState = documentProductUiState,
                onDocumentProductClick = onDocumentProductClick,
                onClickDeleteDocumentProduct = onClickDeleteDocumentProduct,
                currentClientId = deliveryNote.client?.id,
                currentIssuerId = deliveryNote.issuer?.id,
                currentProductsIds = deliveryNote.documentProducts?.mapNotNull { it.productId },
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                bottomFormOnValueChange = bottomFormOnValueChange,
                bottomFormPlaceCursor = bottomFormPlaceCursor,
                onClickDoneForm = onClickDoneForm,
                onClickCancelForm = onClickCancelForm,
                onSelectTaxRate = onSelectTaxRate,
                localFocusManager =  LocalFocusManager.current
            )
        },
        sheetShadowElevation = 30.dp
    )
    {
        val context = LocalContext.current

        // As it's not possible to have a bottom bar inside a BottomSheetScaffold,
        // as a temporary solution, we use Scaffold inside BottomSheetScaffold
        Scaffold(
            topBar = {
                DeliveryNoteAddEditTopBar(
                    isNewDeliveryNote = isNewDeliveryNote,
                    navController = navController,
                    onClickShare = {
                    },
                    onClickBack = onClickBack
                )
            },
            bottomBar = {
                DeliveryNoteAddEditBottomBar(
                    onClickComponents = {
                        expandBottomSheet(scope, scaffoldState)
                    },
                    onClickStyle = {
                        Toast.makeText(
                            context,
                            "Bientôt disponible pour les abonné·e·s :)",
                            Toast.LENGTH_LONG
                        ).show()
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


                /*val widthValue = 320.dp

                ComposePagerSnapHelper(
                    width = widthValue
                ) { listState ->
                    LazyRow(
                        state = listState,
                    ) {
                        items(count = 2) { item ->
                            Card(
                                modifier = Modifier
                                    .width(widthValue)
                                    .height(350.dp)
                                    .padding(
                                        start = if (item == 0) 16.dp else 16.dp,
                                        top = 16.dp, bottom = 16.dp,
                                        end = if (item == 4) 16.dp else 8.dp
                                    ),
                            ) {
                                //Put text or whatever here

                            }
                        }
                    }
                }*/
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
  //  keyboardController: SoftwareKeyboardController?,
) {
    scope.launch {
   /*     keyboardController?.let {// If the keyboard was open, must hide it
            it.hide()
            delay(200L) // Small delay because it's janky without it,
            // added here because we can't access bottomsheet ".animate" (it's not public)
        }*/
        scaffoldState.bottomSheetState.hide()
    }
}

@Composable
private fun DeliveryNoteAddEditTopBar(
    isNewDeliveryNote: Boolean,
    navController: NavController,
    onClickShare: () -> Unit,
    onClickBack: () -> Unit,
) {
    TopBar(
        title = null,
        actionExport(
            onClick = {  }
        ),
        navController = navController,
        onClickBackArrow = {
            onClickBack()
        }
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
