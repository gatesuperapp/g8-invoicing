package com.a4a.g8invoicing.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerListViewModel
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import com.a4a.g8invoicing.ui.screens.shared.DocumentAddEdit
import com.a4a.g8invoicing.ui.screens.shared.DocumentBottomSheetTypeOfForm
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductListViewModel

import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.viewmodels.InvoiceAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductType
import com.itextpdf.kernel.pdf.PdfName.a
import kotlinx.coroutines.launch

fun NavGraphBuilder.invoiceAddEdit(
    navController: NavController,
    onClickBack: () -> Unit,
) {
    composable(
        route = Screen.InvoiceAddEdit.name + "?itemId={itemId}",
        enterTransition = { // Define smooth enter transition
            fadeIn(animationSpec = tween(500))
        },
        exitTransition = { // Define smooth exit transition for when navigating away from detail
            fadeOut(animationSpec = tween(500))
        },
        arguments = listOf(
            navArgument("itemId") { nullable = true },
        )
    ) {
        val scope = rememberCoroutineScope()

        val invoiceViewModel: InvoiceAddEditViewModel = koinViewModel()
        val document by invoiceViewModel.documentUiState.collectAsStateWithLifecycle()

        val clientOrIssuerListViewModel: ClientOrIssuerListViewModel = koinViewModel()
        val clientListUiState by clientOrIssuerListViewModel.clientsUiState
            .collectAsStateWithLifecycle()
        val issuerListUiState by clientOrIssuerListViewModel.issuersUiState
            .collectAsStateWithLifecycle()

        val clientOrIssuerAddEditViewModel: ClientOrIssuerAddEditViewModel = koinViewModel()
        val documentClientUiState by clientOrIssuerAddEditViewModel.documentClientUiState.collectAsState()
        val documentIssuerUiState by clientOrIssuerAddEditViewModel.documentIssuerUiState.collectAsState()

        // all available products
        val productListViewModel: ProductListViewModel = koinViewModel()
        val productListUiState by productListViewModel.productsUiState
            .collectAsStateWithLifecycle()

        // when adding a product to the document
        val productAddEditViewModel: ProductAddEditViewModel = koinViewModel()
        val documentProduct by productAddEditViewModel.documentProductUiState.collectAsState()

        var showDocumentForm by remember { mutableStateOf(false) }

        // Get result from "Add new" screen, to know if it's
        // a client or issuer that has been added
        DocumentAddEdit(
            navController = navController,
            document = document,
            onClickBack = onClickBack,
            clientList = clientListUiState.clientsOrIssuerList.toMutableList(),
            issuerList = issuerListUiState.clientsOrIssuerList.toMutableList(),
            documentClientUiState = documentClientUiState,
            documentIssuerUiState = documentIssuerUiState,
            documentProductUiState = documentProduct, // Used when choosing a product or creating new product from the bottom sheet
            taxRates = productAddEditViewModel.fetchTaxRatesFromLocalDb(),
            products = productListUiState.products.toMutableList(), // The list of products to display when adding a product
            onValueChange = { pageElement, value ->
                invoiceViewModel.updateUiState(pageElement, value)
            },
            onSelectProduct = { product, clientId ->
                // Initialize documentProductUiState to display it in the bottomSheet form
                productAddEditViewModel.setDocumentProductUiStateWithProduct(
                    product,
                    clientId
                )
            },
            onClickNewDocumentProduct = {
                productAddEditViewModel.clearProductNameAndDescription()
            },
            onClickEditDocumentProduct = {// Edit a document product
                productAddEditViewModel.setDocumentProductUiState(it)
            },
            onClickDeleteDocumentProduct = {
                invoiceViewModel.removeDocumentProductFromUiState(it)
                invoiceViewModel.removeDocumentProductFromLocalDb(it)
            },
            onSelectClientOrIssuer = { clientOrIssuer ->
                // Conserver l'ID original du client avant de le transformer en document client
                clientOrIssuer.originalClientId = clientOrIssuer.id
                if (clientOrIssuer.type == ClientOrIssuerType.CLIENT) {
                    documentClientUiState.type = ClientOrIssuerType.DOCUMENT_CLIENT
                    clientOrIssuer.type = ClientOrIssuerType.DOCUMENT_CLIENT
                } else {
                    documentIssuerUiState.type = ClientOrIssuerType.DOCUMENT_ISSUER
                    clientOrIssuer.type = ClientOrIssuerType.DOCUMENT_ISSUER
                }
                invoiceViewModel.saveDocumentClientOrIssuerInLocalDb(clientOrIssuer)
                invoiceViewModel.saveDocumentClientOrIssuerInUiState(clientOrIssuer)

            },
            onClickNewDocumentClientOrIssuer = {
                clientOrIssuerAddEditViewModel.clearClientOrIssuerUiState(it)
            },
            onClickDocumentClientOrIssuer = {// Edit a document product
                clientOrIssuerAddEditViewModel.setDocumentClientOrIssuerUiState(it)
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
                        pageElement,
                        value,
                        ProductType.DOCUMENT_PRODUCT
                    )
                } else {
                    type?.let {
                        clientOrIssuerAddEditViewModel.updateClientOrIssuerState(
                            pageElement,
                            value,
                            it
                        )
                    }
                }
            },
            bottomFormPlaceCursor = { pageElement, clientOrIssuer ->
                if (pageElement.name.contains(ProductType.DOCUMENT_PRODUCT.name)) {
                    productAddEditViewModel.updateCursor(
                        pageElement,
                        ProductType.DOCUMENT_PRODUCT
                    )
                } else if (clientOrIssuer == ClientOrIssuerType.DOCUMENT_ISSUER) {
                    clientOrIssuerAddEditViewModel.updateCursor(
                        pageElement,
                        ClientOrIssuerType.DOCUMENT_ISSUER
                    )
                } else if (clientOrIssuer == ClientOrIssuerType.DOCUMENT_CLIENT) {
                    clientOrIssuerAddEditViewModel.updateCursor(
                        pageElement,
                        ClientOrIssuerType.DOCUMENT_CLIENT
                    )
                }
            },
            onClickDoneForm = { typeOfCreation ->
                scope.launch {
                    when (typeOfCreation) {
                        // NEW = create new & save in clients list too
                        DocumentBottomSheetTypeOfForm.NEW_CLIENT -> {
                            //documentClientUiState.type = ClientOrIssuerType.DOCUMENT_CLIENT
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_CLIENT)) {
                                createNewClientOrIssuer(
                                    clientOrIssuerAddEditViewModel,
                                    ClientOrIssuerType.DOCUMENT_CLIENT
                                )
                                documentClientUiState.type = ClientOrIssuerType.DOCUMENT_CLIENT

                                invoiceViewModel.saveDocumentClientOrIssuerInUiState(
                                    documentClientUiState
                                )
                                invoiceViewModel.saveDocumentClientOrIssuerInLocalDb(
                                    documentClientUiState
                                )
                                showDocumentForm = false
                            }
                        }
                        // EDIT = edit the chosen item (will only impact the document, doesn't change
                        // the initial object)
                        DocumentBottomSheetTypeOfForm.EDIT_CLIENT -> {
                            documentClientUiState.type = ClientOrIssuerType.DOCUMENT_CLIENT
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_CLIENT)) {
                                invoiceViewModel.updateUiState(
                                    ScreenElement.DOCUMENT_CLIENT,
                                    documentClientUiState
                                )
                                clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                    ClientOrIssuerType.DOCUMENT_CLIENT,
                                    documentClientUiState
                                )
                            }
                            showDocumentForm = false
                        }

                        DocumentBottomSheetTypeOfForm.NEW_ISSUER -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_ISSUER)) {
                                createNewClientOrIssuer(
                                    clientOrIssuerAddEditViewModel,
                                    ClientOrIssuerType.DOCUMENT_ISSUER
                                )
                                documentIssuerUiState.type = ClientOrIssuerType.DOCUMENT_ISSUER
                                invoiceViewModel.saveDocumentClientOrIssuerInUiState(
                                    documentIssuerUiState
                                )
                                invoiceViewModel.saveDocumentClientOrIssuerInLocalDb(
                                    documentIssuerUiState
                                )
                                showDocumentForm = false
                            }
                        }

                        DocumentBottomSheetTypeOfForm.EDIT_ISSUER -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_ISSUER)) {
                                invoiceViewModel.updateUiState(
                                    ScreenElement.DOCUMENT_ISSUER,
                                    documentIssuerUiState
                                )
                                clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                    ClientOrIssuerType.DOCUMENT_ISSUER,
                                    documentIssuerUiState
                                )
                                showDocumentForm = false
                            }
                        }

                        DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                // 1. Save DocumentProduct in db and get the id
                                val documentProductId =
                                    invoiceViewModel.saveDocumentProductInLocalDbAndGetId(
                                        documentProduct
                                    )
                                // 2. Update UI only if ID is obtained
                                if (documentProductId != null) {
                                    invoiceViewModel.saveDocumentProductInUiState(
                                        documentProduct.copy(id = documentProductId)
                                    )

                                    // 5. Close bottom sheet
                                    showDocumentForm = false
                                } else {
                                    // Les validations ont échoué
                                    //println("Validation failed for new product.")
                                    // Le message d'erreur de validation devrait déjà être affiché dans le formulaire
                                    // Ne pas fermer le formulaire.
                                }
                            }
                        }

                        DocumentBottomSheetTypeOfForm.NEW_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                // 1. Updating product viewModel
                                productAddEditViewModel.setProductUiState()
                                productAddEditViewModel.saveProductInLocalDb()

                                // 2. Save DocumentProduct in db and get the id
                                val documentProductId =
                                    invoiceViewModel.saveDocumentProductInLocalDbAndGetId(
                                        documentProduct
                                    )
                                // 3. Update UI only if ID is obtained
                                if (documentProductId != null) {
                                    invoiceViewModel.saveDocumentProductInUiState(
                                        documentProduct.copy(id = documentProductId)
                                    )
                                    // 4. Clear product viewModel
                                    productAddEditViewModel.clearProductUiState()

                                    // 5. Close bottom sheet
                                    showDocumentForm = false
                                } else {
                                    // Les validations ont échoué
                                    //println("Validation failed for new product.")
                                    // Le message d'erreur de validation devrait déjà être affiché dans le formulaire
                                    // Ne pas fermer le formulaire.
                                }
                            }
                        }

                        DocumentBottomSheetTypeOfForm.EDIT_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                invoiceViewModel.updateUiState(
                                    ScreenElement.DOCUMENT_PRODUCT,
                                    documentProduct
                                )
                                productAddEditViewModel.updateInLocalDb(ProductType.DOCUMENT_PRODUCT)
                                productAddEditViewModel.clearProductUiState()
                                showDocumentForm = false
                            }
                        }
                    }
                }
            },
            onClickCancelForm = {
            },
            onSelectTaxRate = {
                productAddEditViewModel.updateTaxRate(
                    it,
                    ProductType.DOCUMENT_PRODUCT
                )
            },
            showDocumentForm = showDocumentForm,
            onShowDocumentForm = {
                showDocumentForm = it
            },
            onClickDeleteAddress = {
                clientOrIssuerAddEditViewModel.removeAddressFromClientOrIssuerState(it)
            },
            onOrderChange = invoiceViewModel::updateDocumentProductsOrderInUiStateAndDb
        )
    }
}


suspend fun createNewClientOrIssuer(
    clientOrIssuerAddEditViewModel: ClientOrIssuerAddEditViewModel,
    type: ClientOrIssuerType,
) {
    clientOrIssuerAddEditViewModel.setClientOrIssuerUiState(type)
    // Convertir DOCUMENT_CLIENT/DOCUMENT_ISSUER vers CLIENT/ISSUER pour sauvegarder dans la bonne table
    val saveType = when (type) {
        ClientOrIssuerType.DOCUMENT_CLIENT -> ClientOrIssuerType.CLIENT
        ClientOrIssuerType.DOCUMENT_ISSUER -> ClientOrIssuerType.ISSUER
        else -> type
    }
    clientOrIssuerAddEditViewModel.createNew(saveType)
}
