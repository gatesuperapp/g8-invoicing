package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

/**
 * Draws a subtle scrollbar (light track + moving thumb) on the right
 * edge of a vertically-scrollable composable. Both track + thumb only
 * appear when the content actually overflows the viewport
 * ([ScrollState.maxValue] > 0), so short content stays clean.
 *
 * Thumb height tracks the visible / total ratio; thumb Y tracks the
 * current scroll progress. Track is drawn full-viewport-height as a
 * lighter shade so the user sees the "room to move" even before they
 * start scrolling.
 *
 * Call sites should also pad the inner content ([Modifier.padding])
 * so the scrollable text doesn't collide with the thumb — see the two
 * validation dialogs for the pattern.
 */
fun Modifier.drawScrollThumb(
    scrollState: ScrollState,
    thumbColor: Color = Color(0x99888888),
    trackColor: Color = Color(0x33999999),
    thumbWidthPx: Float = 8f,
    thumbRightInsetPx: Float = 4f,
    minThumbHeightPx: Float = 24f,
): Modifier = this.drawWithContent {
    drawContent()
    val max = scrollState.maxValue
    if (max <= 0) return@drawWithContent

    val viewportHeight = size.height
    val contentHeight = viewportHeight + max
    // Cap the visible/total ratio well below 1 so the thumb is always
    // clearly shorter than the track — otherwise a barely-overflowing body
    // produces a thumb the size of the track and it looks like one big
    // static bar to the user.
    val ratio = (viewportHeight / contentHeight).coerceIn(0f, 0.2f)
    val thumbHeight = (viewportHeight * ratio).coerceAtLeast(minThumbHeightPx)
    val trackTravel = viewportHeight - thumbHeight
    val progress = (scrollState.value.toFloat() / max).coerceIn(0f, 1f)
    val thumbY = trackTravel * progress
    val x = size.width - thumbWidthPx - thumbRightInsetPx
    val cornerRadius = CornerRadius(thumbWidthPx / 2, thumbWidthPx / 2)

    // Track — full viewport height, subtle grey so the user sees the
    // full "room to move" before scrolling.
    drawRoundRect(
        color = trackColor,
        topLeft = Offset(x, 0f),
        size = Size(thumbWidthPx, viewportHeight),
        cornerRadius = cornerRadius,
    )
    // Thumb — darker, moves as the user scrolls.
    drawRoundRect(
        color = thumbColor,
        topLeft = Offset(x, thumbY),
        size = Size(thumbWidthPx, thumbHeight),
        cornerRadius = cornerRadius,
    )
}
