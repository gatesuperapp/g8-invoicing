package com.a4a.g8invoicing.ui.screens.shared

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.a4a.g8invoicing.ui.navigation.actionTextElements
import com.a4a.g8invoicing.ui.navigation.actionExport
import com.a4a.g8invoicing.ui.navigation.actionItems
import com.a4a.g8invoicing.ui.screens.ExportPdf
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentAddEdit(
    navController: NavController,
    document: DocumentState,
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
    onClickDeleteDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    showDocumentForm: Boolean,
    onShowDocumentForm: (Boolean) -> Unit,
) {
  

    // We use BottomSheetScaffold to open a bottom sheet modal
    // (We could use ModalBottomSheet but there are issues with overlapping system navigation)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val bottomSheetType = remember { mutableStateOf(BottomSheetType.ITEMS) }
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
    val documentDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate,
        initialDisplayMode = DisplayMode.Picker
    )
    val documentDueDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate,
        initialDisplayMode = DisplayMode.Picker
    )
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
    document.documentDate =
        documentDatePickerState.selectedDateMillis?.let { formatter.format(Date(it)) }
            ?: Strings.get(R.string.document_default_date)
    if(document is InvoiceState) {
        document.dueDate = documentDueDatePickerState.selectedDateMillis?.let { formatter.format(Date(it)) }
            ?: Strings.get(R.string.document_default_date)
    }

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
            if (bottomSheetType.value == BottomSheetType.ELEMENTS) {
                DocumentBottomSheetTextElements(
                    document = document,
                    datePickerState = documentDatePickerState,
                    dueDatePickerState = documentDueDatePickerState,
                    onDismissBottomSheet = {
                        hideBottomSheet(scope, scaffoldState)
                    },
                    clients = clientList,
                    issuers = issuerList,
                    documentClientUiState = documentClientUiState,
                    documentIssuerUiState = documentIssuerUiState,
                    taxRates = taxRates,
                    onValueChange = onValueChange,
                    onClickClientOrIssuer = onClickClientOrIssuer,
                    onClickDocumentClientOrIssuer = onClickDocumentClientOrIssuer,
                    onClickDeleteDocumentClientOrIssuer = onClickDeleteDocumentClientOrIssuer,
                    currentClientId = document.documentClient?.id,
                    currentIssuerId = document.documentIssuer?.id,
                    placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                    bottomFormOnValueChange = bottomFormOnValueChange,
                    bottomFormPlaceCursor = bottomFormPlaceCursor,
                    onClickDoneForm = onClickDoneForm,
                    onClickCancelForm = onClickCancelForm,
                    onSelectTaxRate = onSelectTaxRate,
                    localFocusManager = LocalFocusManager.current,
                    showDocumentForm = showDocumentForm,
                    onShowDocumentForm = onShowDocumentForm
                )
            } else {
                DocumentBottomSheetProducts(
                    document = document,
                    onDismissBottomSheet = {
                        hideBottomSheet(scope, scaffoldState)
                    },
                    documentProductUiState = documentProductUiState,
                    products = products,
                    taxRates = taxRates,
                    onClickProduct = onClickProduct,
                    onClickDocumentProduct = onClickDocumentProduct,
                    onClickDeleteDocumentProduct = onClickDeleteDocumentProduct,
                    placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                    bottomFormOnValueChange = bottomFormOnValueChange,
                    bottomFormPlaceCursor = bottomFormPlaceCursor,
                    onClickDoneForm = onClickDoneForm,
                    onClickCancelForm = onClickCancelForm,
                    onSelectTaxRate = onSelectTaxRate,
                    localFocusManager = LocalFocusManager.current,
                    showDocumentForm = showDocumentForm,
                    onShowDocumentForm = onShowDocumentForm
                )
            }
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
                DocumentAddEditBottomBar(
                    onClickElements = {
                        bottomSheetType.value = BottomSheetType.ELEMENTS
                        expandBottomSheet(scope, scaffoldState)
                    },
                    onClickItems = {
                        bottomSheetType.value = BottomSheetType.ITEMS
                        expandBottomSheet(scope, scaffoldState)
                    },
                    onClickStyle = {
                        bottomSheetType.value = BottomSheetType.STYLE
                        Toast.makeText(
                            context,
                            "Bientôt disponible :)",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        ) { innerPadding ->
            if (showPopup) {
                ExportPopup(document, onDismissRequest = { showPopup = false })
            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .background(Color.LightGray.copy(alpha = 0.4f))
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )
            ) {
                DocumentBasicTemplate(
                    uiState = document,
                    onClickElement = {
                        if (it == ScreenElement.DOCUMENT_HEADER ||
                            it == ScreenElement.DOCUMENT_NUMBER ||
                            it == ScreenElement.DOCUMENT_DATE ||
                            it == ScreenElement.DOCUMENT_ISSUER ||
                            it == ScreenElement.DOCUMENT_CLIENT ||
                            it == ScreenElement.DOCUMENT_FOOTER ||
                            it == ScreenElement.DOCUMENT_ORDER_NUMBER
                        ) {
                            bottomSheetType.value = BottomSheetType.ELEMENTS
                        } else {
                            bottomSheetType.value = BottomSheetType.ITEMS
                        }
                        expandBottomSheet(scope, scaffoldState)
                        /*                        when(it) {
                                                    ScreenElement.DOCUMENT_NUMBER ->
                                                     selectedItem = ScreenElement.DOCUMENT_ORDER_NUMBER
                                                    ScreenElement.DOCUMENT_DATE ->
                                                    ScreenElement.DOCUMENT_ISSUER ->
                                                    ScreenElement.DOCUMENT_CLIENT ->
                                                    ScreenElement.DOCUMENT_ORDER_NUMBER ->
                                                    ScreenElement.DOCUMENT_PRODUCTS ->*/
                    },
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
private fun DocumentAddEditBottomBar(
    onClickElements: () -> Unit,
    onClickItems: () -> Unit,
    onClickStyle: () -> Unit,
) {
    BottomBarEdition(
        actions = arrayOf(
            actionTextElements(onClickElements),
            actionItems(onClickItems)
        )
    )
}

@Composable
fun ExportPopup(
    deliveryNote: DocumentState,
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
                .zIndex(10F),
            contentAlignment = Alignment.Center
        ) {
            ExportPdf(deliveryNote, onDismissRequest)
        }
    }
}

enum class BottomSheetType {
    ELEMENTS, ITEMS, IMAGES, STYLE
}