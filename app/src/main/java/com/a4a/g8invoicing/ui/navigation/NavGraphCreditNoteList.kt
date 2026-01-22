package com.a4a.g8invoicing.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.CreditNoteList
import com.a4a.g8invoicing.ui.viewmodels.CreditNoteListViewModel

fun NavGraphBuilder.creditNoteList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.CreditNoteList.name) {
        val viewModel: CreditNoteListViewModel = koinViewModel()
        val creditNotesUiState by viewModel.documentsUiState
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
            onClickBack = { onClickBack() },
            isCategoriesMenuOpen = isCategoriesMenuOpen,
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it }
        )
    }
}