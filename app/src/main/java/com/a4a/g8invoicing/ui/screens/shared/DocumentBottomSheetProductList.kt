package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.a4a.g8invoicing.ui.screens.ProductListContent
import com.a4a.g8invoicing.ui.states.ProductState
import com.ninetyninepercent.funfactu.icons.IconArrowBack

// User can either select an item (client or product) in the list, or add a new item
@Composable
fun DocumentBottomSheetProductList(
    list: List<ProductState>,
    onClickBack: () -> Unit,
    onProductClick: (ProductState) -> Unit, // To select a product
) {
    Column(
        modifier = Modifier
            .fillMaxHeight(0.5f)
            .background(Color.White)
    ) {
        // Header: display "back" button
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onClickBack) {
                Icon(
                    imageVector = IconArrowBack,
                    contentDescription = "Validate"
                )
            }
        }
        // Display the existing list
        ProductListContent(
            products = list,
            onProductClick = onProductClick,
            displayCheckboxes = false
        )
    }
}