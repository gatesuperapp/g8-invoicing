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
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.client_selection_all_used
import com.a4a.g8invoicing.shared.resources.client_selection_empty
import com.a4a.g8invoicing.shared.resources.client_selection_title
import com.a4a.g8invoicing.shared.resources.client_selection_validate
import com.a4a.g8invoicing.ui.states.ClientRef
import org.jetbrains.compose.resources.stringResource

// Used in additional product prices client selection
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientMultiSelectSheet(
    allClients: List<ClientRef>,          // Clients disponibles (filtrés)
    totalClientsInDatabase: Int,          // Nombre total de clients en BDD
    selectedClients: List<ClientRef>,
    onToggleClient: (ClientRef) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {

            Text(
                text = stringResource(Res.string.client_selection_title),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            if (totalClientsInDatabase == 0) {
                // Aucun client en base de données
                Text(
                    text = stringResource(Res.string.client_selection_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else if (allClients.isEmpty()) {
                // Des clients existent mais tous sont déjà assignés à d'autres prix
                Text(
                    text = stringResource(Res.string.client_selection_all_used),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // Ordre figé à l'ouverture - clients sélectionnés en premier, puis alphabétique
                val sortedClients = remember(allClients) {
                    val initialSelectedIds = selectedClients.map { it.id }.toSet()
                    allClients.sortedWith(
                        compareByDescending<ClientRef> { initialSelectedIds.contains(it.id) }
                            .thenBy { it.name.lowercase() }
                    )
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
                                .clickable { onToggleClient(client) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedClients.any { it.id == client.id },
                                onCheckedChange = { onToggleClient(client) }
                            )
                            Text(text = listOfNotNull(client.name, client.firstName).joinToString(" "))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onConfirm
            ) {
                Text(stringResource(Res.string.client_selection_validate))
            }
        }
    }
}
