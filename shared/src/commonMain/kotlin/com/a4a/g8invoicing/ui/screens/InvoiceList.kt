package com.a4a.g8invoicing.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.appbar_invoices
import com.a4a.g8invoicing.shared.resources.invoice_advice_bottom_menu
import com.a4a.g8invoicing.shared.resources.invoice_advice_bottom_menu1
import com.a4a.g8invoicing.shared.resources.invoice_advice_bottom_menu2
import com.a4a.g8invoicing.shared.resources.invoice_advice_legal_1
import com.a4a.g8invoicing.shared.resources.invoice_advice_legal_2
import com.a4a.g8invoicing.shared.resources.invoice_advice_legal_3
import com.a4a.g8invoicing.shared.resources.invoice_advice_legal_4
import com.a4a.g8invoicing.shared.resources.invoice_advice_legal_5
import com.a4a.g8invoicing.shared.resources.invoice_advice_legal_6
import com.a4a.g8invoicing.shared.resources.invoice_advice_legal_help
import com.a4a.g8invoicing.shared.resources.invoice_advice_legal_url
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.screens.shared.ScaffoldWithDimmedOverlay
import com.a4a.g8invoicing.ui.shared.AlertDialogDeleteDocument
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.shared.WhatsNewDialog
import com.a4a.g8invoicing.ui.shared.animations.BatOpenMouth
import com.a4a.g8invoicing.ui.shared.animations.BatSmilingEyes
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.InvoicesUiState
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.textWithLinkCenteredMedium
import org.jetbrains.compose.resources.stringResource

@Composable
fun InvoiceList(
    navController: NavController,
    documentsUiState: InvoicesUiState,
    onClickDelete: (List<InvoiceState>) -> Unit,
    onClickDuplicate: (List<InvoiceState>) -> Unit,
    onClickCreateCreditNote: (List<InvoiceState>) -> Unit,
    onClickCreateCorrectedInvoice: (List<InvoiceState>) -> Unit,
    onClickTag: (List<InvoiceState>, DocumentTag) -> Unit,
    onClickNew: () -> Unit,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickBack: () -> Unit,
    onSendReminder: (InvoiceState) -> Unit,
    // Callbacks for platform-specific back handling
    isCategoriesMenuOpen: Boolean = false,
    onCategoriesMenuOpenChange: (Boolean) -> Unit = {},
    // What's New Dialog
    showWhatsNewDialog: Boolean = false,
    appVersion: String = "",
    onDismissWhatsNew: () -> Unit = {},
) {
    // Main list to handle actions with selected items
    val selectedItems = remember { mutableStateListOf<InvoiceState>() }
    // Will recompose the BottomBar (only) when an item is selected, or when all items are unselected
    val selectedMode = remember { mutableStateOf(false) }
    // Will recompose all the items when clicking "unselect all"
    val keyToResetCheckboxes = remember { mutableStateOf(false) }

    // Alert dialogs
    val openDeleteAlertDialog = remember { mutableStateOf(false) }

    // Add background when bottom menu expanded
    val isDimActive = remember { mutableStateOf(false) }

    // What's New dialog
    if (showWhatsNewDialog) {
        WhatsNewDialog(
            appVersion = appVersion,
            onDismiss = onDismissWhatsNew
        )
    }

    ScaffoldWithDimmedOverlay(
        isDimmed = isDimActive.value,
        onDismissDim = { isDimActive.value = false },
        topBar = {
            TopBar(
                title = stringResource(Res.string.appbar_invoices),
                navController = navController,
                onClickBackArrow = onClickBack,
                isCancelCtaDisplayed = false
            )
        },
        bottomBar = {
            GeneralBottomBar(
                navController = navController,
                numberOfItemsSelected = selectedItems.size,
                onClickDelete = {
                    isDimActive.value = !isDimActive.value
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
                    onSendReminder(document)
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickNew = { onClickNew() },
                onClickCategory = onClickCategory,
                isInvoice = true,
                onChangeBackground = {
                    isDimActive.value = !isDimActive.value
                },
                isCategoriesMenuOpen = isCategoriesMenuOpen,
                onCategoriesMenuOpenChange = onCategoriesMenuOpenChange
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
                // Délai pour éviter le flash de l'animation pendant le chargement
                var showEmptyState by remember { mutableStateOf(false) }
                LaunchedEffect(documentsUiState.documentStates.isEmpty()) {
                    if (documentsUiState.documentStates.isEmpty()) {
                        kotlinx.coroutines.delay(300)
                        showEmptyState = true
                    } else {
                        showEmptyState = false
                    }
                }

                if (documentsUiState.documentStates.isEmpty() && showEmptyState) {
                    DisplayBatHelperWelcome()
                } else if (documentsUiState.documentStates.isNotEmpty()) {
                    Column {
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
                            keyToResetCheckboxes = keyToResetCheckboxes.value
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        if (documentsUiState.documentStates.size == 1)
                            DisplayBatHelperMenuAdvice()
                    }
                }
            }
        }

        if (openDeleteAlertDialog.value) {
            AlertDialogDeleteDocument(
                onDismissRequest = {
                    openDeleteAlertDialog.value = false
                    isDimActive.value = !isDimActive.value
                },
                onConfirmation = {
                    openDeleteAlertDialog.value = false
                    onClickDelete(selectedItems.toList())
                    selectedItems.clear()
                    selectedMode.value = false
                    isDimActive.value = !isDimActive.value
                },
                isInvoice = true
            )
        }
    }
}


@Composable
private fun DisplayBatHelperWelcome() {
    var visibleText by remember { mutableIntStateOf(0) }
    val numberOfIterations = remember { mutableIntStateOf(1) }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp)
            .padding(
                top = 20.dp,
                start = 40.dp,
                end = 40.dp,
                bottom = 20.dp
            )
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                if (visibleText < 5) {
                    visibleText += 1
                } else visibleText = 0
                numberOfIterations.intValue += 1
            }
        ) {
            BatSmilingEyes(
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
                    .align(Alignment.Center),
                iterations = numberOfIterations.intValue
            )
        }

        AnimatedVisibility(
            visible = visibleText == 0,
            enter = fadeIn(tween(1000)),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = stringResource(Res.string.invoice_advice_legal_help),
                textAlign = TextAlign.Center
            )
        }

        AnimatedVisibility(
            visible = visibleText == 1,
            enter = fadeIn(
                tween(
                    2000,
                    delayMillis = 100,
                    easing = LinearOutSlowInEasing
                )
            ),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = stringResource(Res.string.invoice_advice_legal_1),
                textAlign = TextAlign.Center
            )
        }

        AnimatedVisibility(
            visible = visibleText == 2,
            enter = fadeIn(
                tween(
                    2000,
                    delayMillis = 100,
                    easing = LinearOutSlowInEasing
                )
            ),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = stringResource(Res.string.invoice_advice_legal_2),
                textAlign = TextAlign.Center
            )
        }


        AnimatedVisibility(
            visible = visibleText == 3,
            enter = fadeIn(
                tween(
                    2000,
                    delayMillis = 100,
                    easing = LinearOutSlowInEasing
                )
            ),
            exit = fadeOut(tween(100)),
        ) {
            TextAdvice(uriHandler)
        }

        AnimatedVisibility(
            visible = visibleText == 4,
            enter = fadeIn(
                tween(
                    2000,
                    delayMillis = 100,
                    easing = LinearOutSlowInEasing
                )
            ),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = stringResource(Res.string.invoice_advice_legal_6),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DisplayBatHelperMenuAdvice() {
    var visibleText by remember { mutableIntStateOf(0) }
    val numberOfIterations = remember { mutableIntStateOf(2) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp)
            .padding(
                top = 20.dp,
                start = 40.dp,
                end = 40.dp,
                bottom = 20.dp
            )
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                if (visibleText < 3) {
                    visibleText += 1
                } else visibleText = 0
                numberOfIterations.intValue += 1
            }
        ) {
            BatOpenMouth(
                modifier = Modifier
                    .width(100.dp)
                    .height(45.dp)
                    .align(Alignment.Center),
                iterations = numberOfIterations.intValue
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        AnimatedVisibility(
            visible = visibleText == 1,
            enter = fadeIn(tween(1000)),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = stringResource(Res.string.invoice_advice_bottom_menu),
                textAlign = TextAlign.Center
            )
        }

        AnimatedVisibility(
            visible = visibleText == 2,
            enter = fadeIn(tween(1000)),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = stringResource(Res.string.invoice_advice_bottom_menu1),
                textAlign = TextAlign.Center
            )
        }


        AnimatedVisibility(
            visible = visibleText == 3,
            enter = fadeIn(
                tween(
                    2000,
                    delayMillis = 100,
                    easing = LinearOutSlowInEasing
                )
            ),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = stringResource(Res.string.invoice_advice_bottom_menu2),
                textAlign = TextAlign.Center
            )
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
    keyToResetCheckboxes.value = !keyToResetCheckboxes.value
}

@Composable
private fun TextAdvice(uriHandler: UriHandler) {
    val text3 = stringResource(Res.string.invoice_advice_legal_3)
    val text4 = stringResource(Res.string.invoice_advice_legal_4)
    val text5 = stringResource(Res.string.invoice_advice_legal_5)
    val url = stringResource(Res.string.invoice_advice_legal_url)

    val annotatedString = buildAnnotatedString {
        append("$text3 ")

        pushStringAnnotation(
            tag = "link",
            annotation = url
        )
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(text4)
        }
        append(text5)
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.textWithLinkCenteredMedium,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "link", start = offset, end = offset)
                .firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
        })
}
