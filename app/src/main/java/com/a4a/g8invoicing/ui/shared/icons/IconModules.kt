package com.ninetyninepercent.funfactu.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconModules: ImageVector
    get() {
        if (_widgets != null) {
            return _widgets!!
        }
        _widgets = materialIcon(name = "Outlined.Widgets") {
            materialPath {
                moveTo(16.66f, 4.52f)
                lineToRelative(2.83f, 2.83f)
                lineToRelative(-2.83f, 2.83f)
                lineToRelative(-2.83f, -2.83f)
                lineToRelative(2.83f, -2.83f)
                moveTo(9.0f, 5.0f)
                verticalLineToRelative(4.0f)
                lineTo(5.0f, 9.0f)
                lineTo(5.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                moveToRelative(10.0f, 10.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                moveTo(9.0f, 15.0f)
                verticalLineToRelative(4.0f)
                lineTo(5.0f, 19.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                moveToRelative(7.66f, -13.31f)
                lineTo(11.0f, 7.34f)
                lineTo(16.66f, 13.0f)
                lineToRelative(5.66f, -5.66f)
                lineToRelative(-5.66f, -5.65f)
                close()
                moveTo(11.0f, 3.0f)
                lineTo(3.0f, 3.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(8.0f)
                lineTo(11.0f, 3.0f)
                close()
                moveTo(21.0f, 13.0f)
                horizontalLineToRelative(-8.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-8.0f)
                close()
                moveTo(11.0f, 13.0f)
                lineTo(3.0f, 13.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-8.0f)
                close()
            }
        }
        return _widgets!!
    }

private var _widgets: ImageVector? = null
