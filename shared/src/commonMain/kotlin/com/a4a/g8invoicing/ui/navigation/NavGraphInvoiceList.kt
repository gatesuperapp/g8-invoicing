package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.getAppVersion
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.ui.screens.InvoiceList
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.currentTimeMillis
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.viewmodels.InvoiceListViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.invoiceList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
    // Platform-specific callbacks
    onSendReminder: (InvoiceState) -> Unit = {},
    showCategoryButton: Boolean = true,
    showBottomBar: Boolean = true,
    // What's New dialog
    initialShowWhatsNew: Boolean = false,
    onWhatsNewDismissed: () -> Unit = {},
) {
    composable(route = Screen.InvoiceList.name) {
        val viewModel: InvoiceListViewModel = koinViewModel()
        val invoicesUiState by viewModel.documentsUiState.collectAsState()

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

        // What's New dialog state
        var showWhatsNewDialog by remember { mutableStateOf(initialShowWhatsNew) }

        InvoiceList(
            navController = navController,
            documentsUiState = invoicesUiState,
            onClickDelete = viewModel::delete,
            onClickDuplicate = viewModel::duplicate,
            onClickCreateCreditNote = viewModel::convertToCreditNote,
            onClickCreateCorrectedInvoice = viewModel::convertToCorrectedInvoice,
            onClickTag = { selectedDocuments, tag ->
                viewModel.setTag(selectedDocuments, tag, TagUpdateOrCreationCase.UPDATED_BY_USER)
                viewModel.markAsPaid(selectedDocuments, tag)
            },
            onClickNew = { onClickNew() },
            onClickCategory = onClickCategory,
            onClickListItem = onClickListItem,
            onClickBack = { onClickBack() },
            onSendReminder = onSendReminder,
            isCategoriesMenuOpen = isCategoriesMenuOpen,
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it },
            showCategoryButton = showCategoryButton,
            showBottomBar = showBottomBar,
            showWhatsNewDialog = showWhatsNewDialog,
            appVersion = getAppVersion(),
            onDismissWhatsNew = {
                showWhatsNewDialog = false
                onWhatsNewDismissed()
            }
        )
    }
}
