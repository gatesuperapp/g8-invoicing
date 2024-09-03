package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.screens.shared.getDateFormatter
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.CreditNoteState

/**/
@Composable
fun CreditNoteListContent(
    documents: List<CreditNoteState>,
    onItemClick: (Int) -> Unit = {},
    addDeliveryNoteToSelectedList: (CreditNoteState) -> Unit = {},
    removeDeliveryNoteFromSelectedList: (CreditNoteState) -> Unit = {},
    keyToUnselectAll: Boolean,
) {
    LazyColumn {
        items(
            items = documents.sortedByDescending { getDateFormatter(pattern = "yyyy-MM-dd HH:mm:ss").parse(it.createdDate ?: "")?.time },
            key = {
                it.documentId!!
            }
        ) { document ->
            /* val dismissDirections = setOf(DismissDirection.EndToStart)

             val dismissState = rememberDismissState(
                 confirmValueChange = {
                     if (it == DismissValue.DismissedToEnd) {
                     }
                     true
                 }
             )

             // After swiping, restore item to original state (don't remove it as in a delete swipe)
             if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                 if (dismissState.currentValue != DismissValue.Default) {
                     ResetItem(dismissState)
                 }
             }
 */

            CreditNoteListItem(
                document = document,
                onItemClick = {
                    document.documentId?.let {
                        onItemClick(it)
                    }
                },
                onItemCheckboxClick = { isChecked ->
                    // Update deliveryNote list
                    if (isChecked) {
                        addDeliveryNoteToSelectedList(document)
                    } else {
                        removeDeliveryNoteFromSelectedList(document)
                    }
                },
                keyToUnselectAll
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = 0.6f)
            )
        }
    }
}


