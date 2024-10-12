package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.data.TagUpdateOrCreationCase
import com.a4a.g8invoicing.ui.screens.InvoiceList
import com.a4a.g8invoicing.ui.shared.AlertDialogErrorOrInfo
import com.a4a.g8invoicing.ui.viewmodels.AlertDialogViewModel
import com.a4a.g8invoicing.ui.viewmodels.InvoiceListViewModel

fun NavGraphBuilder.invoiceList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
    ) {
    composable(route = Screen.InvoiceList.name) {
        val viewModel: InvoiceListViewModel = hiltViewModel()
        val invoicesUiState by viewModel.documentsUiState
            .collectAsStateWithLifecycle()

        val alertDialogViewModel: AlertDialogViewModel = hiltViewModel()
        val displayAutoSaveAlertDialog = alertDialogViewModel.fetchAlertDialogFromLocalDb(4) ?: false
        val displayDeleteAlertDialog = remember {
            mutableStateOf(
                alertDialogViewModel.fetchAlertDialogFromLocalDb(6) ?: false
            )
        }
        InvoiceList(
            displayAutoSaveAlertDialog = displayAutoSaveAlertDialog,
            onDisplayAutoSaveAlertDialogClose = {
                alertDialogViewModel.updateAlertDialogInLocalDb(4)
            },
            openCreateNewScreen = { onClickNew() },
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
            onClickCategory = onClickCategory,
            onClickListItem = onClickListItem,
            onClickBack = { onClickBack() }
        )

        when {
            (displayDeleteAlertDialog.value
                    && invoicesUiState.documentStates.isNotEmpty()) -> {
                alertDialogViewModel.updateAlertDialogInLocalDb(6)
                AlertDialogErrorOrInfo(
                    onDismissRequest = {
                        displayDeleteAlertDialog.value = false
                    },
                    onConfirmation = {
                        displayDeleteAlertDialog.value = false
                    },
                    message = Strings.get(R.string.alert_delete_dialog_info),
                    confirmationText = stringResource(id = R.string.alert_delete_info_confirm)
                )
            }
        }
    }
}