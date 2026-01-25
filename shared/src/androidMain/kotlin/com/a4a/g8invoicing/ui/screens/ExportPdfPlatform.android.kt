package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.states.DocumentState

/**
 * Android implementation - delegates to the existing ExportPdf in app module.
 * This is a placeholder that shows a message to use the app module's ExportPdf.
 *
 * Note: The actual ExportPdf with full functionality (permissions, sharing, email)
 * is in app/src/main/.../ExportPdf.kt and should be passed via the exportPdfContent slot.
 */
@Composable
actual fun ExportPdfPlatform(
    document: DocumentState,
    onDismissRequest: () -> Unit,
) {
    // This is a fallback - the actual implementation should be provided by the app module
    // via the exportPdfContent slot in DocumentAddEdit
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(14.dp)
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .clickable { onDismissRequest() },
                text = "Fermer",
                color = Color.White
            )
        }

        Text(
            modifier = Modifier.padding(16.dp),
            text = "Export PDF non disponible dans ce contexte.\nUtilisez l'export depuis l'application principale.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}
