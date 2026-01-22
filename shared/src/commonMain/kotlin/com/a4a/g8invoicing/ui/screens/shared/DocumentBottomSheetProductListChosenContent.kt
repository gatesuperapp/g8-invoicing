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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.states.DocumentProductState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun DocumentBottomSheetProductListChosenContent(
    documentProducts: List<DocumentProductState>,
    onClickItem: (DocumentProductState) -> Unit,
    onClickDelete: (Int) -> Unit,
    onOrderChange: (List<DocumentProductState>) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    // Local documentProducts list, used to enable items re-ordering
    var list by remember { mutableStateOf<List<DocumentProductState>>(emptyList()) }
    // This LaunchedEffect will execute each time `documentProducts` change.
    // It will update the local list
    LaunchedEffect(documentProducts) {
        //Update only if the sorting is different than local sorting
        if (documentProducts != list) {
            list = documentProducts
        }
    }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val newList = list.toMutableList().apply {
            val fromIndex = indexOfFirst { it.id == from.key }
            val toIndex = indexOfFirst { it.id == to.key }
            add(toIndex, removeAt(fromIndex))
        }

        list = newList
        // Call onOrderChange so the newOrder is saved
        onOrderChange(newList)

        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    LazyColumn(
        modifier = Modifier
            .padding(
                top = 20.dp,
                bottom = 30.dp
            ),
        state = lazyListState,
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(list, key = { it.id!! }) { item ->
            ReorderableItem(reorderableLazyListState, key = item.id!!) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                Surface(shadowElevation = elevation) {
                    DocumentBottomSheetProductListChosenItem(
                        documentProduct = item,
                        onClickDocumentProduct = {
                            onClickItem(item)
                        },
                        onClickDeleteDocumentProduct = {
                            item.id?.let {
                                onClickDelete(it)
                            }
                        },
                        this
                    )

                }
            }
        }
    }
}
