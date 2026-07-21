package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.Account
import com.a4a.g8invoicing.ui.screens.ExportResult
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.currentTimeMillis

fun NavGraphBuilder.account(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
    onShareContent: (String) -> Unit = {},
    onExportDatabase: () -> ExportResult = { ExportResult.Error("Not available on this platform") },
    onSendDatabaseByEmail: (String) -> Unit = {},
    pendingMagicLinkToken: String? = null,
    onMagicLinkTokenConsumed: () -> Unit = {},
) {
    composable(route = Screen.Account.name) {
        var isCategoriesMenuOpen by remember { mutableStateOf(false) }
        var lastBackPressTime by remember { mutableStateOf(0L) }

        // Mirror the list screens: first system-back opens the categories drawer;
        // a second within 2s falls through to the platform exit behaviour. Without
        // this handler, tapping back straight from Mon Compte would just close the
        // app since Account is the root of its own navigation subtree.
        PlatformBackHandler {
            val currentTime = currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                // Double back — exit
            } else {
                lastBackPressTime = currentTime
                isCategoriesMenuOpen = true
            }
        }

        Account(
            navController = navController,
            onClickCategory = onClickCategory,
            onClickBack = onClickBack,
            onShareContent = onShareContent,
            onExportDatabase = onExportDatabase,
            onSendDatabaseByEmail = onSendDatabaseByEmail,
            pendingMagicLinkToken = pendingMagicLinkToken,
            onMagicLinkTokenConsumed = onMagicLinkTokenConsumed,
            isCategoriesMenuOpen = isCategoriesMenuOpen,
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it },
        )
    }
}
