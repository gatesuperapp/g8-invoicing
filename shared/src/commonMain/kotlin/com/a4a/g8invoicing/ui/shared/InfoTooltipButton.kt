package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.a4a.g8invoicing.ui.theme.callForActionsViolet
import com.russhwolf.settings.Settings
import org.jetbrains.compose.resources.stringResource

private const val TOOLTIP_DISMISSED_PREFIX = "tooltip_dismissed_"
private const val INFO_GLYPH = "?"

/**
 * Small info affordance drawn as a kaomoji `••)?` in dark violet on a light violet
 * rounded pill background. Discreet but discoverable next to a form label.
 *
 * The modal offers two exits:
 * - **OK** → dismisses the modal, glyph stays visible for next time
 * - **Ne plus afficher** → persist via `com.russhwolf.settings.Settings`; the glyph
 *   disappears forever (only rendered when [persistenceKey] is non-null)
 */
@Composable
fun InfoTooltipButton(
    title: String,
    content: String,
    contentDescription: String,
    persistenceKey: String? = null,
    fontSize: TextUnit = 10.sp,
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

    Text(
        text = INFO_GLYPH,
        color = ColorVioletLink,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(ColorVioletLink.copy(alpha = 0.15f))
            .clickable { open = true }
            .padding(horizontal = 8.dp, vertical = 1.dp)
            .semantics { this.contentDescription = contentDescription },
    )

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
                            style = MaterialTheme.typography.callForActionsViolet,
                        )
                    }
                }
            },
            modifier = Modifier.padding(16.dp),
        )
    }
}
