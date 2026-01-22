package com.a4a.g8invoicing.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.ProductList
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.viewmodels.ProductListViewModel
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.productList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (ProductState) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
) {
    composable(
        route = Screen.ProductList.name,
        exitTransition = {
            fadeOut(
                animationSpec = tween(100) // Adjust duration as needed
            )
        }
    ) {
        val viewModel: ProductListViewModel = koinViewModel()
        val productsUiState by viewModel.productsUiState
            .collectAsStateWithLifecycle()

        val context = LocalContext.current
        var isCategoriesMenuOpen by remember { mutableStateOf(false) }
        var lastBackPressTime by remember { mutableStateOf(0L) }

        // Handle system back button (Android-specific)
        BackHandler {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                (context as? android.app.Activity)?.finish()
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
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it }
        )
    }
}
