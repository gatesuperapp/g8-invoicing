package com.a4a.g8invoicing.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.Account

fun NavGraphBuilder.account(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.Account.name) {
        Account(
            navController = navController,
            onClickCategory = onClickCategory,
            onClickBack = onClickBack,
        )
    }
}
