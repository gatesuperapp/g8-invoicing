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
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.navigation.actionTagCancelled
import com.a4a.g8invoicing.ui.navigation.actionTagReminded
import com.a4a.g8invoicing.ui.navigation.actionTagDraft
import com.a4a.g8invoicing.ui.navigation.actionTagLate
import com.a4a.g8invoicing.ui.navigation.actionTagPaid
import com.a4a.g8invoicing.ui.navigation.actionTagSent
import com.a4a.g8invoicing.ui.shared.FlippyCheckBox
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.ColorGreen
import com.a4a.g8invoicing.ui.theme.ColorPinkOrange

@Composable
fun InvoiceListItem(
    document: InvoiceState,
    onItemClick: () -> Unit = {},
    onItemCheckboxClick: (Boolean) -> Unit = {},
    keyToResetCheckboxes: Boolean,
) {
    var action by remember { mutableStateOf(actionTagDraft()) }

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
                    end = 20.dp,
                    top = 14.dp,
                    bottom = 14.dp
                )
        ) {
            action = when (document.documentTag) {
                DocumentTag.DRAFT -> actionTagDraft()
                DocumentTag.SENT -> actionTagSent()
                DocumentTag.PAID -> actionTagPaid()
                DocumentTag.LATE -> actionTagLate()
                DocumentTag.CANCELLED -> actionTagCancelled()
                DocumentTag.CREDIT -> actionTagReminded()
            }
            Column {
                FlippyCheckBox(
                    color = action.iconColor,
                    onItemCheckboxClick = onItemCheckboxClick,
                    keyToResetCheckboxes
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

                if (document.documentTag == DocumentTag.PAID || document.documentTag == DocumentTag.CANCELLED) {
                    action.label?.let {
                        Text(
                            text = stringResource(id = it),
                        )
                    }
                } else {
                    Text(
                        Strings.get(R.string.invoice_due_date) + " " + document.dueDate.substringBefore(
                            " "
                        )
                    )
                }
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
                    color = when (document.documentTag) {
                        DocumentTag.PAID -> ColorGreen
                        DocumentTag.LATE -> ColorPinkOrange
                        else -> Color.Black
                    }
                )
            }
        }
    }
}

/*@Composable
fun AddIconAndLabelInRow(action: AppBarAction) {
    Row(verticalAlignment = CenterVertically) {
        Icon(
            action.icon,
            modifier = Modifier
                .size(14.dp)
                .padding(end = 2.dp),
            tint = action.iconColor ?: MaterialTheme.colorScheme.onBackground,
            contentDescription = stringResource(id = action.description)
        )
        action.label?.let {
            Text(
                text = stringResource(id = it),
                fontStyle = if (action.name == "DRAFT") FontStyle.Italic else null,
                color = if (action.name == "DRAFT") action.iconColor ?: Color.Black
                else Color.Black,
            )
        }
    }
}*/



