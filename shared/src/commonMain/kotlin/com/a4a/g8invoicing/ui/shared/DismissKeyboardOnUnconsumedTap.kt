package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

// Catches unconsumed taps on Main pass (descendants like BasicTextField get
// first crack) and dispatches to onTap with the local tap position. Used at
// the FormUI Column level as a workaround: .clickable on Row / wrapper Column
// silently fails to fire in release APKs, so tap-to-focus is centralised here
// with per-row Y tracking for dispatch.
fun Modifier.absorbAndDispatchTap(
    tag: String = "",
    onTap: (androidx.compose.ui.geometry.Offset) -> Unit,
): Modifier = pointerInput(tag) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = true)
        down.consume()
        val pos = down.position
        while (true) {
            val event = awaitPointerEvent()
            if (event.changes.all { !it.pressed }) break
        }
        onTap(pos)
    }
}

// Observes consumption on Final pass across the whole gesture. If nothing was
// consumed by any descendant, the tap landed on truly empty space (grey area
// between form blocks, outer padding) — clear focus and hide the keyboard.
// A plain .clickable here raced with children on release-build timings;
// checking Final-pass isConsumed makes the resolution deterministic.
fun Modifier.dismissKeyboardOnUnconsumedTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            var anyConsumed = false
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Final)
                if (event.changes.any { it.isConsumed }) {
                    anyConsumed = true
                }
                if (event.changes.all { !it.pressed }) break
            }
            if (!anyConsumed) {
                focusManager.clearFocus()
                keyboard?.hide()
            }
        }
    }
}
