package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textBodySmall

// Non-interactive header block shown above a group of product rows that share the
// same source document (delivery note / quote), or above the free-standing
// "Autres lignes" group. Outlined (no fill) to visually differentiate it from the
// clickable product rows underneath. The trailing eye toggles visibility of ALL
// header blocks in the preview + PDF at once — all icons on the screen mirror the
// same state, and clicking any one flips them together.
@Composable
fun DocumentBottomSheetProductListSourceBlock(
    docNumber: String,
    date: String?,
    isHidden: Boolean,
    onToggleHidden: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val dateText = date?.substringBefore(" ")?.takeIf { it.isNotBlank() }
    val label = if (dateText != null) "$docNumber — $dateText" else docNumber
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.divider, RectangleShape)
            .padding(
                // Align the label with the drag-handle column of the product
                // rows underneath (Box.padding(8.dp) around the DragHandle
                // icon), instead of indenting it further in.
                start = 8.dp,
                end = 5.dp,
                top = 5.dp,
                bottom = 5.dp
            ),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            style = MaterialTheme.typography.textBodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (isHidden) AppColors.textMuted else AppColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (onToggleHidden != null) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onToggleHidden() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = if (isHidden) Icons.Outlined.VisibilityOff
                    else Icons.Outlined.Visibility,
                    contentDescription = if (isHidden) "Show source header in preview"
                    else "Hide source header in preview",
                    tint = AppColors.iconSecondary,
                )
            }
        }
    }
}
