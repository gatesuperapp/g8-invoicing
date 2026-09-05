package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textScreenTitle

/**
 * App-wide modal design system. Three composables cover every non-bespoke
 * modal in the codebase; the pattern mirrors the export-format chooser
 * (`ExportFormatChooserDialog` in DocumentAddEdit.kt) that the product agreed
 * on as the canonical look.
 *
 * Tokens (all in this file so future tweaks land in one spot):
 *   • Surface           — AppColors.surface, 16.dp rounded corners
 *   • Outer margin      — 32.dp horizontal (leaves the dialog "floating")
 *   • Inner padding     — 24.dp horizontal + vertical
 *   • Title             — typography.textScreenTitle @ 18.sp, textPrimary
 *   • Body              — typography.textBodySmall, textSecondary, lineHeight 20.sp
 *   • Primary CTA       — Button, buttonActive fill + textOnAccent text
 *   • Secondary CTA     — OutlinedButton, textLink content, no fill
 *   • Destructive CTA   — Button, dangerAccent fill + textOnAccent text
 *   • Vertical rhythm   — 12.dp title→body, 24.dp body→first-button, 8.dp between buttons
 *
 * Bespoke modals that intentionally deviate (kept out of this system):
 *   • `AuthMessageDialog` when showOhNoHeader=true — carries the 𝕠𝕙 𝕟𝕠
 *     kaomoji header, custom identity
 *   • `OupsDialog` — CII export blocker punch list, emoji-prefixed body sections
 *     with bullet lists, kept as-is (scrollable body is exceptional)
 *   • `OnboardingDialog` / `OnboardingMigration19Dialog` — full-screen guided
 *     flows, not tap-and-dismiss modals
 */

/**
 * Single-CTA info / success / error dialog. Optional [bodyContent] slot lets
 * the caller drop custom content (e.g. a scrollable list, an icon, a link)
 * instead of / in addition to the plain [body] string.
 */
@Composable
fun AppInfoDialog(
    body: String? = null,
    onDismiss: () -> Unit,
    title: String? = null,
    confirmText: String = "OK",
    onConfirm: () -> Unit = onDismiss,
    bodyContent: (@Composable () -> Unit)? = null,
) {
    AppDialogShell(onDismiss = onDismiss) {
        if (title != null) {
            AppDialogTitle(title)
            Spacer(Modifier.height(12.dp))
        }
        if (body != null) {
            AppDialogBody(body)
        }
        if (bodyContent != null) {
            if (body != null) Spacer(Modifier.height(12.dp))
            bodyContent()
        }
        Spacer(Modifier.height(24.dp))
        AppPrimaryButton(text = confirmText, onClick = onConfirm)
    }
}

/**
 * Two-CTA confirm/cancel. [destructive] flips the confirm button to the danger
 * palette (used for irreversible actions — delete, wipe). [bodyContent] slot
 * lets callers render richer content (clickable link, list) in place of the
 * plain [body] string.
 */
@Composable
fun AppConfirmDialog(
    title: String? = null,
    body: String? = null,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    cancelText: String = "Annuler",
    destructive: Boolean = false,
    bodyContent: (@Composable () -> Unit)? = null,
) {
    AppDialogShell(onDismiss = onDismiss) {
        if (title != null) {
            AppDialogTitle(title)
            Spacer(Modifier.height(12.dp))
        }
        if (body != null) {
            AppDialogBody(body)
        }
        if (bodyContent != null) {
            if (body != null) Spacer(Modifier.height(12.dp))
            bodyContent()
        }
        Spacer(Modifier.height(24.dp))
        if (destructive) {
            AppDestructiveButton(text = confirmText, onClick = onConfirm)
        } else {
            AppPrimaryButton(text = confirmText, onClick = onConfirm)
        }
        Spacer(Modifier.height(8.dp))
        AppSecondaryButton(text = cancelText, onClick = onDismiss)
    }
}

/**
 * N-choice picker. Each [choice] renders as its own button; exactly ONE choice
 * may be marked primary (violet fill) — the rest render outlined. Dismiss is
 * implicit via the platform back gesture / scrim tap.
 */
@Composable
fun AppChoiceDialog(
    title: String,
    choices: List<AppDialogChoice>,
    onDismiss: () -> Unit,
    body: String? = null,
) {
    AppDialogShell(onDismiss = onDismiss) {
        AppDialogTitle(title)
        if (body != null) {
            Spacer(Modifier.height(12.dp))
            AppDialogBody(body)
        }
        Spacer(Modifier.height(24.dp))
        choices.forEachIndexed { index, choice ->
            if (choice.primary) {
                AppPrimaryButton(text = choice.label, onClick = choice.onClick)
            } else {
                AppSecondaryButton(text = choice.label, onClick = choice.onClick)
            }
            if (index != choices.lastIndex) Spacer(Modifier.height(8.dp))
        }
    }
}

data class AppDialogChoice(
    val label: String,
    val onClick: () -> Unit,
    val primary: Boolean = false,
)

// ---- Shared building blocks (private to this file) ----

@Composable
private fun AppDialogShell(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .background(AppColors.surface, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Long bodies (multi-line explanations, error lists) stay
                    // reachable on short devices without breaking the padded
                    // layout above.
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
            ) {
                content()
            }
        }
    }
}

@Composable
private fun AppDialogTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.textScreenTitle.copy(fontSize = 18.sp),
        textAlign = TextAlign.Start,
    )
}

@Composable
private fun AppDialogBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
        textAlign = TextAlign.Start,
        lineHeight = 20.sp,
    )
}

@Composable
private fun AppPrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.buttonActive,
            contentColor = AppColors.textOnAccent,
        ),
    ) { Text(text) }
}

@Composable
private fun AppSecondaryButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.textLink),
    ) { Text(text) }
}

@Composable
private fun AppDestructiveButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.dangerAccent,
            contentColor = AppColors.textOnAccent,
        ),
    ) { Text(text) }
}
