package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.shared.ButtonAddOrChoose
import com.a4a.g8invoicing.ui.shared.CheckboxFace

@Composable
fun ClientOrIssuerListContent(
    clientsOrIssuers: List<ClientOrIssuerState>,
    onItemClick: (ClientOrIssuerState) -> Unit = {},
    addToSelectedList: (ClientOrIssuerState) -> Unit = {},
    removeFromSelectedList: (ClientOrIssuerState) -> Unit = {},
    keyToResetCheckboxes: Boolean = false,
    isCheckboxDisplayed: Boolean = true,
    currentClientOrIssuerId: Int? = null,
) {
    LazyColumn {
        items(
            items = clientsOrIssuers,
            key = { client ->
                client.id!!
            }
        ) { client ->
            // TODO: Allow swipe only if no item selected?
            /* val dismissDirections = setOf(DismissDirection.EndToStart)

             val dismissState = rememberSwipeToDismissBoxState(
                 confirmValueChange = {
                     if (it == SwipeToDismissBoxValue.StartToEnd) {
                     }
                     true
                 }
             )

             // After swiping, restore item to original state (don't remove it as in a delete swipe)
             if (dismissState.dismissDirection(DismissDirection.EndToStart)) {
                 if (dismissState.currentValue != SwipeToDismissBoxValue.) {
                     ResetItem(dismissState)
                 }
             }*/

            // If a client has already been selected for a document, it must be highlighted in the list
            val highlightInList = client.id == currentClientOrIssuerId

            ClientOrIssuerListItem(
                clientOrIssuer = client,
                onItemClick = {
                    onItemClick(client)
                },
                onItemCheckboxClick = { isChecked ->
                    // Update client list
                    if (isChecked) {
                        addToSelectedList(client)
                    } else {
                        removeFromSelectedList(client)
                    }
                },
                isCheckboxDisplayed,
                keyToResetCheckbox = keyToResetCheckboxes
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = 0.6f)
            )
        }
    }
}
