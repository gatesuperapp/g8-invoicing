package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.states.DeliveryNoteState

/**/
@Composable
fun DeliveryNoteListContent(
    deliveryNotes: List<DeliveryNoteState>,
    onItemClick: (Int) -> Unit = {},
    addDeliveryNoteToSelectedList: (DeliveryNoteState) -> Unit = {},
    removeDeliveryNoteFromSelectedList: (DeliveryNoteState) -> Unit = {},
    keyToUnselectAll: Boolean,
) {
    LazyColumn {
        items(
            items = deliveryNotes,
            key = {
                it.deliveryNoteId!! //All items displayed in list have an ID
            }
        ) { deliveryNote ->
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

            DeliveryNoteListItem(
                deliveryNote = deliveryNote,
                onItemClick = {
                    deliveryNote.deliveryNoteId?.let {
                        onItemClick(it)
                    }
                },
                onItemCheckboxClick = { isChecked ->
                    // Update deliveryNote list
                    if (isChecked) {
                        addDeliveryNoteToSelectedList(deliveryNote)
                    } else {
                        removeDeliveryNoteFromSelectedList(deliveryNote)
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


