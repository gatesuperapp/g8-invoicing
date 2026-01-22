package com.a4a.g8invoicing.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.data.CURRENT_APP_VERSION
import com.a4a.g8invoicing.data.hasSeenPopup
import com.a4a.g8invoicing.data.hasSeenWhatsNew
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.data.setSeenWhatsNew
import com.a4a.g8invoicing.ui.screens.DatabaseEmailDialog
import com.a4a.g8invoicing.ui.screens.DatabaseExportDialog
import com.a4a.g8invoicing.ui.screens.InvoiceList
import com.a4a.g8invoicing.ui.screens.composeEmail
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.viewmodels.InvoiceListViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File

fun NavGraphBuilder.invoiceList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.InvoiceList.name) {
        val viewModel: InvoiceListViewModel = koinViewModel()
        val invoicesUiState by viewModel.documentsUiState
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

        // Database export dialog state
        val hasSeenPopup by hasSeenPopup(context).collectAsState(initial = null)
        var showExportDatabaseDialog by remember { mutableStateOf(false) }
        var showSendDatabaseByEmail by remember { mutableStateOf(false) }
        var exportedFile: File? by remember { mutableStateOf(null) }

        LaunchedEffect(hasSeenPopup, viewModel.hasUserData()) {
            val loaded = hasSeenPopup ?: return@LaunchedEffect
            if (!loaded && viewModel.hasUserData()) {
                showExportDatabaseDialog = true
            }
        }

        LaunchedEffect(exportedFile) {
            if (exportedFile != null) {
                showSendDatabaseByEmail = true
            }
        }

        if (showExportDatabaseDialog) {
            DatabaseExportDialog(
                context = context,
                onDismiss = { showExportDatabaseDialog = false },
                onResult = {
                    showExportDatabaseDialog = false
                    exportedFile = it
                }
            )
        }

        if (showSendDatabaseByEmail) {
            exportedFile?.let {
                DatabaseEmailDialog(
                    context = context,
                    onDismiss = { showSendDatabaseByEmail = false },
                    file = it
                )
            }
        }

        // What's New dialog state
        val hasSeenWhatsNew by hasSeenWhatsNew(context).collectAsState(initial = null)
        var showWhatsNewDialog by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(hasSeenWhatsNew) {
            val seen = hasSeenWhatsNew ?: return@LaunchedEffect
            if (!seen) {
                showWhatsNewDialog = true
            }
        }

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
            onSendReminder = { document ->
                composeEmail(
                    address = document.documentClient?.email?.text,
                    documentNumber = document.documentNumber.text,
                    context = context,
                    emailSubject = Strings.get(
                        R.string.send_reminder_email_subject,
                        document.documentNumber.text
                    ),
                    emailMessage = createReminderTextMessage(document)
                )
            },
            isCategoriesMenuOpen = isCategoriesMenuOpen,
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it },
            showWhatsNewDialog = showWhatsNewDialog,
            appVersion = CURRENT_APP_VERSION,
            onDismissWhatsNew = {
                showWhatsNewDialog = false
                coroutineScope.launch {
                    setSeenWhatsNew(context)
                }
            }
        )
    }
}

private fun createReminderTextMessage(document: InvoiceState): String {
    return Strings.get(
        R.string.send_reminder_email_content,
        document.documentNumber.text,
        document.documentTotalPrices?.totalPriceWithTax ?: 0,
        document.dueDate.take(10)
    )
}