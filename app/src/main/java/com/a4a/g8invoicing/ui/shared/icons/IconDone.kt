package icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconDone: ImageVector
    get() {
        if (_done != null) {
            return _done!!
        }
        _done = materialIcon(name = "Filled.Done") {
            materialPath {
                moveTo(9.0f, 16.2f)
                lineTo(4.8f, 12.0f)
                lineToRelative(-1.4f, 1.4f)
                lineTo(9.0f, 19.0f)
                lineTo(21.0f, 7.0f)
                lineToRelative(-1.4f, -1.4f)
                lineTo(9.0f, 16.2f)
                close()
            }
        }
        return _done!!
    }

private var _done: ImageVector? = null
