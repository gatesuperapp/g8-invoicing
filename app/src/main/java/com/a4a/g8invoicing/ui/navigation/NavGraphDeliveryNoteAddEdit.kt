package com.a4a.g8invoicing.ui.navigation

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
import com.a4a.g8invoicing.ui.shared.ScreenElement

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
        val deliveryNoteUiState by deliveryNoteViewModel.deliveryNoteUiState

        val clientOrIssuerListViewModel: ClientOrIssuerListViewModel = hiltViewModel()
        val clientListUiState by clientOrIssuerListViewModel.clientsUiState
            .collectAsStateWithLifecycle()
        val issuerListUiState by clientOrIssuerListViewModel.issuersUiState
            .collectAsStateWithLifecycle()

        val clientOrIssuerAddEditViewModel: ClientOrIssuerAddEditViewModel = hiltViewModel()
        val documentClientUiState by clientOrIssuerAddEditViewModel.documentClientOrIssuerUiState
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
            isNewDeliveryNote = backStackEntry.arguments?.getString("itemId") == null,
            onClickShare = {},
            onClickBack = {
                deliveryNoteViewModel.updateDeliveryNoteInLocalDb()
                onClickBack()
            },
            clientList = clientListUiState.clientsOrIssuerList.toMutableList(),
            issuerList = issuerListUiState.clientsOrIssuerList.toMutableList(),
            documentClientUiState = documentClientUiState,
            documentIssuerUiState = documentIssuerUiState,
            taxRates = productAddEditViewModel.fetchTaxRatesFromLocalDb(),
            products = productListUiState.products.toMutableList(), // The list of products to display when adding a product
            onValueChange = { pageElement, value ->
                deliveryNoteViewModel.updateDeliveryNoteState(pageElement, value)

            },
            onDocumentProductClick = {// Edit a document product
                productAddEditViewModel.setDocumentProductUiState(it)
            },
            onDocumentClientOrIssuerClick = {// Edit a document product
                clientOrIssuerAddEditViewModel.setDocumentClientOrIssuerUiState(it)
            },
            onProductClick = {
                // Initialize documentProductUiState to display it in the bottomSheet form
                productAddEditViewModel.setDocumentProductUiStateWithProduct(it)
            },
            documentProductUiState = documentProduct, // Used when choosing a product or creating new product from the bottom sheet
            onClickDeleteDocumentProduct = {
                deliveryNoteViewModel.removeDocumentProductFromLocalDb(it)
            },
            placeCursorAtTheEndOfText = { pageElement ->
                deliveryNoteViewModel.updateTextFieldCursorOfDeliveryNoteState(pageElement)
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
                    DocumentBottomSheetTypeOfForm.ADD_CLIENT ->
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(documentClientUiState)

                    DocumentBottomSheetTypeOfForm.ADD_ISSUER ->
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(documentIssuerUiState)

                    // User can create client or issuer from bottom sheet (but not edit)
                    DocumentBottomSheetTypeOfForm.NEW_CLIENT -> {
                        clientOrIssuerAddEditViewModel.saveInLocalDb()
                        deliveryNoteViewModel.updateDeliveryNoteState(
                            ScreenElement.DOCUMENT_CLIENT,
                            documentClientUiState
                        )
                        clientOrIssuerAddEditViewModel.clearClientUiState()
                    }

                    DocumentBottomSheetTypeOfForm.NEW_ISSUER -> {
                        clientOrIssuerAddEditViewModel.setClientOrIssuerUiState(ClientOrIssuerType.ISSUER)
                        clientOrIssuerAddEditViewModel.saveInLocalDb()
                        deliveryNoteViewModel.updateDeliveryNoteState(
                            ScreenElement.DOCUMENT_ISSUER,
                            documentClientUiState
                        )
                        clientOrIssuerAddEditViewModel.clearIssuerUiState()
                    }

                    DocumentBottomSheetTypeOfForm.ADD_PRODUCT -> {
                        deliveryNoteViewModel.saveDocumentProductInLocalDb(documentProduct)
                    }

                    DocumentBottomSheetTypeOfForm.EDIT_ITEM -> {
                        productAddEditViewModel.updateInLocalDb(ProductType.DOCUMENT_PRODUCT)
                    }

                    DocumentBottomSheetTypeOfForm.NEW_PRODUCT -> {
                        productAddEditViewModel.setProductUiState()
                        productAddEditViewModel.saveInLocalDb()
                        deliveryNoteViewModel.saveDocumentProductInLocalDb(documentProduct)
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
            onClickExport = {}
        )
    }
}

