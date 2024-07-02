package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.states.DocumentProductState

@Composable
fun DocumentProductListContent(
    documentProducts: List<DocumentProductState>,
    onClickItem: (DocumentProductState) -> Unit,
    onClickDelete: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .padding(bottom = 20.dp)
            .fillMaxSize()
    ) {
        items(
            items = documentProducts,
            key = { documentProduct ->
                documentProduct.id!!
            }
        ) { documentProduct ->
            DocumentProductListItem(
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


