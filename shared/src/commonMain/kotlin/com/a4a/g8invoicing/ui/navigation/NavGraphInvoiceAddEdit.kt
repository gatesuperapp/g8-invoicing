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
import com.a4a.g8invoicing.ui.shared.FormValidationDialogHost
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.rememberFormValidationDialogState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerListViewModel
import com.a4a.g8invoicing.ui.viewmodels.InvoiceAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductListViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductType
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.invoiceAddEdit(
    navController: NavController,
    onClickBack: () -> Unit,
    onShowMessage: (String) -> Unit = {},
    exportPdfContent: @Composable (DocumentState, () -> Unit) -> Unit = { _, _ -> },
) {
    composable(
        route = Screen.InvoiceAddEdit.name + "?itemId={itemId}",
        enterTransition = { fadeIn(animationSpec = tween(500)) },
        exitTransition = { fadeOut(animationSpec = tween(500)) },
        arguments = listOf(
            navArgument("itemId") { nullable = true },
        )
    ) { backStackEntry ->
        val scope = rememberCoroutineScope()
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
        val itemId = backStackEntry.arguments?.getString("itemId")

        val invoiceViewModel: InvoiceAddEditViewModel = koinViewModel(
            parameters = { parametersOf(itemId) }
        )
        val document by invoiceViewModel.documentUiState.collectAsState()

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
        // Product.type visibility follows the CURRENT document's issuer. Read from
        // document.documentIssuer (the picked issuer for this invoice) — NOT from
        // clientOrIssuerAddEditViewModel.documentIssuerUiState, which is only
        // populated when the user opens the issuer-edit sub-form. The pick flow
        // (onSelectClientOrIssuer above) writes to invoiceViewModel via
        // saveDocumentClientOrIssuerInUiState, so that's the authoritative source.
        val showProductType = document.documentIssuer?.intraEuSales == true
        LaunchedEffect(showProductType) {
            productAddEditViewModel.setShowProductType(showProductType)
        }

        var showDocumentForm by remember { mutableStateOf(false) }

        // Pre-save error recap modal for the client/issuer sub-form hosted
        // in the bottom sheet. Populated with the fresh state.errors when
        // validateInputs returns false; dismissed via its own confirm button.
        val errorDialog = rememberFormValidationDialogState()

        // When the bottom-sheet form is open, system back closes it instead
        // of popping back to the doc list.
        PlatformBackHandler(enabled = showDocumentForm) {
            showDocumentForm = false
        }

        var showVersionMismatchDialog by remember { mutableStateOf(false) }
        var pendingIssuerToEdit by remember { mutableStateOf<ClientOrIssuerState?>(null) }
        // Fires when the user tries to save an issuer edit with the sync-to-
        // master switch OFF AND they've modified banks. Banks are a master-
        // owned resource — nothing to persist them doc-side, so we prompt to
        // force-sync (whole fiche syncs, banks included) or cancel and let
        // the user reconsider. See TaskCreate #12.
        var showBankChangesModal by remember { mutableStateOf(false) }
        var pendingForceSyncSave by remember { mutableStateOf<(() -> Unit)?>(null) }
        // Whether the pending dialog was triggered by the edit-link flow (open
        // the form after the user's choice) or by the refresh-from-master icon
        // (don't open the form — refresh is a standalone action).
        var pendingIssuerOpensForm by remember { mutableStateOf(false) }
        var showClientVersionMismatchDialog by remember { mutableStateOf(false) }
        var pendingClientToEdit by remember { mutableStateOf<ClientOrIssuerState?>(null) }
        var pendingClientOpensForm by remember { mutableStateOf(false) }

        // Version mismatch dialog for issuer
        if (showVersionMismatchDialog && pendingIssuerToEdit != null) {
            com.a4a.g8invoicing.ui.shared.AppConfirmDialog(
                title = stringResource(Res.string.version_mismatch_title),
                body = stringResource(Res.string.version_mismatch_message),
                confirmText = stringResource(Res.string.version_mismatch_load_latest),
                cancelText = stringResource(Res.string.version_mismatch_keep_current),
                onConfirm = {
                    // Dismiss the dialog synchronously so the bottom-sheet form
                    // takes over immediately; the master fetch keeps running in
                    // the background and updates the state when it lands.
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
                            // Retention toggle transition: write DB rows synchronously
                            // before saveDocumentClientOrIssuerInLocalDb reloads state.
                            // saveDocumentClientOrIssuerInUiState only seeds in state
                            // (async), which the subsequent reload wipes out — the
                            // refresh flow would then look silent until the user runs
                            // through EDIT_ISSUER.
                            val hadRetentions = invoiceViewModel.documentUiState.value.retentions.isNotEmpty()
                            if (updated.taxWithholdingEnabled && !hadRetentions) {
                                invoiceViewModel.seedDefaultRetentionsInDb(updated)
                            } else if (!updated.taxWithholdingEnabled && hadRetentions) {
                                invoiceViewModel.clearRetentionsInDb()
                            }
                            val hadExemptionText = invoiceViewModel.documentUiState.value.vatExemptionText?.text?.isNotBlank() == true
                            if (updated.vatExempt && !hadExemptionText) {
                                invoiceViewModel.seedDefaultVatExemptionTextInDb(updated)
                            }
                            invoiceViewModel.saveDocumentClientOrIssuerInUiState(updated)
                            invoiceViewModel.saveDocumentClientOrIssuerInLocalDb(updated)
                        }
                    }
                },
                onDismiss = {
                    // "Keep current" — bump the doc snapshot's originalVersion
                    // to master so the dialog stops re-firing on every reopen.
                    // Same behavior for scrim tap / back gesture: the master
                    // fetch has landed, keeping the frozen data is a valid
                    // final choice, not an abort.
                    val opensForm = pendingIssuerOpensForm
                    val issuerToAck = pendingIssuerToEdit
                    showVersionMismatchDialog = false
                    pendingIssuerToEdit = null
                    pendingIssuerOpensForm = false
                    if (opensForm) showDocumentForm = true
                    issuerToAck?.let { issuer ->
                        scope.launch {
                            clientOrIssuerAddEditViewModel
                                .acknowledgeMasterVersion(issuer)
                                ?.let { invoiceViewModel.saveDocumentClientOrIssuerInUiState(it) }
                        }
                    }
                },
            )
        }

        // Version mismatch dialog for client
        if (showClientVersionMismatchDialog && pendingClientToEdit != null) {
            com.a4a.g8invoicing.ui.shared.AppConfirmDialog(
                title = stringResource(Res.string.version_mismatch_client_title),
                body = stringResource(Res.string.version_mismatch_client_message),
                confirmText = stringResource(Res.string.version_mismatch_load_latest),
                cancelText = stringResource(Res.string.version_mismatch_keep_current),
                onConfirm = {
                    // Dismiss the dialog synchronously so a back tap on the
                    // bottom-sheet form doesn't slip the dialog back on top
                    // while loadLatestMasterVersion is still suspended.
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
                            invoiceViewModel.saveDocumentClientOrIssuerInUiState(updated)
                            invoiceViewModel.saveDocumentClientOrIssuerInLocalDb(updated)
                        }
                    }
                },
                onDismiss = {
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
                                ?.let { invoiceViewModel.saveDocumentClientOrIssuerInUiState(it) }
                        }
                    }
                },
            )
        }

        // Observe the counter so validating the doc-product tax edit dialog
        // triggers a recomposition and the picker re-reads the fresh list.
        val taxRatesRefreshCounter by productAddEditViewModel.taxRatesRefreshCounter.collectAsState()

        DocumentAddEditPlatform(
            navController = navController,
            document = document,
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
                invoiceViewModel.updateUiState(pageElement, value)
            },
            onSelectProduct = { product, clientId ->
                productAddEditViewModel.setDocumentProductUiStateWithProduct(product, clientId)
            },
            onClickNewDocumentProduct = {
                productAddEditViewModel.clearProductNameAndDescription()
            },
            onClickEditDocumentProduct = {
                productAddEditViewModel.setDocumentProductUiState(it)
            },
            onClickDeleteDocumentProduct = {
                invoiceViewModel.removeDocumentProductFromUiState(it)
                invoiceViewModel.removeDocumentProductFromLocalDb(it)
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
                invoiceViewModel.saveDocumentClientOrIssuerInLocalDb(documentClientOrIssuer)
                invoiceViewModel.saveDocumentClientOrIssuerInUiState(documentClientOrIssuer)
            },
            onClickNewDocumentClientOrIssuer = {
                clientOrIssuerAddEditViewModel.clearClientOrIssuerUiState(it)
            },
            onClickDocumentClientOrIssuer = { clientOrIssuer, openFormOnCompletion ->
                clientOrIssuerAddEditViewModel.setDocumentClientOrIssuerUiState(clientOrIssuer)
                // Version-mismatch check gates the form opening: if a mismatch
                // fires, defer showDocumentForm to the dialog's confirm/dismiss
                // buttons; otherwise open the form immediately when the caller
                // asked for it. Refresh-from-master (openFormOnCompletion=false)
                // never opens the form here.
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
            onClickDeleteDocumentClientOrIssuer = { type ->
                invoiceViewModel.removeDocumentClientOrIssuerFromUiState(type)
                invoiceViewModel.removeDocumentClientOrIssuerFromLocalDb(type)
            },
            placeCursorAtTheEndOfText = { pageElement ->
                if (pageElement == ScreenElement.DOCUMENT_NUMBER ||
                    pageElement == ScreenElement.DOCUMENT_REFERENCE
                ) {
                    invoiceViewModel.updateTextFieldCursorOfInvoiceState(pageElement)
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
                    // Mirror the standalone-form flow: clear focus + hide the
                    // keyboard before validating so the email field's
                    // onFocusChanged fires — that's how tryAddEmail commits a
                    // pending value and sets _pendingEmailIsValid. Without
                    // this, a user who types an invalid email and clicks
                    // Valider without leaving the field would sneak past
                    // validation entirely.
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
                                invoiceViewModel.saveDocumentClientOrIssuerInUiState(documentClientUiState)
                                invoiceViewModel.saveDocumentClientOrIssuerInLocalDb(documentClientUiState)
                                showDocumentForm = false
                            } else {
                                errorDialog.showFrom(clientOrIssuerAddEditViewModel.documentClientUiState.value.errors)
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_CLIENT -> {
                            documentClientUiState.type = ClientOrIssuerType.DOCUMENT_CLIENT
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_CLIENT)) {
                                clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                    ClientOrIssuerType.DOCUMENT_CLIENT, documentClientUiState, syncToMaster = syncToMaster
                                )
                                invoiceViewModel.reloadDocument()
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
                                invoiceViewModel.saveDocumentClientOrIssuerInUiState(documentIssuerUiState)
                                invoiceViewModel.saveDocumentClientOrIssuerInLocalDb(documentIssuerUiState)
                                showDocumentForm = false
                            } else {
                                errorDialog.showFrom(clientOrIssuerAddEditViewModel.documentIssuerUiState.value.errors)
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_ISSUER -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_ISSUER)) {
                                val originalBanks = invoiceViewModel.documentUiState.value
                                    .documentIssuer?.banks.orEmpty()
                                val banksChanged = !banksSemanticallyEqual(
                                    originalBanks,
                                    documentIssuerUiState.banks,
                                )
                                // doIssuerSave captures everything the save needs so both
                                // the direct path and the modal's confirm path can call
                                // the same closure with the effective syncToMaster value.
                                // reloadDocumentAwait (suspending) — not the fire-and-
                                // forget reloadDocument — so document.documentIssuer.banks
                                // is guaranteed fresh before the sheet closes; without
                                // the await, tapping the issuer picker again immediately
                                // after save reads a stale banks list.
                                val doIssuerSave: suspend (Boolean) -> Unit = { effectiveSync ->
                                    val hadRetentions = invoiceViewModel.documentUiState.value.retentions.isNotEmpty()
                                    val turnedOffRetention = !documentIssuerUiState.taxWithholdingEnabled && hadRetentions
                                    val turnedOnRetention = documentIssuerUiState.taxWithholdingEnabled && !hadRetentions
                                    val hadExemptionText = invoiceViewModel.documentUiState.value.vatExemptionText?.text?.isNotBlank() == true
                                    val needsExemptionSeed = documentIssuerUiState.vatExempt && !hadExemptionText
                                    clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                        ClientOrIssuerType.DOCUMENT_ISSUER, documentIssuerUiState, syncToMaster = effectiveSync
                                    )
                                    if (turnedOffRetention) {
                                        invoiceViewModel.clearRetentionsInDb()
                                    } else if (turnedOnRetention) {
                                        invoiceViewModel.seedDefaultRetentionsInDb(documentIssuerUiState)
                                    }
                                    if (needsExemptionSeed) {
                                        invoiceViewModel.seedDefaultVatExemptionTextInDb(documentIssuerUiState)
                                    }
                                    invoiceViewModel.reloadDocumentAwait()
                                    showDocumentForm = false
                                }
                                if (!syncToMaster && banksChanged) {
                                    // Stash the confirm action for the modal to invoke
                                    // with syncToMaster forced ON — banks can't persist
                                    // without hitting the master IssuerBank table.
                                    pendingForceSyncSave = {
                                        scope.launch { doIssuerSave(true) }
                                    }
                                    showBankChangesModal = true
                                } else {
                                    doIssuerSave(syncToMaster)
                                }
                            } else {
                                errorDialog.showFrom(clientOrIssuerAddEditViewModel.documentIssuerUiState.value.errors)
                            }
                        }
                        DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                val documentProductId = invoiceViewModel.saveDocumentProductInLocalDbAndGetId(documentProduct)
                                if (documentProductId != null) {
                                    invoiceViewModel.saveDocumentProductInUiState(documentProduct.copy(id = documentProductId))
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
                                val documentProductId = invoiceViewModel.saveDocumentProductInLocalDbAndGetId(docProductWithLink)
                                if (documentProductId != null) {
                                    invoiceViewModel.saveDocumentProductInUiState(docProductWithLink.copy(id = documentProductId))
                                    productAddEditViewModel.clearProductUiState()
                                    showDocumentForm = false
                                }
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                invoiceViewModel.updateUiState(ScreenElement.DOCUMENT_PRODUCT, documentProduct)
                                productAddEditViewModel.updateInLocalDb(ProductType.DOCUMENT_PRODUCT)
                                if (syncToMaster) productAddEditViewModel.syncDocumentProductToMaster()
                                productAddEditViewModel.clearProductUiState()
                                showDocumentForm = false
                            }
                        }
                        // Retention save routes through the form's onRetentionSave
                        // callback below, not through this when. Kept exhaustive.
                        DocumentBottomSheetTypeOfForm.EDIT_RETENTION -> {}
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
            onOrderChange = invoiceViewModel::updateDocumentProductsOrderInUiStateAndDb,
            onShowMessage = onShowMessage,
            exportPdfContent = exportPdfContent,
            showProductType = showProductType,
            hideLinkedSourceHeaders = document.hideLinkedSourceHeaders,
            onToggleHideLinkedSourceHeaders = invoiceViewModel::toggleHideLinkedSourceHeaders,
            onSaveRetention = { idx, updated ->
                invoiceViewModel.updateRetentionAt(idx, updated)
            },
            onToggleRetentionHidden = { idx ->
                invoiceViewModel.toggleRetentionHiddenAt(idx)
            },
            onFontSelect = { font ->
                invoiceViewModel.setDocumentFont(font.id)
            },
        )

        FormValidationDialogHost(errorDialog)

        if (showBankChangesModal) {
            com.a4a.g8invoicing.ui.shared.AppConfirmDialog(
                title = "Modification des comptes bancaires",
                body = "Tu as modifié les comptes bancaires. Pour les enregistrer, la fiche entreprise doit être mise à jour. Confirmer ?",
                confirmText = "Mettre à jour",
                cancelText = "Annuler",
                onConfirm = {
                    val action = pendingForceSyncSave
                    showBankChangesModal = false
                    pendingForceSyncSave = null
                    action?.invoke()
                },
                onDismiss = {
                    showBankChangesModal = false
                    pendingForceSyncSave = null
                },
            )
        }
    }
}

// True when two bank lists carry the same payload (identifier / bic / label /
// country) in the same order. Ignores DB ids and sort_order — those aren't
// user-visible edits.
private fun banksSemanticallyEqual(
    a: List<com.a4a.g8invoicing.ui.states.IssuerBankState>,
    b: List<com.a4a.g8invoicing.ui.states.IssuerBankState>,
): Boolean {
    if (a.size != b.size) return false
    a.forEachIndexed { i, left ->
        val right = b[i]
        if (left.identifier.text.trim() != right.identifier.text.trim()) return false
        if (left.bic.text.trim() != right.bic.text.trim()) return false
        if ((left.label?.text?.trim().orEmpty()) != (right.label?.text?.trim().orEmpty())) return false
        if ((left.countryCode?.trim().orEmpty()) != (right.countryCode?.trim().orEmpty())) return false
    }
    return true
}

suspend fun createNewClientOrIssuer(
    clientOrIssuerAddEditViewModel: ClientOrIssuerAddEditViewModel,
    type: ClientOrIssuerType,
): Long? {
    clientOrIssuerAddEditViewModel.setClientOrIssuerUiState(type)
    val saveType = when (type) {
        ClientOrIssuerType.DOCUMENT_CLIENT -> ClientOrIssuerType.CLIENT
        ClientOrIssuerType.DOCUMENT_ISSUER -> ClientOrIssuerType.ISSUER
        else -> type
    }
    return clientOrIssuerAddEditViewModel.createNew(saveType)
}
