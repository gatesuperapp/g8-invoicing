package com.a4a.g8invoicing.ui.shared.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconExport: ImageVector
    get() {
        if (_iosShare != null) {
            return _iosShare!!
        }
        _iosShare = materialIcon(name = "Outlined.IosShare") {
            materialPath {
                moveTo(16.0f, 5.0f)
                lineToRelative(-1.42f, 1.42f)
                lineToRelative(-1.59f, -1.59f)
                verticalLineTo(16.0f)
                horizontalLineToRelative(-1.98f)
                verticalLineTo(4.83f)
                lineTo(9.42f, 6.42f)
                lineTo(8.0f, 5.0f)
                lineToRelative(4.0f, -4.0f)
                lineTo(16.0f, 5.0f)
                close()
                moveTo(20.0f, 10.0f)
                verticalLineToRelative(11.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                horizontalLineTo(6.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                verticalLineTo(10.0f)
                curveToRelative(0.0f, -1.11f, 0.89f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(11.0f)
                horizontalLineToRelative(12.0f)
                verticalLineTo(10.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(3.0f)
                curveTo(19.1f, 8.0f, 20.0f, 8.89f, 20.0f, 10.0f)
                close()
            }
        }
        return _iosShare!!
    }

private var _iosShare: ImageVector? = null
