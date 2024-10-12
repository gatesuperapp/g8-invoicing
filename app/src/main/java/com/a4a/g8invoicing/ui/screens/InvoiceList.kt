package com.a4a.g8invoicing.ui.screens

import android.text.TextUtils.substring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.shared.AlertDialogDeleteDocument
import com.a4a.g8invoicing.ui.shared.AlertDialogErrorOrInfo
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.InvoicesUiState
import com.a4a.g8invoicing.ui.theme.ColorBlueLink
import com.a4a.g8invoicing.ui.theme.ColorGrayTransp
import com.a4a.g8invoicing.ui.theme.textWithLinkCenteredMedium

@Composable
fun InvoiceList(
    displayAutoSaveAlertDialog: Boolean,
    onDisplayAutoSaveAlertDialogClose: () -> Unit,
    navController: NavController,
    documentsUiState: InvoicesUiState,
    onClickDelete: (List<InvoiceState>) -> Unit,
    onClickDuplicate: (List<InvoiceState>) -> Unit,
    onClickCreateCreditNote: (List<InvoiceState>) -> Unit,
    onClickCreateCorrectedInvoice: (List<InvoiceState>) -> Unit,
    onClickTag: (List<InvoiceState>, DocumentTag) -> Unit,
    openCreateNewScreen: () -> Unit,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickBack: () -> Unit,
) {
    // Main list to handle actions with selected items
    val selectedItems = remember { mutableStateListOf<InvoiceState>() }
    // Will recompose the BottomBar (only) when an item is selected, or when all items are unselected
    val selectedMode = remember { mutableStateOf(false) }
    // Will recompose all the items when clicking "unselect all"
    val keyToResetCheckboxes = remember { mutableStateOf(false) }

    // Alert dialogs
    val openDeleteAlertDialog = remember { mutableStateOf(false) }
    val checkIfAutoSaveDialogMustBeOpened = remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Add background when bottom menu expanded
    val transparent = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    val backgroundColor = remember { mutableStateOf(transparent) }

    val uriHandler = LocalUriHandler.current

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
                numberOfItemsSelected = selectedItems.size,
                onClickDelete = {
                    backgroundColor.value =
                        changeBackgroundWithVerticalGradient(backgroundColor.value)
                    openDeleteAlertDialog.value = true
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
                    composeEmail(
                        address = document.documentClient?.email?.text,
                        documentNumber = document.documentNumber.text,
                        documentType = document.documentType,
                        context = context,
                        emailMessage = createReminderTextMessage(document)
                    )
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickNew = {
                    checkIfAutoSaveDialogMustBeOpened.value = true
                },
                onClickCategory = onClickCategory,
                isInvoice = true,
                onChangeBackground = {
                    backgroundColor.value =
                        changeBackgroundWithVerticalGradient(backgroundColor.value)
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
                if (documentsUiState.documentStates.isNotEmpty()) {
                    DocumentListContent(
                        documents = documentsUiState.documentStates,
                        onItemClick = onClickListItem,
                        addDocumentToSelectedList = {
                            selectedItems.add(it as InvoiceState)
                            selectedMode.value = true
                        },
                        removeDocumentFromSelectedList = {
                            selectedItems.remove(it as InvoiceState)
                            if (selectedItems.isEmpty()) {
                                selectedMode.value = false
                            }
                        },
                        keyToUnselectAll = keyToResetCheckboxes.value
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(top = 40.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextAdvice(uriHandler)
                    }
                }

                Column(
                    // apply darker background when bottom menu is expanded
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor.value),
                ) {}
            }
        }

        when {
            openDeleteAlertDialog.value -> {
                AlertDialogDeleteDocument(
                    onDismissRequest = {
                        openDeleteAlertDialog.value = false
                        backgroundColor.value =
                            changeBackgroundWithVerticalGradient(backgroundColor.value)
                    },
                    onConfirmation = {
                        openDeleteAlertDialog.value = false
                        onClickDelete(selectedItems.toList())
                        selectedItems.clear()
                        selectedMode.value = false
                        backgroundColor.value =
                            changeBackgroundWithVerticalGradient(backgroundColor.value)
                    },
                    isInvoice = true
                )
            }
        }

        when {
            checkIfAutoSaveDialogMustBeOpened.value -> {
                if(displayAutoSaveAlertDialog) {
                    onDisplayAutoSaveAlertDialogClose()
                    AlertDialogErrorOrInfo(
                        onDismissRequest = {
                            openCreateNewScreen()
                            checkIfAutoSaveDialogMustBeOpened.value = false

                                           },
                        onConfirmation = {
                            openCreateNewScreen()
                            checkIfAutoSaveDialogMustBeOpened.value = false
                        },
                        message = Strings.get(R.string.alert_auto_save_dialog_info),
                        confirmationText = stringResource(id = R.string.alert_dialog_info_confirm)
                    )

                } else {
                    openCreateNewScreen()
                    checkIfAutoSaveDialogMustBeOpened.value = false
                }

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

fun changeBackgroundWithVerticalGradient(initialColor: Brush): Brush {
    val transparent = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    val gradient = Brush.verticalGradient(
        listOf(Color.Transparent, ColorGrayTransp),
        startY = 4f,
        endY = 600f
    )
    return if (initialColor == gradient) {
        transparent
    } else gradient
}

@Composable
fun TextAdvice(uriHandler: UriHandler) {
    val annotatedString = buildAnnotatedString {
        append(Strings.get(R.string.invoice_advice_legal_1) + " ")
        append(
            AnnotatedString(
                Strings.get(R.string.invoice_advice_legal_2) + " ",
                SpanStyle(fontStyle = FontStyle.Italic)
            )
        )
        append(Strings.get(R.string.invoice_advice_legal_3) + " ")

        pushStringAnnotation(
            tag = "link",
            annotation = Strings.get(R.string.invoice_advice_legal_url)
        )
        withStyle(style = SpanStyle(color = ColorBlueLink)) {
            append(Strings.get(R.string.invoice_advice_legal_4))
        }
        append(Strings.get(R.string.invoice_advice_legal_5))
    }

    ClickableText(
        modifier = Modifier
            .padding(top = 20.dp, start = 40.dp, end = 40.dp, bottom = 20.dp),
        text = annotatedString,
        style = MaterialTheme.typography.textWithLinkCenteredMedium,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "link", start = offset, end = offset)
                .firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
        })
}
