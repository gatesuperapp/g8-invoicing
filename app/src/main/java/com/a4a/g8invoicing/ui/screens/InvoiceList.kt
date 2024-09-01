package com.a4a.g8invoicing.ui.screens

import android.content.ContentValues
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.shared.AlertDialogDeleteDocument
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.InvoicesUiState

@Composable
fun InvoiceList(
    navController: NavController,
    documentsUiState: InvoicesUiState,
    onClickDelete: (List<InvoiceState>) -> Unit,
    onClickDuplicate: (List<InvoiceState>) -> Unit,
    onClickTag: (List<InvoiceState>, DocumentTag) -> Unit,
    onClickNew: () -> Unit,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickBack: () -> Unit,
) {
    // Main list to handle actions with selected items
    val selectedItems = mutableListOf<InvoiceState>()
    // Will recompose the BottomBar (only) when an item is selected, or when all items are unselected
    val selectedMode = remember { mutableStateOf(false) }
    // Will recompose all the items when clicking "unselect all"
    val keyToResetCheckboxes = remember { mutableStateOf(false) }

    // On delete document
    val openAlertDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            com.a4a.g8invoicing.ui.navigation.TopBar(
                title = R.string.appbar_invoices,
                navController = navController,
                onClickBackArrow = onClickBack
            )
        },
        bottomBar = {
            GeneralBottomBar(
                navController = navController,
                isListItemSelected = selectedMode.value,
                onClickDelete = {
                    openAlertDialog.value = true
                },
                onClickDuplicate = {
                    onClickDuplicate(selectedItems.toList())
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickUnselectAll = {
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickTag = {
                    onClickTag(selectedItems.toList(), it)
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickNew = { onClickNew() },
                onClickCategory = onClickCategory,
                isInvoice = true
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(
                    padding
                )
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // No need to have pull to refresh because it's a flow,
            // thus the list is updated when anything changes in db
            InvoiceListContent(
                documents = documentsUiState.documentStates,
                onItemClick = onClickListItem,
                addDeliveryNoteToSelectedList = {
                    selectedItems.add(it)
                    selectedMode.value = true
                },
                removeDeliveryNoteFromSelectedList = {
                    selectedItems.remove(it)
                    if (selectedItems.isEmpty()) {
                        selectedMode.value = false
                    }
                },
                keyToUnselectAll = keyToResetCheckboxes.value
            )
        }

        when {
            openAlertDialog.value -> {
                AlertDialogDeleteDocument(
                    onDismissRequest = { openAlertDialog.value = false },
                    onConfirmation = {
                        openAlertDialog.value = false
                        onClickDelete(selectedItems.toList())
                        selectedItems.clear()
                        selectedMode.value = false
                    }
                )
            }
        }
    }
}

private fun resetSelectedItems(
    selectedItems: MutableList<InvoiceState>,
    selectedMode: MutableState<Boolean>,
    keyToResetCheckboxes: MutableState<Boolean>,
) {
    selectedItems.clear()
    selectedMode.value = false
    // Allow to "reset" the checkbox rememberValue to false inDeliveryNoteListItem when recomposing
    keyToResetCheckboxes.value = !keyToResetCheckboxes.value
}
