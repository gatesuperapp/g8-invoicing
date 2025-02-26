package com.a4a.g8invoicing.ui.shared.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconFullScreen: ImageVector
    get() {
        if (_fullscreen != null) {
            return _fullscreen!!
        }
        _fullscreen = materialIcon(name = "Filled.Fullscreen") {
            materialPath {
                moveTo(7.0f, 14.0f)
                lineTo(5.0f, 14.0f)
                verticalLineToRelative(5.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(-2.0f)
                lineTo(7.0f, 17.0f)
                verticalLineToRelative(-3.0f)
                close()
                moveTo(5.0f, 10.0f)
                horizontalLineToRelative(2.0f)
                lineTo(7.0f, 7.0f)
                horizontalLineToRelative(3.0f)
                lineTo(10.0f, 5.0f)
                lineTo(5.0f, 5.0f)
                verticalLineToRelative(5.0f)
                close()
                moveTo(17.0f, 17.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(-5.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(3.0f)
                close()
                moveTo(14.0f, 5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(2.0f)
                lineTo(19.0f, 5.0f)
                horizontalLineToRelative(-5.0f)
                close()
            }
        }
        return _fullscreen!!
    }

private var _fullscreen: ImageVector? = null
