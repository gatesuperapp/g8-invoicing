package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerListViewModel
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import com.a4a.g8invoicing.ui.screens.shared.DocumentAddEdit
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductListViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductType
import com.a4a.g8invoicing.ui.screens.shared.DocumentBottomSheetTypeOfForm
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.viewmodels.InvoiceAddEditViewModel

fun NavGraphBuilder.invoiceAddEdit(
    navController: NavController,
    onClickBack: () -> Unit,
) {
    composable(
        route = Screen.InvoiceAddEdit.name + "?itemId={itemId}",
        arguments = listOf(
            navArgument("itemId") { nullable = true },
        )
    ) {
        val invoiceViewModel: InvoiceAddEditViewModel = hiltViewModel()
        val uiState by invoiceViewModel.documentUiState.collectAsStateWithLifecycle()

        val clientOrIssuerListViewModel: ClientOrIssuerListViewModel = hiltViewModel()
        val clientListUiState by clientOrIssuerListViewModel.clientsUiState
            .collectAsStateWithLifecycle()
        val issuerListUiState by clientOrIssuerListViewModel.issuersUiState
            .collectAsStateWithLifecycle()

        val clientOrIssuerAddEditViewModel: ClientOrIssuerAddEditViewModel = hiltViewModel()
        val documentClientUiState by clientOrIssuerAddEditViewModel.documentClientUiState.collectAsState()
        val documentIssuerUiState by clientOrIssuerAddEditViewModel.documentIssuerUiState.collectAsState()

        val productListViewModel: ProductListViewModel = hiltViewModel()
        val productListUiState by productListViewModel.productsUiState
            .collectAsStateWithLifecycle()

        val productAddEditViewModel: ProductAddEditViewModel = hiltViewModel()
        val documentProduct by productAddEditViewModel.documentProductUiState.collectAsState()

        var showDocumentForm by remember { mutableStateOf(false) }
        var moveDocumentPagerToLastPage by remember { mutableStateOf(false) }

        // Get result from "Add new" screen, to know if it's
        // a client or issuer that has been added
        DocumentAddEdit(
            navController = navController,
            document = uiState,
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
            onClickProduct = { product ->
                // Initialize documentProductUiState to display it in the bottomSheet form
                productAddEditViewModel.setDocumentProductUiStateWithProduct(
                    product,
                )
            },
            onClickNewProduct = {
                productAddEditViewModel.clearProductNameAndDescription()
            },
            onClickClientOrIssuer = {
                // Initialize to display it in the bottomSheet form
                clientOrIssuerAddEditViewModel.setDocumentClientOrIssuerUiStateWithSelected(it)
            },
            onClickDocumentProduct = {// Edit a document product
                productAddEditViewModel.autoSaveDocumentProductInLocalDb()
                productAddEditViewModel.setDocumentProductUiState(it)
            },
            onClickNewDocumentClientOrIssuer = {
                clientOrIssuerAddEditViewModel.clearClientOrIssuerUiState(it)
            },
            onClickDocumentClientOrIssuer = {// Edit a document product
                clientOrIssuerAddEditViewModel.setDocumentClientOrIssuerUiState(it)
            },
            onClickDeleteDocumentProduct = {
                invoiceViewModel.removeDocumentProductFromUiState(it)
                invoiceViewModel.removeDocumentProductFromLocalDb(it)
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
                    productAddEditViewModel.updateCursor(pageElement, ProductType.DOCUMENT_PRODUCT)
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
                when (typeOfCreation) {
                    // ADD = choose from existing list
                    DocumentBottomSheetTypeOfForm.ADD_EXISTING_CLIENT -> {
                        if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_CLIENT)) {
                            documentClientUiState.type = ClientOrIssuerType.DOCUMENT_CLIENT
                            clientOrIssuerAddEditViewModel.setClientOrIssuerUiState(
                                ClientOrIssuerType.DOCUMENT_CLIENT
                            )
                            invoiceViewModel.saveDocumentClientOrIssuerInLocalDb(
                                documentClientUiState
                            )
                            invoiceViewModel.saveDocumentClientOrIssuerInUiState(
                                documentClientUiState
                            )
                            showDocumentForm = false
                        }
                    }
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


                    DocumentBottomSheetTypeOfForm.ADD_EXISTING_ISSUER -> {
                        if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_ISSUER)) {
                            invoiceViewModel.saveDocumentClientOrIssuerInLocalDb(
                                documentIssuerUiState
                            )
                            invoiceViewModel.saveDocumentClientOrIssuerInUiState(
                                documentIssuerUiState
                            )
                            showDocumentForm = false
                        }
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
                        moveDocumentPagerToLastPage = true
                        if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                            documentProduct.id =
                                invoiceViewModel.saveDocumentProductInLocalDbAndWaitForTheId(
                                    documentProduct
                                )
                            invoiceViewModel.saveDocumentProductInUiState(documentProduct)

                            // And if a document product is overflowing the page, and page is changed
                            // we want this to be saved in db
                            productAddEditViewModel.autoSaveDocumentProductInLocalDb()

                            //  productAddEditViewModel.clearProductUiState()
                            showDocumentForm = false
                        }
                    }

                    DocumentBottomSheetTypeOfForm.NEW_PRODUCT -> {
                        moveDocumentPagerToLastPage = true
                        if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                            productAddEditViewModel.setProductUiState()
                            productAddEditViewModel.saveProductInLocalDb()
                            documentProduct.id =
                                invoiceViewModel.saveDocumentProductInLocalDbAndWaitForTheId(
                                    documentProduct
                                )
                            invoiceViewModel.saveDocumentProductInUiState(documentProduct)

                            // And if a document product is overflowing the page, and page is changed
                            // we want this to be saved in db
                            productAddEditViewModel.autoSaveDocumentProductInLocalDb()
                            // productAddEditViewModel.clearProductUiState()
                            showDocumentForm = false
                        }
                    }

                    DocumentBottomSheetTypeOfForm.EDIT_PRODUCT -> {
                        if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                            productAddEditViewModel.stopAutoSaveFormInputsInLocalDb()
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
            }
        )
    }
}

fun createNewClientOrIssuer(
    clientOrIssuerAddEditViewModel: ClientOrIssuerAddEditViewModel,
    type: ClientOrIssuerType,
) {
    clientOrIssuerAddEditViewModel.setClientOrIssuerUiState(type)
    clientOrIssuerAddEditViewModel.createNew(type)
}

