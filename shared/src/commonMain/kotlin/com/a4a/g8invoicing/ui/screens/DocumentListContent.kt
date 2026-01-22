package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.screens.shared.parseDate
import com.a4a.g8invoicing.ui.states.DocumentState


@Composable
fun DocumentListContent(
    documents: List<DocumentState>,
    onItemClick: (Int) -> Unit = {},
    addDocumentToSelectedList: (DocumentState) -> Unit = {},
    removeDocumentFromSelectedList: (DocumentState) -> Unit = {},
    keyToResetCheckboxes: Boolean,
) {
    LazyColumn {
        items(
            items = documents.sortedByDescending { doc ->
                parseDate(doc.createdDate ?: "", "yyyy-MM-dd HH:mm:ss") ?: 0L
            },
            key = {
                it.documentId!!
            }
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

            HorizontalDivider(
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = 0.6f)
            )
        }
    }
}
