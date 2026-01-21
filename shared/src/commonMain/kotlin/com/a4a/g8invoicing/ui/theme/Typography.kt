package com.a4a.g8invoicing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.dmsansmedium
import com.a4a.g8invoicing.shared.resources.dmsansregular
import com.a4a.g8invoicing.shared.resources.helvetica
import com.a4a.g8invoicing.shared.resources.helveticabold
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

@Composable
fun getUiFont(): FontFamily = FontFamily(Font(Res.font.dmsansregular))

@Composable
fun getUiFontBold(): FontFamily = FontFamily(Font(Res.font.dmsansmedium))

@Composable
fun getPdfFont(): FontFamily = FontFamily(Font(Res.font.helvetica))

@Composable
fun getPdfFontBold(): FontFamily = FontFamily(Font(Res.font.helveticabold))

// Default typography using system fonts (for non-composable contexts)
private val defaultTypography = Typography()

// Create typography with default fonts - will be overridden in Theme with custom fonts
val customTypography = Typography(
    displayLarge = defaultTypography.displayLarge,
    displayMedium = defaultTypography.displayMedium,
    displaySmall = defaultTypography.displaySmall,
    headlineLarge = defaultTypography.headlineLarge,
    headlineMedium = defaultTypography.headlineMedium,
    headlineSmall = defaultTypography.headlineSmall,
    titleLarge = defaultTypography.titleLarge,
    titleMedium = defaultTypography.titleMedium,
    titleSmall = defaultTypography.titleSmall,
    bodyLarge = defaultTypography.bodyLarge,
    bodyMedium = defaultTypography.bodyMedium,
    bodySmall = defaultTypography.bodySmall,
    labelLarge = defaultTypography.labelLarge,
    labelMedium = defaultTypography.labelMedium,
    labelSmall = defaultTypography.labelSmall
)

@Composable
fun createCustomTypography(): Typography {
    val uiFont = getUiFont()
    val uiFontBold = getUiFontBold()

    return Typography(
        displayLarge = defaultTypography.displayLarge.merge(fontFamily = uiFontBold),
        displayMedium = defaultTypography.displayMedium.merge(fontFamily = uiFontBold),
        displaySmall = defaultTypography.displaySmall.merge(fontFamily = uiFont),
        headlineLarge = defaultTypography.headlineLarge.merge(fontFamily = uiFontBold),
        headlineMedium = defaultTypography.headlineMedium.merge(fontFamily = uiFontBold),
        headlineSmall = defaultTypography.headlineSmall.merge(fontFamily = uiFontBold),
        titleLarge = defaultTypography.titleLarge.merge(fontFamily = uiFontBold),
        titleMedium = defaultTypography.titleMedium.merge(fontFamily = uiFontBold),
        titleSmall = defaultTypography.titleSmall.merge(fontFamily = uiFontBold),
        bodyLarge = defaultTypography.bodyLarge.merge(fontFamily = uiFont),
        bodyMedium = defaultTypography.bodyMedium.merge(fontFamily = uiFont),
        bodySmall = defaultTypography.bodySmall.merge(fontFamily = uiFont),
        labelLarge = defaultTypography.labelLarge.merge(fontFamily = uiFontBold),
        labelMedium = defaultTypography.labelMedium.merge(fontFamily = uiFont),
        labelSmall = defaultTypography.labelSmall.merge(fontFamily = uiFont)
    )
}

val Typography.textTitle: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getUiFontBold(),
            fontSize = 18.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }

val Typography.textNormalBold: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getUiFontBold(),
            fontSize = 16.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            ),
        )
    }

val Typography.textSmall: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getUiFont(),
            fontSize = 14.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }


val Typography.textVerySmall: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getUiFont(),
            fontSize = 9.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }

val Typography.textWithLinkCenteredMedium: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getUiFont(),
            fontSize = 16.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            ),
            textAlign = TextAlign.Center
        )
    }


val Typography.callForActions: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getUiFont(),
            color = Color.DarkGray,
            fontSize = 14.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }

val Typography.callForActionsViolet: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getUiFontBold(),
            color = ColorVioletLight,
            fontSize = 14.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }

val Typography.callForActionsDisabled: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getUiFontBold(),
            color = ColorDarkGrayTransp,
            fontSize = 14.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }


val Typography.inputLabel: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getUiFont(),
            color = ColorDarkGray,
            fontSize = 16.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            ),
            fontWeight = FontWeight.SemiBold
        )
    }

val Typography.inputField: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getUiFont(),
            color = Color.LightGray,
            fontSize = 16.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            ),
        )
    }

val Typography.textForDocuments: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getPdfFont(),
            fontSize = 6.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            ),
        )
    }

val Typography.textForDocumentsBold: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getPdfFontBold(),
            fontSize = 6.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            ),
        )
    }

val Typography.textForDocumentsSecondary: TextStyle
    @Composable
    get() {
        return TextStyle(
            color = Color.DarkGray,
            fontFamily = getPdfFont(),
            fontSize = 6.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }


val Typography.titleForDocuments: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getPdfFontBold(),
            fontSize = 13.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            ),
        )
    }

val Typography.subTitleForDocuments: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = getPdfFontBold(),
            fontSize = 10.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            ),
        )
    }
