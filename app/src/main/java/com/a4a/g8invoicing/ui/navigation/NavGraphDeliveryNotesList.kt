package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.DeliveryNoteList
import com.a4a.g8invoicing.ui.screens.DeliveryNoteListViewModel

fun NavGraphBuilder.deliveryNotesList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.DeliveryNoteList.name) {
        val viewModel: DeliveryNoteListViewModel = hiltViewModel()
        val deliveryNotesUiState by viewModel.deliveryNotesUiState
            .collectAsStateWithLifecycle()

        DeliveryNoteList(
            navController = navController,
            deliveryNotesUiState = deliveryNotesUiState,
            onClickDelete = viewModel::deleteDeliveryNotes,
            onClickDuplicate = viewModel::duplicateDeliveryNotes,
            onClickNew = { onClickNew() },
            onClickCategory = onClickCategory,
            onClickListItem = onClickListItem,
            onClickBack = { onClickBack() })
    }
}