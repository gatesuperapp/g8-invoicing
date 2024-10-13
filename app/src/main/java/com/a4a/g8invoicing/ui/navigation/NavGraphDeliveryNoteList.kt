package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.DeliveryNoteList
import com.a4a.g8invoicing.ui.viewmodels.AlertDialogViewModel
import com.a4a.g8invoicing.ui.viewmodels.DeliveryNoteListViewModel

fun NavGraphBuilder.deliveryNoteList(
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

        val alertDialogViewModel: AlertDialogViewModel = hiltViewModel()

        DeliveryNoteList(
            navController = navController,
            documentsUiState = deliveryNotesUiState,
            onClickDelete = viewModel::deleteDeliveryNotes,
            onClickDuplicate = viewModel::duplicateDeliveryNotes,
            onClickConvert = viewModel::convertDeliveryNotes,
            onClickNew = { onClickNew() },
            onClickCategory = onClickCategory,
            onClickListItem = onClickListItem,
            onClickBack = { onClickBack() })
    }
}