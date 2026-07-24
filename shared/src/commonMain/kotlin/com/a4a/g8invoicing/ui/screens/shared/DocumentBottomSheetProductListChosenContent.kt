package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_products_other_lines
import com.a4a.g8invoicing.ui.states.DocumentProductState
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun DocumentBottomSheetProductListChosenContent(
    documentProducts: List<DocumentProductState>,
    onClickItem: (DocumentProductState) -> Unit,
    onClickDelete: (Int) -> Unit,
    onOrderChange: (List<DocumentProductState>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current

    var list by remember { mutableStateOf<List<DocumentProductState>>(emptyList()) }
    LaunchedEffect(documentProducts) {
        if (documentProducts != list) {
            list = documentProducts
        }
    }

    val lazyListState = rememberLazyListState()
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }.collect { info ->
            val total = info.visibleItemsInfo.sumOf { it.size }
            println("[SCROLL] viewport=${info.viewportSize.height}px visibleContentSum=${total}px totalItems=${info.totalItemsCount} visibleItems=${info.visibleItemsInfo.size} canFwd=${lazyListState.canScrollForward} canBack=${lazyListState.canScrollBackward}")
        }
    }
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromKey = from.key
        val toKey = to.key
        // Rejection path throws CancellationException instead of returning early so
        // reorderable v3 doesn't set predictedDraggingItemOffset and hang for 1s
        // waiting for a layoutInfo change that will never come. That mid-drag hang is
        // what makes the item snap toward the target visually while the finger is
        // still held. Cancelling here aborts moveItems cleanly before that repositioning.
        if (fromKey !is Int || toKey !is Int) {
            throw CancellationException("Header not draggable")
        }
        val fromProduct = list.firstOrNull { it.id == fromKey }
            ?: throw CancellationException("Product not found")
        val toProduct = list.firstOrNull { it.id == toKey }
            ?: throw CancellationException("Product not found")
        if (fromProduct.linkedDocNumber != toProduct.linkedDocNumber) {
            throw CancellationException("Cross-group drag rejected")
        }
        val newList = list.toMutableList().apply {
            val fromIndex = indexOfFirst { it.id == fromKey }
            val toIndex = indexOfFirst { it.id == toKey }
            add(toIndex, removeAt(fromIndex))
        }
        list = newList
        onOrderChange(newList)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Group products by linkedDocNumber preserving order. Rendered per-group with a
    // stickyHeader followed by an items() call for reorderable products — this is the
    // reorderable v3 grouped pattern.
    val filtered = list.filter { it.id != null }
    val groups = filtered.groupBy { it.linkedDocNumber?.takeIf { s -> s.isNotEmpty() } }
    val orderedKeys = filtered
        .map { it.linkedDocNumber?.takeIf { s -> s.isNotEmpty() } }
        .distinct()
    // In a mixed invoice (some rows from a BL/quote, some added directly), render an
    // "Autres lignes" header above the free-standing rows so every product row sits
    // under a section block. Pure regular invoices (all null groups) get no header.
    val hasLinkedRow = orderedKeys.any { it != null }
    val otherLinesLabel = stringResource(Res.string.document_products_other_lines)

    LazyColumn(
        modifier = modifier
            .padding(
                top = 20.dp,
                bottom = 30.dp
            ),
        state = lazyListState,
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        orderedKeys.forEach { docNumber ->
            val productsInGroup = groups[docNumber].orEmpty()
            if (docNumber != null) {
                item(key = "header_$docNumber") {
                    DocumentBottomSheetProductListSourceBlock(
                        docNumber = docNumber,
                        date = productsInGroup.firstOrNull()?.linkedDate,
                    )
                }
            } else if (hasLinkedRow) {
                item(key = "header_other_lines") {
                    DocumentBottomSheetProductListSourceBlock(
                        docNumber = otherLinesLabel,
                        date = null,
                    )
                }
            }
            items(productsInGroup, key = { it.id!! }) { product ->
                ReorderableItem(reorderableLazyListState, key = product.id!!) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                    Surface(
                        shadowElevation = elevation,
                    ) {
                        DocumentBottomSheetProductListChosenItem(
                            documentProduct = product,
                            onClickDocumentProduct = { onClickItem(product) },
                            onClickDeleteDocumentProduct = {
                                product.id?.let { onClickDelete(it) }
                            },
                            scope = this
                        )
                    }
                }
            }
        }
    }
}
