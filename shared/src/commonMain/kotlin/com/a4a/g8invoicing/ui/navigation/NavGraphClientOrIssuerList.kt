package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerList
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.currentTimeMillis
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerListViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.clientOrIssuerList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (ClientOrIssuerState) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
    showCategoryButton: Boolean = true,
) {
    composable(route = Screen.ClientOrIssuerList.name) {
        val viewModel: ClientOrIssuerListViewModel = koinViewModel()
        val clientsUiState by viewModel.clientsUiState.collectAsState()

        var isCategoriesMenuOpen by remember { mutableStateOf(false) }
        var lastBackPressTime by remember { mutableStateOf(0L) }

        // Handle system back button
        PlatformBackHandler {
            val currentTime = currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                // Double back press - exit app (handled by platform)
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
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it },
            showCategoryButton = showCategoryButton
        )
    }
}
