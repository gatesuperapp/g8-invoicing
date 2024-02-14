package com.a4a.g8invoicing.ui.shared.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val IconText: ImageVector
    get() {
        if (_title != null) {
            return _title!!
        }
        _title = materialIcon(name = "Outlined.Title") {
            materialPath {
                moveTo(5.0f, 4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(5.5f)
                verticalLineToRelative(12.0f)
                horizontalLineToRelative(3.0f)
                verticalLineTo(7.0f)
                horizontalLineTo(19.0f)
                verticalLineTo(4.0f)
                horizontalLineTo(5.0f)
                close()
            }
        }
        return _title!!
    }

private var _title: ImageVector? = null
