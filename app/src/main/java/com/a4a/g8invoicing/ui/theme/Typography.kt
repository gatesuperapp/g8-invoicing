package com.a4a.g8invoicing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.helveticaBold


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

    bodyLarge = defaultTypography.bodyLarge.merge(fontFamily = uiFontBold),
    bodyMedium = defaultTypography.bodyMedium.merge(fontFamily = uiFont),
    bodySmall = defaultTypography.bodySmall.merge(fontFamily = uiFont),

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

val Typography.textForDocuments: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = pdfFont,
            fontSize = 7.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            ),

            )
    }

val Typography.titleForDocuments: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = pdfFont,
            fontSize = 12.sp,
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
            fontSize = 8.sp,
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

val Typography.textForDocumentsVerySmall: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = pdfFont,
            fontSize = 5.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }

val Typography.textForDocumentsVerySmallBold: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = pdfFontBold,
            fontSize = 5.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }

val Typography.textForDocumentsVerySmallAndItalic: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = pdfFont,
            fontSize = 5.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            ),
            fontStyle = FontStyle.Italic
        )
    }

val Typography.textSmall: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = uiFont,
            fontSize = 10.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }

val Typography.textTitle: TextStyle
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

val Typography.textFormBlock: TextStyle
    @Composable
    get() {
        return TextStyle(
            color = ColorDarkGrayTransp,
            fontFamily = uiFont,
            fontSize = 20.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }

val Typography.textForFormLabelVerySmall: TextStyle
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