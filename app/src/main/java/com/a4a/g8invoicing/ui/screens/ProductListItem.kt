package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.states.ProductState


@Composable
fun ProductListItem(
    product: ProductState,
    onItemClick: () -> Unit = {},
    onItemCheckboxClick: (it: Boolean) -> Unit = {},
    displayCheckboxes: Boolean,
    keyToResetCheckboxes: Boolean,
) {

    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
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
            // Retriggers remember calculation when key changes
            val checkedState = remember(keyToResetCheckboxes) { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .padding(end = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (displayCheckboxes) {
                    Checkbox(
                        checked = checkedState.value,
                        onCheckedChange =
                        {
                            checkedState.value = it
                            onItemCheckboxClick(it)
                        },
                    )
                }
                Text(
                    modifier = Modifier.weight(1F),
                    text = product.name.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    // maxLines = 1,
                    //overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = product.priceWithTax?.let {
                        it.toString() + stringResource(R.string.currency)
                    } ?: " - "
                )
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
