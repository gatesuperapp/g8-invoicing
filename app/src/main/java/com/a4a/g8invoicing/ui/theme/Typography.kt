package com.a4a.g8invoicing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.R


val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val uiFont = FontFamily(
    Font(R.font.dmsansregular),
)
val uiFontBold = FontFamily(
    Font(R.font.dmsansmedium),
)

val pdfFont = FontFamily(
    Font(R.font.helvetica),
)

val pdfFontBold = FontFamily(
    Font(R.font.helveticabold),
)

private val defaultTypography = Typography()
val customTypography = Typography(
    displayLarge = defaultTypography.displayLarge.merge(
        fontFamily = uiFontBold,
        //    fontWeight = FontWeight.SemiBold
    ),
    displayMedium = defaultTypography.displayMedium.merge(
        fontFamily = uiFontBold,
        //   fontWeight = FontWeight.SemiBold
    ),
    displaySmall = defaultTypography.displaySmall.merge(
        fontFamily = uiFont,
        //   fontWeight = FontWeight.SemiBold
    ),

    headlineLarge = defaultTypography.headlineLarge.merge(
        fontFamily = uiFontBold,
        //    fontWeight = FontWeight.SemiBold
    ),
    headlineMedium = defaultTypography.headlineMedium.merge(
        fontFamily = uiFontBold,
        //    fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = defaultTypography.headlineSmall.merge(
        fontFamily = uiFontBold,
        //    fontWeight = FontWeight.SemiBold
    ),

    titleLarge = defaultTypography.titleLarge.merge(
        fontFamily = uiFontBold,
        //    fontWeight = FontWeight.SemiBold
    ),
    titleMedium = defaultTypography.titleMedium.merge(
        fontFamily = uiFontBold,
    ),
    titleSmall = defaultTypography.titleSmall.merge(
        fontFamily = uiFontBold,
        //     fontWeight = FontWeight.SemiBold
    ),

    bodyLarge = defaultTypography.bodyLarge.merge(// THE DEFAULT ONE
        fontFamily = uiFont
    ),
    bodyMedium = defaultTypography.bodyMedium.merge(fontFamily = uiFont),
    bodySmall = defaultTypography.bodySmall.merge(
        fontFamily = uiFont
    ),

    labelLarge = defaultTypography.labelLarge.merge(
        fontFamily = uiFontBold,
        //     fontWeight = FontWeight.SemiBold
    ),
    labelMedium = defaultTypography.labelMedium.merge(
        fontFamily = uiFont,
        //    fontWeight = FontWeight.SemiBold
    ),
    labelSmall = defaultTypography.labelSmall.merge(
        fontFamily = uiFont,
        //    fontWeight = FontWeight.SemiBold
    )

)

val Typography.textSmall: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = uiFont,
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
            fontFamily = uiFont,
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
            fontFamily = uiFont,
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
            fontFamily = uiFont,
            color = ColorDarkGray,
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
            fontFamily = uiFont,
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
            fontFamily = uiFont,
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
            fontFamily = pdfFont,
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
            fontFamily = pdfFontBold,
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
            fontFamily = pdfFont,
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
            fontFamily = pdfFont,
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
            fontFamily = pdfFont,
            fontSize = 10.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            ),
        )
    }


