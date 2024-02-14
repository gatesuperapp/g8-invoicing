
package com.a4a.g8invoicing.ui.navigation
/*
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a4a.g8invoicing.ui.screens.DocumentProductForm
import com.a4a.g8invoicing.ui.screens.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.screens.ProductType
import com.a4a.g8invoicing.ui.shared.ScreenElement

fun NavGraphBuilder.documentProductAddEdit(
    navController: NavController,
    onClickBack: () -> Unit,
    onClickForward: (ScreenElement) -> Unit,
) {
    composable(
        route = Screen.DocumentProductAddEdit.name + "?itemId={itemId}&type={type}",
        arguments = listOf(
            navArgument("itemId") { nullable = true },
            navArgument("type") { nullable = true }
        )
    ) {backStackEntry ->
        val viewModel = backStackEntry.sharedViewModel<ProductAddEditViewModel>(navController)
        val documentProductUiState by viewModel.documentProductUiState
        val isNew = backStackEntry.arguments?.getString("itemId") == null

        DocumentProductForm(
            navController = navController,
            product = documentProductUiState,
            isNew = isNew,
            onValueChange = { pageElement, value ->
                viewModel.updateProductState(pageElement, value, ProductType.DOCUMENT_PRODUCT)
            },
            placeCursorAtTheEndOfText = { pageElement ->
                viewModel.updateCursorOfProductState(pageElement)
            },
            onClickDone = {
                if (isNew) {
                    viewModel.saveProductInLocalDb(ProductType.DOCUMENT_PRODUCT)
                } else {
                    viewModel.updateProductInLocalDb(ProductType.DOCUMENT_PRODUCT)
                }
                onClickBack()
            },
            onClickBack = onClickBack,
            onClickForward = onClickForward
        )
    }
}*/
