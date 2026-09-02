package com.a4a.g8invoicing.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerListViewModel
import com.a4a.g8invoicing.ui.viewmodels.DeliveryNoteAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductListViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductType
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.deliveryNoteAddEdit(
    navController: NavController,
    onClickBack: () -> Unit,
    onShowMessage: (String) -> Unit = {},
    exportPdfContent: @Composable (DocumentState, () -> Unit) -> Unit = { _, _ -> },
) {
    composable(
        route = Screen.DeliveryNoteAddEdit.name + "?itemId={itemId}",
        enterTransition = { fadeIn(animationSpec = tween(500)) },
        exitTransition = { fadeOut(animationSpec = tween(500)) },
        arguments = listOf(
            navArgument("itemId") { nullable = true },
        )
    ) { backStackEntry ->
        val scope = rememberCoroutineScope()
        val itemId = backStackEntry.arguments?.getString("itemId")

        val deliveryNoteViewModel: DeliveryNoteAddEditViewModel = koinViewModel(
            parameters = { parametersOf(itemId) }
        )
        val deliveryNoteUiState by deliveryNoteViewModel.deliveryNoteUiState.collectAsState()

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
        // Product.type visibility: read from deliveryNoteUiState.documentIssuer
        // (authoritative source, set by onSelectClientOrIssuer via
        // saveDocumentClientOrIssuerInUiState). NOT clientOrIssuerAddEditViewModel
        // .documentIssuerUiState — that's only for the issuer-edit sub-form.
        val showProductType = deliveryNoteUiState.documentIssuer?.intraEuSales == true
        LaunchedEffect(showProductType) {
            productAddEditViewModel.setShowProductType(showProductType)
        }

        var showDocumentForm by remember { mutableStateOf(false) }

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
                                    deliveryNoteViewModel.saveDocumentClientOrIssuerInUiState(updated)
                                    deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(updated)
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
                                        ?.let { deliveryNoteViewModel.saveDocumentClientOrIssuerInUiState(it) }
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
                                    deliveryNoteViewModel.saveDocumentClientOrIssuerInUiState(updated)
                                    deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(updated)
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
                                        ?.let { deliveryNoteViewModel.saveDocumentClientOrIssuerInUiState(it) }
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

        DocumentAddEditPlatform(
            navController = navController,
            document = deliveryNoteUiState,
            onClickBack = onClickBack,
            clientList = clientListUiState.clientsOrIssuerList.toMutableList(),
            issuerList = issuerListUiState.clientsOrIssuerList.toMutableList(),
            documentClientUiState = documentClientUiState,
            documentIssuerUiState = documentIssuerUiState,
            documentProductUiState = documentProduct,
            taxRates = productAddEditViewModel.fetchTaxRatesFromLocalDb(),
            products = productListUiState.products.toMutableList(),
            onValueChange = { pageElement, value ->
                deliveryNoteViewModel.updateUiState(pageElement, value)
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
                deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(documentClientOrIssuer)
                deliveryNoteViewModel.saveDocumentClientOrIssuerInUiState(documentClientOrIssuer)
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
                deliveryNoteViewModel.removeDocumentProductFromUiState(it)
                deliveryNoteViewModel.removeDocumentProductFromLocalDb(it)
            },
            onClickDeleteDocumentClientOrIssuer = { type ->
                deliveryNoteViewModel.removeDocumentClientOrIssuerFromUiState(type)
                deliveryNoteViewModel.removeDocumentClientOrIssuerFromLocalDb(type)
            },
            placeCursorAtTheEndOfText = { pageElement ->
                if (pageElement == ScreenElement.DOCUMENT_NUMBER ||
                    pageElement == ScreenElement.DOCUMENT_REFERENCE
                ) {
                    deliveryNoteViewModel.updateTextFieldCursorOfDeliveryNoteState(pageElement)
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
                    when (typeOfCreation) {
                        DocumentBottomSheetTypeOfForm.NEW_CLIENT -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_CLIENT)) {
                                val masterId = createNewClientOrIssuer(
                                    clientOrIssuerAddEditViewModel,
                                    ClientOrIssuerType.DOCUMENT_CLIENT
                                )
                                documentClientUiState.type = ClientOrIssuerType.DOCUMENT_CLIENT
                                documentClientUiState.originalClientOrIssuerId = masterId?.toInt()
                                deliveryNoteViewModel.saveDocumentClientOrIssuerInUiState(documentClientUiState)
                                deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(documentClientUiState)
                                showDocumentForm = false
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_CLIENT -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_CLIENT)) {
                                clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                    ClientOrIssuerType.DOCUMENT_CLIENT, documentClientUiState, syncToMaster = syncToMaster
                                )
                                deliveryNoteViewModel.reloadDocument()
                                showDocumentForm = false
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
                                deliveryNoteViewModel.saveDocumentClientOrIssuerInUiState(documentIssuerUiState)
                                deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(documentIssuerUiState)
                                showDocumentForm = false
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_ISSUER -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_ISSUER)) {
                                clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                    ClientOrIssuerType.DOCUMENT_ISSUER, documentIssuerUiState, syncToMaster = syncToMaster
                                )
                                deliveryNoteViewModel.reloadDocument()
                                showDocumentForm = false
                            }
                        }
                        DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                val documentProductId = deliveryNoteViewModel.saveDocumentProductInLocalDbAndGetId(documentProduct)
                                if (documentProductId != null) {
                                    deliveryNoteViewModel.saveDocumentProductInUiState(documentProduct.copy(id = documentProductId))
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
                                val documentProductId = deliveryNoteViewModel.saveDocumentProductInLocalDbAndGetId(docProductWithLink)
                                if (documentProductId != null) {
                                    deliveryNoteViewModel.saveDocumentProductInUiState(docProductWithLink.copy(id = documentProductId))
                                    productAddEditViewModel.clearProductUiState()
                                    showDocumentForm = false
                                }
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                deliveryNoteViewModel.updateUiState(ScreenElement.DOCUMENT_PRODUCT, documentProduct)
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
            onOrderChange = deliveryNoteViewModel::updateDocumentProductsOrderInUiStateAndDb,
            onShowMessage = onShowMessage,
            exportPdfContent = exportPdfContent,
            showProductType = showProductType,
            onFontSelect = { font ->
                deliveryNoteViewModel.setDocumentFont(font.id)
            },
        )
    }
}
