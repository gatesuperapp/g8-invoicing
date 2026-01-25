package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.About
import com.a4a.g8invoicing.ui.screens.ExportResult
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.currentTimeMillis

fun NavGraphBuilder.about(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
    // Platform-specific callbacks
    versionName: String = "1.0.0",
    onShareContent: (String) -> Unit = {},
    onExportDatabase: () -> ExportResult = { ExportResult.Error("Not available on this platform") },
    onSendDatabaseByEmail: (String) -> Unit = {},
    onComposeEmail: (String, String, String) -> Unit = { _, _, _ -> },
    showCategoryButton: Boolean = true,
) {
    composable(route = Screen.About.name) {
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

        About(
            navController = navController,
            onClickCategory = onClickCategory,
            onClickBack = onClickBack,
            versionName = versionName,
            onShareContent = onShareContent,
            onExportDatabase = onExportDatabase,
            onSendDatabaseByEmail = onSendDatabaseByEmail,
            onComposeEmail = onComposeEmail,
            isCategoriesMenuOpen = isCategoriesMenuOpen,
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it },
            showCategoryButton = showCategoryButton
        )
    }
}
