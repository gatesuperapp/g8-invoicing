package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.font_picker_title
import com.a4a.g8invoicing.shared.resources.gstore_premium_badge
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.DocumentFont
import com.a4a.g8invoicing.ui.theme.regularFamily
import com.a4a.g8invoicing.ui.theme.textBodyBold
import org.jetbrains.compose.resources.stringResource

/**
 * Per-document font picker sheet. Reached from the "Police" bottom-bar
 * button. Each row renders its own name in its own typeface so the label
 * doubles as a live preview.
 *
 * Premium rows still tap-through: the picker never gates selection, only the
 * export flow does (see the premium-font check in the export chooser).
 */
@Composable
fun DocumentBottomSheetFont(
    sheetContentHeight: Dp,
    selected: DocumentFont,
    onSelect: (DocumentFont) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(sheetContentHeight),
    ) {
        Text(
            text = stringResource(Res.string.font_picker_title),
            style = MaterialTheme.typography.textBodyBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(DocumentFont.entries) { font ->
                FontRow(
                    font = font,
                    isSelected = font == selected,
                    onClick = { onSelect(font) },
                )
            }
        }
    }
}

@Composable
private fun FontRow(
    font: DocumentFont,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    // Blue tint on the selected row mirrors the picker's currentSelected
    // treatment for issuers/clients (see ClientOrIssuerPickerBottomSheet's
    // SelectedViolet). Same violet at a lower opacity so it stays readable.
    val background = if (isSelected) SelectedFontHighlight else AppColors.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Name rendered in the font's own typeface — instant preview.
        Text(
            modifier = Modifier.weight(1f),
            text = font.displayName,
            fontFamily = font.regularFamily(),
            fontSize = 18.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = AppColors.textPrimary,
        )
        if (font.isPremium) {
            Spacer(Modifier.padding(start = 8.dp))
            PremiumPill()
        }
    }
}

@Composable
private fun PremiumPill() {
    // Same look as GStore.PremiumPill (private there) — light lavender pill
    // with the "★ PREMIUM" tag in the brand violet.
    Box(
        modifier = Modifier
            .background(PremiumPillBackground, shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(Res.string.gstore_premium_badge),
            style = MaterialTheme.typography.textBodyBold.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorVioletLight,
            ),
        )
    }
}

// Shared with GStore.IconBackground / picker's SelectedViolet — kept local
// (rather than moved to AppColors) to avoid ballooning the palette until we
// have a second consumer inside a bottom sheet.
private val PremiumPillBackground = androidx.compose.ui.graphics.Color(0xFFEFE3F0)
private val SelectedFontHighlight = ColorVioletLight.copy(alpha = 0.08f)
