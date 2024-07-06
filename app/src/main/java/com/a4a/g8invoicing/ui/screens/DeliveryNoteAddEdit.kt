package com.a4a.g8invoicing.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.navigation.BottomBarEdition
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.navigation.actionComponents
import com.a4a.g8invoicing.ui.navigation.actionExport
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
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
    documentClientUiState: DocumentClientOrIssuerState,
    documentIssuerUiState: DocumentClientOrIssuerState,
    documentProductUiState: DocumentProductState,
    taxRates: List<BigDecimal>,
    products: MutableList<ProductState>,
    onClickBack: () -> Unit,
    onValueChange: (ScreenElement, Any) -> Unit, // OUT : update ui state with user input
    onClickProduct: (ProductState) -> Unit,
    onClickClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickDocumentProduct: (DocumentProductState) -> Unit,
    onClickDocumentClientOrIssuer: (DocumentClientOrIssuerState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    onClickDeleteDocumentClientOrIssuer: (Int) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
) {
    // We use BottomSheetScaffold to open a bottom sheet modal
    // (We could use ModalBottomSheet but there are issues with overlapping system navigation)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val scope = rememberCoroutineScope()

    /*    // Keyboard: if it was open and the user swipes down the bottom sheet:
        // close the keyboard (if we close keyboard before sheet, there is a weird effect)
        val keyboardController = LocalSoftwareKeyboardController.current
        if (!scaffoldState.bottomSheetState.isVisible) {
            keyboardController?.hide()
        }*/

    // Handling native navigation back action
    BackHandler {
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
    deliveryNote.documentDate =
        datePickerState.selectedDateMillis?.let { formatter.format(Date(it)) }
            ?: Strings.get(R.string.document_default_date)

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
                documentClientUiState = documentClientUiState,
                documentIssuerUiState = documentIssuerUiState,
                documentProductUiState = documentProductUiState,
                products = products,
                taxRates = taxRates,
                onValueChange = onValueChange,
                onClickProduct = onClickProduct,
                onClickClientOrIssuer = onClickClientOrIssuer,
                onClickDocumentProduct = onClickDocumentProduct,
                onClickDocumentClientOrIssuer = onClickDocumentClientOrIssuer,
                onClickDeleteDocumentProduct = onClickDeleteDocumentProduct,
                onClickDeleteDocumentClientOrIssuer = onClickDeleteDocumentClientOrIssuer,
                currentClientId = deliveryNote.documentClient?.id,
                currentIssuerId = deliveryNote.documentIssuer?.id,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                bottomFormOnValueChange = bottomFormOnValueChange,
                bottomFormPlaceCursor = bottomFormPlaceCursor,
                onClickDoneForm = onClickDoneForm,
                onClickCancelForm = onClickCancelForm,
                onSelectTaxRate = onSelectTaxRate,
                localFocusManager = LocalFocusManager.current
            )
        },
        sheetShadowElevation = 30.dp
    )
    {
        val context = LocalContext.current
        var showPopup by rememberSaveable {
            mutableStateOf(false)
        }


        // As it's not possible to have a bottom bar inside a BottomSheetScaffold,
        // as a temporary solution, we use Scaffold inside BottomSheetScaffold
        Scaffold(
            topBar = {
                DeliveryNoteAddEditTopBar(
                    navController = navController,
                    onClickBack = onClickBack,
                    onClickExport = {
                        showPopup = true
                    }
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
            if (showPopup) {
                ExportPopup(deliveryNote, onDismissRequest = { showPopup = false })
            }

            Column(
                modifier = Modifier
                    .background(Color.LightGray.copy(alpha = 0.4f))
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )
            ) {

                DeliveryNoteBasicTemplate(
                    uiState = deliveryNote,
                    onClickElement = {
                        expandBottomSheet(scope, scaffoldState)
                        /*                        when(it) {
                                                    ScreenElement.DOCUMENT_NUMBER ->
                                                     selectedItem = ScreenElement.DOCUMENT_ORDER_NUMBER
                                                    ScreenElement.DOCUMENT_DATE ->
                                                    ScreenElement.DOCUMENT_ISSUER ->
                                                    ScreenElement.DOCUMENT_CLIENT ->
                                                    ScreenElement.DOCUMENT_ORDER_NUMBER ->
                                                    ScreenElement.DOCUMENT_PRODUCTS ->*/
                    }
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
    navController: NavController,
    onClickBack: () -> Unit,
    onClickExport: () -> Unit,
) {
    TopBar(
        title = null,
        actionExport(
            onClick = onClickExport
        ),
        navController = navController,
        onClickBackArrow = onClickBack
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
        )
    )
}

@Composable
fun ExportPopup(
    deliveryNote: DeliveryNoteState,
    onDismissRequest: () -> Unit,
) {
    // full screen background
    Dialog(
        onDismissRequest = {},
        DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Blue)
                .zIndex(10F),
            contentAlignment = Alignment.Center
        ) {
            ExportPdf(deliveryNote, onDismissRequest)
        }
    }
}