package com.ninetyninepercent.funfactu.icons


import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val IconLabel: ImageVector
    get() {
        if (_sell != null) {
            return _sell!!
        }
        _sell = materialIcon(name = "Outlined.Sell") {
            materialPath {
                moveTo(21.41f, 11.41f)
                lineToRelative(-8.83f, -8.83f)
                curveTo(12.21f, 2.21f, 11.7f, 2.0f, 11.17f, 2.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 2.0f, 2.0f, 2.9f, 2.0f, 4.0f)
                verticalLineToRelative(7.17f)
                curveToRelative(0.0f, 0.53f, 0.21f, 1.04f, 0.59f, 1.41f)
                lineToRelative(8.83f, 8.83f)
                curveToRelative(0.78f, 0.78f, 2.05f, 0.78f, 2.83f, 0.0f)
                lineToRelative(7.17f, -7.17f)
                curveTo(22.2f, 13.46f, 22.2f, 12.2f, 21.41f, 11.41f)
                close()
                moveTo(12.83f, 20.0f)
                lineTo(4.0f, 11.17f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(7.17f)
                lineTo(20.0f, 12.83f)
                lineTo(12.83f, 20.0f)
                close()
            }
            materialPath {
                moveTo(6.5f, 6.5f)
                moveToRelative(-1.5f, 0.0f)
                arcToRelative(1.5f, 1.5f, 0.0f, true, true, 3.0f, 0.0f)
                arcToRelative(1.5f, 1.5f, 0.0f, true, true, -3.0f, 0.0f)
            }
        }
        return _sell!!
    }

private var _sell: ImageVector? = null
