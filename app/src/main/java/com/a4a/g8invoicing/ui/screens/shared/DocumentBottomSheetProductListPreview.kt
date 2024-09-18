package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.shared.ButtonAddOrChoose

// User can either select an item (client or product) in the list, or add a new item
@Composable
fun DocumentBottomSheetDocumentProductListPreview(
    list: List<DocumentProductState>,
    onClickNew: () -> Unit, // Add a new product to the document (product list)
    onClickChooseExisting: () -> Unit, // Add a new product to the document (product list)
    onClickDocumentProduct: (DocumentProductState) -> Unit, // Edit an existing document product (add/edit screen)
    onClickDelete: (Int) -> Unit, // Delete a document product
) {
    Column(
        modifier = Modifier
            .fillMaxHeight(0.5f)
    ) {
        // Header: display "back" button

        Spacer(modifier = Modifier.height(50.dp))

        ButtonAddOrChoose(
            onClickNew,
            hasBorder = true,
            hasBackground = false,
            stringResource(id = R.string.document_bottom_sheet_list_add_new)
        )
        ButtonAddOrChoose( // Choosing a product to add to the document
            onClickChooseExisting,
            hasBorder = false,
            hasBackground = true,
            stringResource(id = R.string.document_bottom_sheet_document_product_add)
        )

        // Display the existing list
        DocumentProductListContent(
            documentProducts = list,
            onClickItem = onClickDocumentProduct,
            onClickDelete = onClickDelete
        )
    }
}