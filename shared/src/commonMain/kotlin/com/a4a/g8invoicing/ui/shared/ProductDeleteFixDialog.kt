package com.a4a.g8invoicing.ui.shared

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.product_delete_fix_contact_cta
import com.a4a.g8invoicing.shared.resources.whats_new_24_product_delete_fix_description
import com.a4a.g8invoicing.shared.resources.whats_new_24_product_delete_fix_title
import com.a4a.g8invoicing.shared.resources.whats_new_close
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorHotPink
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textScreenTitle
import org.jetbrains.compose.resources.stringResource

/**
 * One-shot dialog shown to every 2.4 upgrader warning about the product-delete
 * regression that shipped between 1.7 and 2.3. Carries the same kaomoji
 * header + violet/pink gradient CTA as the What's New dialog so returning
 * users recognise the "message from us" visual.
 *
 * [onContactSupport] opens the platform mail composer with contact@the-gate.fr
 * pre-filled — wired from MainCompose through the existing onComposeEmail
 * callback. [onDismiss] fires on back / scrim tap and after the contact CTA.
 */
@Composable
fun ProductDeleteFixDialog(
    onContactSupport: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Animated violet/pink border brush — same construction as Account /
    // About so the CTA reads as "a g8 message" rather than a plain button.
    val infiniteTransition = rememberInfiniteTransition(label = "border")
    val targetOffset = with(LocalDensity.current) { 1000.dp.toPx() }
    val brushOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = targetOffset,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "offset",
    )
    val brushSize = 400f
    val ctaBrush = Brush.linearGradient(
        colors = listOf(AppColors.accent, ColorHotPink),
        start = Offset(brushOffset, brushOffset),
        end = Offset(brushOffset + brushSize, brushOffset + brushSize),
        tileMode = TileMode.Mirror,
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .background(AppColors.surface, shape = RoundedCornerShape(16.dp)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Same kaomoji the What's New dialog uses as its welcome
                // header — ties this dialog visually to the "nouveauté"
                // feeling rather than an error / alert.
                Text(
                    text = "((„• ֊ •„)♡",
                    style = MaterialTheme.typography.textScreenTitle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.whats_new_24_product_delete_fix_title),
                    style = MaterialTheme.typography.textScreenTitle.copy(fontSize = 18.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.whats_new_24_product_delete_fix_description),
                    style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        onContactSupport()
                        onDismiss()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .border(
                            BorderStroke(width = 4.dp, brush = ctaBrush),
                            shape = RoundedCornerShape(50),
                        ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                ) {
                    Text(stringResource(Res.string.product_delete_fix_contact_cta))
                }
            }
            // Close affordance top-right — declared after the Column so it
            // sits on top in the Box z-order. The Column's verticalScroll
            // modifier creates a hit target over the whole area; without
            // this swap the tap would be swallowed by the scroll handler
            // and the IconButton click would never fire.
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.whats_new_close),
                    tint = AppColors.iconSecondary,
                )
            }
        }
    }
}
