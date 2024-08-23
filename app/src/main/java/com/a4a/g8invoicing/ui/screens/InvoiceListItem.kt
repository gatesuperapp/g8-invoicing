package com.a4a.g8invoicing.ui.screens

import android.content.ContentValues
import android.util.Log
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
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.screens.shared.getDateFormatter
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.ColorGreen
import com.a4a.g8invoicing.ui.theme.ColorLightGreen
import com.a4a.g8invoicing.ui.theme.ColorPinkOrange
import com.a4a.g8invoicing.ui.theme.pdfFont
import java.time.LocalDate
import java.util.Calendar

@Composable
fun InvoiceListItem(
    document: InvoiceState,
    onItemClick: () -> Unit = {},
    onItemCheckboxClick: (it: Boolean) -> Unit = {},
    keyToResetCheckboxes: Boolean,
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val formatter = getDateFormatter()
    val dueDate = formatter.parse(document.dueDate)?.time
    val currentDate = java.util.Date().time
    var latePayment = dueDate != null && dueDate < currentDate

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
                verticalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                Text(
                    text = document.documentNumber.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    // maxLines = 1,
                    //overflow = TextOverflow.Ellipsis
                )
                document.documentClient?.let {
                    Text(
                        text = (it.firstName?.let { it.text + " " } ?: "") + it.name.text
                    )
                }
                Text(
                    text = if (document.paymentStatus == 2) {
                        Strings.get(R.string.invoice_paid)
                    } else {
                        Strings.get(R.string.invoice_due_date) + " " + document.dueDate.substringBefore(" ")
                    },
                    color = if (document.paymentStatus == 2) ColorGreen else if (latePayment) ColorPinkOrange else Color.Black
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(space = 2.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = document.documentDate.substringBefore(" "),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    // maxLines = 1,
                    //overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = (document.documentPrices?.let { it.totalPriceWithTax.toString() }
                        ?: "") + stringResource(
                        id = R.string.currency
                    ),
                    color = if (document.paymentStatus == 2) ColorGreen else if (latePayment) ColorPinkOrange else Color.Black
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
