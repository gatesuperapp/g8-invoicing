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
import androidx.compose.ui.res.stringResource
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.ButtonAddOrChoose
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import com.ninetyninepercent.funfactu.icons.IconArrowBack

// User can either select an item (client or product) in the list, or add a new item
@Composable
fun DocumentBottomSheetClientOrIssuerPreview(
    item: ClientOrIssuerState?,
    onClickBack: () -> Unit,
    onClickNewButton: () -> Unit,
    onClickChooseButton: () -> Unit,
    onClickItem: (ClientOrIssuerState) -> Unit,
    onClickDelete: (ClientOrIssuerType)  -> Unit,
    isClientOrIssuerListEmpty: Boolean
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
                    contentDescription = "Back"
                )
            }
        }

        if (item == null) {
            ButtonAddOrChoose(
                onClickNewButton,
                hasBorder = true,
                hasBackground = false,
                stringResource(id = R.string.document_bottom_sheet_list_add_new)
            )
            if(!isClientOrIssuerListEmpty) {
                ButtonAddOrChoose(
                    onClickChooseButton,
                    hasBorder = false,
                    hasBackground = true,
                    stringResource(id = R.string.document_bottom_sheet_document_product_add)
                )
            }
        } else {
            // Display the existing item
            DocumentClientOrIssuerContent(
                item = item,
                onClickItem = onClickItem,
                onClickDelete = onClickDelete
            )
        }
    }
}