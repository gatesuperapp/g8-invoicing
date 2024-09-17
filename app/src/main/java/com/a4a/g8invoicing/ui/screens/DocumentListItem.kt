package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.a4a.g8invoicing.ui.navigation.actionTagUndefined
import com.a4a.g8invoicing.ui.shared.CheckboxFace
import com.a4a.g8invoicing.ui.shared.FlippyCheckBox
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.ColorGrayTransp
import com.a4a.g8invoicing.ui.theme.ColorGreen
import com.a4a.g8invoicing.ui.theme.ColorPinkOrange

@Composable
fun DocumentListItem(
    document: DocumentState,
    onItemClick: () -> Unit = {},
    onItemCheckboxClick: (Boolean) -> Unit = {},
    keyToResetCheckboxes: Boolean,
) {
    var action by remember { mutableStateOf(actionTagDraft()) }
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    // For the checkbox
    var checkboxFace by remember(keyToResetCheckboxes) { mutableStateOf(CheckboxFace.Back) }
    // Re-triggers remember calculation when key changes
    val checkedState = remember(keyToResetCheckboxes) { mutableStateOf(false) }

    // For changing background when item selected
    val backgroundColor = remember(keyToResetCheckboxes) { mutableStateOf(Color.Transparent) }

    Box(
        modifier = Modifier
    ) {
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
                            checkboxFace = checkboxFace.next
                            checkedState.value = !checkedState.value
                            onItemCheckboxClick(checkedState.value)
                            // Change item background color
                            backgroundColor.value =
                                changeSelectedItemBackgroundColor(backgroundColor.value)
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
                    DocumentTag.REMINDED -> actionTagReminded()
                    DocumentTag.CANCELLED -> actionTagCancelled()
                    else -> actionTagUndefined()
                }
                Column {
                    FlippyCheckBox(
                        fillColor = action.iconColor,
                        borderColor = action.iconBorder,
                        onItemCheckboxClick = {
                            checkboxFace = checkboxFace.next
                            checkedState.value = !checkedState.value
                            onItemCheckboxClick(it)
                            // Change item background color
                            backgroundColor.value =
                                changeSelectedItemBackgroundColor(backgroundColor.value)
                        },
                        checkboxFace = checkboxFace,
                        checkedState = checkedState.value
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    document.documentClient?.let {
                        Text(
                            text = (it.firstName?.let { it.text + " " } ?: "") + it.name.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } ?: Text(" - ")

                    if(document is InvoiceState) {
                        if (document.documentTag == DocumentTag.PAID || document.documentTag == DocumentTag.CANCELLED) {
                            action.label?.let {
                                Text(
                                    text = stringResource(id = it),
                                )
                            }
                        } else {
                            Text(
                                text = Strings.get(R.string.invoice_due_date) + " " + document.dueDate.substringBefore(
                                    " "
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
        Column(
            // apply darker background when item is selected
            modifier = Modifier
                .height(if(document is InvoiceState) 104.dp else 78.dp)
                .fillMaxWidth()
                .background(backgroundColor.value),
        ) {}
    }
}

fun changeSelectedItemBackgroundColor(initialColor: Color): Color {
    return if (initialColor == Color.Transparent) {
        ColorGrayTransp
    } else Color.Transparent
}