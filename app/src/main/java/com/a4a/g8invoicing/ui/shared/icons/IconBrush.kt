package com.a4a.g8invoicing.ui.shared.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconBrush: ImageVector
    get() {
        if (_brush != null) {
            return _brush!!
        }
        _brush = materialIcon(name = "Outlined.Brush") {
            materialPath {
                moveTo(7.0f, 16.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                curveToRelative(-0.17f, 0.0f, -0.33f, -0.02f, -0.5f, -0.05f)
                curveToRelative(0.31f, -0.55f, 0.5f, -1.21f, 0.5f, -1.95f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                moveTo(18.67f, 3.0f)
                curveToRelative(-0.26f, 0.0f, -0.51f, 0.1f, -0.71f, 0.29f)
                lineTo(9.0f, 12.25f)
                lineTo(11.75f, 15.0f)
                lineToRelative(8.96f, -8.96f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(-1.34f, -1.34f)
                curveToRelative(-0.2f, -0.2f, -0.45f, -0.29f, -0.7f, -0.29f)
                close()
                moveTo(7.0f, 14.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                curveToRelative(0.0f, 1.31f, -1.16f, 2.0f, -2.0f, 2.0f)
                curveToRelative(0.92f, 1.22f, 2.49f, 2.0f, 4.0f, 2.0f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                close()
            }
        }
        return _brush!!
    }

private var _brush: ImageVector? = null
