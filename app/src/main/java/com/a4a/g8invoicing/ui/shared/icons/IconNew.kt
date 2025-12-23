package icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconNew: ImageVector
    get() {
        if (_postAdd != null) {
            return _postAdd!!
        }
        _postAdd = materialIcon(name = "Filled.PostAdd") {
            materialPath {
                moveTo(17.0f, 19.22f)
                horizontalLineTo(5.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(5.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 5.0f, 3.0f, 5.9f, 3.0f, 7.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-7.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(19.22f)
                close()
            }
            materialPath {
                moveTo(19.0f, 2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(-3.0f)
                curveToRelative(0.01f, 0.01f, 0.0f, 2.0f, 0.0f, 2.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(2.99f)
                curveToRelative(0.01f, 0.01f, 2.0f, 0.0f, 2.0f, 0.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(3.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineTo(2.0f)
                close()
            }
            materialPath {
                moveTo(7.0f, 9.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-8.0f)
                close()
            }
            materialPath {
                moveTo(7.0f, 12.0f)
                lineToRelative(0.0f, 2.0f)
                lineToRelative(8.0f, 0.0f)
                lineToRelative(0.0f, -2.0f)
                lineToRelative(-3.0f, 0.0f)
                close()
            }
            materialPath {
                moveTo(7.0f, 15.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-8.0f)
                close()
            }
        }
        return _postAdd!!
    }

private var _postAdd: ImageVector? = null
