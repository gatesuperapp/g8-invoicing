package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.theme.textForDocumentsBold

@Composable
fun DeliveryNoteListItem(
    deliveryNote: DeliveryNoteState,
    onItemClick: () -> Unit = {},
    onItemCheckboxClick: (it: Boolean) -> Unit = {},
    keyToResetCheckboxes: Boolean,
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = CenterVertically,
        modifier = Modifier
            //      .fillMaxWidth()
            //.background(itemBackgroundColor)
            .clip(RoundedCornerShape(5.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.Black, bounded = false),
            ) {
                //onItemClick()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = PressInteraction.Press(offset)
                        isPressed = true
                        interactionSource.emit(press)
                        tryAwaitRelease()
                        interactionSource.emit(PressInteraction.Release(press))
                        isPressed = false
                    },
                    onTap = {
                        onItemClick()
                    },
                    onLongPress = {
                        //TODO Implement check
                        // onItemCheckboxClick()
                    }
                )
            }
    ) {
        // Adding padding in the inside row, to keep the click & the ripple in all row
        // (NB: putting padding on the checkbox works, but then when name is on 2 lines it's
        // not centered anymore)
        Row(
            modifier = Modifier
                .background(Color.White)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 14.dp,
                    bottom = 14.dp
                )
        ) {
            // Re-triggers remember calculation when key changes
            val checkedState = remember(keyToResetCheckboxes) { mutableStateOf(false) }

            Column(
            ) {
                Checkbox(
                    checked = checkedState.value,
                    onCheckedChange =
                    {
                        checkedState.value = it
                        onItemCheckboxClick(it)
                    },
                )
            }

            Column(
                modifier = Modifier
                    .weight(1F)
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.spacedBy(space = 6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = deliveryNote.documentNumber.text,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        // maxLines = 1,
                        //overflow = TextOverflow.Ellipsis
                    )
                }
                deliveryNote.documentClient?.let {
                    Row() {
                        Text(
                            text = (it.firstName?.let { it.text + " " } ?: "") + it.name.text
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(space = 6.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = deliveryNote.documentDate,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        // maxLines = 1,
                        //overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                ) {
                    Text(
                        text = (deliveryNote.documentPrices?.let {it.totalPriceWithTax.toString()} ?: "") + stringResource(
                            id = R.string.currency
                        ),
                    )
                }
            }
        }
    }
}

/*@Preview
@Composable
fun ClientListItem() {
    G8InvoicingTheme {
        ClientListItem(
            client = Client(
                client_id = 1,
                first_name = "Patou",
                name = "George",
                null,
                null,
                null,
                null,
                "0989789898",
                "georgi@koko.fr",
                null,
                null,
                null,
            ),
            onItemClick = {},
            onItemCheckboxClick = {},
            isChecked = false
        )
    }
}*/
