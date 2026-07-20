package com.a4a.g8invoicing.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.LocalAutofillHighlightBrush
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

private val LightColorScheme = lightColorScheme(
    primary = ColorBlueGrey,
    onPrimary = ColorLightBlack,
    primaryContainer = ColorBlueGrey,
    secondary = ColorDarkGreen,
    onSecondary = ColorLightBlack,
    secondaryContainer = ColorBlueGrey,
    tertiary = ColorCoral,
    onTertiary = ColorLightBlack,
    tertiaryContainer = ColorBlueGrey,
    background = MainBackground,
    onBackground = ColorLightBlack,
    surface = Color.White,
    onSurface = ColorLightBlack,
    surfaceTint = Color.White,
    outlineVariant = ColorVeryLightGreen,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
)

@Composable
fun G8InvoicingTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (!useDarkTheme) {
        LightColorScheme
    } else {
        // Dark theme not implemented yet
        LightColorScheme
    }

    val typography = createCustomTypography()

    // Suppress the persistent yellow autofill highlight that Compose 1.8 draws
    // behind any autofilled BasicTextField — Google Password Manager fills the
    // email chip input and the highlight stays visible under the text even
    // after focus moves. Transparent brush = no highlight.
    CompositionLocalProvider(
        LocalAutofillHighlightBrush provides SolidColor(Color.Transparent)
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            content = content
        )
    }
}
