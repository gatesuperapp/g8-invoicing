package com.a4a.g8invoicing.ui.shared.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconArrowForward: ImageVector
    get() {
        if (_arrowForward != null) {
            return _arrowForward!!
        }
        _arrowForward = materialIcon(name = "Outlined.ArrowForward") {
            materialPath {
                moveTo(12.0f, 4.0f)
                lineToRelative(-1.41f, 1.41f)
                lineTo(16.17f, 11.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(12.17f)
                lineToRelative(-5.58f, 5.59f)
                lineTo(12.0f, 20.0f)
                lineToRelative(8.0f, -8.0f)
                lineToRelative(-8.0f, -8.0f)
                close()
            }
        }
        return _arrowForward!!
    }

private var _arrowForward: ImageVector? = null
