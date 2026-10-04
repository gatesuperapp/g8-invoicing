package com.a4a.g8invoicing.ui.shared.animations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.a4a.g8invoicing.ui.theme.AppColors

/**
 * Radial halo behind a Lottie mascot. Bat* animations ship a dark baked-in
 * palette so they disappear on dark surfaces — this soft grey → transparent
 * gradient lifts them off the background. Light mode doesn't need the lift,
 * so the halo is skipped entirely there.
 */
private val HaloCore = Color(0xFFB8B8B8)

@Composable
fun LottieMascotHalo(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val haloModifier = if (AppColors.isDark) {
        modifier.background(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to HaloCore,
                    0.55f to Color.Transparent,
                ),
            ),
        )
    } else {
        modifier
    }
    Box(
        modifier = haloModifier,
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
