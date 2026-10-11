package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.screens.shared.resolveCategoryColor
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textSecondary

@Composable
fun FormInputCreatorGoForward(
    forwardInput: ForwardElement
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (forwardInput.chipColor != null) {
            // Colour chip variant — same corner radius as the email chip in
            // the client form (RoundedCornerShape 8.dp). Chip is only as wide
            // as its content; the weighted Spacer below pushes the arrow to
            // the far right of the row.
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(resolveCategoryColor(forwardInput.chipColor))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = forwardInput.text,
                    style = MaterialTheme.typography.textSecondary.copy(
                        color = Color(0xFF1A1A1A),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.weight(1F))
        } else {
            Text(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .weight(1F)
                    .fillMaxWidth(),
                text = forwardInput.text,
                maxLines = forwardInput.maxLines
                    ?: if (!forwardInput.isMultiline) 1 else 10,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (forwardInput.displayArrow)
            Icon(
                modifier = Modifier
                    .width(15.dp)
                    .padding(bottom = 3.dp),
                tint = AppColors.iconSecondary,
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Right arrow"
            )
    }
}
