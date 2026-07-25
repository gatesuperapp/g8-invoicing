package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.textBodySmall

// Inert header block shown above a group of product rows that share the same source
// document (delivery note or quote). Displays "<docNumber> — <date>" so users can tie
// each product row back to its origin. Not draggable, not clickable.
@Composable
fun DocumentBottomSheetProductListSourceBlock(
    docNumber: String,
    date: String?,
    modifier: Modifier = Modifier,
) {
    val dateText = date?.substringBefore(" ")?.takeIf { it.isNotBlank() }
    val label = if (dateText != null) "$docNumber — $dateText" else docNumber
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFFF5F5F5))
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 5.dp,
                bottom = 5.dp
            ),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.textBodySmall.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
