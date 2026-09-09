package com.a4a.g8invoicing.ui.navigation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.a4a.g8invoicing.ui.theme.textCta
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.version_mismatch_client_message
import com.a4a.g8invoicing.shared.resources.version_mismatch_client_title
import com.a4a.g8invoicing.shared.resources.version_mismatch_keep_current
import com.a4a.g8invoicing.shared.resources.version_mismatch_load_latest
import com.a4a.g8invoicing.shared.resources.version_mismatch_message
import com.a4a.g8invoicing.shared.resources.version_mismatch_title
import com.a4a.g8invoicing.ui.screens.shared.DocumentAddEditPlatform
import com.a4a.g8invoicing.ui.screens.shared.DocumentBottomSheetTypeOfForm
import com.a4a.g8invoicing.ui.shared.FormValidationDialogHost
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.rememberFormValidationDialogState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerListViewModel
import com.a4a.g8invoicing.ui.viewmodels.CreditNoteAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductListViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductType
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.creditNoteAddEdit(
    navController: NavController,
    onClickBack: () -> Unit,
    onShowMessage: (String) -> Unit = {},
    exportPdfContent: @Composable (DocumentState, () -> Unit) -> Unit = { _, _ -> },
) {
    composable(
        route = Screen.CreditNoteAddEdit.name + "?itemId={itemId}",
        arguments = listOf(
            navArgument("itemId") { nullable = true },
        )
    ) { backStackEntry ->
        val scope = rememberCoroutineScope()
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
        val itemId = backStackEntry.arguments?.getString("itemId")

        val creditNoteViewModel: CreditNoteAddEditViewModel = koinViewModel(
            parameters = { parametersOf(itemId) }
        )
        val uiState by creditNoteViewModel.documentUiState.collectAsState()

        val clientOrIssuerListViewModel: ClientOrIssuerListViewModel = koinViewModel()
        val clientListUiState by clientOrIssuerListViewModel.clientsUiState.collectAsState()
        val issuerListUiState by clientOrIssuerListViewModel.issuersUiState.collectAsState()

        val clientOrIssuerAddEditViewModel: ClientOrIssuerAddEditViewModel = koinViewModel()
        val documentClientUiState by clientOrIssuerAddEditViewModel.documentClientUiState.collectAsState()
        val documentIssuerUiState by clientOrIssuerAddEditViewModel.documentIssuerUiState.collectAsState()

        val productListViewModel: ProductListViewModel = koinViewModel()
        val productListUiState by productListViewModel.productsUiState.collectAsState()

        val productAddEditViewModel: ProductAddEditViewModel = koinViewModel()
        val documentProduct by productAddEditViewModel.documentProductUiState.collectAsState()
        // Product.type visibility: read from uiState.documentIssuer (authoritative
        // source, set by onSelectClientOrIssuer via saveDocumentClientOrIssuerInUiState).
        // NOT clientOrIssuerAddEditViewModel.documentIssuerUiState — that's only for
        // the issuer-edit sub-form.
        val showProductType = uiState.documentIssuer?.intraEuSales == true
        LaunchedEffect(showProductType) {
            productAddEditViewModel.setShowProductType(showProductType)
        }

        var showDocumentForm by remember { mutableStateOf(false) }
        val errorDialog = rememberFormValidationDialogState()

        // When the bottom-sheet form is open, system back closes it instead
        // of popping back to the doc list.
        PlatformBackHandler(enabled = showDocumentForm) {
            showDocumentForm = false
        }

        var showVersionMismatchDialog by remember { mutableStateOf(false) }
        var pendingIssuerToEdit by remember { mutableStateOf<ClientOrIssuerState?>(null) }
        var pendingIssuerOpensForm by remember { mutableStateOf(false) }
        var showClientVersionMismatchDialog by remember { mutableStateOf(false) }
        var pendingClientToEdit by remember { mutableStateOf<ClientOrIssuerState?>(null) }
        var pendingClientOpensForm by remember { mutableStateOf(false) }

        // Version mismatch dialog for issuer
        if (showVersionMismatchDialog && pendingIssuerToEdit != null) {
            AlertDialog(
                onDismissRequest = {
                    showVersionMismatchDialog = false
                    pendingIssuerToEdit = null
                    pendingIssuerOpensForm = false
                },
                title = { Text(stringResource(Res.string.version_mismatch_title)) },
                text = { Text(stringResource(Res.string.version_mismatch_message)) },
                confirmButton = {
                    Button(
                        onClick = {
                            val opensForm = pendingIssuerOpensForm
                            showVersionMismatchDialog = false
                            pendingIssuerToEdit = null
                            pendingIssuerOpensForm = false
                            if (opensForm) showDocumentForm = true
                            scope.launch {
                                val updated = clientOrIssuerAddEditViewModel.loadLatestMasterVersion(
                                    ClientOrIssuerType.DOCUMENT_ISSUER
                                )
                                if (updated != null) {
                                    // See NavGraphInvoiceAddEdit — same retention
                                    // toggle transition fix on the refresh path.
                                    val hadRetentions = creditNoteViewModel.documentUiState.value.retentions.isNotEmpty()
                                    if (updated.taxWithholdingEnabled && !hadRetentions) {
                                        creditNoteViewModel.seedDefaultRetentionsInDb(updated)
                                    } else if (!updated.taxWithholdingEnabled && hadRetentions) {
                                        creditNoteViewModel.clearRetentionsInDb()
                                    }
                                    val hadExemptionText = creditNoteViewModel.documentUiState.value.vatExemptionText?.text?.isNotBlank() == true
                                    if (updated.vatExempt && !hadExemptionText) {
                                        creditNoteViewModel.seedDefaultVatExemptionTextInDb(updated)
                                    }
                                    creditNoteViewModel.saveDocumentClientOrIssuerInUiState(updated)
                                    // Persist via UPDATE (id-preserving) — see
                                    // NavGraphInvoiceAddEdit for the full story.
                                    clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                        ClientOrIssuerType.DOCUMENT_ISSUER,
                                        updated,
                                        syncToMaster = false,
                                    )
                                    creditNoteViewModel.reloadDocument()
                                }
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.version_mismatch_load_latest),
                            style = MaterialTheme.typography.textCta
                        )
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            val opensForm = pendingIssuerOpensForm
                            val issuerToAck = pendingIssuerToEdit
                            showVersionMismatchDialog = false
                            pendingIssuerToEdit = null
                            pendingIssuerOpensForm = false
                            if (opensForm) showDocumentForm = true
                            // Bump the doc snapshot's originalVersion to master so the
                            // dialog stops re-firing on every reopen. Data stays frozen.
                            issuerToAck?.let { issuer ->
                                scope.launch {
                                    clientOrIssuerAddEditViewModel
                                        .acknowledgeMasterVersion(issuer)
                                        ?.let { creditNoteViewModel.saveDocumentClientOrIssuerInUiState(it) }
                                }
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.version_mismatch_keep_current),
                            style = MaterialTheme.typography.textCta
                        )
                    }
                }
            )
        }

        // Version mismatch dialog for client
        if (showClientVersionMismatchDialog && pendingClientToEdit != null) {
            AlertDialog(
                onDismissRequest = {
                    showClientVersionMismatchDialog = false
                    pendingClientToEdit = null
                    pendingClientOpensForm = false
                },
                title = { Text(stringResource(Res.string.version_mismatch_client_title)) },
                text = { Text(stringResource(Res.string.version_mismatch_client_message)) },
                confirmButton = {
                    Button(
                        onClick = {
                            val opensForm = pendingClientOpensForm
                            showClientVersionMismatchDialog = false
                            pendingClientToEdit = null
                            pendingClientOpensForm = false
                            if (opensForm) showDocumentForm = true
                            scope.launch {
                                val updated = clientOrIssuerAddEditViewModel.loadLatestMasterVersion(
                                    ClientOrIssuerType.DOCUMENT_CLIENT
                                )
                                if (updated != null) {
                                    creditNoteViewModel.saveDocumentClientOrIssuerInUiState(updated)
                                    // See the issuer path above.
                                    clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                        ClientOrIssuerType.DOCUMENT_CLIENT,
                                        updated,
                                        syncToMaster = false,
                                    )
                                    creditNoteViewModel.reloadDocument()
                                }
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.version_mismatch_load_latest),
                            style = MaterialTheme.typography.textCta
                        )
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            val opensForm = pendingClientOpensForm
                            val clientToAck = pendingClientToEdit
                            showClientVersionMismatchDialog = false
                            pendingClientToEdit = null
                            pendingClientOpensForm = false
                            if (opensForm) showDocumentForm = true
                            clientToAck?.let { client ->
                                scope.launch {
                                    clientOrIssuerAddEditViewModel
                                        .acknowledgeMasterVersion(client)
                                        ?.let { creditNoteViewModel.saveDocumentClientOrIssuerInUiState(it) }
                                }
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.version_mismatch_keep_current),
                            style = MaterialTheme.typography.textCta
                        )
                    }
                }
            )
        }

        // Observe the counter so validating the doc-product tax edit dialog
        // triggers a recomposition and the picker re-reads the fresh list.
        val taxRatesRefreshCounter by productAddEditViewModel.taxRatesRefreshCounter.collectAsState()

        DocumentAddEditPlatform(
            navController = navController,
            document = uiState,
            onClickBack = onClickBack,
            clientList = clientListUiState.clientsOrIssuerList.toMutableList(),
            issuerList = issuerListUiState.clientsOrIssuerList.toMutableList(),
            documentClientUiState = documentClientUiState,
            documentIssuerUiState = documentIssuerUiState,
            documentProductUiState = documentProduct,
            taxRates = remember(taxRatesRefreshCounter) { productAddEditViewModel.fetchTaxRatesFromLocalDb() },
            taxRatesWithIds = remember(taxRatesRefreshCounter) { productAddEditViewModel.fetchTaxRatesWithIdsFromLocalDb() },
            products = productListUiState.products.toMutableList(),
            onValueChange = { pageElement, value ->
                creditNoteViewModel.updateUiState(pageElement, value)
            },
            onSelectProduct = { product, clientId ->
                productAddEditViewModel.setDocumentProductUiStateWithProduct(product, clientId)
            },
            onClickNewDocumentProduct = {
                productAddEditViewModel.clearProductNameAndDescription()
            },
            onSelectClientOrIssuer = { clientOrIssuer ->
                // Copy instead of mutating: clientOrIssuer is the live item from the
                // master client list; mutating its id to null poisons that list and
                // makes the item disappear from the picker on the next open.
                val newType = if (clientOrIssuer.type == ClientOrIssuerType.CLIENT) {
                    documentClientUiState.type = ClientOrIssuerType.DOCUMENT_CLIENT
                    ClientOrIssuerType.DOCUMENT_CLIENT
                } else {
                    documentIssuerUiState.type = ClientOrIssuerType.DOCUMENT_ISSUER
                    ClientOrIssuerType.DOCUMENT_ISSUER
                }
                val documentClientOrIssuer = clientOrIssuer.copy(
                    id = null,
                    originalClientOrIssuerId = clientOrIssuer.id,
                    originalVersion = clientOrIssuer.version ?: 1,
                    type = newType,
                )
                creditNoteViewModel.saveDocumentClientOrIssuerInLocalDb(documentClientOrIssuer)
                creditNoteViewModel.saveDocumentClientOrIssuerInUiState(documentClientOrIssuer)
            },
            onClickEditDocumentProduct = {
                productAddEditViewModel.setDocumentProductUiState(it)
            },
            onClickNewDocumentClientOrIssuer = {
                clientOrIssuerAddEditViewModel.clearClientOrIssuerUiState(it)
            },
            onClickDocumentClientOrIssuer = { clientOrIssuer, openFormOnCompletion ->
                clientOrIssuerAddEditViewModel.setDocumentClientOrIssuerUiState(clientOrIssuer)
                if (clientOrIssuer.type == ClientOrIssuerType.DOCUMENT_ISSUER ||
                    clientOrIssuer.type == ClientOrIssuerType.ISSUER) {
                    scope.launch {
                        if (clientOrIssuerAddEditViewModel.checkVersionMismatch(clientOrIssuer)) {
                            pendingIssuerToEdit = clientOrIssuer
                            pendingIssuerOpensForm = openFormOnCompletion
                            showVersionMismatchDialog = true
                        } else if (openFormOnCompletion) {
                            showDocumentForm = true
                        }
                    }
                }
                if (clientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT ||
                    clientOrIssuer.type == ClientOrIssuerType.CLIENT) {
                    scope.launch {
                        if (clientOrIssuerAddEditViewModel.checkVersionMismatch(clientOrIssuer)) {
                            pendingClientToEdit = clientOrIssuer
                            pendingClientOpensForm = openFormOnCompletion
                            showClientVersionMismatchDialog = true
                        } else if (openFormOnCompletion) {
                            showDocumentForm = true
                        }
                    }
                }
            },
            onClickDeleteDocumentProduct = {
                creditNoteViewModel.removeDocumentProductFromUiState(it)
                creditNoteViewModel.removeDocumentProductFromLocalDb(it)
            },
            onClickDeleteDocumentClientOrIssuer = { type ->
                creditNoteViewModel.removeDocumentClientOrIssuerFromUiState(type)
                creditNoteViewModel.removeDocumentClientOrIssuerFromLocalDb(type)
            },
            placeCursorAtTheEndOfText = { pageElement ->
                if (pageElement == ScreenElement.DOCUMENT_NUMBER ||
                    pageElement == ScreenElement.DOCUMENT_REFERENCE
                ) {
                    creditNoteViewModel.updateTextFieldCursorOfCreditNoteState(pageElement)
                }
            },
            bottomFormOnValueChange = { pageElement, value, type ->
                if (pageElement.name.contains("PRODUCT")) {
                    productAddEditViewModel.updateProductState(
                        pageElement, value, ProductType.DOCUMENT_PRODUCT
                    )
                } else {
                    type?.let {
                        clientOrIssuerAddEditViewModel.updateClientOrIssuerState(
                            pageElement, value, it
                        )
                    }
                }
            },
            bottomFormPlaceCursor = { pageElement, clientOrIssuer ->
                if (pageElement.name.contains(ProductType.DOCUMENT_PRODUCT.name)) {
                    productAddEditViewModel.updateCursor(pageElement, ProductType.DOCUMENT_PRODUCT)
                } else if (clientOrIssuer == ClientOrIssuerType.DOCUMENT_ISSUER) {
                    clientOrIssuerAddEditViewModel.updateCursor(
                        pageElement, ClientOrIssuerType.DOCUMENT_ISSUER
                    )
                } else if (clientOrIssuer == ClientOrIssuerType.DOCUMENT_CLIENT) {
                    clientOrIssuerAddEditViewModel.updateCursor(
                        pageElement, ClientOrIssuerType.DOCUMENT_CLIENT
                    )
                }
            },
            onClickDoneForm = { typeOfCreation, syncToMaster ->
                scope.launch {
                    // Focus clear + keyboard hide before validate — commits
                    // any pending email so validateInputs sees the invalid
                    // value. See standalone NavGraphClientOrIssuerAddEdit
                    // for the same pattern.
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    when (typeOfCreation) {
                        DocumentBottomSheetTypeOfForm.NEW_CLIENT -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_CLIENT)) {
                                val masterId = createNewClientOrIssuer(
                                    clientOrIssuerAddEditViewModel,
                                    ClientOrIssuerType.DOCUMENT_CLIENT
                                )
                                documentClientUiState.type = ClientOrIssuerType.DOCUMENT_CLIENT
                                documentClientUiState.originalClientOrIssuerId = masterId?.toInt()
                                creditNoteViewModel.saveDocumentClientOrIssuerInUiState(documentClientUiState)
                                creditNoteViewModel.saveDocumentClientOrIssuerInLocalDb(documentClientUiState)
                                showDocumentForm = false
                            } else {
                                errorDialog.showFrom(clientOrIssuerAddEditViewModel.documentClientUiState.value.errors)
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_CLIENT -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_CLIENT)) {
                                // See NavGraphInvoiceAddEdit — read the fresh
                                // StateFlow value rather than the collectAsState
                                // snapshot to catch the cleanFieldsForClientType
                                // mutation triggered by validateInputs.
                                val freshClient = clientOrIssuerAddEditViewModel
                                    .documentClientUiState.value
                                clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                    ClientOrIssuerType.DOCUMENT_CLIENT, freshClient, syncToMaster = syncToMaster
                                )
                                creditNoteViewModel.reloadDocument()
                                showDocumentForm = false
                            } else {
                                errorDialog.showFrom(clientOrIssuerAddEditViewModel.documentClientUiState.value.errors)
                            }
                        }
                        DocumentBottomSheetTypeOfForm.NEW_ISSUER -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_ISSUER)) {
                                val masterId = createNewClientOrIssuer(
                                    clientOrIssuerAddEditViewModel,
                                    ClientOrIssuerType.DOCUMENT_ISSUER
                                )
                                documentIssuerUiState.type = ClientOrIssuerType.DOCUMENT_ISSUER
                                documentIssuerUiState.originalClientOrIssuerId = masterId?.toInt()
                                creditNoteViewModel.saveDocumentClientOrIssuerInUiState(documentIssuerUiState)
                                creditNoteViewModel.saveDocumentClientOrIssuerInLocalDb(documentIssuerUiState)
                                showDocumentForm = false
                            } else {
                                errorDialog.showFrom(clientOrIssuerAddEditViewModel.documentIssuerUiState.value.errors)
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_ISSUER -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_ISSUER)) {
                                val hadRetentions = creditNoteViewModel.documentUiState.value.retentions.isNotEmpty()
                                val turnedOffRetention = !documentIssuerUiState.taxWithholdingEnabled && hadRetentions
                                val turnedOnRetention = documentIssuerUiState.taxWithholdingEnabled && !hadRetentions
                                val hadExemptionText = creditNoteViewModel.documentUiState.value.vatExemptionText?.text?.isNotBlank() == true
                                val needsExemptionSeed = documentIssuerUiState.vatExempt && !hadExemptionText
                                clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                    ClientOrIssuerType.DOCUMENT_ISSUER, documentIssuerUiState, syncToMaster = syncToMaster
                                )
                                if (turnedOffRetention) {
                                    creditNoteViewModel.clearRetentionsInDb()
                                } else if (turnedOnRetention) {
                                    creditNoteViewModel.seedDefaultRetentionsInDb(documentIssuerUiState)
                                }
                                if (needsExemptionSeed) {
                                    creditNoteViewModel.seedDefaultVatExemptionTextInDb(documentIssuerUiState)
                                }
                                creditNoteViewModel.reloadDocument()
                                showDocumentForm = false
                            } else {
                                errorDialog.showFrom(clientOrIssuerAddEditViewModel.documentIssuerUiState.value.errors)
                            }
                        }
                        DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                val documentProductId = creditNoteViewModel.saveDocumentProductInLocalDbAndGetId(documentProduct)
                                if (documentProductId != null) {
                                    creditNoteViewModel.saveDocumentProductInUiState(documentProduct.copy(id = documentProductId))
                                    if (syncToMaster) productAddEditViewModel.syncDocumentProductToMaster()
                                    showDocumentForm = false
                                }
                            }
                        }
                        DocumentBottomSheetTypeOfForm.NEW_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                productAddEditViewModel.setProductUiState()
                                val masterProductId = productAddEditViewModel.saveProductInLocalDbAndGetId()
                                val docProductWithLink = documentProduct.copy(productId = masterProductId?.toInt())
                                val documentProductId = creditNoteViewModel.saveDocumentProductInLocalDbAndGetId(docProductWithLink)
                                if (documentProductId != null) {
                                    creditNoteViewModel.saveDocumentProductInUiState(docProductWithLink.copy(id = documentProductId))
                                    productAddEditViewModel.clearProductUiState()
                                    showDocumentForm = false
                                }
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                creditNoteViewModel.updateUiState(ScreenElement.DOCUMENT_PRODUCT, documentProduct)
                                productAddEditViewModel.updateInLocalDb(ProductType.DOCUMENT_PRODUCT)
                                if (syncToMaster) productAddEditViewModel.syncDocumentProductToMaster()
                                productAddEditViewModel.clearProductUiState()
                                showDocumentForm = false
                            }
                        }
                        else -> null
                    }
                }
            },
            onClickCancelForm = {},
            onSelectTaxRate = {
                productAddEditViewModel.updateTaxRate(it, ProductType.DOCUMENT_PRODUCT)
            },
            onSaveTaxRates = { rates ->
                productAddEditViewModel.saveTaxRates(rates)
            },
            showDocumentForm = showDocumentForm,
            onShowDocumentForm = { showDocumentForm = it },
            onClickDeleteAddress = {
                clientOrIssuerAddEditViewModel.removeAddressFromClientOrIssuerState(it)
            },
            onClickDeleteEmail = { type, index ->
                clientOrIssuerAddEditViewModel.removeEmailFromClientOrIssuerState(type, index)
            },
            onAddEmail = { type, email ->
                clientOrIssuerAddEditViewModel.addEmailToClientOrIssuerState(type, email)
            },
            onPendingEmailValidationResult = { _, isValid ->
                clientOrIssuerAddEditViewModel.setPendingEmailValidationResult(isValid)
            },
            onOrderChange = creditNoteViewModel::updateDocumentProductsOrderInUiStateAndDb,
            onShowMessage = onShowMessage,
            exportPdfContent = exportPdfContent,
            showProductType = showProductType,
            onSaveRetention = { idx, updated ->
                creditNoteViewModel.updateRetentionAt(idx, updated)
            },
            onToggleRetentionHidden = { idx ->
                creditNoteViewModel.toggleRetentionHiddenAt(idx)
            },
            onFontSelect = { font ->
                creditNoteViewModel.setDocumentFont(font.id)
            },
        )

        FormValidationDialogHost(errorDialog)
    }
}
