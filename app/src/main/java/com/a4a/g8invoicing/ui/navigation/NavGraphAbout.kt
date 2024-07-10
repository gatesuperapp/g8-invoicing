package com.a4a.g8invoicing.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.About

fun NavGraphBuilder.about(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.About.name) {
        About(
            navController = navController,
            onClickCategory = onClickCategory,
            onClickBack = onClickBack,
        )
    }
}