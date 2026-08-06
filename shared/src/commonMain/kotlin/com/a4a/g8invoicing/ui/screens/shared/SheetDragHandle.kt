package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

// Drag handle sitting at the top of a BottomSheetScaffold's sheetContent. Reacts to
// vertical drag (first delta decides direction) and to tap. Callers wire the
// callbacks to their scaffoldState transitions.
@Composable
fun SheetDragHandle(
    onDragUp: () -> Unit,
    onDragDown: () -> Unit,
    onTap: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            )
            .pointerInput(Unit) {
                var handled = false
                detectVerticalDragGestures(
                    onDragStart = { handled = false },
                    onDragCancel = { handled = false },
                    onDragEnd = { handled = false },
                ) { _, dragAmount ->
                    if (!handled) {
                        if (dragAmount < -1f) {
                            onDragUp()
                            handled = true
                        } else if (dragAmount > 1f) {
                            onDragDown()
                            handled = true
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFE0E0E0))
        )
    }
}
