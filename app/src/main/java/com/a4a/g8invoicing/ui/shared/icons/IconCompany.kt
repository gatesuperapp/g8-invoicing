package com.ninetyninepercent.funfactu.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconCompany: ImageVector
    get() {
        if (_businessCenter != null) {
            return _businessCenter!!
        }
        _businessCenter = materialIcon(name = "Outlined.BusinessCenter") {
            materialPath {
                moveTo(20.0f, 7.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(16.0f, 5.0f)
                lineToRelative(-2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(8.0f, 5.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 7.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 0.75f, 0.4f, 1.38f, 1.0f, 1.73f)
                lineTo(3.0f, 19.0f)
                curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.89f, 2.0f, -2.0f)
                verticalLineToRelative(-3.28f)
                curveToRelative(0.59f, -0.35f, 1.0f, -0.99f, 1.0f, -1.72f)
                lineTo(22.0f, 9.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(10.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(10.0f, 5.0f)
                close()
                moveTo(4.0f, 9.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(5.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineToRelative(-3.0f)
                lineTo(9.0f, 11.0f)
                verticalLineToRelative(3.0f)
                lineTo(4.0f, 14.0f)
                lineTo(4.0f, 9.0f)
                close()
                moveTo(13.0f, 15.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(19.0f, 19.0f)
                lineTo(5.0f, 19.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(3.0f)
                close()
            }
        }
        return _businessCenter!!
    }

private var _businessCenter: ImageVector? = null
