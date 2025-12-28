package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.data.TagUpdateOrCreationCase
import com.a4a.g8invoicing.ui.screens.InvoiceList
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
            hasUserData = viewModel.hasUserData()
        )
    }
}