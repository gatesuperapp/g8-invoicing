package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.CreditNoteList
import com.a4a.g8invoicing.ui.viewmodels.AlertDialogViewModel
import com.a4a.g8invoicing.ui.viewmodels.CreditNoteListViewModel

fun NavGraphBuilder.creditNoteList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.CreditNoteList.name) {
        val viewModel: CreditNoteListViewModel = hiltViewModel()
        val creditNotesUiState by viewModel.documentsUiState
            .collectAsStateWithLifecycle()

        CreditNoteList(
            navController = navController,
            documentsUiState = creditNotesUiState,
            onClickDelete = viewModel::delete,
            onClickDuplicate = viewModel::duplicate,
            onClickTag = {selectedDocuments, tag ->
                viewModel.setTag(selectedDocuments, tag)
            },
            onClickNew = { onClickNew() },
            onClickCategory = onClickCategory,
            onClickListItem = onClickListItem,
            onClickBack = { onClickBack() })
    }
}