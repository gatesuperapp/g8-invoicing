package com.a4a.g8invoicing.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
    composable(
        route = Screen.InvoiceList.name,
        enterTransition = { // Define smooth enter transition
            fadeIn(animationSpec = tween(500))
        },
        exitTransition = { // Define smooth exit transition for when navigating away from detail
            fadeOut(animationSpec = tween(500))
        },
    ) {
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
        )
    }
}