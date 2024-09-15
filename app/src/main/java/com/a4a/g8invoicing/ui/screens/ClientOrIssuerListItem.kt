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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.ColorLightGreenTransp
import com.a4a.g8invoicing.ui.theme.textForDocumentsBold

@Composable
fun ClientOrIssuerListItem(
    clientOrIssuer: ClientOrIssuerState,
    onItemClick: () -> Unit = {},
    onItemCheckboxClick: (it: Boolean) -> Unit = {},
    keyToResetCheckboxes: Boolean,
    isCheckboxDisplayed: Boolean, // Don't display checkboxes when list is displayed in bottomSheet
    highlightInList: Boolean = false,
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val itemBackground = if (highlightInList) {
        ColorLightGreenTransp
    } else {
        Color.White
    }

    // For changing background when item selected
    val backgroundColor = remember { mutableStateOf(Color.Transparent) }

    Box(
        modifier = Modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                //      .fillMaxWidth()
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
                    .background(itemBackground)
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 20.dp,
                        bottom = 20.dp
                    )
            ) {
                // Retriggers remember calculation when key changes
                val checkedState = remember(keyToResetCheckboxes) { mutableStateOf(false) }

                if (isCheckboxDisplayed) {
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
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(space = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(end = 20.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        val clientName = (clientOrIssuer.firstName?.let { it.text + " " }
                            ?: "") + clientOrIssuer.name.text
                        Text(
                            text = clientName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = clientOrIssuer.email?.text?.ifEmpty { " - " } ?: " - ",
                        )
                    }
                }
            }
        }
        Column(
            // apply darker background when item is selected
            modifier = Modifier
                .height(104.dp)
                .fillMaxWidth()
                .background(backgroundColor.value),
        ) {}
    }
}

