package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.appbar_sales_orders
import com.a4a.g8invoicing.shared.resources.appbar_tag_cancelled_masc
import com.a4a.g8invoicing.shared.resources.appbar_tag_draft
import com.a4a.g8invoicing.shared.resources.appbar_tag_invoiced
import com.a4a.g8invoicing.shared.resources.appbar_tag_sent_masc
import com.a4a.g8invoicing.shared.resources.delivery_note_created_body
import com.a4a.g8invoicing.shared.resources.delivery_note_created_button
import com.a4a.g8invoicing.shared.resources.sales_order_empty_cta
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.navigation.TagFilterOption
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.screens.shared.ScaffoldWithDimmedOverlay
import com.a4a.g8invoicing.ui.screens.shared.filterByQuery
import com.a4a.g8invoicing.ui.shared.AlertDialogDeleteDocument
import com.a4a.g8invoicing.ui.shared.AlertDialogInvoiceCreated
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.states.SalesOrderState
import com.a4a.g8invoicing.ui.states.SalesOrdersUiState
import com.a4a.g8invoicing.ui.theme.ColorCancelled
import com.a4a.g8invoicing.ui.theme.ColorGreenPaid
import com.a4a.g8invoicing.ui.theme.ColorGreyDraft
import com.a4a.g8invoicing.ui.theme.ColorSent
import org.jetbrains.compose.resources.stringResource

@Composable
fun SalesOrderList(
    navController: NavController,
    documentsUiState: SalesOrdersUiState,
    onClickDelete: (List<SalesOrderState>) -> Unit,
    onClickDuplicate: (List<SalesOrderState>) -> Unit,
    onClickConvertToInvoice: (List<SalesOrderState>) -> Unit,
    onClickConvertToDeliveryNote: (List<SalesOrderState>) -> Unit,
    onClickNew: () -> Unit,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickBack: () -> Unit,
    onClickViewCreatedInvoice: (Long) -> Unit = {},
    onDismissInvoiceCreatedDialog: () -> Unit = {},
    onClickViewCreatedDeliveryNote: (Long) -> Unit = {},
    onDismissDeliveryNoteCreatedDialog: () -> Unit = {},
    onClickTag: (List<SalesOrderState>, DocumentTag) -> Unit = { _, _ -> },
    isTagPickerEnabled: Boolean = false,
    isCategoriesMenuOpen: Boolean = false,
    onCategoriesMenuOpenChange: (Boolean) -> Unit = {},
    showCategoryButton: Boolean = true,
) {
    val selectedItems = remember { mutableStateListOf<SalesOrderState>() }
    val selectedMode = remember { mutableStateOf(false) }
    val keyToResetCheckboxes = remember { mutableStateOf(false) }
    val openAlertDialog = remember { mutableStateOf(false) }
    val isDimActive = remember { mutableStateOf(false) }

    var searchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    var selectedTagFilterOrdinal by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedTagFilter: DocumentTag? = selectedTagFilterOrdinal
        ?.let { DocumentTag.entries.getOrNull(it) }
    val tagFilterOptions = listOf(
        TagFilterOption(DocumentTag.DRAFT, stringResource(Res.string.appbar_tag_draft), ColorGreyDraft),
        TagFilterOption(DocumentTag.SENT, stringResource(Res.string.appbar_tag_sent_masc), ColorSent),
        TagFilterOption(DocumentTag.CANCELLED, stringResource(Res.string.appbar_tag_cancelled_masc), ColorCancelled),
        TagFilterOption(DocumentTag.INVOICED, stringResource(Res.string.appbar_tag_invoiced), ColorGreenPaid),
    )

    PlatformBackHandler(enabled = searchExpanded) {
        searchQuery = TextFieldValue("")
        searchExpanded = false
    }

    PlatformBackHandler(enabled = selectedItems.isNotEmpty()) {
        resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
    }

    ScaffoldWithDimmedOverlay(
        isDimmed = isDimActive.value,
        onDismissDim = { isDimActive.value = false },
        topBar = {
            TopBar(
                title = stringResource(Res.string.appbar_sales_orders),
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
                onClickConvertToInvoice = {
                    onClickConvertToInvoice(selectedItems.toList())
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickConvertToDeliveryNote = {
                    onClickConvertToDeliveryNote(selectedItems.toList())
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
                isSalesOrder = true,
                isTagPickerEnabled = isTagPickerEnabled,
                onChangeBackground = {
                    isDimActive.value = !isDimActive.value
                },
                isCategoriesMenuOpen = isCategoriesMenuOpen,
                onCategoriesMenuOpenChange = onCategoriesMenuOpenChange,
                showCategoryButton = showCategoryButton,
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
            Box(modifier = Modifier.fillMaxSize()) {
                val filteredDocuments = remember(
                    documentsUiState.salesOrderStates, searchQuery.text, selectedTagFilter,
                ) {
                    documentsUiState.salesOrderStates
                        .filterByQuery(searchQuery.text)
                        .let { list ->
                            val tag = selectedTagFilter ?: return@let list
                            list.filter { it.documentTag == tag }
                        }
                }

                var showEmptyState by remember { mutableStateOf(false) }
                LaunchedEffect(documentsUiState.salesOrderStates.isEmpty()) {
                    if (documentsUiState.salesOrderStates.isEmpty()) {
                        kotlinx.coroutines.delay(300)
                        showEmptyState = true
                    } else {
                        showEmptyState = false
                    }
                }

                if (documentsUiState.salesOrderStates.isEmpty() && showEmptyState) {
                    EmptyListMascot(stringResource(Res.string.sales_order_empty_cta))
                } else if (documentsUiState.salesOrderStates.isNotEmpty()) {
                    Column {
                        DocumentListContent(
                            documents = filteredDocuments,
                            onItemClick = onClickListItem,
                            addDocumentToSelectedList = {
                                selectedItems.add(it as SalesOrderState)
                                selectedMode.value = true
                            },
                            removeDocumentFromSelectedList = {
                                selectedItems.remove(it as SalesOrderState)
                                if (selectedItems.isEmpty()) {
                                    selectedMode.value = false
                                }
                            },
                            keyToResetCheckboxes = keyToResetCheckboxes.value,
                            tagsEnabled = isTagPickerEnabled,
                        )

                        Spacer(modifier = Modifier.height(20.dp))
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

        documentsUiState.createdInvoiceId?.let { invoiceId ->
            AlertDialogInvoiceCreated(
                onDismissRequest = { onDismissInvoiceCreatedDialog() },
                onConfirmation = {
                    onDismissInvoiceCreatedDialog()
                    onClickViewCreatedInvoice(invoiceId)
                }
            )
        }

        documentsUiState.createdDeliveryNoteId?.let { deliveryNoteId ->
            AlertDialogInvoiceCreated(
                onDismissRequest = { onDismissDeliveryNoteCreatedDialog() },
                onConfirmation = {
                    onDismissDeliveryNoteCreatedDialog()
                    onClickViewCreatedDeliveryNote(deliveryNoteId)
                },
                titleText = stringResource(Res.string.delivery_note_created_body),
                buttonText = stringResource(Res.string.delivery_note_created_button),
            )
        }
    }
}

private fun resetSelectedItems(
    selectedItems: MutableList<SalesOrderState>,
    selectedMode: MutableState<Boolean>,
    keyToResetCheckboxes: MutableState<Boolean>,
) {
    selectedItems.clear()
    selectedMode.value = false
    keyToResetCheckboxes.value = !keyToResetCheckboxes.value
}
