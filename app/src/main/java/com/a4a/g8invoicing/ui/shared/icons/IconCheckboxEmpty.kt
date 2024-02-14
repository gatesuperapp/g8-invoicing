package com.ninetyninepercent.funfactu.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector
val IconCheckboxEmpty: ImageVector
    get() {
        if (_checkBoxOutlineBlank != null) {
            return _checkBoxOutlineBlank!!
        }
        _checkBoxOutlineBlank = materialIcon(name = "Outlined.CheckBoxOutlineBlank") {
            materialPath {
                moveTo(19.0f, 5.0f)
                verticalLineToRelative(14.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(14.0f)
                moveToRelative(0.0f, -2.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
            }
        }
        return _checkBoxOutlineBlank!!
    }

private var _checkBoxOutlineBlank: ImageVector? = null
