package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.ColorLightGrey
import icons.IconDelete

@Composable
fun DocumentClientOrIssuerContent(
    item: DocumentClientOrIssuerState,
    onClickItem: (DocumentClientOrIssuerState) -> Unit,
    onClickDelete: (ClientOrIssuerType) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(
                top = 20.dp,
                start = 20.dp,
                end = 20.dp
            )
            .clip(RoundedCornerShape(5.dp))
            .background(ColorLightGrey)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.Black, bounded = false)
            ) {
                onClickItem(item)
            }
    ) {
        // Adding padding in the inside row, to keep the click & the ripple in all row
        Row(
            modifier = Modifier
                .padding(
                    start = 20.dp,
                    end = 30.dp,
                    top = 10.dp,
                    bottom = 10.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1F),
                text = (item.firstName?.let { it.text + " " } ?: "") + item.name.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                modifier = Modifier
                    .size(15.dp)
                    .clickable(
                        onClick = {
                                item.type?.let {type ->
                                    onClickDelete(type)
                                }
                        }
                    ),
                imageVector = IconDelete,
                contentDescription = "Delete line item"
            )
        }
    }
}


