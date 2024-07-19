package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a4a.g8invoicing.ui.screens.ProductAddEdit
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductType
import com.a4a.g8invoicing.ui.shared.ScreenElement

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
        )
    ) { backStackEntry ->
        val viewModel = backStackEntry.sharedViewModel<ProductAddEditViewModel>(navController)
        val productUiState by viewModel.productUiState
        val isNew = backStackEntry.arguments?.getString("itemId") == null

        ProductAddEdit(
            navController = navController,
            product = productUiState,
            isNew = isNew,
            onValueChange = { pageElement, value ->
                viewModel.updateProductState(pageElement, value, ProductType.PRODUCT)
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
            onClickForward = onClickForward
        )
    }
}