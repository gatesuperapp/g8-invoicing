package com.ninetyninepercent.funfactu.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector


val IconRemoveCircleFilled: ImageVector
    get() {
        if (_doNotDisturbOn != null) {
            return _doNotDisturbOn!!
        }
        _doNotDisturbOn = materialIcon(name = "Filled.DoNotDisturbOn") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(17.0f, 13.0f)
                lineTo(7.0f, 13.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _doNotDisturbOn!!
    }

private var _doNotDisturbOn: ImageVector? = null
