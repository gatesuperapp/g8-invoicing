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
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.shared.ButtonAddOrChoose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListContent(
    products: List<ProductState>,
    onProductClick: (ProductState) -> Unit = {},
    onClickNew: () -> Unit = {}, // Used only in documents, clicking the "Add new" button
    displayTopButton: Boolean = false,
    addProductToSelectedList: (ProductState) -> Unit = {},
    removeProductFromSelectedList: (ProductState) -> Unit = {},
    keyToUnselectAll: Boolean = false,
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
                    stringResource(id = R.string.document_bottom_sheet_list_add_new)
                )
            }
        }
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
                        addProductToSelectedList(product)
                    } else {
                        removeProductFromSelectedList(product)
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


