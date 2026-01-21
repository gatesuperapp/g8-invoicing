package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun Separator() {
    Canvas(
        modifier = Modifier
            .height(3.dp)
            .fillMaxWidth()
    ) {
        drawLine(
            color = Color.LightGray.copy(alpha = 0.6f), // Line color
            start = Offset(
                0f,
                2f
            ), // Starting point (1f to be positioned on the line)
            end = Offset(size.width, 2f), // Ending point
            strokeWidth = 2f // Line width
        )
    }
}

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }
