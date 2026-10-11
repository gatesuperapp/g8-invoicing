package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.theme.AppColors

@Composable
fun ProductListContent(
    products: List<ProductState>,
    onProductClick: (ProductState) -> Unit = {},
    addToSelectedList: (ProductState) -> Unit = {},
    removeFromSelectedList: (ProductState) -> Unit = {},
    displayCheckboxes: Boolean = true, // Will not be displayed when the list is opened from a document
    keyToUnselectAll: Boolean = false,
    clientId: Int? = null, // ID du client pour afficher son prix spécifique
) {
    // Same recipe as DocumentListContent: screen-coloured container +
    // 1dp spacedBy between rows. Each ProductListItem paints its own
    // surface background on top, so the gap shows the screen hue and
    // reads as a visible separator without a hard divider line.
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.screen),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        items(
            // Skip transient products that haven't been persisted yet (id == null)
            // so the `key` lambda below never crashes on `!!`.
            items = products.filter { it.id != null },
            key = { product ->
                product.id!!
            }
        ) { product ->
            ProductListItem(
                product = product,
                onItemClick = {
                    onProductClick(product)
                },
                onItemCheckboxClick = { isChecked ->
                    // Update product list
                    if (isChecked) {
                        addToSelectedList(product)
                    } else {
                        removeFromSelectedList(product)
                    }
                },
                displayCheckboxes,
                keyToUnselectAll,
                clientId = clientId
            )
        }
    }
}
