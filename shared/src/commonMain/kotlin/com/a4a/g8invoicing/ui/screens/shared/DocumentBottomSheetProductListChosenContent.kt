package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_product_advice
import com.a4a.g8invoicing.shared.resources.document_products_other_lines
import com.a4a.g8invoicing.ui.shared.animations.BatWavyArms
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.RetentionState
import com.a4a.g8invoicing.ui.theme.textBodySmall
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
    // null → no eye rendered (doc type doesn't support the toggle, or state has
    // no source headers to hide). Non-null → single boolean shared across every
    // source-header block; every eye click flips all of them.
    hideLinkedSourceHeaders: Boolean = false,
    onToggleHideLinkedSourceHeaders: (() -> Unit)? = null,
    retentions: List<RetentionState> = emptyList(),
    onClickRetention: (Int) -> Unit = {},
    onToggleRetentionHidden: (Int) -> Unit = {},
    // Petit rhino / bat helper docked at the bottom of the list to hint at
    // "long-tap to edit". Rendered inside the LazyColumn so it doesn't
    // reduce the available vertical space in the parent Column and push
    // retention rows out of the viewport.
    showBatHelperAdvice: Boolean = false,
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
        modifier = modifier.padding(top = 20.dp),
        state = lazyListState,
        // contentPadding.bottom is the only bottom breathing room now — the
        // outer Column no longer reserves a bottom padding, so the LazyColumn
        // reaches down to the system-nav-bar edge and the last row (the
        // 1-product bat-helper advice) has enough scroll headroom for its
        // expanded state.
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        orderedKeys.forEach { docNumber ->
            val productsInGroup = groups[docNumber].orEmpty()
            if (docNumber != null) {
                item(key = "header_$docNumber") {
                    DocumentBottomSheetProductListSourceBlock(
                        docNumber = docNumber,
                        date = productsInGroup.firstOrNull()?.linkedDate,
                        isHidden = hideLinkedSourceHeaders,
                        onToggleHidden = onToggleHideLinkedSourceHeaders,
                    )
                }
            } else if (hasLinkedRow) {
                item(key = "header_other_lines") {
                    DocumentBottomSheetProductListSourceBlock(
                        docNumber = otherLinesLabel,
                        date = null,
                        isHidden = hideLinkedSourceHeaders,
                        onToggleHidden = onToggleHideLinkedSourceHeaders,
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
        itemsIndexed(retentions) { idx, retention ->
            RetentionLineRow(
                retention = retention,
                onClick = { onClickRetention(idx) },
                onToggleHidden = { onToggleRetentionHidden(idx) },
            )
        }
        if (showBatHelperAdvice) {
            item(key = "bat_helper_advice") {
                BatHelperAdvice()
            }
        }
    }
}

// Trailing "long-press to edit" hint shown when the user only has one
// product on the doc. Rendered as a LazyColumn item so it participates in
// scroll instead of eating the parent Column's remaining vertical space
// (which was the earlier arrangement, and squeezed the retention rows out
// of the sheet fold at half-height).
@Composable
private fun BatHelperAdvice() {
    var adviceVisible by remember { mutableStateOf(false) }
    val numberOfIterations = remember { mutableIntStateOf(4) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = adviceVisible,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = stringResource(Res.string.document_product_advice),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.textBodySmall,
            )
        }

        Box(
            Modifier
                .padding(bottom = 32.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    adviceVisible = !adviceVisible
                    numberOfIterations.intValue += 1
                },
        ) {
            BatWavyArms(
                modifier = Modifier
                    .width(80.dp)
                    .height(50.dp)
                    .align(Alignment.Center),
                iterations = numberOfIterations.intValue,
            )
        }
    }
}
