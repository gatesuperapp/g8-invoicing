package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.data.DocumentProductEditable
import com.a4a.g8invoicing.ui.shared.ButtonAddOrChoose
import com.ninetyninepercent.funfactu.icons.IconArrowBack

// User can either select an item (client or product) in the list, or add a new item
@Composable
fun DeliveryNoteBottomSheetDocumentProductList(
    list: List<DocumentProductEditable>,
    onClickBack: () -> Unit,
    onClickChooseProduct: () -> Unit, // Add a new product to the document (product list)
    onDocumentProductClick: (Int) -> Unit, // Edit an existing document product (add/edit screen)
    onClickDeleteDocumentProduct: (Int) -> Unit, // Delete a document product
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
        ButtonAddOrChoose( // Choosing a product to add to the document
            onClickChooseProduct,
            hasBorder = false,
            hasBackground = true,
            stringResource(id = R.string.delivery_note_bottom_sheet_document_product_add)
        )

        Spacer(modifier = Modifier.weight(1F))

        // Display the existing list
        DocumentProductListContent(
            documentProducts = list,
            onItemClick = onDocumentProductClick,
            onClickDeleteDocumentProduct = onClickDeleteDocumentProduct
        )
    }
}