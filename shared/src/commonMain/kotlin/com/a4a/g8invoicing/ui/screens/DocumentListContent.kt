package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.screens.shared.parseDate
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textSection


@Composable
fun DocumentListContent(
    documents: List<DocumentState>,
    onItemClick: (Int) -> Unit = {},
    addDocumentToSelectedList: (DocumentState) -> Unit = {},
    removeDocumentFromSelectedList: (DocumentState) -> Unit = {},
    keyToResetCheckboxes: Boolean,
) {
    // Skip transient documents that haven't been persisted yet (documentId == null)
    // so the `key` lambda below never crashes on `!!`. Second-precision timestamps
    // collide when several docs are inserted in the same batch (e.g. duplicate a
    // multi-selection): break the tie with documentId so the numerically-newer
    // duplicate stays on top.
    val sorted = documents
        .filter { it.documentId != null }
        .sortedWith(
            compareByDescending<DocumentState> { doc ->
                parseDate(doc.createdDate ?: "", "yyyy-MM-dd HH:mm:ss") ?: 0L
            }.thenByDescending { it.documentId ?: 0 }
        )

    // Group by (year, month) of the document's own date (not createdDate) so
    // rebilling last month's work still slots under last month's header.
    // LinkedHashMap preserves the descending sort order established above.
    val grouped = linkedMapOf<String, MutableList<DocumentState>>()
    for (doc in sorted) {
        val key = monthKey(doc.documentDate)
        grouped.getOrPut(key) { mutableListOf() }.add(doc)
    }

    // Grey container behind everything: shows through the space between rows
    // (1dp gap via spacedBy) and behind the sticky headers, so a header sits
    // on the list's grey — not on the same white as the rows underneath.
    // `divider` (#E3E3E3) is one shade darker than the surfaceMuted used for
    // the row-selected state, so a selected row still visibly pops.
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.divider),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        grouped.forEach { (key, docsInMonth) ->
            // Regular list item (not sticky) so the header scrolls off with
            // its group. Sticky mode was leaving the scrolling rows visible
            // behind the (transparent) header — cleaner to let it disappear.
            item(key = "month-$key") {
                Text(
                    text = monthLabel(key),
                    style = MaterialTheme.typography.textSection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 20.dp, bottom = 7.dp),
                )
            }
            items(
                items = docsInMonth,
                key = { it.documentId!! },
            ) { document ->
                DocumentListItem(
                    document = document,
                    onItemClick = {
                        document.documentId?.let {
                            onItemClick(it)
                        }
                    },
                    onItemCheckboxClick = { isChecked ->
                        // Update list
                        if (isChecked) {
                            addDocumentToSelectedList(document)
                        } else {
                            removeDocumentFromSelectedList(document)
                        }
                    },
                    keyToResetCheckbox = keyToResetCheckboxes
                )
            }
        }
    }
}
