package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.shared.AlertDialogDeleteDocument
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.ProductsUiState


@Composable
fun ProductList(
    navController: NavController,
    productsUiState: ProductsUiState,
    onClickDelete: (List<ProductState>) -> Unit,
    onClickDuplicate: (List<ProductState>) -> Unit,
    onClickNew: () -> Unit,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (ProductState) -> Unit,
    onClickBack: () -> Unit,

    ) {
    // Main list to handle actions with selected items
    val selectedItems = remember { mutableStateListOf<ProductState>() }

    // Will recompose the BottomBar (only) when an item is selected, or when all items are unselected
    val selectedMode = remember { mutableStateOf(false) }
    // Will recompose all the items when clicking "unselect all"
    val keyToResetCheckboxes = remember { mutableStateOf(false) }
    // On delete document
    val openAlertDialog = remember { mutableStateOf(false) }

    // Add background when bottom menu expanded
    val transparent = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    val backgroundColor = remember { mutableStateOf(transparent) }

    Scaffold(
        topBar = {
            TopBar(
                title = R.string.appbar_products,
                navController = navController,
                onClickBackArrow = onClickBack
            )
        },
        bottomBar = {
            GeneralBottomBar(
                navController = navController,
                numberOfItemsSelected = selectedItems.size,
                onClickDelete = {
                    backgroundColor.value =
                        changeBackgroundWithVerticalGradient(backgroundColor.value)
                    openAlertDialog.value = true
                },
                onClickDuplicate = {
                    onClickDuplicate(selectedItems.toList())
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickUnselectAll = {
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickNew = { onClickNew() },
                onClickCategory = onClickCategory,
                onChangeBackground = {
                    backgroundColor.value =
                        changeBackgroundWithVerticalGradient(backgroundColor.value)
                }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // No need to have pull to refresh because it's a flow,
                // thus the list is updated when anything changes in db
                ProductListContent(
                    products = productsUiState.products,
                    onProductClick = onClickListItem,
                    addProductToSelectedList = {
                        selectedItems.add(it)
                        selectedMode.value = true
                    },
                    removeProductFromSelectedList = {
                        selectedItems.remove(it)
                        if (selectedItems.isEmpty()) {
                            selectedMode.value = false
                        }
                    },
                    keyToUnselectAll = keyToResetCheckboxes.value
                )

                Column(
                    // apply darker background when bottom menu is expanded
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor.value),
                ) {}
            }
        }

        when {
            openAlertDialog.value -> {
                AlertDialogDeleteDocument(
                    onDismissRequest = {
                        openAlertDialog.value = false
                        backgroundColor.value =
                            changeBackgroundWithVerticalGradient(backgroundColor.value)},
                    onConfirmation = {
                        openAlertDialog.value = false
                        onClickDelete(selectedItems.toList())
                        selectedItems.clear()
                        selectedMode.value = false
                        backgroundColor.value =
                            changeBackgroundWithVerticalGradient(backgroundColor.value)
                    }
                )
            }
        }
    }
}

private fun resetSelectedItems(
    selectedItems: MutableList<ProductState>,
    selectedMode: MutableState<Boolean>,
    triggerRecompose: MutableState<Boolean>,
) {
    selectedItems.clear()
    selectedMode.value = false
    // To "reset" the checkbox rememberValue to false in ProductListItem when recomposing
    triggerRecompose.value = !triggerRecompose.value
}


