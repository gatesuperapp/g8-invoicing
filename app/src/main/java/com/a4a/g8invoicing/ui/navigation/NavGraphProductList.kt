package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.ProductList
import com.a4a.g8invoicing.ui.screens.ProductListViewModel
import com.a4a.g8invoicing.ui.states.ProductState

fun NavGraphBuilder.productList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (ProductState) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.ProductList.name) {
        val viewModel: ProductListViewModel = hiltViewModel()
        val productsUiState by viewModel.productsUiState
            .collectAsStateWithLifecycle()

        ProductList(
            navController = navController,
            productsUiState = productsUiState,
            onClickDelete = viewModel::deleteProducts,
            onClickDuplicate = viewModel::duplicateProducts,
            onClickNew = { onClickNew() },
            onClickCategory = onClickCategory,
            onClickListItem = onClickListItem,
            onClickBack = { onClickBack() }
        )
    }
}
