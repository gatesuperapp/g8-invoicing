package com.a4a.g8invoicing.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.ProductCategories
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.states.ProductCategoryState
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel

fun NavGraphBuilder.productCategories(
    navController: NavController,
    onClickBackOrSelect: () -> Unit,
) {
    composable(
        route = Screen.ProductCategories.name,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(600),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(600),
            )
        }
    ) { backStackEntry ->
        val viewModel = backStackEntry.sharedViewModel<ProductAddEditViewModel>(navController)
        val refreshCounter by viewModel.categoriesRefreshCounter.collectAsState()

        // Load categories asynchronously (DataStore / SQLDelight reads happen
        // on the IO dispatcher inside the data source). The remember is NOT
        // keyed on refreshCounter — resetting to emptyList on each save would
        // flash the "aucune catégorie" empty hint before the fresh fetch lands.
        // Instead we eagerly adopt the user's input in onSaveCategories and
        // let the LaunchedEffect(refreshCounter) overwrite with the canonical
        // rows (with real DB ids) once the write completes.
        var categories by remember {
            mutableStateOf<List<ProductCategoryState>>(emptyList())
        }
        LaunchedEffect(refreshCounter) {
            categories = viewModel.fetchCategoriesFromLocalDb()
        }

        PlatformBackHandler { onClickBackOrSelect() }

        ProductCategories(
            navController = navController,
            categories = categories,
            currentCategoryId = viewModel.productUiState.value.category?.id,
            onSelectCategory = { selected ->
                onClickBackOrSelect()
                viewModel.updateCategory(selected)
            },
            onClickBack = onClickBackOrSelect,
            onSaveCategories = { next ->
                // Optimistic update first so the picker renders the new list
                // immediately — avoids a frame of empty-hint flash between the
                // async save and the Flow-driven refresh. The LaunchedEffect
                // above will overwrite with the DB-backed rows (real ids + any
                // sort_order fixups) once the save completes.
                categories = next
                viewModel.saveCategories(next)
            },
        )
    }
}
