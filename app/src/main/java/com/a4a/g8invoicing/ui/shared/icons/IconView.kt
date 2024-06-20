package com.a4a.g8invoicing.ui.shared.icons


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconVisibility: ImageVector
    get() {
        if (_visibility != null) {
            return _visibility!!
        }
        _visibility = materialIcon(name = "Outlined.Visibility") {
            materialPath {
                moveTo(12.0f, 6.0f)
                curveToRelative(3.79f, 0.0f, 7.17f, 2.13f, 8.82f, 5.5f)
                curveTo(19.17f, 14.87f, 15.79f, 17.0f, 12.0f, 17.0f)
                reflectiveCurveToRelative(-7.17f, -2.13f, -8.82f, -5.5f)
                curveTo(4.83f, 8.13f, 8.21f, 6.0f, 12.0f, 6.0f)
                moveToRelative(0.0f, -2.0f)
                curveTo(7.0f, 4.0f, 2.73f, 7.11f, 1.0f, 11.5f)
                curveTo(2.73f, 15.89f, 7.0f, 19.0f, 12.0f, 19.0f)
                reflectiveCurveToRelative(9.27f, -3.11f, 11.0f, -7.5f)
                curveTo(21.27f, 7.11f, 17.0f, 4.0f, 12.0f, 4.0f)
                close()
                moveTo(12.0f, 9.0f)
                curveToRelative(1.38f, 0.0f, 2.5f, 1.12f, 2.5f, 2.5f)
                reflectiveCurveTo(13.38f, 14.0f, 12.0f, 14.0f)
                reflectiveCurveToRelative(-2.5f, -1.12f, -2.5f, -2.5f)
                reflectiveCurveTo(10.62f, 9.0f, 12.0f, 9.0f)
                moveToRelative(0.0f, -2.0f)
                curveToRelative(-2.48f, 0.0f, -4.5f, 2.02f, -4.5f, 4.5f)
                reflectiveCurveTo(9.52f, 16.0f, 12.0f, 16.0f)
                reflectiveCurveToRelative(4.5f, -2.02f, 4.5f, -4.5f)
                reflectiveCurveTo(14.48f, 7.0f, 12.0f, 7.0f)
                close()
            }
        }
        return _visibility!!
    }

private var _visibility: ImageVector? = null
