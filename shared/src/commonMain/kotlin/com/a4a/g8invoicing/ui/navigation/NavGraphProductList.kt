package com.a4a.g8invoicing.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.ProductList
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.currentTimeMillis
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.viewmodels.ProductListViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.productList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (ProductState) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
    showCategoryButton: Boolean = true,
) {
    composable(
        route = Screen.ProductList.name,
        exitTransition = {
            fadeOut(animationSpec = tween(100))
        }
    ) {
        val viewModel: ProductListViewModel = koinViewModel()
        val productsUiState by viewModel.productsUiState.collectAsState()

        var isCategoriesMenuOpen by remember { mutableStateOf(false) }
        var lastBackPressTime by remember { mutableStateOf(0L) }

        PlatformBackHandler {
            val currentTime = currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                // Double back - exit
            } else {
                lastBackPressTime = currentTime
                isCategoriesMenuOpen = true
            }
        }

        ProductList(
            navController = navController,
            productsUiState = productsUiState,
            onClickDelete = viewModel::deleteProducts,
            onClickDuplicate = viewModel::duplicateProducts,
            onClickNew = { onClickNew() },
            onClickCategory = onClickCategory,
            onClickListItem = onClickListItem,
            onClickBack = { onClickBack() },
            isCategoriesMenuOpen = isCategoriesMenuOpen,
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it },
            showCategoryButton = showCategoryButton
        )
    }
}
