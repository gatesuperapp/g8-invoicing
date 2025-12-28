package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun FormInputCreatorListPicker(
    input: ListPicker
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (input.selectedItems.isEmpty()) {
            Text(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .weight(1F),
                text = "Sélectionner...",
            )
        } else if (input.selectedItems.size <= 3) {
            FlowRow(
                modifier = Modifier
                    .weight(1F)
                    .padding(end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                input.selectedItems.forEach { clientRef ->
                    InputChip(
                        selected = false,
                        onClick = { input.onClick?.invoke() },
                        label = { Text(clientRef.name) },
                        trailingIcon = {
                            IconButton(
                                onClick = { input.onRemoveItem?.invoke(clientRef.id) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Supprimer ${clientRef.name}",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = Color(0xFFF5F5F5),
                            disabledContainerColor = Color(0xFFF5F5F5)
                        ),
                        border = null
                    )
                }
            }
        } else {
            Text(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .weight(1F),
                text = "${input.selectedItems.size} clients sélectionnés",
            )
        }
    }
}