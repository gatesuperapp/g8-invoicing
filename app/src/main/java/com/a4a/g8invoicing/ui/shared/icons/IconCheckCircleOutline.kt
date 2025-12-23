package icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector


val IconCheckCircleOutline: ImageVector
    get() {
        if (_checkCircleOutline != null) {
            return _checkCircleOutline!!
        }
        _checkCircleOutline = materialIcon(name = "Filled.CheckCircleOutline") {
            materialPath {
                moveTo(16.59f, 7.58f)
                lineTo(10.0f, 14.17f)
                lineToRelative(-3.59f, -3.58f)
                lineTo(5.0f, 12.0f)
                lineToRelative(5.0f, 5.0f)
                lineToRelative(8.0f, -8.0f)
                close()
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                close()
            }
        }
        return _checkCircleOutline!!
    }

private var _checkCircleOutline: ImageVector? = null
