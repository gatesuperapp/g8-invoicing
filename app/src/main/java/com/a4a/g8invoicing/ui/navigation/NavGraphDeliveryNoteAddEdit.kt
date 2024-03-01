package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerListViewModel
import com.a4a.g8invoicing.ui.screens.DeliveryNoteAddEdit
import com.a4a.g8invoicing.ui.screens.DeliveryNoteAddEditViewModel
import com.a4a.g8invoicing.ui.screens.PersonType
import com.a4a.g8invoicing.ui.screens.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.screens.ProductListViewModel
import com.a4a.g8invoicing.ui.screens.ProductType
import com.a4a.g8invoicing.ui.screens.TypeOfProductCreation

fun NavGraphBuilder.deliveryNoteAddEdit(
    navController: NavController,
    onClickBack: () -> Unit,
    onClickNewClientOrIssuer: (PersonType) -> Unit = {},
) {
    composable(
        route = Screen.DeliveryNoteAddEdit.name + "?itemId={itemId}",
        arguments = listOf(
            navArgument("itemId") { nullable = true },
        )
    ) { backStackEntry ->
        val deliveryNoteViewModel: DeliveryNoteAddEditViewModel = hiltViewModel()
        val deliveryNoteUiState by deliveryNoteViewModel.deliveryNoteUiState

        val clientOrIssuerViewModel: ClientOrIssuerListViewModel = hiltViewModel()
        val clientsUiState by clientOrIssuerViewModel.clientsUiState
            .collectAsStateWithLifecycle()
        val issuersUiState by clientOrIssuerViewModel.issuersUiState
            .collectAsStateWithLifecycle()

        val productListViewModel: ProductListViewModel = hiltViewModel()
        val productListUiState by productListViewModel.productsUiState
            .collectAsStateWithLifecycle()

        val productAddEditViewModel: ProductAddEditViewModel = hiltViewModel()
        val documentProductUiState by productAddEditViewModel.documentProductUiState

        // If the previous screen was Add/edit page
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
        }

        // Get result from "Add new" screen, to know if it's
        // a client or issuer that has been added
        DeliveryNoteAddEdit(
            navController = navController,
            deliveryNote = deliveryNoteUiState,
            isNewDeliveryNote = backStackEntry.arguments?.getString("itemId") == null,
            onClickDone = { isNewClient ->
                if (isNewClient) {
                    deliveryNoteViewModel.saveDeliveryNoteInLocalDb()
                } else {
                    deliveryNoteViewModel.updateDeliveryNoteInLocalDb()
                }
                onClickBack()
            },
            onClickBack = onClickBack,
            clients = clientsUiState.clientsOrIssuers.toMutableList(),
            issuers = issuersUiState.clientsOrIssuers.toMutableList(),
            products = productListUiState.products.toMutableList(), // The list of products to display when choosing to add a product
            onValueChange = { pageElement, value ->
                deliveryNoteViewModel.updateDeliveryNoteState(pageElement, value)
                deliveryNoteViewModel.updateDeliveryNoteInLocalDb()

            },
            onClickNewClientOrIssuer = onClickNewClientOrIssuer,
            onProductClick = {
                // Initialize documentProductUiState to display it in the bottomSheet form
                productAddEditViewModel.setDocumentProductUiState(it)
            },
            documentProductUiState = documentProductUiState, // Used when choosing a product or creating new product from the bottom sheet
            onNewProductClick = {},
            onDocumentProductClick = {},
            onClickDeleteDocumentProduct = {
                deliveryNoteViewModel.removeDocumentProductFromDeliveryNote(it)
            },
            placeCursorAtTheEndOfText = { pageElement ->
                deliveryNoteViewModel.updateTextFieldCursorOfDeliveryNoteState(pageElement)
            },
            documentProductOnValueChange = { pageElement, value ->
                productAddEditViewModel.updateProductState(
                    pageElement,
                    value,
                    ProductType.DOCUMENT_PRODUCT
                )
            },
            documentProductPlaceCursor = { pageElement ->
                productAddEditViewModel.updateCursor(pageElement, ProductType.DOCUMENT_PRODUCT)
            },
            onClickDoneForm = { typeOfCreation ->
                when (typeOfCreation) {
                    TypeOfProductCreation.ADD_PRODUCT -> {
                        deliveryNoteViewModel.saveDocumentProductInDbAndAddToDeliveryNote(documentProductUiState)
                    }

                    TypeOfProductCreation.EDIT_DOCUMENT_PRODUCT -> {
                        productAddEditViewModel.updateInLocalDb(ProductType.DOCUMENT_PRODUCT)
                    }

                    TypeOfProductCreation.CREATE_NEW_PRODUCT -> {
                        productAddEditViewModel.saveInLocalDb(ProductType.PRODUCT)
                        productAddEditViewModel.saveInLocalDb(ProductType.DOCUMENT_PRODUCT)
                    }
                }
            },
            onClickCancelForm = {
                productAddEditViewModel.clearDocumentProductUiState()
            }
        )
    }
}

