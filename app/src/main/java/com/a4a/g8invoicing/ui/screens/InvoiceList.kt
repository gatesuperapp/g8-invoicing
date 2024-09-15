package com.a4a.g8invoicing.ui.screens

import android.text.TextUtils.substring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.shared.AlertDialogDeleteDocument
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.InvoicesUiState
import com.a4a.g8invoicing.ui.theme.ColorGrayTransp

@Composable
fun InvoiceList(
    navController: NavController,
    documentsUiState: InvoicesUiState,
    onClickDelete: (List<InvoiceState>) -> Unit,
    onClickDuplicate: (List<InvoiceState>) -> Unit,
    onClickCreateCreditNote: (List<InvoiceState>) -> Unit,
    onClickCreateCorrectedInvoice: (List<InvoiceState>) -> Unit,
    onClickTag: (List<InvoiceState>, DocumentTag) -> Unit,
    onClickSendReminder: (InvoiceState) -> Unit,
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

    val context = LocalContext.current

    val transparent = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    var backgroundColor = remember { mutableStateOf(transparent) }

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
                selectedMode = selectedMode.value,
                numberOfItemsSelected = selectedItems.size,
                onClickDelete = {
                    openAlertDialog.value = true
                },
                onClickCreateCreditNote = {
                    onClickCreateCreditNote(selectedItems.toList())
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickCreateCorrectedInvoice = {
                    onClickCreateCorrectedInvoice(selectedItems.toList())
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
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
                onClickSendReminder = {
                    val document = selectedItems.first()
                    onClickSendReminder(selectedItems.first())
                    composeEmail(
                        address = document.documentClient?.email?.text,
                        documentNumber = document.documentNumber.text,
                        documentType = document.documentType,
                        context = context,
                        emailMessage = createReminderTextMessage(document)
                    )
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickNew = { onClickNew() },
                onClickCategory = onClickCategory,
                isInvoice = true,
                onChangeBackground = {
                    backgroundColor.value = changeBackgroundColor(backgroundColor.value)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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

                Column(
                    // apply darker background when bottom menu is expanded
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor.value),
                ) {}
            }
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
                    },
                    isInvoice = true
                )
            }
        }
    }
}

private fun createReminderTextMessage(document: InvoiceState): String {
    val message =
        Strings.get(R.string.send_reminder_email_1) + " " + document.documentNumber.text + " " + Strings.get(
            R.string.send_reminder_email_2
        ) + " " + (document.documentPrices?.totalPriceWithTax
            ?: 0) + Strings.get(R.string.currency) + " " + Strings.get(R.string.send_reminder_email_3) + " " + substring(
            document.dueDate,
            0,
            10
        ) + ".\n\n" + Strings.get(R.string.send_reminder_email_4)

    return message
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

fun changeBackgroundColor(initialColor: Brush): Brush {
    val transparent = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    val gradient = Brush.verticalGradient(
        listOf(Color.Transparent, ColorGrayTransp),
        startY = 4f,
        endY = 600f
        )
    return if(initialColor == gradient) {
        transparent
    } else gradient
}