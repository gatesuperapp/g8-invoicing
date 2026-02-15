package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_modal_add_product
import com.a4a.g8invoicing.shared.resources.document_modal_edit_client
import com.a4a.g8invoicing.shared.resources.document_modal_edit_issuer
import com.a4a.g8invoicing.shared.resources.document_modal_edit_product
import com.a4a.g8invoicing.shared.resources.document_modal_new_client
import com.a4a.g8invoicing.shared.resources.document_modal_new_issuer
import com.a4a.g8invoicing.shared.resources.document_modal_new_product
import com.a4a.g8invoicing.shared.resources.document_modal_product_back
import com.a4a.g8invoicing.shared.resources.document_modal_product_cancel
import com.a4a.g8invoicing.shared.resources.document_modal_product_save
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerAddEditForm
import com.a4a.g8invoicing.ui.screens.ProductTaxRatesContent
import com.a4a.g8invoicing.ui.shared.FormInputsValidator
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.theme.callForActionsDisabled
import com.a4a.g8invoicing.ui.theme.callForActionsViolet
import com.a4a.g8invoicing.ui.viewmodels.ProductType
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentBottomSheetForm(
    typeOfCreation: DocumentBottomSheetTypeOfForm?,
    documentClientUiState: ClientOrIssuerState = ClientOrIssuerState(),
    documentIssuerUiState: ClientOrIssuerState = ClientOrIssuerState(),
    documentProduct: DocumentProductState = DocumentProductState(),
    taxRates: List<BigDecimal>? = null,
    onClickCancel: () -> Unit, // Called when the bottom sheet is fully dismissed by cancel/back
    onClickDone: () -> Unit,   // Called when the main form is submitted
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    onClickDeleteAddress: (ClientOrIssuerType) -> Unit = {},
    onClickDeleteEmail: (ClientOrIssuerType, Int) -> Unit = { _, _ -> },
    onAddEmail: (ClientOrIssuerType, String) -> Unit = { _, _ -> },
    onPendingEmailValidationResult: (ClientOrIssuerType, Boolean) -> Unit = { _, _ -> },
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // State holder to access pending email from child component
    val pendingEmailState = remember { mutableStateOf("") }
    // Track if there's an email validation error to show
    var emailValidationError by remember { mutableStateOf<String?>(null) }
    // State for the ModalBottomSheet itself
    // confirmValueChange prevents swipe-to-dismiss (only allow expanded state)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    val scope = rememberCoroutineScope()

    // State to manage visibility of the tax selection screen
    var isTaxSelectionVisible by remember { mutableStateOf(false) }
    // State to determine if a text field (name or description) should be shown in full screen
    val fullScreenElementToShow: MutableState<ScreenElement?> = remember { mutableStateOf(null) }
    // State to hold the text being edited in the full-screen text editor
    var fullScreenEditorText by remember { mutableStateOf(TextFieldValue("")) }

    // Effect to initialize/update `fullScreenEditorText` when `fullScreenElementToShow` changes
    LaunchedEffect(fullScreenElementToShow.value) {
        fullScreenElementToShow.value?.let { screenElement ->
            fullScreenEditorText = if (screenElement == ScreenElement.DOCUMENT_PRODUCT_NAME) {
                documentProduct.name
            } else { // Assuming any other full screen element is for description
                documentProduct.description ?: TextFieldValue("")
            }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        dragHandle = null, // Using a custom header, so no default drag handle needed
        onDismissRequest = {
            // This is called when the user tries to dismiss the sheet (e.g., by swiping down or pressing back)
            when {
                isTaxSelectionVisible -> {
                    // Navigate back from tax selection but keep sheet open
                    isTaxSelectionVisible = false
                    scope.launch { sheetState.expand() }
                }
                fullScreenElementToShow.value != null -> {
                    // Navigate back from full screen text editor but keep sheet open
                    fullScreenElementToShow.value = null
                    scope.launch { sheetState.expand() }
                }
                else -> onClickCancel() // Fully dismiss the bottom sheet
            }
        },
        // Note: sheetGesturesEnabled is not available in Compose Multiplatform
    ) {
        Column {
            DocumentBottomSheetHeader(
                typeOfCreation = typeOfCreation,
                isTaxSelectionVisible = isTaxSelectionVisible,
                fullScreenElementCurrentlyShown = fullScreenElementToShow.value,
                isDoneButtonEnabled = calculateIsDoneButtonEnabled( // Calculate if the done button should be enabled
                    typeOfCreation,
                    documentClientUiState,
                    documentIssuerUiState,
                    documentProduct,
                    fullScreenElementToShow.value
                ),
                onClickCancelOrBack = { // Handles both "Cancel" and "Back" from sub-screens
                    handleBackNavigation(
                        isTaxSelectionVisible = isTaxSelectionVisible,
                        fullScreenElementToShowState = fullScreenElementToShow, // Pass the MutableState
                        onDismissSheet = onClickCancel,
                        onNavigateBackFromTaxSelection = { isTaxSelectionVisible = false },
                        onNavigateBackFromFullScreen = { fullScreenElementToShow.value = null }
                    )
                },
                onClickDoneMain = {
                    // Hide keyboard
                    keyboardController?.hide()

                    // Check if there's a pending email to validate
                    val pendingEmail = pendingEmailState.value.trim()
                    if (pendingEmail.isNotEmpty()) {
                        // Validate the pending email
                        val validationError = FormInputsValidator.validateEmail(pendingEmail)
                        if (validationError != null) {
                            // Invalid email - show error and don't proceed
                            emailValidationError = validationError
                            return@DocumentBottomSheetHeader
                        }
                        // Valid email - add it first
                        val clientOrIssuerType = when {
                            typeOfCreation.toString().contains(ClientOrIssuerType.CLIENT.name) -> ClientOrIssuerType.DOCUMENT_CLIENT
                            typeOfCreation.toString().contains(ClientOrIssuerType.ISSUER.name) -> ClientOrIssuerType.DOCUMENT_ISSUER
                            else -> null
                        }
                        clientOrIssuerType?.let { onAddEmail(it, pendingEmail) }
                        // Clear the pending email state
                        pendingEmailState.value = ""
                    }
                    // Proceed with saving
                    onClickDone()
                }, // Main "Done" action for the form
                onClickDoneFullScreen = { // "Done" action for the full-screen text editor
                    fullScreenElementToShow.value?.let { screenElement ->
                        bottomFormOnValueChange(screenElement, fullScreenEditorText, null)
                    }
                    fullScreenElementToShow.value = null // Exit full screen mode
                }
            )

            DocumentBottomSheetContent(
                typeOfCreation = typeOfCreation,
                documentClientUiState = documentClientUiState,
                documentIssuerUiState = documentIssuerUiState,
                documentProduct = documentProduct,
                taxRates = taxRates,
                isTaxSelectionVisible = isTaxSelectionVisible,
                fullScreenElementCurrentlyShown = fullScreenElementToShow.value,
                currentTaxRate = documentProduct.taxRate,
                fullScreenTextForEditor = fullScreenEditorText, // Pass the current text for the editor
                onFullScreenTextChanged = { newText -> fullScreenEditorText = newText }, // Callback to update the editor's text
                bottomFormOnValueChange = bottomFormOnValueChange,
                bottomFormPlaceCursor = bottomFormPlaceCursor,
                onSelectTaxRate = { selectedRate ->
                    isTaxSelectionVisible = false // Hide tax selection after selection
                    onSelectTaxRate(selectedRate)
                },
                onClickDeleteAddress = onClickDeleteAddress,
                onClickDeleteEmail = onClickDeleteEmail,
                onAddEmail = onAddEmail,
                pendingEmailStateHolder = pendingEmailState,
                onPendingEmailValidationResult = onPendingEmailValidationResult,
                onNavigateToFullScreenText = { screenElement -> // Callback to show full screen editor
                    fullScreenElementToShow.value = screenElement
                    // fullScreenEditorText is initialized by the LaunchedEffect
                },
                onNavigateToTaxSelection = { isTaxSelectionVisible = true } // Callback to show tax selection
            )
        }
    }
}

/**
 * Calculates whether the main "Done" or "Save" button in the header should be enabled.
 */
@Composable
private fun calculateIsDoneButtonEnabled(
    typeOfCreation: DocumentBottomSheetTypeOfForm?,
    documentClientUiState: ClientOrIssuerState,
    documentIssuerUiState: ClientOrIssuerState,
    documentProduct: DocumentProductState,
    fullScreenElementCurrentlyShown: ScreenElement?
): Boolean {
    if (fullScreenElementCurrentlyShown != null) {
        return true // Always enabled in full screen text edit mode (save is per field)
    }
    return when {
        typeOfCreation.toString().contains(ClientOrIssuerType.CLIENT.name) &&
                documentClientUiState.name.text.isNotEmpty() -> true

        typeOfCreation.toString().contains(ClientOrIssuerType.ISSUER.name) &&
                documentIssuerUiState.name.text.isNotEmpty() -> true

        typeOfCreation.toString().contains(ProductType.PRODUCT.name) &&
                documentProduct.name.text.isNotEmpty() &&
                documentProduct.quantity != BigDecimal.ZERO -> true // Use BigDecimal.ZERO for comparison

        else -> false
    }
}

/**
 * Handles the logic for the "Cancel" or "Back" button press.
 */
private fun handleBackNavigation(
    isTaxSelectionVisible: Boolean,
    fullScreenElementToShowState: MutableState<ScreenElement?>, // Receive the MutableState to modify it
    onDismissSheet: () -> Unit,
    onNavigateBackFromTaxSelection: () -> Unit,
    onNavigateBackFromFullScreen: () -> Unit
) {
    when {
        isTaxSelectionVisible -> onNavigateBackFromTaxSelection()
        fullScreenElementToShowState.value != null -> onNavigateBackFromFullScreen()
        else -> onDismissSheet()
    }
}

/**
 * Composable for the header of the bottom sheet.
 * Contains Cancel/Back button, Title, and Done/Save button.
 */
@Composable
private fun DocumentBottomSheetHeader(
    typeOfCreation: DocumentBottomSheetTypeOfForm?,
    isTaxSelectionVisible: Boolean,
    fullScreenElementCurrentlyShown: ScreenElement?,
    isDoneButtonEnabled: Boolean,
    onClickCancelOrBack: () -> Unit,
    onClickDoneMain: () -> Unit, // For the main form
    onClickDoneFullScreen: () -> Unit // For the full-screen text editor
) {
    // Hoist string resources
    val cancelText = stringResource(Res.string.document_modal_product_cancel)
    val backText = stringResource(Res.string.document_modal_product_back)
    val saveText = stringResource(Res.string.document_modal_product_save)
    val newClientText = stringResource(Res.string.document_modal_new_client)
    val newIssuerText = stringResource(Res.string.document_modal_new_issuer)
    val newProductText = stringResource(Res.string.document_modal_new_product)
    val addProductText = stringResource(Res.string.document_modal_add_product)
    val editClientText = stringResource(Res.string.document_modal_edit_client)
    val editIssuerText = stringResource(Res.string.document_modal_edit_issuer)
    val editProductText = stringResource(Res.string.document_modal_edit_product)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 50.dp, // Increased to avoid camera notch
                end = 30.dp,
                start = 30.dp
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Cancel or Back button
            Text(
                style = MaterialTheme.typography.callForActionsViolet,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 20.dp) // Consistent padding
                    .clickable { onClickCancelOrBack() },
                text = if (!isTaxSelectionVisible && fullScreenElementCurrentlyShown == null) {
                    cancelText
                } else {
                    backText
                }
            )

            // Modal Title
            Text(
                modifier = Modifier
                    .padding(bottom = 20.dp) // Consistent padding
                    .align(Alignment.TopCenter),
                style = MaterialTheme.typography.titleMedium,
                text = when (typeOfCreation) {
                    DocumentBottomSheetTypeOfForm.NEW_CLIENT -> newClientText
                    DocumentBottomSheetTypeOfForm.NEW_ISSUER -> newIssuerText
                    DocumentBottomSheetTypeOfForm.NEW_PRODUCT -> newProductText
                    DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT -> addProductText
                    DocumentBottomSheetTypeOfForm.EDIT_CLIENT -> editClientText
                    DocumentBottomSheetTypeOfForm.EDIT_ISSUER -> editIssuerText
                    DocumentBottomSheetTypeOfForm.EDIT_PRODUCT -> editProductText
                    else -> "" // Default empty title
                }
            )

            // Done or Save button
            Text(
                style = if (isDoneButtonEnabled) MaterialTheme.typography.callForActionsViolet
                else MaterialTheme.typography.callForActionsDisabled,
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 20.dp) // Consistent padding
                    .align(Alignment.TopEnd)
                    .clickable(enabled = isDoneButtonEnabled) {
                        if (fullScreenElementCurrentlyShown != null) {
                            onClickDoneFullScreen() // Specific action for full screen
                        } else {
                            onClickDoneMain() // Main form submission
                        }
                    },
                text = if (!isTaxSelectionVisible) { // Only show "Save" if not in tax selection
                    saveText
                } else {
                    "" // No "Save" text during tax selection
                }
            )
        }
    }
}

/**
 * Composable for the main content area of the bottom sheet.
 * Switches between Client/Issuer form, Product form, FullScreen text editor, or Tax selection.
 */
@Composable
private fun DocumentBottomSheetContent(
    typeOfCreation: DocumentBottomSheetTypeOfForm?,
    documentClientUiState: ClientOrIssuerState,
    documentIssuerUiState: ClientOrIssuerState,
    documentProduct: DocumentProductState,
    taxRates: List<BigDecimal>?,
    isTaxSelectionVisible: Boolean,
    fullScreenElementCurrentlyShown: ScreenElement?,
    currentTaxRate: BigDecimal?,
    fullScreenTextForEditor: TextFieldValue, // Current text for the full screen editor
    onFullScreenTextChanged: (TextFieldValue) -> Unit, // Callback when full screen text changes
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    onClickDeleteAddress: (ClientOrIssuerType) -> Unit,
    onClickDeleteEmail: (ClientOrIssuerType, Int) -> Unit,
    onAddEmail: (ClientOrIssuerType, String) -> Unit,
    pendingEmailStateHolder: MutableState<String>,
    onPendingEmailValidationResult: (ClientOrIssuerType, Boolean) -> Unit,
    onNavigateToFullScreenText: (ScreenElement) -> Unit, // Callback to request full screen view
    onNavigateToTaxSelection: (ScreenElement) -> Unit // Callback to request tax selection view
) {
    // Determine which form or view to show based on the current state
    when {
        typeOfCreation.toString().contains(ClientOrIssuerType.CLIENT.name) -> {
            ClientOrIssuerAddEditForm(
                clientOrIssuerUiState = documentClientUiState,
                typeOfCreation = typeOfCreation,
                onValueChange = { screenElement, value ->
                    bottomFormOnValueChange(screenElement, value, ClientOrIssuerType.DOCUMENT_CLIENT)
                },
                placeCursorAtTheEndOfText = { screenElement ->
                    bottomFormPlaceCursor(screenElement, ClientOrIssuerType.DOCUMENT_CLIENT)
                },
                isInBottomSheetModal = true,
                onClickDeleteAddress = { onClickDeleteAddress(ClientOrIssuerType.DOCUMENT_CLIENT) },
                onClickDeleteEmail = { index -> onClickDeleteEmail(ClientOrIssuerType.DOCUMENT_CLIENT, index) },
                onAddEmail = { email -> onAddEmail(ClientOrIssuerType.DOCUMENT_CLIENT, email) },
                pendingEmailStateHolder = pendingEmailStateHolder,
                onPendingEmailValidationResult = { isValid -> onPendingEmailValidationResult(ClientOrIssuerType.DOCUMENT_CLIENT, isValid) }
            )
        }

        typeOfCreation.toString().contains(ClientOrIssuerType.ISSUER.name) -> {
            ClientOrIssuerAddEditForm(
                clientOrIssuerUiState = documentIssuerUiState,
                typeOfCreation = typeOfCreation,
                onValueChange = { screenElement, value ->
                    bottomFormOnValueChange(screenElement, value, ClientOrIssuerType.DOCUMENT_ISSUER)
                },
                placeCursorAtTheEndOfText = { screenElement ->
                    bottomFormPlaceCursor(screenElement, ClientOrIssuerType.DOCUMENT_ISSUER)
                },
                isInBottomSheetModal = true,
                onClickDeleteAddress = { onClickDeleteAddress(ClientOrIssuerType.DOCUMENT_ISSUER) },
                onClickDeleteEmail = { index -> onClickDeleteEmail(ClientOrIssuerType.DOCUMENT_ISSUER, index) },
                onAddEmail = { email -> onAddEmail(ClientOrIssuerType.DOCUMENT_ISSUER, email) },
                pendingEmailStateHolder = pendingEmailStateHolder,
                onPendingEmailValidationResult = { isValid -> onPendingEmailValidationResult(ClientOrIssuerType.DOCUMENT_ISSUER, isValid) }
            )
        }

        else -> { // Product related content
            // The order of these conditions is important for clarity and correctness
            if (isTaxSelectionVisible) {
                // If tax selection is active, it takes precedence
                DocumentBottomSheetTaxSelection(
                    taxRates = taxRates,
                    currentTaxRate = currentTaxRate,
                    onSelectTaxRate = onSelectTaxRate
                )
            } else if (fullScreenElementCurrentlyShown != null) {
                // If not in tax selection, but in full-screen text mode
                DocumentBottomSheetLargeText(
                    initialText = fullScreenTextForEditor,
                    onValueChange = onFullScreenTextChanged
                )
            } else {
                // Default for product: show the product add/edit form
                // This means !isTaxSelectionVisible && fullScreenElementCurrentlyShown == null
                DocumentBottomSheetProductAddEditForm(
                    documentProduct = documentProduct,
                    bottomFormOnValueChange = { screenElement, value ->
                        bottomFormOnValueChange(screenElement, value, null)
                    },
                    placeCursorAtTheEndOfText = { screenElement ->
                        bottomFormPlaceCursor(screenElement, null)
                    },
                    onClickForward = onNavigateToTaxSelection,
                    showFullScreenText = onNavigateToFullScreenText
                )
            }
        }
    }
}


/**
 * Composable for the tax selection part of the bottom sheet.
 */
@Composable
fun DocumentBottomSheetTaxSelection(
    taxRates: List<BigDecimal>?,
    currentTaxRate: BigDecimal?,
    onSelectTaxRate: (BigDecimal?) -> Unit,
) {
    ProductTaxRatesContent( // Assuming this composable displays the list of tax rates
        taxRates = taxRates,
        currentTaxRate = currentTaxRate,
        onSelectTaxRate = onSelectTaxRate, // Ensure prop name matches if different in ProductTaxRatesContent
        isDisplayedInBottomSheet = true // Or any other relevant prop for styling/layout
    )
}
