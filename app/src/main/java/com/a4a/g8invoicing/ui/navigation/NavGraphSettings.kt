package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.DeliveryNoteListViewModel
import com.a4a.g8invoicing.ui.screens.Settings
import com.a4a.g8invoicing.ui.shared.ScreenElement

fun NavGraphBuilder.settings(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
    onClickForward: (ScreenElement) -> Unit
) {
    composable(route = Screen.DeliveryNoteList.name) {
        val viewModel: DeliveryNoteListViewModel = hiltViewModel()
        val deliveryNotesUiState by viewModel.deliveryNotesUiState
            .collectAsStateWithLifecycle()

        Settings(
            navController = navController,
            onClickCategory = onClickCategory,
            onClickBack = onClickBack,
            onClickForward =onClickForward
        )
    }
}