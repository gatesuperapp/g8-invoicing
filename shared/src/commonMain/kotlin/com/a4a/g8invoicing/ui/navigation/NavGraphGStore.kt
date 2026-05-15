package com.a4a.g8invoicing.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.GStore

fun NavGraphBuilder.gStore(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.GStore.name) {
        GStore(
            navController = navController,
            onClickCategory = onClickCategory,
            onClickBack = onClickBack,
        )
    }
}
