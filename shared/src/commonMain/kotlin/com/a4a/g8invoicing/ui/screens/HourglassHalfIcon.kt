package com.a4a.g8invoicing.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Half-drained hourglass — no Material variant fits between HourglassTop (all
// sand still up) and HourglassBottom (all sand fallen), so we roll one:
//
//   ┌───────┐
//   │╲     ╱│    top frame + upper triangle (outline)
//   │ ╲   ╱ │
//   │  ╲▂╱  │    ▂ = remaining sand piled at the bottom of the top bulb
//   │   ▽   │
//   │   ▲   │    ▲ = fallen sand piled at the top of the bottom bulb
//   │  ╱▔╲  │
//   │ ╱   ╲ │
//   │╱     ╲│    bottom frame + lower triangle (outline)
//   └───────┘
//
// Frame outline uses the same 24pt viewBox as the stock Material hourglass
// icons so it visually matches HourglassTop / HourglassBottom in a row.
val HourglassHalfIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "HourglassHalf",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Outer frame (matches HourglassEmpty's outline).
        path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 2f)
            verticalLineToRelative(6f)
            lineToRelative(4f, 4f)
            lineToRelative(-4f, 4f)
            verticalLineToRelative(6f)
            horizontalLineToRelative(12f)
            verticalLineToRelative(-6f)
            lineToRelative(-4f, -4f)
            lineToRelative(4f, -4f)
            verticalLineTo(2f)
            close()
            // Bottom bulb outline
            moveTo(16f, 16.5f)
            verticalLineTo(20f)
            horizontalLineTo(8f)
            verticalLineToRelative(-3.5f)
            lineToRelative(4f, -4f)
            close()
            // Top bulb outline
            moveTo(8f, 7.5f)
            verticalLineTo(4f)
            horizontalLineToRelative(8f)
            verticalLineToRelative(3.5f)
            lineToRelative(-4f, 4f)
            close()
        }
        // Sand pile in the top bulb — trapezoid hugging the neck.
        path(fill = SolidColor(Color.Black)) {
            moveTo(9f, 8.5f)
            horizontalLineTo(15f)
            lineTo(13f, 10.5f)
            horizontalLineTo(11f)
            close()
        }
        // Sand pile in the bottom bulb — pyramid piled up from the base.
        path(fill = SolidColor(Color.Black)) {
            moveTo(8f, 20f)
            horizontalLineTo(16f)
            lineTo(13f, 17f)
            horizontalLineTo(11f)
            close()
        }
    }.build()
}
