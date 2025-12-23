package icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconTitle: ImageVector
    get() {
        if (_title != null) {
            return _title!!
        }
        _title = materialIcon(name = "Filled.Title") {
            materialPath {
                moveTo(5.0f, 4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(5.5f)
                verticalLineToRelative(12.0f)
                horizontalLineToRelative(3.0f)
                verticalLineTo(7.0f)
                horizontalLineTo(19.0f)
                verticalLineTo(4.0f)
                close()
            }
        }
        return _title!!
    }

private var _title: ImageVector? = null
