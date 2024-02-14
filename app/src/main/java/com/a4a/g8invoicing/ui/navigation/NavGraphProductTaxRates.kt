package com.a4a.g8invoicing.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.screens.ProductTaxRates
import java.math.BigDecimal

fun NavGraphBuilder.productTaxRates(
    navController: NavController,
    onClickBackOrSelect: () -> Unit
) {
    composable(route = Screen.ProductTaxRates.name) {backStackEntry ->
        val viewModel = backStackEntry.sharedViewModel<ProductAddEditViewModel>(navController)
        val taxRates = viewModel.fetchTaxRatesFromLocalDb()


        ProductTaxRates(
            navController = navController,
            taxRates = taxRates,
            currentTaxRate = viewModel.productUiState.value.taxRate ?: BigDecimal(0),
            onSelectTaxRate = { selectedTaxRate ->
                onClickBackOrSelect()
                viewModel.updateTaxRate(selectedTaxRate)
            },
            onClickBack = onClickBackOrSelect,
        )
    }
}