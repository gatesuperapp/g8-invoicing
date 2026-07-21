package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.GStore
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.currentTimeMillis

fun NavGraphBuilder.gStore(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.GStore.name) {
        var isCategoriesMenuOpen by remember { mutableStateOf(false) }
        var lastBackPressTime by remember { mutableStateOf(0L) }

        // Same first-back-opens-drawer / second-back-exits pattern as the list
        // screens. Without this, system back on the gStore just closes the app.
        PlatformBackHandler {
            val currentTime = currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                // Double back — exit
            } else {
                lastBackPressTime = currentTime
                isCategoriesMenuOpen = true
            }
        }

        GStore(
            navController = navController,
            onClickCategory = onClickCategory,
            onClickBack = onClickBack,
            isCategoriesMenuOpen = isCategoriesMenuOpen,
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it },
        )
    }
}
