package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.data.ClientOrIssuerEditable
import com.a4a.g8invoicing.ui.shared.ButtonAddOrChoose

@Composable
fun ClientOrIssuerListContent(
    clientsOrIssuers: List<ClientOrIssuerEditable>,
    onItemClick: (ClientOrIssuerEditable) -> Unit = {},
    onClickNew: () -> Unit = {}, // Used only in documents, clicking the "Add new" button
    displayTopButton: Boolean = false,
    addToSelectedList: (ClientOrIssuerEditable) -> Unit = {},
    removeFromSelectedList: (ClientOrIssuerEditable) -> Unit = {},
    keyToUnselectAll: Boolean = false,
    isCheckboxDisplayed: Boolean = true,
    currentClientOrIssuerId: Int? = null,
) {
    LazyColumn {
        if (displayTopButton) {
            // Display "Add new" button on top of the list
            // when the list is displayed in documents bottom sheet
            item {
                ButtonAddOrChoose(
                    onClickNew,
                    hasBorder = true,
                    hasBackground = false,
                    stringResource(id = R.string.delivery_note_bottom_sheet_list_add_new)
                )
            }
        }
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
                keyToUnselectAll,
                isCheckboxDisplayed,
                highlightInList
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = 0.6f)
            )
        }
    }
}

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetItem(dismissState: DismissState) {
    LaunchedEffect(Unit) {
        dismissState.reset()
    }
}*/



