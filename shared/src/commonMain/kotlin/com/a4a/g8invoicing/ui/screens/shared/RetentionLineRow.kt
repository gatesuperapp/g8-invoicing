package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.retention_default_label
import com.a4a.g8invoicing.shared.resources.retention_hide_a11y
import com.a4a.g8invoicing.shared.resources.retention_show_a11y
import com.a4a.g8invoicing.ui.states.RetentionState
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textBodySmall
import org.jetbrains.compose.resources.stringResource

/**
 * Row shape for a retention line in the invoice / credit note line list.
 * Matches [DocumentBottomSheetProductListChosenItem]'s outer shell
 * (same clip, same surfaceSubtle background, same click ripple, same
 * end padding + trailing icon size) so the rows land in the same LazyColumn
 * without visual mismatch. Differences:
 *  - no drag handle Box (retentions are pinned bottom, not reorderable)
 *  - single "Label rate%" text (no separate right-aligned amount column)
 *  - eye icon (15dp, same size as product trash) instead of trash — the row
 *    can't be deleted, only hidden per invoice
 *
 * Tapping the row opens the retention edit sheet. Tapping the eye toggles
 * [retention.hidden]; when true the row dims and the amount is skipped by
 * the totals renderer.
 */
@Composable
fun RetentionLineRow(
    retention: RetentionState,
    onClick: () -> Unit,
    onToggleHidden: () -> Unit,
) {
    val rowInteractionSource = remember { MutableInteractionSource() }
    val labelColor = if (retention.hidden) AppColors.textMuted else AppColors.textPrimary
    val fallbackLabel = stringResource(Res.string.retention_default_label)
    val label = retention.label.text.ifBlank { fallbackLabel }
    val rate = "${retention.rate.stripTrailingZeros().toPlainString().replace(".", ",")} %"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(AppColors.surfaceSubtle)
            .clickable(
                interactionSource = rowInteractionSource,
                indication = ripple(color = Color.Black, bounded = false),
                onClick = onClick,
            )
            // start=8dp aligns the label with the left edge of the product
            // rows' drag handles above.
            .padding(start = 8.dp, end = 13.dp, top = 10.dp, bottom = 10.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            style = MaterialTheme.typography.textBodySmall.copy(
                color = labelColor,
                fontWeight = FontWeight.SemiBold,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = rate,
            style = MaterialTheme.typography.textBodySmall.copy(
                color = labelColor,
                fontWeight = FontWeight.SemiBold,
            ),
            maxLines = 1,
        )
        Spacer(Modifier.width(20.dp))
        Icon(
            modifier = Modifier
                .size(15.dp)
                .clickable(onClick = onToggleHidden),
            imageVector = if (retention.hidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            tint = if (retention.hidden) AppColors.iconSecondary else AppColors.iconPrimary,
            contentDescription = stringResource(
                if (retention.hidden) Res.string.retention_show_a11y else Res.string.retention_hide_a11y
            ),
        )
    }
}
