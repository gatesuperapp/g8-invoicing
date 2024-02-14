package com.ninetyninepercent.funfactu.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconPaid: ImageVector
    get() {
        if (_paid != null) {
            return _paid!!
        }
        _paid = materialIcon(name = "Outlined.Paid") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.41f, 0.0f, -8.0f, -3.59f, -8.0f, -8.0f)
                curveToRelative(0.0f, -4.41f, 3.59f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.59f, 8.0f, 8.0f)
                curveTo(20.0f, 16.41f, 16.41f, 20.0f, 12.0f, 20.0f)
                close()
                moveTo(12.89f, 11.1f)
                curveToRelative(-1.78f, -0.59f, -2.64f, -0.96f, -2.64f, -1.9f)
                curveToRelative(0.0f, -1.02f, 1.11f, -1.39f, 1.81f, -1.39f)
                curveToRelative(1.31f, 0.0f, 1.79f, 0.99f, 1.9f, 1.34f)
                lineToRelative(1.58f, -0.67f)
                curveToRelative(-0.15f, -0.44f, -0.82f, -1.91f, -2.66f, -2.23f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(-1.75f)
                verticalLineToRelative(1.26f)
                curveToRelative(-2.6f, 0.56f, -2.62f, 2.85f, -2.62f, 2.96f)
                curveToRelative(0.0f, 2.27f, 2.25f, 2.91f, 3.35f, 3.31f)
                curveToRelative(1.58f, 0.56f, 2.28f, 1.07f, 2.28f, 2.03f)
                curveToRelative(0.0f, 1.13f, -1.05f, 1.61f, -1.98f, 1.61f)
                curveToRelative(-1.82f, 0.0f, -2.34f, -1.87f, -2.4f, -2.09f)
                lineTo(8.1f, 14.75f)
                curveToRelative(0.63f, 2.19f, 2.28f, 2.78f, 3.02f, 2.96f)
                verticalLineTo(19.0f)
                horizontalLineToRelative(1.75f)
                verticalLineToRelative(-1.24f)
                curveToRelative(0.52f, -0.09f, 3.02f, -0.59f, 3.02f, -3.22f)
                curveTo(15.9f, 13.15f, 15.29f, 11.93f, 12.89f, 11.1f)
                close()
            }
        }
        return _paid!!
    }

private var _paid: ImageVector? = null
