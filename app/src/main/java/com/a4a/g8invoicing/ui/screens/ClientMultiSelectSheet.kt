package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.states.ClientRef

// Used in additional product prices client selection
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientMultiSelectSheet(
    allClients: List<ClientRef>,
    selectedClients: List<ClientRef>,
    onToggleClient: (ClientRef) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {

            Text(
                text = stringResource(R.string.client_selection_title),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            if (allClients.isEmpty()) {
                // Message quand il n'y a pas de clients
                Text(
                    text = stringResource(R.string.client_selection_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // Ordre figé à l'ouverture - clients sélectionnés en premier
                val sortedClients = remember(allClients) {
                    val initialSelectedIds = selectedClients.map { it.id }.toSet()
                    allClients.sortedByDescending { client ->
                        initialSelectedIds.contains(client.id)
                    }
                }
                Column(
                    Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    sortedClients.forEach { client ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleClient(client) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedClients.any { it.id == client.id },
                                onCheckedChange = { onToggleClient(client) }
                            )
                            Text(text = client.name)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.client_selection_validate))
            }
        }
    }
}
