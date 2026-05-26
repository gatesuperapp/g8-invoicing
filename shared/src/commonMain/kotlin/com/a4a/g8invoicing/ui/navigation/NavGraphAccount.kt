package com.a4a.g8invoicing.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.Account
import com.a4a.g8invoicing.ui.screens.ExportResult

fun NavGraphBuilder.account(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
    onShareContent: (String) -> Unit = {},
    onExportDatabase: () -> ExportResult = { ExportResult.Error("Not available on this platform") },
    onSendDatabaseByEmail: (String) -> Unit = {},
) {
    composable(route = Screen.Account.name) {
        Account(
            navController = navController,
            onClickCategory = onClickCategory,
            onClickBack = onClickBack,
            onShareContent = onShareContent,
            onExportDatabase = onExportDatabase,
            onSendDatabaseByEmail = onSendDatabaseByEmail,
        )
    }
}
