package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.ButtonAddOrChoose
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType

// User can either select an item (client or product) in the list, or add a new item
@Composable
fun DocumentBottomSheetClientOrIssuerPreview(
    pageElement: ScreenElement,
    clientOrIssuer: ClientOrIssuerState?,
    onClickBack: () -> Unit,
    onClickNew: () -> Unit,
    onClickSelect: () -> Unit,
    onClickEdit: (ClientOrIssuerState) -> Unit,
    onClickDelete: (ClientOrIssuerType) -> Unit,
    isClientOrIssuerListEmpty: Boolean,
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
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            if (clientOrIssuer == null) {
                ButtonAddOrChoose(
                    onClickNew,
                    hasBorder = true,
                    isPickerButton = false,
                    stringResource(
                        id = if (pageElement == ScreenElement.DOCUMENT_CLIENT)
                            R.string.document_bottom_sheet_list_add_new_client
                        else R.string.document_bottom_sheet_list_add_new_issuer
                    )
                )
                if (!isClientOrIssuerListEmpty) {
                    ButtonAddOrChoose(
                        onClickSelect,
                        hasBorder = false,
                        isPickerButton = true,
                        stringResource(id = R.string.document_bottom_sheet_document_product_add)
                    )
                }
            } else {
                // Display the existing item
                DocumentClientOrIssuerContent(
                    item = clientOrIssuer,
                    onClickItem = onClickEdit,
                    onClickDelete = onClickDelete
                )
            }
        }
    }
}