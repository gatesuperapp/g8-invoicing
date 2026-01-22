package com.a4a.g8invoicing.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerList
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerListViewModel
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.clientOrIssuerList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (ClientOrIssuerState) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.ClientOrIssuerList.name) {
        val viewModel: ClientOrIssuerListViewModel = koinViewModel()
        val clientsUiState by viewModel.clientsUiState
            .collectAsStateWithLifecycle()

        val context = LocalContext.current
        var isCategoriesMenuOpen by remember { mutableStateOf(false) }
        var lastBackPressTime by remember { mutableStateOf(0L) }

        // Handle system back button (Android-specific)
        BackHandler {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                (context as? android.app.Activity)?.finish()
            } else {
                lastBackPressTime = currentTime
                isCategoriesMenuOpen = true
            }
        }

        ClientOrIssuerList(
            navController = navController,
            uiState = clientsUiState,
            onClickDelete = viewModel::deleteClientsOrIssuers,
            onClickDuplicate = viewModel::duplicateClientsOrIssuers,
            onClickNew = { onClickNew() },
            onClickCategory = onClickCategory,
            onClickListItem = onClickListItem,
            onClickBack = { onClickBack() },
            isCategoriesMenuOpen = isCategoriesMenuOpen,
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it }
        )
    }
}