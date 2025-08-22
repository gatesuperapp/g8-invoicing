package com.a4a.g8invoicing.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

    )




/*private val DarkColorScheme = darkColorScheme(
    primary = ColorLightGreen,
    secondary = ColorDarkGreen,
    tertiary = ColorCoral,
    background = ColorBlack,
    surface = ColorBlack,
    onPrimary = ColorLightBlack,
    onSecondary = ColorLightBlack,
    onTertiary = ColorLightBlack,
    onBackground = ColorLightBlack,
    onSurface = ColorLightBlack,
    surfaceTint = ColorRedCultured
)*/

@Composable
fun G8InvoicingTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (!useDarkTheme) {
        LightColorScheme
    } else {
        //DarkColorScheme
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = customTypography,
        content = content
    )
}