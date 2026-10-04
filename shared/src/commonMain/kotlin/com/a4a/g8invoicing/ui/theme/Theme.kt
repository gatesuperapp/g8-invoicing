package com.a4a.g8invoicing.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.LocalAutofillHighlightBrush
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

// Dark scheme — Material3 defaults tuned to match DarkAppColors. `primary`
// is a near-black tone so Material3 Buttons (which use primary as their
// container by default) render as dark chips with off-white labels — the
// convention the app wants for dark mode: dark fill, white text, keeping
// any existing OutlinedButton border on top. Off-white text on dark surface
// hits WCAG AA at every level defined here.
private val DarkColorScheme = darkColorScheme(
    // Button container (primary) matches the app background so Buttons blend
    // into the surface behind them and only their content + optional outline
    // read as an element.
    primary = Color(0xFF1E1B21),
    onPrimary = Color(0xFFDEDAE0),
    primaryContainer = Color(0xFF35303B),
    secondary = ColorDarkGreen,
    onSecondary = Color(0xFFDEDAE0),
    secondaryContainer = Color(0xFF35303B),
    tertiary = ColorCoral,
    onTertiary = Color(0xFF1E1B21),
    tertiaryContainer = Color(0xFF35303B),
    // The full grey-hierarchy tokens below track DarkAppColors so Material3
    // defaults (BottomAppBar, Card, Surface with elevation, etc.) inherit
    // the same "violet whisper" cast instead of falling back to pure grey.
    background = Color(0xFF1E1B21),
    onBackground = Color(0xFFDEDAE0),
    surface = Color(0xFF272229),
    onSurface = Color(0xFFDEDAE0),
    surfaceTint = Color(0xFF272229),
    outlineVariant = Color(0xFF302B36),
    surfaceContainerLow = Color(0xFF231F26),
    surfaceContainer = Color(0xFF272229),
)

@Composable
fun G8InvoicingTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (useDarkTheme) DarkAppColors else LightAppColors

    val typography = createCustomTypography()

    // Belt-and-suspenders: FormInputCreatorText already strips autofill
    // semantics from every text field so this brush should never draw, but
    // if a stray BasicTextField ever slips through without opting out, the
    // transparent highlight keeps Compose 1.8's persistent yellow overlay
    // from showing up under user text.
    CompositionLocalProvider(
        LocalAutofillHighlightBrush provides SolidColor(Color.Transparent),
        LocalAppColors provides appColors,
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            content = content
        )
    }
}
