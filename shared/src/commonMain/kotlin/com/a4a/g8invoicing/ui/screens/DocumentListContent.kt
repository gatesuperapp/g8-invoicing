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
    // false = the doc-type-specific gStore tagging module is off, hide
    // pastilles + status labels on every row. Defaults to true so invoice
    // and credit-note lists (no gStore gate) keep rendering tags.
    tagsEnabled: Boolean = true,
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

    // Warm off-white container behind everything: shows through the space
    // between rows (1dp gap via spacedBy) and behind the month labels.
    // surfaceSubtle (#F6F5F2) is barely tinted so the white rows still stand
    // clearly on top; the row-selected state uses `divider` (a full shade
    // darker) so a checked row visibly pops on this softer background.
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.screen),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        grouped.forEach { (key, docsInMonth) ->
            // Regular list item (not sticky) so the header scrolls off with
            // its group. Sticky mode was leaving the scrolling rows visible
            // behind the (transparent) header — cleaner to let it disappear.
            item(key = "month-$key") {
                // start = 16dp lines the month up with the pill (which
                // FlippyCheckBox now positions at 16dp too) and with the
                // Material-3 TopAppBar title above the list. textSecondary
                // (~6.6:1 on surfaceSubtle) passes WCAG AA — textPale
                // was ~2.5:1, below the 4.5:1 required for 12sp text.
                // The SemiBold + letter-spacing on textSection keeps the
                // rendering read as a section label, not a heading.
                Text(
                    text = monthLabel(key),
                    style = MaterialTheme.typography.textSection.copy(color = AppColors.textSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 20.dp, bottom = 7.dp),
                )
            }
            items(
                items = docsInMonth,
                key = { it.documentId!! },
            ) { document ->
                DocumentListItem(
                    document = document,
                    tagsEnabled = tagsEnabled,
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
