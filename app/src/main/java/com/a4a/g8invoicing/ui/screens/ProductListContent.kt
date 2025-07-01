package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.shared.ButtonAddOrChoose
import com.a4a.g8invoicing.ui.shared.CheckboxFace

@Composable
fun ProductListContent(
    products: List<ProductState>,
    onProductClick: (ProductState) -> Unit = {},
    addToSelectedList: (ProductState) -> Unit = {},
    removeFromSelectedList: (ProductState) -> Unit = {},
    displayCheckboxes: Boolean = true, // Will not be displayed when the list is opened from a document
    keyToUnselectAll: Boolean = false,
) {
    LazyColumn {
        items(
            items = products,
            key = { product ->
                product.productId!!
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
                keyToUnselectAll
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = 0.6f)
            )
        }
    }
}


