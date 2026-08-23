package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.models.ClientType
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.client_type_individual
import com.a4a.g8invoicing.shared.resources.client_type_professional
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import org.jetbrains.compose.resources.stringResource

/**
 * Two-chip mono-select for the client's B2B/B2C flag. Same visual language
 * as [PaymentMeansMultiSelect]'s chips. Re-tapping the active chip clears
 * the selection (fires null) — used by the Factur-X export gate to
 * distinguish "user actively said Particulier" from "user hasn't answered".
 *
 * Only rendered on client forms; issuers don't have a client_type.
 */
@Composable
fun ClientTypePicker(
    selected: ClientType?,
    onSelect: (ClientType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ClientTypeChip(
            text = stringResource(Res.string.client_type_individual),
            active = selected == ClientType.INDIVIDUAL,
            // Re-tap on the active chip → clear to null. Otherwise → set.
            onClick = {
                onSelect(if (selected == ClientType.INDIVIDUAL) null else ClientType.INDIVIDUAL)
            },
        )
        ClientTypeChip(
            text = stringResource(Res.string.client_type_professional),
            active = selected == ClientType.PROFESSIONAL,
            onClick = {
                onSelect(if (selected == ClientType.PROFESSIONAL) null else ClientType.PROFESSIONAL)
            },
        )
    }
}

@Composable
private fun ClientTypeChip(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (active) ColorVioletLink.copy(alpha = 0.12f) else AppColors.surfaceMuted
    val fg = if (active) ColorVioletLink else AppColors.textMuted
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = fg,
        )
    }
}
