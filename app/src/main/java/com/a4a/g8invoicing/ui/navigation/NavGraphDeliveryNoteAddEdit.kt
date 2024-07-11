package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerAddEditViewModel
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerListViewModel
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerType
import com.a4a.g8invoicing.ui.screens.DeliveryNoteAddEdit
import com.a4a.g8invoicing.ui.screens.DeliveryNoteAddEditViewModel
import com.a4a.g8invoicing.ui.screens.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.screens.ProductListViewModel
import com.a4a.g8invoicing.ui.screens.ProductType
import com.a4a.g8invoicing.ui.screens.DocumentBottomSheetTypeOfForm
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

fun NavGraphBuilder.deliveryNoteAddEdit(
    navController: NavController,
    onClickBack: () -> Unit,
) {
    composable(
        route = Screen.DeliveryNoteAddEdit.name + "?itemId={itemId}",
        arguments = listOf(
            navArgument("itemId") { nullable = true },
        )
    ) { backStackEntry ->
        val deliveryNoteViewModel: DeliveryNoteAddEditViewModel = hiltViewModel()
        val deliveryNoteUiState by deliveryNoteViewModel.deliveryNoteUiState.collectAsState()

        val clientOrIssuerListViewModel: ClientOrIssuerListViewModel = hiltViewModel()
        val clientListUiState by clientOrIssuerListViewModel.clientsUiState
            .collectAsStateWithLifecycle()
        val issuerListUiState by clientOrIssuerListViewModel.issuersUiState
            .collectAsStateWithLifecycle()

        val clientOrIssuerAddEditViewModel: ClientOrIssuerAddEditViewModel = hiltViewModel()
        val documentClientUiState by clientOrIssuerAddEditViewModel.documentClientUiState
        val documentIssuerUiState by clientOrIssuerAddEditViewModel.documentIssuerUiState

        val productListViewModel: ProductListViewModel = hiltViewModel()
        val productListUiState by productListViewModel.productsUiState
            .collectAsStateWithLifecycle()

        val productAddEditViewModel: ProductAddEditViewModel = hiltViewModel()
        val documentProduct by productAddEditViewModel.documentProductUiState

        /*        // If the previous screen was Add/edit page
                // (accessed after user clicks "Add new" in the bottom sheet)
                // we retrieve the new client/issuer to display it in the document
                val arguments = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getLiveData<Pair<String, String>>("result")?.observeAsState()
                val type = arguments?.value?.first
                val id = arguments?.value?.second

                id?.toLongOrNull()?.let {
                    when (type) {
                        "client" -> {
                            deliveryNoteUiState.client =
                                clientOrIssuerViewModel.fetchClientOrIssuerFromLocalDb(it)
                        }

                        "issuer" -> {
                            deliveryNoteUiState.issuer =
                                clientOrIssuerViewModel.fetchClientOrIssuerFromLocalDb(it)
                        }

                        else -> {}
                    }
                }*/

        // Get result from "Add new" screen, to know if it's
        // a client or issuer that has been added
        DeliveryNoteAddEdit(
            navController = navController,
            deliveryNote = deliveryNoteUiState,
            onClickBack = {
                //deliveryNoteViewModel.updateDeliveryNoteInLocalDb()
                onClickBack()
            },
            clientList = clientListUiState.clientsOrIssuerList.toMutableList(),
            issuerList = issuerListUiState.clientsOrIssuerList.toMutableList(),
            documentClientUiState = documentClientUiState,
            documentIssuerUiState = documentIssuerUiState,
            documentProductUiState = documentProduct, // Used when choosing a product or creating new product from the bottom sheet
            taxRates = productAddEditViewModel.fetchTaxRatesFromLocalDb(),
            products = productListUiState.products.toMutableList(), // The list of products to display when adding a product
            onValueChange = { pageElement, value ->
                deliveryNoteViewModel.updateDeliveryNoteState(pageElement, value)
            },
            onClickProduct = {
                // Initialize documentProductUiState to display it in the bottomSheet form
                productAddEditViewModel.setDocumentProductUiStateWithProduct(it)
            },
            onClickClientOrIssuer = {
                // Initialize documentProductUiState to display it in the bottomSheet form
                clientOrIssuerAddEditViewModel.setDocumentClientOrIssuerUiStateWithSelected(it)
            },
            onClickDocumentProduct = {// Edit a document product
                productAddEditViewModel.setDocumentProductUiState(it)
            },
            onClickDocumentClientOrIssuer = {// Edit a document product
                clientOrIssuerAddEditViewModel.setDocumentClientOrIssuerUiState(it)
            },
            onClickDeleteDocumentProduct = {
                deliveryNoteViewModel.removeDocumentProductFromLocalDb(it)
            },
            onClickDeleteDocumentClientOrIssuer = {
                deliveryNoteViewModel.removeDocumentClientOrIssuerFromLocalDb(it)
            },
            placeCursorAtTheEndOfText = { pageElement ->
               // deliveryNoteViewModel.updateTextFieldCursorOfDeliveryNoteState(pageElement)
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
            bottomFormPlaceCursor = { pageElement ->
                productAddEditViewModel.updateCursor(pageElement, ProductType.DOCUMENT_PRODUCT)
            },
            onClickDoneForm = { typeOfCreation ->
                when (typeOfCreation) {
                    // ADD = choose from existing list
                    DocumentBottomSheetTypeOfForm.ADD_CLIENT -> {
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(
                            documentClientUiState
                        )
                        clientOrIssuerAddEditViewModel.clearClientUiState()
                    }

                    DocumentBottomSheetTypeOfForm.ADD_ISSUER -> {
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(
                            documentIssuerUiState
                        )
                        clientOrIssuerAddEditViewModel.clearIssuerUiState()
                    }

                    DocumentBottomSheetTypeOfForm.ADD_PRODUCT -> {
                        deliveryNoteViewModel.saveDocumentProductInLocalDb(documentProduct)
                    }
                    // NEW = create new
                    DocumentBottomSheetTypeOfForm.NEW_CLIENT -> {
                        clientOrIssuerAddEditViewModel.setClientOrIssuerUiState(ClientOrIssuerType.CLIENT)
                        clientOrIssuerAddEditViewModel.saveClientOrIssuerInLocalDb(ClientOrIssuerType.CLIENT)
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(documentClientUiState)
                    }

                    DocumentBottomSheetTypeOfForm.NEW_ISSUER -> {
                        clientOrIssuerAddEditViewModel.setClientOrIssuerUiState(ClientOrIssuerType.ISSUER)
                        clientOrIssuerAddEditViewModel.saveClientOrIssuerInLocalDb(ClientOrIssuerType.ISSUER)
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(documentIssuerUiState)
                    }

                    DocumentBottomSheetTypeOfForm.NEW_PRODUCT -> {
                        productAddEditViewModel.setProductUiState()
                        productAddEditViewModel.saveProductInLocalDb()
                        deliveryNoteViewModel.saveDocumentProductInLocalDb(documentProduct)
                    }
                    // EDIT = edit the chosen item (will only impact the document, doesn't change
                    // the initial object)
                    DocumentBottomSheetTypeOfForm.EDIT_CLIENT -> {
                        clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(ClientOrIssuerType.DOCUMENT_CLIENT)
                    }

                    DocumentBottomSheetTypeOfForm.EDIT_ISSUER -> {
                        clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(ClientOrIssuerType.DOCUMENT_ISSUER)
                    }

                    DocumentBottomSheetTypeOfForm.EDIT_PRODUCT -> {
                        productAddEditViewModel.updateInLocalDb(ProductType.DOCUMENT_PRODUCT)
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
            }
        )
    }
}

