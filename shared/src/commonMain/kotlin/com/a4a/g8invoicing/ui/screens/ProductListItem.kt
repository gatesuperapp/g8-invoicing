package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.currency
import com.a4a.g8invoicing.ui.shared.CheckboxFace
import com.a4a.g8invoicing.ui.shared.FlippyCheckBox
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.theme.ColorLightGreyo
import com.a4a.g8invoicing.ui.theme.textSmall
import org.jetbrains.compose.resources.stringResource


@Composable
fun ProductListItem(
    product: ProductState,
    onItemClick: () -> Unit = {},
    onItemCheckboxClick: (Boolean) -> Unit = {},
    isCheckboxDisplayed: Boolean,
    keyToResetCheckbox: Boolean,
    clientId: Int? = null, // ID du client pour afficher son prix spécifique
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val checkedState = remember(keyToResetCheckbox) { mutableStateOf(false) }

    // Trouver le prix spécifique au client si clientId est fourni
    val displayPrice = if (clientId != null) {
        // Chercher un prix additionnel qui contient ce client
        product.additionalPrices?.find { price ->
            price.clients.any { it.id == clientId }
        }?.priceWithTax ?: product.defaultPriceWithTax
    } else {
        product.defaultPriceWithTax
    }

    val currencySymbol = stringResource(Res.string.currency)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.Black, bounded = false),
            ) {
                //onItemClick()
            }
            .pointerInput(keyToResetCheckbox) {
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
                        checkedState.value = !checkedState.value
                        onItemCheckboxClick(checkedState.value)
                    }
                )
            }
            .background(if (checkedState.value) ColorLightGreyo else Color.White)
    ) {
        // Adding padding in the inside row, to keep the click & the ripple in all row
        // (NB: putting padding on the checkbox works, but then when name is on 2 lines it's
        // not centered anymore)
        Row(
            modifier = Modifier
                .padding(
                    start = if (isCheckboxDisplayed) 0.dp else 30.dp,
                    end = 20.dp,
                    top = 14.dp,
                    bottom = 14.dp
                )
        ) {
            if (isCheckboxDisplayed) {
                Column {
                    FlippyCheckBox(
                        fillColorWhenSelectionOff = Color.White, // was actionTagUndefined().iconColor
                        backgroundColorWhenSelectionOn = if (checkedState.value) ColorLightGreyo else Color.White,
                        onItemCheckboxClick = {
                            checkedState.value = !checkedState.value
                            onItemCheckboxClick(checkedState.value)
                        },
                        checkboxFace = if (checkedState.value) CheckboxFace.Front
                        else CheckboxFace.Back,
                        checkedState = checkedState.value,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(end = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1F),
                        text = product.name.text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = displayPrice?.let {
                            it.toPlainString().replace(".", ",") + currencySymbol
                        } ?: " - "
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = if (!product.description?.text.isNullOrEmpty())
                            product.description!!.text else " - ",
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.textSmall
                    )
                }
            }
        }
    }
}
