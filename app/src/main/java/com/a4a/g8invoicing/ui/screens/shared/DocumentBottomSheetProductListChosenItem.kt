package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.theme.ColorLightGrey
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Composable
fun DocumentBottomSheetProductListChosenItem(
    documentProduct: DocumentProductState,
    onClickDocumentProduct: () -> Unit,
    onClickDeleteDocumentProduct: () -> Unit,
    scope: ReorderableCollectionItemScope,
) {
    // Interaction source for the Row: ripple on the Row itself
    val rowInteractionSource = remember { MutableInteractionSource() }

    // Separate Interaction source specifically for the IconButton, if needed for other reasons,
    // but for simply disabling ripple, it's not strictly necessary if you apply clickable directly.
    val iconButtonInteractionSource = remember { MutableInteractionSource() }

    val hapticFeedback = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(
                when {
                    isDragging -> Color(0xFFD1D1D1)
                    else -> ColorLightGrey
                }
            )
            .clickable(
                interactionSource = rowInteractionSource,
                indication = ripple(color = Color.Black, bounded = false)
            ) {
                onClickDocumentProduct()
            }

    ) {
        Box(
            modifier = with(scope) {
                Modifier
                    .draggableHandle(
                        onDragStarted = {
                            isDragging = true
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                        },
                        onDragStopped = {
                            isDragging = false
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                        },
                    )
                    .clickable(
                        interactionSource = rowInteractionSource, // 🔑 partage la même interactionSource que la Row
                        indication = null, // 🚀 pas de ripple local sur l'icône
                        onClick = {} // pas d’action locale, la Row gère déjà le clic
                    )
                    .padding(8.dp) // IconButton par défaut applique un padding interne → on le refait ici
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.DragHandle,
                contentDescription = "Reorder"
            )
        }

        // Adding padding in the inside row, to keep the click & the ripple in all row
        Row(
            modifier = Modifier
                .padding(
                    start = 20.dp,
                    end = 30.dp,
                    top = 10.dp,
                    bottom = 10.dp
                )
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = documentProduct.quantity.stripTrailingZeros().toPlainString()
                    .replace(".", ","),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                modifier = Modifier.weight(1F),
                text = documentProduct.name.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                modifier = Modifier
                    .size(15.dp)
                    .clickable(
                        onClick = onClickDeleteDocumentProduct
                    ),
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Delete line item"
            )
        }
    }
}