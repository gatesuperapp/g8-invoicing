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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import com.a4a.g8invoicing.ui.screens.shared.resolveCategoryColor
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.CurrencyManager
import com.a4a.g8invoicing.data.formatAmount
import com.a4a.g8invoicing.ui.shared.CheckboxFace
import com.a4a.g8invoicing.ui.shared.FlippyCheckBox
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textBodyBold
import com.a4a.g8invoicing.ui.theme.textSecondary
import org.koin.compose.koinInject

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

    val currencyManager: CurrencyManager = koinInject()
    val currencyCode = currencyManager.currentCurrency

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AppColors.ripple, bounded = true),
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
            .background(if (checkedState.value) AppColors.surfaceMuted else AppColors.surface)
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
                        fillColorWhenSelectionOff = AppColors.surface,
                        backgroundColorWhenSelectionOn = if (checkedState.value) AppColors.surfaceMuted else AppColors.surface,
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
                        style = MaterialTheme.typography.textBodyBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = displayPrice?.let { formatAmount(it, currencyCode) } ?: " - "
                    )
                }
                // Description row — omitted entirely when the product has no
                // description, so the row collapses to just name + chip. Mixed
                // row heights across the list is fine per the spec.
                product.description?.text?.takeIf { it.isNotBlank() }?.let { desc ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = desc,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.textSecondary,
                        )
                    }
                }

                // Category chip — coloured background behind the category
                // name. Not clickable: taps still bubble to the row click.
                // Text is pinned to a near-black regardless of theme because
                // the chip backgrounds are mid-saturation Material-300ish
                // tones that read best with dark ink on top (matches how
                // Material tag/badge components themselves handle onColor —
                // low-tone text on a lifted container).
                product.category?.let { category ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp, end = 20.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(resolveCategoryColor(category.color))
                                .padding(horizontal = 10.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.textSecondary.copy(
                                    color = Color(0xFF1A1A1A),
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
