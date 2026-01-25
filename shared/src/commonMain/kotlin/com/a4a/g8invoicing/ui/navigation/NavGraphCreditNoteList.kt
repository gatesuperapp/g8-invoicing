package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.CreditNoteList
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.currentTimeMillis
import com.a4a.g8invoicing.ui.viewmodels.CreditNoteListViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.creditNoteList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
    showCategoryButton: Boolean = true,
) {
    composable(route = Screen.CreditNoteList.name) {
        val viewModel: CreditNoteListViewModel = koinViewModel()
        val creditNotesUiState by viewModel.documentsUiState.collectAsState()

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

        CreditNoteList(
            navController = navController,
            documentsUiState = creditNotesUiState,
            onClickDelete = viewModel::delete,
            onClickDuplicate = viewModel::duplicate,
            onClickTag = { selectedDocuments, tag ->
                viewModel.setTag(selectedDocuments, tag)
            },
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
