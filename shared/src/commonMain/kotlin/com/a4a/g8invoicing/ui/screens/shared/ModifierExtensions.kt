package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.theme.ColorGreenPaidCompl

// Double-tap handler for the doc preview clickables. When provided by an
// ancestor, [customCombinedClickable] forwards it as `onDoubleClick`, which
// (a) drives the zoom-toggle gesture and (b) makes Compose wait for the
// double-tap window before firing onClick — so a double-tap no longer
// opens-then-closes the bottom sheet in the same beat (which produced a
// visible white flash of the system nav bar as the sheet dismissed).
val LocalPreviewDoubleTap = compositionLocalOf<(() -> Unit)?> { null }

fun Modifier.getBorder(item: ScreenElement, selectedItem: ScreenElement?) = then(
    border(
        if (item == selectedItem) {
            BorderStroke(1.dp, ColorGreenPaidCompl)
        } else {
            BorderStroke(0.dp, Color.Transparent)
        }
    )
)

// Remove the indicator on click
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.customCombinedClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "combinedClickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
        properties["onDoubleClick"] = onDoubleClick
        properties["onLongClick"] = onLongClick
        properties["onLongClickLabel"] = onLongClickLabel
    }
) {
    // Fall back to the preview-scoped double-tap handler when the caller
    // doesn't set its own — see [LocalPreviewDoubleTap].
    val resolvedDoubleClick = onDoubleClick ?: LocalPreviewDoubleTap.current
    Modifier.combinedClickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        onLongClickLabel = onLongClickLabel,
        onLongClick = onLongClick,
        onDoubleClick = resolvedDoubleClick,
        onClick = onClick,
        role = role,
        indication = null, // Removing the indicator on click
        interactionSource = remember { MutableInteractionSource() }
    )
}
