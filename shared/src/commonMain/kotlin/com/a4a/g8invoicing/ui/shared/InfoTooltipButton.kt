package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.info_tooltip_ok
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.theme.textCta
import com.russhwolf.settings.Settings
import org.jetbrains.compose.resources.stringResource

private const val TOOLTIP_DISMISSED_PREFIX = "tooltip_dismissed_"
private const val QUESTION_GLYPH = "?"
const val INFO_GLYPH_I = "i"

/**
 * Small info affordance drawn as a kaomoji glyph in dark violet on a light violet
 * rounded pill background. Two intended uses via [glyph]:
 * - "?" (default) → one-off explanation. Combine with [persistenceKey] so the user
 *   can dismiss it forever via "Ne plus afficher".
 * - "i" ([INFO_GLYPH_I]) → reference info kept visible in permanence (no
 *   persistenceKey). Meant for field hints that the user will want to consult
 *   again (e.g. country-specific bank identifier format).
 */
@Composable
fun InfoTooltipButton(
    title: String,
    content: String,
    contentDescription: String,
    persistenceKey: String? = null,
    fontSize: TextUnit = 10.sp,
    glyph: String = QUESTION_GLYPH,
    modifier: Modifier = Modifier,
) {
    val settings = remember { Settings() }
    var dismissed by remember(persistenceKey) {
        mutableStateOf(
            persistenceKey?.let { settings.getBoolean(TOOLTIP_DISMISSED_PREFIX + it, false) }
                ?: false
        )
    }
    if (dismissed) return

    var open by remember { mutableStateOf(false) }

    // Tout close = user a lu = pastille disparaît. Regroupé pour que le back-tap et le
    // scrim-tap fassent la même chose que le bouton OK.
    val closeAndDismiss: () -> Unit = {
        open = false
        persistenceKey?.let {
            settings.putBoolean(TOOLTIP_DISMISSED_PREFIX + it, true)
            dismissed = true
        }
    }

    // Circular pastille: fixed square size + CircleShape + Text centered inside.
    // Larger circle + larger glyph so "?" fills the circle instead of floating
    // near the top. `includeFontPadding = false` strips the extra vertical
    // metrics that pushed the character off-centre inside the Box.
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(ColorVioletLink.copy(alpha = 0.15f))
            .clickable { open = true }
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            color = ColorVioletLink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                    alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                    trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.Both,
                ),
            ),
        )
    }

    if (open) {
        AlertDialog(
            onDismissRequest = closeAndDismiss,
            title = { Text(title) },
            text = {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            textContentColor = Color.Black,
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(onClick = closeAndDismiss) {
                        Text(
                            text = stringResource(Res.string.info_tooltip_ok),
                            style = MaterialTheme.typography.textCta,
                        )
                    }
                }
            },
            modifier = Modifier.padding(16.dp),
        )
    }
}
