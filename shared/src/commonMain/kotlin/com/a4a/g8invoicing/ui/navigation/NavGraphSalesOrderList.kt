package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.ui.screens.SalesOrderList
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.currentTimeMillis
import com.a4a.g8invoicing.ui.viewmodels.SalesOrderListViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.salesOrderList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
    onClickViewCreatedInvoice: (Long) -> Unit,
    onClickViewCreatedDeliveryNote: (Long) -> Unit,
    showCategoryButton: Boolean = true,
) {
    composable(route = Screen.SalesOrderList.name) {
        val viewModel: SalesOrderListViewModel = koinViewModel()
        val salesOrdersUiState by viewModel.salesOrdersUiState.collectAsState()
        val activatedModules = koinInject<ActivatedModulesRepository>()
        val activatedState by activatedModules.state.collectAsState()
        // Reuse the delivery-note tagging module — both doc types share the
        // same BL/Devis masculine tag palette.
        val isTagPickerEnabled =
            ActivatedModulesRepository.MODULE_DELIVERY_NOTE_TAGGING in activatedState

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

        SalesOrderList(
            navController = navController,
            documentsUiState = salesOrdersUiState,
            onClickDelete = viewModel::deleteSalesOrders,
            onClickDuplicate = viewModel::duplicateSalesOrders,
            onClickConvertToInvoice = viewModel::convertSalesOrdersToInvoice,
            onClickConvertToDeliveryNote = viewModel::convertSalesOrdersToDeliveryNote,
            onClickNew = { onClickNew() },
            onClickCategory = onClickCategory,
            onClickListItem = onClickListItem,
            onClickBack = { onClickBack() },
            onClickViewCreatedInvoice = { invoiceId ->
                viewModel.clearCreatedInvoiceId()
                onClickViewCreatedInvoice(invoiceId)
            },
            onDismissInvoiceCreatedDialog = viewModel::clearCreatedInvoiceId,
            onClickViewCreatedDeliveryNote = { deliveryNoteId ->
                viewModel.clearCreatedDeliveryNoteId()
                onClickViewCreatedDeliveryNote(deliveryNoteId)
            },
            onDismissDeliveryNoteCreatedDialog = viewModel::clearCreatedDeliveryNoteId,
            onClickTag = { selected, tag ->
                viewModel.setTag(selected, tag, TagUpdateOrCreationCase.UPDATED_BY_USER)
            },
            isTagPickerEnabled = isTagPickerEnabled,
            isCategoriesMenuOpen = isCategoriesMenuOpen,
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it },
            showCategoryButton = showCategoryButton,
        )
    }
}
