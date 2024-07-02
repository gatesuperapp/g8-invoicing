package com.a4a.g8invoicing.ui.screens

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
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.ninetyninepercent.funfactu.icons.IconArrowBack

// User can either select an item (client or product) in the list, or add a new item
@Composable
fun DeliveryNoteBottomSheetClientOrIssuerList(
    list: List<ClientOrIssuerState>,
    pageElement: ScreenElement,
    onClickBack: () -> Unit,
    onClientOrIssuerClick: (ClientOrIssuerState) -> Unit,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
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
        ClientOrIssuerListContent(
            clientsOrIssuers = list,
            onItemClick = onClientOrIssuerClick,
            isCheckboxDisplayed = false, // Don't display checkboxes
            currentClientOrIssuerId = if (pageElement == ScreenElement.DOCUMENT_CLIENT) {
                currentClientId
            } else {
                currentIssuerId
            }  // The current client/issuer should be highlighted in the list
        )
    }
}