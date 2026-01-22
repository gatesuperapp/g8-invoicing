package com.a4a.g8invoicing.ui.navigation

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
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
        val context = LocalContext.current

        Account(
            navController = navController,
            onClickCategory = onClickCategory,
            onClickBack = onClickBack,
            onShareContent = { content ->
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, content)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
        )
    }
}
