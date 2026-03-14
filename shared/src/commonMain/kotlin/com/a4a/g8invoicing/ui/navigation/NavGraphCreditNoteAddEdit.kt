package com.a4a.g8invoicing.ui.navigation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.a4a.g8invoicing.ui.theme.callForActionsViolet
import androidx.compose.runtime.Composable
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
import com.a4a.g8invoicing.shared.resources.sync_client_message
import com.a4a.g8invoicing.shared.resources.sync_client_no
import com.a4a.g8invoicing.shared.resources.sync_client_title
import com.a4a.g8invoicing.shared.resources.sync_client_yes
import com.a4a.g8invoicing.shared.resources.version_mismatch_client_message
import com.a4a.g8invoicing.shared.resources.version_mismatch_client_title
import com.a4a.g8invoicing.shared.resources.version_mismatch_keep_current
import com.a4a.g8invoicing.shared.resources.version_mismatch_load_latest
import com.a4a.g8invoicing.shared.resources.version_mismatch_message
import com.a4a.g8invoicing.shared.resources.version_mismatch_title
import com.a4a.g8invoicing.ui.screens.shared.DocumentAddEditPlatform
import com.a4a.g8invoicing.ui.screens.shared.DocumentBottomSheetTypeOfForm
import com.a4a.g8invoicing.ui.shared.ScreenElement
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

        var showDocumentForm by remember { mutableStateOf(false) }
        var showVersionMismatchDialog by remember { mutableStateOf(false) }
        var pendingIssuerToEdit by remember { mutableStateOf<ClientOrIssuerState?>(null) }
        var showClientVersionMismatchDialog by remember { mutableStateOf(false) }
        var pendingClientToEdit by remember { mutableStateOf<ClientOrIssuerState?>(null) }
        var showSyncClientDialog by remember { mutableStateOf(false) }
        var pendingClientToSave by remember { mutableStateOf<ClientOrIssuerState?>(null) }

        // Version mismatch dialog for issuer
        if (showVersionMismatchDialog && pendingIssuerToEdit != null) {
            AlertDialog(
                onDismissRequest = {
                    showVersionMismatchDialog = false
                    pendingIssuerToEdit = null
                },
                title = { Text(stringResource(Res.string.version_mismatch_title)) },
                text = { Text(stringResource(Res.string.version_mismatch_message)) },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                clientOrIssuerAddEditViewModel.loadLatestMasterVersion(
                                    ClientOrIssuerType.DOCUMENT_ISSUER
                                )
                                showVersionMismatchDialog = false
                                pendingIssuerToEdit = null
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.version_mismatch_load_latest),
                            style = MaterialTheme.typography.callForActionsViolet
                        )
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showVersionMismatchDialog = false
                            pendingIssuerToEdit = null
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.version_mismatch_keep_current),
                            style = MaterialTheme.typography.callForActionsViolet
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
                },
                title = { Text(stringResource(Res.string.version_mismatch_client_title)) },
                text = { Text(stringResource(Res.string.version_mismatch_client_message)) },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                clientOrIssuerAddEditViewModel.loadLatestMasterVersion(
                                    ClientOrIssuerType.DOCUMENT_CLIENT
                                )
                                showClientVersionMismatchDialog = false
                                pendingClientToEdit = null
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.version_mismatch_load_latest),
                            style = MaterialTheme.typography.callForActionsViolet
                        )
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showClientVersionMismatchDialog = false
                            pendingClientToEdit = null
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.version_mismatch_keep_current),
                            style = MaterialTheme.typography.callForActionsViolet
                        )
                    }
                }
            )
        }

        // Sync client to master dialog
        if (showSyncClientDialog && pendingClientToSave != null) {
            AlertDialog(
                onDismissRequest = {
                    // On dismiss, save without syncing to master
                    scope.launch {
                        clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                            ClientOrIssuerType.DOCUMENT_CLIENT, pendingClientToSave!!, syncToMaster = false
                        )
                        creditNoteViewModel.reloadDocument()
                        showSyncClientDialog = false
                        pendingClientToSave = null
                        showDocumentForm = false
                    }
                },
                title = { Text(stringResource(Res.string.sync_client_title)) },
                text = { Text(stringResource(Res.string.sync_client_message)) },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                    ClientOrIssuerType.DOCUMENT_CLIENT, pendingClientToSave!!, syncToMaster = true
                                )
                                // Reload document to get updated originalVersion after sync
                                creditNoteViewModel.reloadDocument()
                                showSyncClientDialog = false
                                pendingClientToSave = null
                                showDocumentForm = false
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.sync_client_yes),
                            style = MaterialTheme.typography.callForActionsViolet
                        )
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                    ClientOrIssuerType.DOCUMENT_CLIENT, pendingClientToSave!!, syncToMaster = false
                                )
                                creditNoteViewModel.reloadDocument()
                                showSyncClientDialog = false
                                pendingClientToSave = null
                                showDocumentForm = false
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.sync_client_no),
                            style = MaterialTheme.typography.callForActionsViolet
                        )
                    }
                }
            )
        }

        DocumentAddEditPlatform(
            navController = navController,
            document = uiState,
            onClickBack = onClickBack,
            clientList = clientListUiState.clientsOrIssuerList.toMutableList(),
            issuerList = issuerListUiState.clientsOrIssuerList.toMutableList(),
            documentClientUiState = documentClientUiState,
            documentIssuerUiState = documentIssuerUiState,
            documentProductUiState = documentProduct,
            taxRates = productAddEditViewModel.fetchTaxRatesFromLocalDb(),
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
                clientOrIssuer.originalClientOrIssuerId = clientOrIssuer.id
                clientOrIssuer.originalVersion = clientOrIssuer.version ?: 1
                // Set id to null because this is a NEW DocumentClientOrIssuer to be created
                // The actual id will be assigned by the database
                clientOrIssuer.id = null
                if (clientOrIssuer.type == ClientOrIssuerType.CLIENT) {
                    documentClientUiState.type = ClientOrIssuerType.DOCUMENT_CLIENT
                    clientOrIssuer.type = ClientOrIssuerType.DOCUMENT_CLIENT
                } else {
                    documentIssuerUiState.type = ClientOrIssuerType.DOCUMENT_ISSUER
                    clientOrIssuer.type = ClientOrIssuerType.DOCUMENT_ISSUER
                }
                creditNoteViewModel.saveDocumentClientOrIssuerInLocalDb(clientOrIssuer)
                creditNoteViewModel.saveDocumentClientOrIssuerInUiState(clientOrIssuer)
            },
            onClickEditDocumentProduct = {
                productAddEditViewModel.setDocumentProductUiState(it)
            },
            onClickNewDocumentClientOrIssuer = {
                clientOrIssuerAddEditViewModel.clearClientOrIssuerUiState(it)
            },
            onClickDocumentClientOrIssuer = { clientOrIssuer ->
                clientOrIssuerAddEditViewModel.setDocumentClientOrIssuerUiState(clientOrIssuer)
                // Check for version mismatch for issuers
                if (clientOrIssuer.type == ClientOrIssuerType.DOCUMENT_ISSUER ||
                    clientOrIssuer.type == ClientOrIssuerType.ISSUER) {
                    scope.launch {
                        if (clientOrIssuerAddEditViewModel.checkVersionMismatch(clientOrIssuer)) {
                            pendingIssuerToEdit = clientOrIssuer
                            showVersionMismatchDialog = true
                        }
                    }
                }
                // Check for version mismatch for clients
                if (clientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT ||
                    clientOrIssuer.type == ClientOrIssuerType.CLIENT) {
                    scope.launch {
                        if (clientOrIssuerAddEditViewModel.checkVersionMismatch(clientOrIssuer)) {
                            pendingClientToEdit = clientOrIssuer
                            showClientVersionMismatchDialog = true
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
            onClickDoneForm = { typeOfCreation ->
                scope.launch {
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
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_CLIENT -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_CLIENT)) {
                                // Check if there are actual changes from master
                                val hasChanges = clientOrIssuerAddEditViewModel.hasChangesFromMaster(documentClientUiState)
                                if (hasChanges) {
                                    // Show sync dialog to ask user if they want to update master client
                                    pendingClientToSave = documentClientUiState.copy()
                                    showSyncClientDialog = true
                                } else {
                                    // No changes from master, just save document without sync dialog
                                    clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                        ClientOrIssuerType.DOCUMENT_CLIENT, documentClientUiState, syncToMaster = false
                                    )
                                    creditNoteViewModel.reloadDocument()
                                    showDocumentForm = false
                                }
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
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_ISSUER -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_ISSUER)) {
                                clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                    ClientOrIssuerType.DOCUMENT_ISSUER, documentIssuerUiState
                                )
                                // Reload document to get updated originalVersion after sync
                                creditNoteViewModel.reloadDocument()
                                showDocumentForm = false
                            }
                        }
                        DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                val documentProductId = creditNoteViewModel.saveDocumentProductInLocalDbAndGetId(documentProduct)
                                if (documentProductId != null) {
                                    creditNoteViewModel.saveDocumentProductInUiState(documentProduct.copy(id = documentProductId))
                                    showDocumentForm = false
                                }
                            }
                        }
                        DocumentBottomSheetTypeOfForm.NEW_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                productAddEditViewModel.setProductUiState()
                                productAddEditViewModel.saveProductInLocalDb()
                                val documentProductId = creditNoteViewModel.saveDocumentProductInLocalDbAndGetId(documentProduct)
                                if (documentProductId != null) {
                                    creditNoteViewModel.saveDocumentProductInUiState(documentProduct.copy(id = documentProductId))
                                    productAddEditViewModel.clearProductUiState()
                                    showDocumentForm = false
                                }
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                creditNoteViewModel.updateUiState(ScreenElement.DOCUMENT_PRODUCT, documentProduct)
                                productAddEditViewModel.updateInLocalDb(ProductType.DOCUMENT_PRODUCT)
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
            exportPdfContent = exportPdfContent
        )
    }
}
