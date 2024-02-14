package icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconSend: ImageVector
    get() {
        if (_send != null) {
            return _send!!
        }
        _send = materialIcon(name = "Filled.Send") {
            materialPath {
                moveTo(2.01f, 21.0f)
                lineTo(23.0f, 12.0f)
                lineTo(2.01f, 3.0f)
                lineTo(2.0f, 10.0f)
                lineToRelative(15.0f, 2.0f)
                lineToRelative(-15.0f, 2.0f)
                close()
            }
        }
        return _send!!
    }

private var _send: ImageVector? = null
