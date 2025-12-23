package com.a4a.g8invoicing.ui.shared.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val IconCancel: ImageVector
    get() {
        if (_cancel != null) {
            return _cancel!!
        }
        _cancel = materialIcon(name = "Outlined.Cancel") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.47f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.47f, 10.0f, -10.0f)
                reflectiveCurveTo(17.53f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.41f, 0.0f, -8.0f, -3.59f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.59f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.59f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.59f, 8.0f, -8.0f, 8.0f)
                close()
                moveTo(15.59f, 7.0f)
                lineTo(12.0f, 10.59f)
                lineTo(8.41f, 7.0f)
                lineTo(7.0f, 8.41f)
                lineTo(10.59f, 12.0f)
                lineTo(7.0f, 15.59f)
                lineTo(8.41f, 17.0f)
                lineTo(12.0f, 13.41f)
                lineTo(15.59f, 17.0f)
                lineTo(17.0f, 15.59f)
                lineTo(13.41f, 12.0f)
                lineTo(17.0f, 8.41f)
                close()
            }
        }
        return _cancel!!
    }

private var _cancel: ImageVector? = null
