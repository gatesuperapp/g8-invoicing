package com.a4a.g8invoicing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val uiFont = FontFamily(
    Font(
        googleFont = GoogleFont("DM Sans"),
        fontProvider = provider,
        //weight = FontWeight.SemiBold
    )
)

val documentsFont = FontFamily(
    Font(
        googleFont = GoogleFont("Work Sans"),
        fontProvider = provider,
        //weight = FontWeight.SemiBold
    )
)


private val defaultTypography = Typography()
val customTypography = Typography(
    displayLarge = defaultTypography.displayLarge.merge(
        fontFamily = uiFont,
        fontWeight = FontWeight.SemiBold
    ),
    displayMedium = defaultTypography.displayMedium.merge(
        fontFamily = uiFont,
        fontWeight = FontWeight.SemiBold
    ),
    displaySmall = defaultTypography.displaySmall.merge(
        fontFamily = uiFont,
        fontWeight = FontWeight.SemiBold
    ),

    headlineLarge = defaultTypography.headlineLarge.merge(
        fontFamily = uiFont,
        fontWeight = FontWeight.SemiBold
    ),
    headlineMedium = defaultTypography.headlineMedium.merge(
        fontFamily = uiFont,
        fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = defaultTypography.headlineSmall.merge(
        fontFamily = uiFont,
        fontWeight = FontWeight.SemiBold
    ),

    titleLarge = defaultTypography.titleLarge.merge(
        fontFamily = uiFont,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = defaultTypography.titleMedium.merge(
        fontFamily = uiFont,
        fontWeight = FontWeight.SemiBold
    ),
    titleSmall = defaultTypography.titleSmall.merge(
        fontFamily = uiFont,
        fontWeight = FontWeight.SemiBold
    ),

    bodyLarge = defaultTypography.bodyLarge.merge(fontFamily = uiFont),
    bodyMedium = defaultTypography.bodyMedium.merge(fontFamily = uiFont),
    bodySmall = defaultTypography.bodySmall.merge(fontFamily = uiFont),

    labelLarge = defaultTypography.labelLarge.merge(
        fontFamily = uiFont,
        fontWeight = FontWeight.SemiBold
    ),
    labelMedium = defaultTypography.labelMedium.merge(
        fontFamily = uiFont,
        fontWeight = FontWeight.SemiBold
    ),
    labelSmall = defaultTypography.labelSmall.merge(
        fontFamily = uiFont,
        fontWeight = FontWeight.SemiBold
    )

)

val Typography.textForDocuments: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = documentsFont,
            fontWeight = FontWeight.Normal,
            fontSize = 8.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            ),

            )
    }

val Typography.textForDocumentsImportant: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = documentsFont,
            fontWeight = FontWeight.Bold,
            fontSize = 8.sp,
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
            fontFamily = documentsFont,
            fontWeight = FontWeight.Normal,
            fontSize = 8.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }

val Typography.textForDocumentsVerySmall: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = documentsFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 6.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }

val Typography.textForDocumentsVerySmallAndItalic: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = documentsFont,
            fontWeight = FontWeight.Normal,
            fontSize = 6.sp,
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
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
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
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false,
            )
        )
    }