package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.DeliveryNoteList
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.currentTimeMillis
import com.a4a.g8invoicing.ui.viewmodels.DeliveryNoteListViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.deliveryNoteList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
    showCategoryButton: Boolean = true,
) {
    composable(route = Screen.DeliveryNoteList.name) {
        val viewModel: DeliveryNoteListViewModel = koinViewModel()
        val deliveryNotesUiState by viewModel.deliveryNotesUiState.collectAsState()

        var isCategoriesMenuOpen by remember { mutableStateOf(false) }
        var lastBackPressTime by remember { mutableStateOf(0L) }

        PlatformBackHandler {
            val currentTime = currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                // Double back - exit (handled by platform)
            } else {
                lastBackPressTime = currentTime
                isCategoriesMenuOpen = true
            }
        }

        DeliveryNoteList(
            navController = navController,
            documentsUiState = deliveryNotesUiState,
            onClickDelete = viewModel::deleteDeliveryNotes,
            onClickDuplicate = viewModel::duplicateDeliveryNotes,
            onClickConvert = viewModel::convertDeliveryNotes,
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
