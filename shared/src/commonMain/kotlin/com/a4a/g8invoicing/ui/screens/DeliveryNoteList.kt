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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.appbar_delivery_notes
import com.a4a.g8invoicing.shared.resources.appbar_tag_cancelled_masc
import com.a4a.g8invoicing.shared.resources.appbar_tag_draft
import com.a4a.g8invoicing.shared.resources.appbar_tag_invoiced
import com.a4a.g8invoicing.shared.resources.appbar_tag_sent_masc
import com.a4a.g8invoicing.shared.resources.delivery_note_advice_convert
import com.a4a.g8invoicing.shared.resources.delivery_note_advice_convert1
import com.a4a.g8invoicing.shared.resources.delivery_note_advice_convert2
import com.a4a.g8invoicing.shared.resources.delivery_note_empty_cta
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.navigation.TagFilterOption
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.theme.ColorCancelled
import com.a4a.g8invoicing.ui.theme.ColorGreenPaid
import com.a4a.g8invoicing.ui.theme.ColorGreyDraft
import com.a4a.g8invoicing.ui.theme.ColorSent
import com.a4a.g8invoicing.ui.screens.shared.ScaffoldWithDimmedOverlay
import com.a4a.g8invoicing.ui.screens.shared.filterByQuery
import com.a4a.g8invoicing.ui.shared.AlertDialogDeleteDocument
import com.a4a.g8invoicing.ui.shared.AlertDialogInvoiceCreated
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.animations.BatWavyArms
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DeliveryNotesUiState
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeliveryNoteList(
    navController: NavController,
    documentsUiState: DeliveryNotesUiState,
    onClickDelete: (List<DeliveryNoteState>) -> Unit,
    onClickDuplicate: (List<DeliveryNoteState>) -> Unit,
    onClickConvert: (List<DeliveryNoteState>) -> Unit,
    onClickNew: () -> Unit,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickBack: () -> Unit,
    onClickViewCreatedInvoice: (Long) -> Unit = {},
    onDismissInvoiceCreatedDialog: () -> Unit = {},
    onClickTag: (List<DeliveryNoteState>, DocumentTag) -> Unit = { _, _ -> },
    isTagPickerEnabled: Boolean = false,
    // Callbacks for platform-specific back handling
    isCategoriesMenuOpen: Boolean = false,
    onCategoriesMenuOpenChange: (Boolean) -> Unit = {},
    showCategoryButton: Boolean = true,
) {
    // Main list to handle actions with selected items
    val selectedItems = remember { mutableStateListOf<DeliveryNoteState>() }

    // Will recompose the BottomBar (only) when an item is selected, or when all items are unselected
    val selectedMode = remember { mutableStateOf(false) }
    // Will recompose all the items when clicking "unselect all"
    val keyToResetCheckboxes = remember { mutableStateOf(false) }

    // Alert dialogs
    val openAlertDialog = remember { mutableStateOf(false) }

    // Add background when bottom menu expanded
    val isDimActive = remember { mutableStateOf(false) }

    // Search state — lives at the screen level so the query survives item
    // taps + list recompositions but resets when leaving the tab.
    var searchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    // Status filter — BL uses masculine labels and the Invoiced status
    // replaces Invoice's Paid/Late/Reminded/Locked set.
    var selectedTagFilterOrdinal by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedTagFilter: DocumentTag? = selectedTagFilterOrdinal
        ?.let { DocumentTag.entries.getOrNull(it) }
    val tagFilterOptions = listOf(
        TagFilterOption(DocumentTag.DRAFT, stringResource(Res.string.appbar_tag_draft), ColorGreyDraft),
        TagFilterOption(DocumentTag.SENT, stringResource(Res.string.appbar_tag_sent_masc), ColorSent),
        TagFilterOption(DocumentTag.CANCELLED, stringResource(Res.string.appbar_tag_cancelled_masc), ColorCancelled),
        TagFilterOption(DocumentTag.INVOICED, stringResource(Res.string.appbar_tag_invoiced), ColorGreenPaid),
    )

    // System back closes the search bar first if it's open.
    PlatformBackHandler(enabled = searchExpanded) {
        searchQuery = TextFieldValue("")
        searchExpanded = false
    }

    // System back clears selection before navigating.
    PlatformBackHandler(enabled = selectedItems.isNotEmpty()) {
        resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
    }

    ScaffoldWithDimmedOverlay(
        isDimmed = isDimActive.value,
        onDismissDim = { isDimActive.value = false },
        topBar = {
            TopBar(
                title = stringResource(Res.string.appbar_delivery_notes),
                navController = navController,
                onClickBackArrow = {
                    if (selectedItems.isNotEmpty()) {
                        resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                    } else {
                        onClickBack()
                    }
                },
                isCancelCtaDisplayed = false,
                searchEnabled = selectedItems.isEmpty(),
                searchExpanded = searchExpanded,
                searchQuery = searchQuery,
                onSearchToggle = { searchExpanded = !searchExpanded },
                onSearchQueryChange = { searchQuery = it },
                // Only expose the status filter when the "Marquer BL" module
                // is active — otherwise there's no status to filter against,
                // every row is DRAFT by default.
                tagFilterEnabled = isTagPickerEnabled && selectedItems.isEmpty(),
                tagFilterOptions = tagFilterOptions,
                selectedTagFilter = selectedTagFilter,
                onTagFilterChange = { picked -> selectedTagFilterOrdinal = picked?.ordinal },
            )
        },
        bottomBar = {
            GeneralBottomBar(
                navController = navController,
                numberOfItemsSelected = selectedItems.size,
                onClickDelete = {
                    isDimActive.value = !isDimActive.value
                    openAlertDialog.value = true
                },
                onClickDuplicate = {
                    onClickDuplicate(selectedItems.toList())
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickConvert = {
                    onClickConvert(selectedItems.toList())
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickUnselectAll = {
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickNew = { onClickNew() },
                onClickCategory = onClickCategory,
                onClickTag = { tag ->
                    onClickTag(selectedItems.toList(), tag)
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                isConvertible = true,
                isTagPickerEnabled = isTagPickerEnabled,
                onChangeBackground = {
                    isDimActive.value = !isDimActive.value
                },
                isCategoriesMenuOpen = isCategoriesMenuOpen,
                onCategoriesMenuOpenChange = onCategoriesMenuOpenChange,
                showCategoryButton = showCategoryButton
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val filteredDocuments = remember(
                    documentsUiState.deliveryNoteStates, searchQuery.text, selectedTagFilter,
                ) {
                    documentsUiState.deliveryNoteStates
                        .filterByQuery(searchQuery.text)
                        .let { list ->
                            val tag = selectedTagFilter ?: return@let list
                            list.filter { it.documentTag == tag }
                        }
                }
                val hasActiveQuery = searchQuery.text.isNotBlank()

                // Short delay before showing the empty-state mascot — the first
                // DB-flow emission is `[]` and gets corrected ~instantly on
                // cold start, so without the gate the mascot flashes in and
                // out. Mirrors InvoiceList.showEmptyState.
                var showEmptyState by remember { mutableStateOf(false) }
                LaunchedEffect(documentsUiState.deliveryNoteStates.isEmpty()) {
                    if (documentsUiState.deliveryNoteStates.isEmpty()) {
                        kotlinx.coroutines.delay(300)
                        showEmptyState = true
                    } else {
                        showEmptyState = false
                    }
                }

                if (documentsUiState.deliveryNoteStates.isEmpty() && showEmptyState) {
                    EmptyListMascot(stringResource(Res.string.delivery_note_empty_cta))
                } else if (documentsUiState.deliveryNoteStates.isNotEmpty()) {
                    Column {
                        // No need to have pull to refresh because it's a flow,
                        // thus the list is updated when anything changes in db
                        DocumentListContent(
                            documents = filteredDocuments,
                            onItemClick = onClickListItem,
                            addDocumentToSelectedList = {
                                selectedItems.add(it as DeliveryNoteState)
                                selectedMode.value = true
                            },
                            removeDocumentFromSelectedList = {
                                selectedItems.remove(it as DeliveryNoteState)
                                if (selectedItems.isEmpty()) {
                                    selectedMode.value = false
                                }
                            },
                            keyToResetCheckboxes = keyToResetCheckboxes.value,
                            tagsEnabled = isTagPickerEnabled,
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Skip the convert hint while searching (single-result queries
                        // would trigger it out of context).
                        if (documentsUiState.deliveryNoteStates.size == 1 && !hasActiveQuery)
                            DisplayBatHelperAdvice()
                    }
                }
            }
        }

        when {
            openAlertDialog.value -> {
                AlertDialogDeleteDocument(
                    onDismissRequest = {
                        openAlertDialog.value = false
                        isDimActive.value = !isDimActive.value
                    },
                    onConfirmation = {
                        openAlertDialog.value = false
                        onClickDelete(selectedItems.toList())
                        selectedItems.clear()
                        selectedMode.value = false
                        isDimActive.value = !isDimActive.value
                    }
                )
            }
        }

        // Show dialog when invoice is created from delivery notes
        documentsUiState.createdInvoiceId?.let { invoiceId ->
            AlertDialogInvoiceCreated(
                onDismissRequest = {
                    onDismissInvoiceCreatedDialog()
                },
                onConfirmation = {
                    onDismissInvoiceCreatedDialog()
                    onClickViewCreatedInvoice(invoiceId)
                }
            )
        }
    }
}

@Composable
private fun DisplayBatHelperAdvice() {
    var visibleText by remember { mutableIntStateOf(0) }
    val numberOfIterations = remember { mutableIntStateOf(4) }

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
            BatWavyArms(
                modifier = Modifier
                    .width(130.dp)
                    .height(80.dp)
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
                text = stringResource(Res.string.delivery_note_advice_convert),
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
                text = stringResource(Res.string.delivery_note_advice_convert1),
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
                text = stringResource(Res.string.delivery_note_advice_convert2),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun resetSelectedItems(
    selectedItems: MutableList<DeliveryNoteState>,
    selectedMode: MutableState<Boolean>,
    keyToResetCheckboxes: MutableState<Boolean>,
) {
    selectedItems.clear()
    selectedMode.value = false
    // Allow to "reset" the checkbox rememberValue to false inDeliveryNoteListItem when recomposing
    keyToResetCheckboxes.value = !keyToResetCheckboxes.value
}
