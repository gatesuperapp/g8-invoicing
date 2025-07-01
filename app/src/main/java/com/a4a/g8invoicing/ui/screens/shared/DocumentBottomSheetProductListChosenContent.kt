package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.states.DocumentProductState

@Composable
fun DocumentBottomSheetProductListChosenContent(
    documentProducts: List<DocumentProductState>,
    onClickItem: (DocumentProductState) -> Unit,
    onClickDelete: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .padding(
                top = 20.dp,
                bottom = 30.dp
            )
    ) {
        items(
            items = documentProducts,
            key = { documentProduct ->
                documentProduct.id!!
            }
        ) { documentProduct ->
            DocumentBottomSheetProductListChosenItem(
                documentProduct = documentProduct,
                onClickDocumentProduct = {
                    onClickItem(documentProduct)
                },
                onClickDeleteDocumentProduct = {
                    documentProduct.id?.let {
                        onClickDelete(it)
                    }
                }
            )
        }
    }
}


