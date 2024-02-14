package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.data.ClientOrIssuerEditable
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.shared.SharedBottomBar
import com.a4a.g8invoicing.ui.states.ClientsOrIssuerUiState

@Composable
fun ClientOrIssuerList(
    navController: NavController,
    uiState: ClientsOrIssuerUiState,
    onClickDelete: (List<ClientOrIssuerEditable>) -> Unit,
    onClickDuplicate: (List<ClientOrIssuerEditable>) -> Unit,
    onClickNew: () -> Unit,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (ClientOrIssuerEditable) -> Unit,
    onClickBack: () -> Unit,
) {
    // Main list to handle actions with selected items
    val selectedItems = mutableListOf<ClientOrIssuerEditable>()
    // Will recompose the BottomBar (only) when an item is selected, or when all items are unselected
    val selectedMode = remember { mutableStateOf(false) }
    // Will recompose all the items when clicking "unselect all"
    val keyToResetCheckboxes = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                title = R.string.appbar_client_list,
                navController = navController,
                onClickBackArrow = onClickBack
            )
        },
        //   private val _uiState = MutableStateFlow(ClientsUiState())
        // val uiState: StateFlow<ClientsUiState> = _uiState.asStateFlow()
        bottomBar = {
            SharedBottomBar(
                navController = navController,
                selectedMode = selectedMode.value,
                onClickDelete = {
                    onClickDelete(selectedItems.toList())
                    selectedItems.clear()
                    selectedMode.value = false
                    // No need to reset checkboxes as items are deleted
                },
                onClickDuplicate = {
                    onClickDuplicate(selectedItems.toList())
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickUnselectAll = {
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickNew = { onClickNew() },
                onClickCategory = onClickCategory
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(
                    padding
                )
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // No need to have pull to refresh because it's a flow,
            // thus the list is updated when anything changes in db
            ClientOrIssuerListContent(
                clientsOrIssuers = uiState.clientsOrIssuers,
                onItemClick = onClickListItem,
                addToSelectedList = {
                    selectedItems.add(it)
                    selectedMode.value = true
                },
                removeFromSelectedList = {
                    selectedItems.remove(it)
                    if (selectedItems.isEmpty()) {
                        selectedMode.value = false
                    }
                },
                keyToUnselectAll = keyToResetCheckboxes.value
            )
        }
    }
}

private fun resetSelectedItems(
    selectedItems: MutableList<ClientOrIssuerEditable>,
    selectedMode: MutableState<Boolean>,
    keyToResetCheckboxes: MutableState<Boolean>,
) {
    selectedItems.clear()
    selectedMode.value = false
    // Allow to "reset" the checkbox rememberValue to false in ProductListItem when recomposing
    keyToResetCheckboxes.value = !keyToResetCheckboxes.value
}
