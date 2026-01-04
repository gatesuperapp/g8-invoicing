package com.a4a.g8invoicing.ui.screens

import android.text.TextUtils.substring
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.data.hasSeenPopup
import com.a4a.g8invoicing.data.hasSeenWhatsNew
import com.a4a.g8invoicing.data.setSeenWhatsNew
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.screens.shared.ScaffoldWithDimmedOverlay
import com.a4a.g8invoicing.ui.shared.AlertDialogDeleteDocument
import com.a4a.g8invoicing.ui.shared.BatAnimation
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.shared.WhatsNewDialog
import kotlinx.coroutines.launch
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.InvoicesUiState
import com.a4a.g8invoicing.ui.theme.ColorGrayTransp
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.textWithLinkCenteredMedium
import java.io.File

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
    hasUserData: Boolean,
) {
    // Main list to handle actions with selected items
    val selectedItems = remember { mutableStateListOf<InvoiceState>() }
    // Will recompose the BottomBar (only) when an item is selected, or when all items are unselected
    val selectedMode = remember { mutableStateOf(false) }
    // Will recompose all the items when clicking "unselect all"
    val keyToResetCheckboxes = remember { mutableStateOf(false) }

    // Alert dialogs
    val openDeleteAlertDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Add background when bottom menu expanded
    val isDimActive = remember { mutableStateOf(false) }

    // Contrôle du menu catégories et double-tap pour quitter
    var isCategoriesMenuOpen by remember { mutableStateOf(false) }
    var lastBackPressTime by remember { mutableStateOf(0L) }

    // BackHandler pour gérer le retour
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            // Double tap -> quitter
            (context as? android.app.Activity)?.finish()
        } else {
            // Premier tap -> ouvrir le menu
            lastBackPressTime = currentTime
            isCategoriesMenuOpen = true
        }
    }

    //Launch download db popup
    val hasSeenPopup by hasSeenPopup(context).collectAsState(initial = null)
    var showExportDatabaseDialog by remember { mutableStateOf(false) }
    var showSendDatabaseByEmail by remember { mutableStateOf(false) }
    var exportedFile: File? by remember { mutableStateOf(null) }

    LaunchedEffect(hasSeenPopup, hasUserData) {
        val loaded = hasSeenPopup ?: return@LaunchedEffect // wait until the DataStore emits
        if (!loaded && hasUserData) {
            showExportDatabaseDialog = true
        }
    }

    // Show second popup when file has been exported
    LaunchedEffect(exportedFile) {
        if (exportedFile != null) {
            showSendDatabaseByEmail = true
        }
    }

    if (showExportDatabaseDialog) {
        DatabaseExportDialog(
            context = context,
            onDismiss = {
                showExportDatabaseDialog = false
            },
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
                onDismiss = {
                    showSendDatabaseByEmail = false
                },
                file = it
            )
        }
    }

    // What's New dialog pour afficher les nouveautés de la version
    val hasSeenWhatsNew by hasSeenWhatsNew(context).collectAsState(initial = null)
    var showWhatsNewDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(hasSeenWhatsNew) {
        val seen = hasSeenWhatsNew ?: return@LaunchedEffect
        if (!seen) {
            showWhatsNewDialog = true
        }
    }

    if (showWhatsNewDialog) {
        WhatsNewDialog(
            onDismiss = {
                showWhatsNewDialog = false
                coroutineScope.launch {
                    setSeenWhatsNew(context)
                }
            }
        )
    }

    ScaffoldWithDimmedOverlay(
        isDimmed = isDimActive.value,
        onDismissDim = { isDimActive.value = false },
        topBar = {
            TopBar(
                title = R.string.appbar_invoices,
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
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickNew = { onClickNew() },
                onClickCategory = onClickCategory,
                isInvoice = true,
                onChangeBackground = {
                    isDimActive.value = !isDimActive.value
                },
                isCategoriesMenuOpen = isCategoriesMenuOpen,
                onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it }
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
                    isDimActive.value = !isDimActive.value// Réinitialiser le fond //
                },
                isInvoice = true
            )
        }
    }
}


@Composable
fun DisplayBatHelperWelcome() {
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
                interactionSource = remember { MutableInteractionSource() } // This is mandatory
            ) {
                if (visibleText < 5) {
                    visibleText += 1
                } else visibleText = 0
                numberOfIterations.intValue += 1
            }
        ) {
            BatAnimation(
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
                    .align(Alignment.Center),
                file = R.raw.bat_smiling_eyes,
                numberOfIteration = numberOfIterations.intValue
            )
        }

        AnimatedVisibility(
            visible = visibleText == 0,
            enter = fadeIn(tween(1000)),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = Strings.get(R.string.invoice_advice_legal_help),
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
                text = Strings.get(R.string.invoice_advice_legal_1),
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
                text = Strings.get(R.string.invoice_advice_legal_2),
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
                text = Strings.get(R.string.invoice_advice_legal_6),
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
                interactionSource = remember { MutableInteractionSource() } // This is mandatory
            ) {
                if (visibleText < 3) {
                    visibleText += 1
                } else visibleText = 0
                numberOfIterations.intValue += 1
            }
        ) {
            BatAnimation(
                modifier = Modifier
                    .width(100.dp)
                    .height(45.dp)
                    .align(Alignment.Center),
                file = R.raw.bat_openmouth,
                numberOfIteration = numberOfIterations.intValue
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        AnimatedVisibility(
            visible = visibleText == 1,
            enter = fadeIn(tween(1000)),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = Strings.get(R.string.invoice_advice_bottom_menu),
                textAlign = TextAlign.Center
            )
        }

        AnimatedVisibility(
            visible = visibleText == 2,
            enter = fadeIn(tween(1000)),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = Strings.get(R.string.invoice_advice_bottom_menu1),
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
                text = Strings.get(R.string.invoice_advice_bottom_menu2),
                textAlign = TextAlign.Center
            )
        }
    }
}


private fun createReminderTextMessage(document: InvoiceState): String {
    val message =
        Strings.get(
            R.string.send_reminder_email_content, document.documentNumber.text,
            document.documentTotalPrices?.totalPriceWithTax
                ?: 0, substring(
                document.dueDate,
                0,
                10
            )
        )
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
        append(Strings.get(R.string.invoice_advice_legal_3) + " ")

        pushStringAnnotation(
            tag = "link",
            annotation = Strings.get(R.string.invoice_advice_legal_url)
        )
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(Strings.get(R.string.invoice_advice_legal_4))
        }
        append(Strings.get(R.string.invoice_advice_legal_5))
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