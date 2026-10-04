package com.a4a.g8invoicing.ui.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.account_dark_mode_switch
import com.a4a.g8invoicing.shared.resources.first_launch_issuer_skip
import com.a4a.g8invoicing.shared.resources.onboarding_darkmode_migration_body
import com.a4a.g8invoicing.shared.resources.onboarding_no
import com.a4a.g8invoicing.shared.resources.onboarding_yes
import com.a4a.g8invoicing.ui.shared.animations.BatSmilingEyes
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textBody
import com.a4a.g8invoicing.ui.theme.textScreenTitle
import org.jetbrains.compose.resources.stringResource

/**
 * One-shot migration prompt announcing dark mode to users upgrading to 2.0.
 * Dismissable (X icon or tap-outside). Picking "Oui" flips the preference,
 * fades the halo in behind the mascot, and swaps the buttons out — the user
 * then closes the dialog themselves.
 */
@Composable
fun DarkModeMigrationDialog(
    onDismiss: () -> Unit,
    onSetDarkMode: (Boolean) -> Unit,
) {
    var enabled by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .background(AppColors.surface, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = AppColors.iconSecondary,
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    MascotWithHalo(haloVisible = enabled)
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = stringResource(Res.string.account_dark_mode_switch),
                        style = MaterialTheme.typography.textScreenTitle,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(Res.string.onboarding_darkmode_migration_body),
                        style = MaterialTheme.typography.textBody.copy(color = AppColors.textSecondary),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                    ) {
                        Spacer(Modifier.height(28.dp))
                        Button(
                            onClick = {
                                val next = !enabled
                                onSetDarkMode(next)
                                enabled = next
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.buttonActive,
                                contentColor = AppColors.textOnAccent,
                            ),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp),
                        ) {
                            Text(
                                stringResource(
                                    if (enabled) Res.string.onboarding_no
                                    else Res.string.onboarding_yes
                                )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = AppColors.textLink),
                        ) {
                            Text(stringResource(Res.string.first_launch_issuer_skip))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

}

@Composable
private fun MascotWithHalo(haloVisible: Boolean) {
    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = haloVisible,
            enter = fadeIn(tween(900)),
            exit = fadeOut(tween(300)),
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFFE0E0E0),
                                0.55f to Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
        BatSmilingEyes(
            modifier = Modifier.size(120.dp),
            initialDelayMs = 300L,
        )
    }
}
