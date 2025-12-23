package com.ninetyninepercent.funfactu.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconArrowRight: ImageVector
    get() {
        if (_arrowForwardIos != null) {
            return _arrowForwardIos!!
        }
        _arrowForwardIos = materialIcon(name = "Outlined.ArrowForwardIos") {
            materialPath {
                moveTo(6.23f, 20.23f)
                lineToRelative(1.77f, 1.77f)
                lineToRelative(10.0f, -10.0f)
                lineToRelative(-10.0f, -10.0f)
                lineToRelative(-1.77f, 1.77f)
                lineToRelative(8.23f, 8.23f)
                close()
            }
        }
        return _arrowForwardIos!!
    }

private var _arrowForwardIos: ImageVector? = null
