package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.screens.ProductListContent
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.theme.AppColors

@Composable
fun DocumentBottomSheetProductsAvailable(
    list: List<ProductState>,
    onClickBack: () -> Unit,
    onProductClick: (ProductState) -> Unit, // To select a product
    clientId: Int? = null, // ID du client pour afficher son prix spécifique
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.surface)
    ) {
        // Header: display "back" button
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onClickBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Validate"
                )
            }
        }

        Column(
            Modifier
                .padding(20.dp, bottom = 60.dp)
        ) {
            // Display the existing list
            ProductListContent(
                products = list,
                onProductClick = onProductClick,
                displayCheckboxes = false,
                clientId = clientId
            )
        }
    }
}
