package com.a4a.g8invoicing.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.a4a.g8invoicing.ui.screens.shared.DocumentAddEdit
import com.a4a.g8invoicing.ui.screens.shared.DocumentBottomSheetTypeOfForm
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerListViewModel
import com.a4a.g8invoicing.ui.viewmodels.DeliveryNoteAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductListViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductType
import kotlinx.coroutines.launch
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

        var showDocumentForm by remember { mutableStateOf(false) }

        DocumentAddEdit(
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
                clientOrIssuer.originalClientId = clientOrIssuer.id
                if (clientOrIssuer.type == ClientOrIssuerType.CLIENT) {
                    documentClientUiState.type = ClientOrIssuerType.DOCUMENT_CLIENT
                    clientOrIssuer.type = ClientOrIssuerType.DOCUMENT_CLIENT
                } else {
                    documentIssuerUiState.type = ClientOrIssuerType.DOCUMENT_ISSUER
                    clientOrIssuer.type = ClientOrIssuerType.DOCUMENT_ISSUER
                }
                deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(clientOrIssuer)
                deliveryNoteViewModel.saveDocumentClientOrIssuerInUiState(clientOrIssuer)
            },
            onClickEditDocumentProduct = {
                productAddEditViewModel.setDocumentProductUiState(it)
            },
            onClickNewDocumentClientOrIssuer = {
                clientOrIssuerAddEditViewModel.clearClientOrIssuerUiState(it)
            },
            onClickDocumentClientOrIssuer = {
                clientOrIssuerAddEditViewModel.setDocumentClientOrIssuerUiState(it)
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
            onClickDoneForm = { typeOfCreation ->
                scope.launch {
                    when (typeOfCreation) {
                        DocumentBottomSheetTypeOfForm.NEW_CLIENT -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_CLIENT)) {
                                createNewClientOrIssuer(
                                    clientOrIssuerAddEditViewModel,
                                    ClientOrIssuerType.DOCUMENT_CLIENT
                                )
                                documentClientUiState.type = ClientOrIssuerType.DOCUMENT_CLIENT
                                deliveryNoteViewModel.saveDocumentClientOrIssuerInUiState(documentClientUiState)
                                deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(documentClientUiState)
                                showDocumentForm = false
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_CLIENT -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_CLIENT)) {
                                deliveryNoteViewModel.updateUiState(ScreenElement.DOCUMENT_CLIENT, documentClientUiState)
                                clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                    ClientOrIssuerType.DOCUMENT_CLIENT, documentClientUiState
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
                                deliveryNoteViewModel.saveDocumentClientOrIssuerInUiState(documentIssuerUiState)
                                deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(documentIssuerUiState)
                                showDocumentForm = false
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_ISSUER -> {
                            if (clientOrIssuerAddEditViewModel.validateInputs(ClientOrIssuerType.DOCUMENT_ISSUER)) {
                                deliveryNoteViewModel.updateUiState(ScreenElement.DOCUMENT_ISSUER, documentIssuerUiState)
                                clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(
                                    ClientOrIssuerType.DOCUMENT_ISSUER, documentIssuerUiState
                                )
                                showDocumentForm = false
                            }
                        }
                        DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                val documentProductId = deliveryNoteViewModel.saveDocumentProductInLocalDbAndGetId(documentProduct)
                                if (documentProductId != null) {
                                    deliveryNoteViewModel.saveDocumentProductInUiState(documentProduct.copy(id = documentProductId))
                                    showDocumentForm = false
                                }
                            }
                        }
                        DocumentBottomSheetTypeOfForm.NEW_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                productAddEditViewModel.setProductUiState()
                                productAddEditViewModel.saveProductInLocalDb()
                                val documentProductId = deliveryNoteViewModel.saveDocumentProductInLocalDbAndGetId(documentProduct)
                                if (documentProductId != null) {
                                    deliveryNoteViewModel.saveDocumentProductInUiState(documentProduct.copy(id = documentProductId))
                                    productAddEditViewModel.clearProductUiState()
                                    showDocumentForm = false
                                }
                            }
                        }
                        DocumentBottomSheetTypeOfForm.EDIT_PRODUCT -> {
                            if (productAddEditViewModel.validateInputs(ProductType.DOCUMENT_PRODUCT)) {
                                deliveryNoteViewModel.updateUiState(ScreenElement.DOCUMENT_PRODUCT, documentProduct)
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
            onOrderChange = deliveryNoteViewModel::updateDocumentProductsOrderInUiStateAndDb,
            onShowMessage = onShowMessage,
            exportPdfContent = exportPdfContent
        )
    }
}
