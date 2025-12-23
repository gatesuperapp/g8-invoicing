package com.a4a.g8invoicing.ui.shared.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconComponents: ImageVector
    get() {
        if (_category != null) {
            return _category!!
        }
        _category = materialIcon(name = "Outlined.Category") {
            materialPath {
                moveTo(12.0f, 2.0f)
                lineToRelative(-5.5f, 9.0f)
                horizontalLineToRelative(11.0f)
                lineTo(12.0f, 2.0f)
                close()
                moveTo(12.0f, 5.84f)
                lineTo(13.93f, 9.0f)
                horizontalLineToRelative(-3.87f)
                lineTo(12.0f, 5.84f)
                close()
                moveTo(17.5f, 13.0f)
                curveToRelative(-2.49f, 0.0f, -4.5f, 2.01f, -4.5f, 4.5f)
                reflectiveCurveToRelative(2.01f, 4.5f, 4.5f, 4.5f)
                reflectiveCurveToRelative(4.5f, -2.01f, 4.5f, -4.5f)
                reflectiveCurveToRelative(-2.01f, -4.5f, -4.5f, -4.5f)
                close()
                moveTo(17.5f, 20.0f)
                curveToRelative(-1.38f, 0.0f, -2.5f, -1.12f, -2.5f, -2.5f)
                reflectiveCurveToRelative(1.12f, -2.5f, 2.5f, -2.5f)
                reflectiveCurveToRelative(2.5f, 1.12f, 2.5f, 2.5f)
                reflectiveCurveToRelative(-1.12f, 2.5f, -2.5f, 2.5f)
                close()
                moveTo(3.0f, 21.5f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-8.0f)
                lineTo(3.0f, 13.5f)
                verticalLineToRelative(8.0f)
                close()
                moveTo(5.0f, 15.5f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                lineTo(5.0f, 19.5f)
                verticalLineToRelative(-4.0f)
                close()
            }
        }
        return _category!!
    }

private var _category: ImageVector? = null
