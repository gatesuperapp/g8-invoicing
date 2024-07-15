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
        val deliveryNoteUiState by deliveryNoteViewModel.deliveryNoteUiState.collectAsStateWithLifecycle()

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
                productAddEditViewModel.autoSaveFormInputsInLocalDb()
                productAddEditViewModel.setDocumentProductUiStateWithProduct(it)
            },
            onClickNewProduct = {
                // Initialize documentProductUiState to display it in the bottomSheet form
                productAddEditViewModel.autoSaveFormInputsInLocalDb()
            },
            onClickClientOrIssuer = {
                // Initialize documentProductUiState to display it in the bottomSheet form
                clientOrIssuerAddEditViewModel.autoSaveFormInputsInLocalDb()
                clientOrIssuerAddEditViewModel.setDocumentClientOrIssuerUiStateWithSelected(it)
            },
            onClickNewClientOrIssuer = {
                // Initialize documentProductUiState to display it in the bottomSheet form
                clientOrIssuerAddEditViewModel.autoSaveFormInputsInLocalDb()
            },
            onClickDocumentProduct = {// Edit a document product
                productAddEditViewModel.autoSaveFormInputsInLocalDb()
                productAddEditViewModel.setDocumentProductUiState(it)
            },
            onClickDocumentClientOrIssuer = {// Edit a document product
                clientOrIssuerAddEditViewModel.autoSaveFormInputsInLocalDb()
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
                        productAddEditViewModel.stopAutoSaveFormInputsInLocalDb()
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(
                            documentClientUiState
                        )
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInDeliveryNoteUiState(
                            documentClientUiState
                        )
                        clientOrIssuerAddEditViewModel.clearClientUiState()
                    }

                    DocumentBottomSheetTypeOfForm.ADD_ISSUER -> {
                        productAddEditViewModel.stopAutoSaveFormInputsInLocalDb()
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(
                            documentIssuerUiState
                        )
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInDeliveryNoteUiState(
                            documentIssuerUiState
                        )
                        clientOrIssuerAddEditViewModel.clearIssuerUiState()
                    }

                    DocumentBottomSheetTypeOfForm.ADD_PRODUCT -> {
                        productAddEditViewModel.stopAutoSaveFormInputsInLocalDb()
                        deliveryNoteViewModel.saveDocumentProductInDeliveryNoteUiState(
                            documentProduct
                        )
                        deliveryNoteViewModel.saveDocumentProductInLocalDb(documentProduct)
                        productAddEditViewModel.clearProductUiState()
                    }
                    // NEW = create new
                    DocumentBottomSheetTypeOfForm.NEW_CLIENT -> {
                        productAddEditViewModel.stopAutoSaveFormInputsInLocalDb()
                        clientOrIssuerAddEditViewModel.setClientOrIssuerUiState(ClientOrIssuerType.CLIENT)
                        clientOrIssuerAddEditViewModel.saveClientOrIssuerInLocalDb(
                            ClientOrIssuerType.CLIENT
                        )
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInDeliveryNoteUiState(
                            documentClientUiState
                        )
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(
                            documentClientUiState
                        )
                        clientOrIssuerAddEditViewModel.clearClientUiState()
                    }

                    DocumentBottomSheetTypeOfForm.NEW_ISSUER -> {
                        productAddEditViewModel.stopAutoSaveFormInputsInLocalDb()
                        clientOrIssuerAddEditViewModel.setClientOrIssuerUiState(ClientOrIssuerType.ISSUER)
                        clientOrIssuerAddEditViewModel.saveClientOrIssuerInLocalDb(
                            ClientOrIssuerType.ISSUER
                        )
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInDeliveryNoteUiState(
                            documentIssuerUiState
                        )
                        deliveryNoteViewModel.saveDocumentClientOrIssuerInLocalDb(
                            documentIssuerUiState
                        )
                        clientOrIssuerAddEditViewModel.clearIssuerUiState()
                    }

                    DocumentBottomSheetTypeOfForm.NEW_PRODUCT -> {
                        productAddEditViewModel.stopAutoSaveFormInputsInLocalDb()
                        productAddEditViewModel.setProductUiState()
                        productAddEditViewModel.saveProductInLocalDb()
                        deliveryNoteViewModel.saveDocumentProductInDeliveryNoteUiState(
                            documentProduct
                        )
                        deliveryNoteViewModel.saveDocumentProductInLocalDb(documentProduct)
                        productAddEditViewModel.clearProductUiState()
                    }
                    // EDIT = edit the chosen item (will only impact the document, doesn't change
                    // the initial object)
                    DocumentBottomSheetTypeOfForm.EDIT_CLIENT -> {
                        productAddEditViewModel.stopAutoSaveFormInputsInLocalDb()
                        deliveryNoteViewModel.updateDeliveryNoteState(
                            ScreenElement.DOCUMENT_CLIENT,
                            documentClientUiState
                        )
                        clientOrIssuerAddEditViewModel.clearClientUiState()
                        //clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(ClientOrIssuerType.DOCUMENT_CLIENT)
                    }

                    DocumentBottomSheetTypeOfForm.EDIT_ISSUER -> {
                        productAddEditViewModel.stopAutoSaveFormInputsInLocalDb()
                        deliveryNoteViewModel.updateDeliveryNoteState(
                            ScreenElement.DOCUMENT_ISSUER,
                            documentIssuerUiState
                        )
                        clientOrIssuerAddEditViewModel.clearIssuerUiState()
                        //clientOrIssuerAddEditViewModel.updateClientOrIssuerInLocalDb(ClientOrIssuerType.DOCUMENT_ISSUER)
                    }

                    DocumentBottomSheetTypeOfForm.EDIT_PRODUCT -> {
                        productAddEditViewModel.stopAutoSaveFormInputsInLocalDb()
                        deliveryNoteViewModel.updateDeliveryNoteState(
                            ScreenElement.DOCUMENT_PRODUCT,
                            documentProduct
                        )
                        productAddEditViewModel.clearProductUiState()
                        //productAddEditViewModel.updateInLocalDb(ProductType.DOCUMENT_PRODUCT)
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

