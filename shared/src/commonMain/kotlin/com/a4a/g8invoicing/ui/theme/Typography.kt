package com.a4a.g8invoicing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.dmsansmedium
import com.a4a.g8invoicing.shared.resources.dmsansregular
import com.a4a.g8invoicing.shared.resources.helvetica
import com.a4a.g8invoicing.shared.resources.helveticabold
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

// -----------------------------------------------------------------------------
// Semantic text tokens.
//
// Every Text() in the app should reference one of these via
// `style = MaterialTheme.typography.textXxx`. Never set fontSize / color /
// weight inline. If a new visual variant is needed, add a token here.
//
// Colours are baked in so the call site never touches AppColors for text.
// For a one-off tint (rare), use `.copy(color = AppColors.xxx)`.
// -----------------------------------------------------------------------------

val Typography.textDisplay: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getUiFontBold(),
        fontSize = 40.sp,
        color = AppColors.textPrimary,
    )

val Typography.textHeadline: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getUiFontBold(),
        fontSize = 24.sp,
        color = AppColors.textPrimary,
    )

val Typography.textScreenTitle: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getUiFontBold(),
        fontSize = 20.sp,
        color = AppColors.textPrimary,
    )

val Typography.textBody: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getUiFont(),
        fontSize = 16.sp,
        color = AppColors.textPrimary,
    )

val Typography.textBodyBold: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getUiFont(),
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.textPrimary,
    )

val Typography.textInputPlaceholder: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getUiFont(),
        fontSize = 16.sp,
        color = AppColors.textDisabled,
    )

val Typography.textBodySmall: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getUiFont(),
        fontSize = 14.sp,
        color = AppColors.textPrimary,
    )

val Typography.textCta: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getUiFontBold(),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.textLink,
    )

val Typography.textCtaDisabled: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getUiFontBold(),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.textDisabled,
    )

val Typography.textSecondary: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getUiFont(),
        fontSize = 13.sp,
        color = AppColors.textSecondary,
    )

// Small caps section label with letter-spacing (RÉCENTS, PARAMÈTRES…).
// Call sites must uppercase the text themselves (e.g. `text.uppercase()`).
val Typography.textSection: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getUiFontBold(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.08.em,
        color = AppColors.textSecondary,
    )

val Typography.textCaption: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getUiFont(),
        fontSize = 12.sp,
        color = AppColors.textSecondary,
    )

val Typography.textTiny: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getUiFont(),
        fontSize = 10.sp,
        color = AppColors.textPrimary,
    )

// -----------------------------------------------------------------------------
// PDF-only text styles. Kept separate from the in-app scale because the PDF
// renders at a fixed physical size — these sp values feed iText/Compose paint
// on the generated document, not on-screen typography.
// -----------------------------------------------------------------------------

val Typography.textForDocuments: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getPdfFont(),
        fontSize = 6.sp,
    )

val Typography.textForDocumentsBold: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getPdfFontBold(),
        fontSize = 6.sp,
    )

val Typography.textForDocumentsSecondary: TextStyle
    @Composable
    get() = TextStyle(
        color = Color.DarkGray,
        fontFamily = getPdfFont(),
        fontSize = 6.sp,
    )

val Typography.titleForDocuments: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getPdfFontBold(),
        fontSize = 13.sp,
    )

val Typography.subTitleForDocuments: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = getPdfFontBold(),
        fontSize = 10.sp,
    )

