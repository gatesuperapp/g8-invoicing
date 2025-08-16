package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.ui.shared.icons.IconDragHandle
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.theme.ColorLightGrey
import icons.IconDelete
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Composable
fun DocumentBottomSheetProductListChosenItem(
    documentProduct: DocumentProductState,
    onClickDocumentProduct: () -> Unit,
    onClickDeleteDocumentProduct: () -> Unit,
    scope: ReorderableCollectionItemScope,
) {
    val interactionSource = remember { MutableInteractionSource() }
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
                interactionSource = interactionSource,
                indication = ripple(color = Color.Black, bounded = false)
            ) {
                onClickDocumentProduct()
            }

    ) {
        IconButton(
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
            },
            onClick = {
            },
            interactionSource = interactionSource, // 🔑 partage la même interactionSource que la Row
        ) {
            Icon(IconDragHandle, contentDescription = "Reorder")
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
            )
            Icon(
                modifier = Modifier
                    .size(15.dp)
                    .clickable(
                        onClick = onClickDeleteDocumentProduct
                    ),
                imageVector = IconDelete,
                contentDescription = "Delete line item"
            )
        }
    }
}
