package com.a4a.g8invoicing.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a4a.g8invoicing.ui.screens.ClientSelectionDialogState
import com.a4a.g8invoicing.ui.screens.ProductAddEdit
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductType

fun NavGraphBuilder.productAddEdit(
    navController: NavController,
    onClickBack: () -> Unit,
    onClickForward: (ScreenElement) -> Unit,
) {

    composable(
        route = Screen.ProductAddEdit.name + "?itemId={itemId}&type={type}",
        arguments = listOf(
            navArgument("itemId") { nullable = true },
            navArgument("type") { nullable = true },
        ),
        enterTransition = {
            fadeIn(animationSpec = tween(500))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        }

    ) { backStackEntry ->
        val viewModel = backStackEntry.sharedViewModel<ProductAddEditViewModel>(navController)
        val productUiState by viewModel.productUiState
        val isLoading by viewModel.isLoading.collectAsState()
        val clientSelectionDialogState by viewModel.clientSelectionDialogState.collectAsState()
        val isNew = backStackEntry.arguments?.getString("itemId") == null

        // Map ViewModel dialog state to shared module's data class
        val mappedDialogState = clientSelectionDialogState?.let {
            ClientSelectionDialogState(
                priceId = it.priceId,
                availableClients = it.availableClients,
                selectedClients = it.selectedClients
            )
        }

        ProductAddEdit(
            navController = navController,
            product = productUiState,
            isLoading = isLoading,
            clientSelectionDialogState = mappedDialogState,
            onValueChange = { pageElement, value, idStr ->
                viewModel.updateProductState(pageElement, value, ProductType.PRODUCT, idStr)
            },
            placeCursorAtTheEndOfText = { pageElement ->
                viewModel.updateCursor(pageElement, ProductType.PRODUCT)
            },
            onClickDone = {
                if (isNew) {
                    viewModel.saveProductInLocalDb()
                } else {
                    viewModel.updateInLocalDb(ProductType.PRODUCT)
                }
                onClickBack()
            },
            onClickBack = onClickBack,
            onClickForward = onClickForward,
            onClickOpenClientSelection = { priceId ->
                viewModel.openClientSelectionDialog(priceId)
            },
            onClickRemoveClient = { priceId, clientId ->
                viewModel.removeClientFromPrice(priceId, clientId)
            },
            onClickDeletePrice = { viewModel.deletePrice(it) },
            onClickAddPrice = { viewModel.addPrice() },
            onToggleClientSelection = { clientRef ->
                viewModel.toggleClientSelection(clientRef)
            },
            onConfirmClientSelection = {
                viewModel.confirmClientSelection()
            },
            onCloseClientSelectionDialog = {
                viewModel.closeClientSelectionDialog()
            }
        )
    }
}