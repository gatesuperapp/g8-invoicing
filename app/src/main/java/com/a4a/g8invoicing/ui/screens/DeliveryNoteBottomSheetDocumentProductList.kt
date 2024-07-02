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
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.shared.ButtonAddOrChoose
import com.ninetyninepercent.funfactu.icons.IconArrowBack

// User can either select an item (client or product) in the list, or add a new item
@Composable
fun DeliveryNoteBottomSheetDocumentProductList(
    list: List<DocumentProductState>,
    onClickBack: () -> Unit,
    onClickNew: () -> Unit, // Add a new product to the document (product list)
    onClickChooseExisting: () -> Unit, // Add a new product to the document (product list)
    onClickDocumentProduct: (DocumentProductState) -> Unit, // Edit an existing document product (add/edit screen)
    onClickDelete: (Int) -> Unit, // Delete a document product
) {
    Column(
        modifier = Modifier
            .fillMaxHeight(0.5f)
            .background(Color.White)
           // .verticalScroll(rememberScrollState())
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

        Spacer(modifier = Modifier.weight(1F))

        // Display the existing list
        DocumentProductListContent(
            documentProducts = list,
            onClickItem = onClickDocumentProduct,
            onClickDelete = onClickDelete
        )
    }
}