package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun FormInputCreatorListPicker(
    input: ListPicker,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (input.selectedItems.isEmpty()) {
            Text(
                text = "Sélectionner...",
            )
        } else if (input.selectedItems.size <= 3) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                input.selectedItems.forEach { clientRef ->
                    CustomChip(
                        text = clientRef.name,
                        onChipClick = { input.onClick?.invoke() },
                        onRemoveClick = { input.onRemoveItem?.invoke(clientRef.id) }
                    )
                }
            }
        } else {
            Text(
                text = "${input.selectedItems.size} clients sélectionnés",
            )
        }
    }
}

@Composable
fun CustomChip(
    text: String,
    onChipClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
            .clickable { onChipClick() }
            .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f, fill = false)
        )
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Supprimer $text",
            modifier = Modifier
                .padding(start = 4.dp)
                .size(18.dp)
                .clickable { onRemoveClick() },
            tint = Color.Gray
        )
    }
}
