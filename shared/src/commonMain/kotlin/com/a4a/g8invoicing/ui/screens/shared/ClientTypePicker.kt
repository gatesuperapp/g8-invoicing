package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.models.ClientType
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.client_type_individual
import com.a4a.g8invoicing.shared.resources.client_type_professional
import com.a4a.g8invoicing.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource

/**
 * iOS-style two-segment control for the client's B2B/B2C flag. Grey rail with
 * two equal-width zones; the selected zone slides a white pill on top and
 * bolds its label. A thin vertical divider only appears in the null state so
 * an empty rail still reads as "two choices, none active" rather than a plain
 * grey band.
 *
 * Re-tapping the active segment clears the selection (fires null) — used by
 * the Factur-X export gate to distinguish "user actively said Particulier"
 * from "user hasn't answered".
 */
@Composable
fun ClientTypePicker(
    selected: ClientType?,
    onSelect: (ClientType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val railHeight = 40.dp
    val pillInset = 2.dp
    val railShape = RoundedCornerShape(railHeight / 2)
    val pillShape = RoundedCornerShape((railHeight / 2) - pillInset)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .clip(railShape)
            .background(AppColors.surfaceMuted)
            .height(railHeight),
    ) {
        val segmentWidth = maxWidth / 2

        val targetOffset = when (selected) {
            ClientType.INDIVIDUAL, null -> 0.dp
            ClientType.PROFESSIONAL -> segmentWidth
        }
        val pillOffset by animateDpAsState(
            targetValue = targetOffset,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "client-type-pill-offset",
        )

        // White pill only rendered when a segment is selected. Matches iOS:
        // null state stays a plain rail.
        if (selected != null) {
            Box(
                modifier = Modifier
                    .offset(x = pillOffset + pillInset, y = pillInset)
                    .width(segmentWidth - pillInset * 2)
                    .height(railHeight - pillInset * 2)
                    .clip(pillShape)
                    .background(AppColors.surface),
            )
        }

        // Divider only in null state — disappears the instant a pill lands.
        if (selected == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .width(1.dp)
                    .background(AppColors.textPale.copy(alpha = 0.45f)),
            )
        }

        val interactionSource = remember { MutableInteractionSource() }

        SegmentLabel(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(segmentWidth)
                .clip(pillShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) {
                    onSelect(
                        if (selected == ClientType.INDIVIDUAL) null
                        else ClientType.INDIVIDUAL,
                    )
                },
            text = stringResource(Res.string.client_type_individual),
            active = selected == ClientType.INDIVIDUAL,
        )
        SegmentLabel(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(segmentWidth)
                .clip(pillShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) {
                    onSelect(
                        if (selected == ClientType.PROFESSIONAL) null
                        else ClientType.PROFESSIONAL,
                    )
                },
            text = stringResource(Res.string.client_type_professional),
            active = selected == ClientType.PROFESSIONAL,
        )
    }
}

@Composable
private fun SegmentLabel(
    modifier: Modifier,
    text: String,
    active: Boolean,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            ),
            // Inactive stays medium grey in both states (null-rail and
            // one-selected). Active label reads near-black on the white pill.
            color = if (active) AppColors.textPrimary else AppColors.textMuted,
        )
    }
}
