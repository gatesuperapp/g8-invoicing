package icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val IconAddAPhoto: ImageVector
    get() {
        if (_addAPhoto != null) {
            return _addAPhoto!!
        }
        _addAPhoto = materialIcon(name = "Filled.AddAPhoto") {
            materialPath {
                moveTo(3.0f, 4.0f)
                verticalLineTo(1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(3.0f)
                verticalLineTo(6.0f)
                horizontalLineTo(0.0f)
                verticalLineTo(4.0f)
                horizontalLineTo(3.0f)
                close()
                moveTo(6.0f, 10.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(3.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(7.0f)
                lineToRelative(1.83f, 2.0f)
                horizontalLineTo(21.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                verticalLineTo(10.0f)
                horizontalLineTo(6.0f)
                close()
                moveTo(13.0f, 19.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                reflectiveCurveToRelative(-2.24f, -5.0f, -5.0f, -5.0f)
                reflectiveCurveToRelative(-5.0f, 2.24f, -5.0f, 5.0f)
                reflectiveCurveTo(10.24f, 19.0f, 13.0f, 19.0f)
                close()
                moveTo(9.8f, 14.0f)
                curveToRelative(0.0f, 1.77f, 1.43f, 3.2f, 3.2f, 3.2f)
                reflectiveCurveToRelative(3.2f, -1.43f, 3.2f, -3.2f)
                reflectiveCurveToRelative(-1.43f, -3.2f, -3.2f, -3.2f)
                reflectiveCurveTo(9.8f, 12.23f, 9.8f, 14.0f)
                close()
            }
        }
        return _addAPhoto!!
    }

private var _addAPhoto: ImageVector? = null
