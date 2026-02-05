package com.a4a.g8invoicing.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.ProductTaxRates
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductType

fun NavGraphBuilder.productTaxRates(
    navController: NavController,
    onClickBackOrSelect: () -> Unit,
) {
    composable(
        route = Screen.ProductTaxRates.name,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(600)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(600)
            )
        }
    ) { backStackEntry ->
        val viewModel = backStackEntry.sharedViewModel<ProductAddEditViewModel>(navController)
        val taxRates = viewModel.fetchTaxRatesFromLocalDb()
        val taxRatesWithIds = viewModel.fetchTaxRatesWithIdsFromLocalDb()

        PlatformBackHandler {
            onClickBackOrSelect()
        }

        ProductTaxRates(
            navController = navController,
            taxRates = taxRates,
            taxRatesWithIds = taxRatesWithIds,
            currentTaxRate = viewModel.productUiState.value.taxRate,
            onSelectTaxRate = { selectedTaxRate ->
                onClickBackOrSelect()
                viewModel.updateTaxRate(selectedTaxRate, ProductType.PRODUCT)
            },
            onClickBack = onClickBackOrSelect,
            onSaveTaxRates = { rates ->
                viewModel.saveTaxRates(rates)
            }
        )
    }
}
