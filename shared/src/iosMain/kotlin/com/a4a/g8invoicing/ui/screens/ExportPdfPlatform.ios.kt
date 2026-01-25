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
 * iOS implementation - TODO: Implement with PDFKit
 */
@Composable
actual fun ExportPdfPlatform(
    document: DocumentState,
    onDismissRequest: () -> Unit,
) {
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
            text = "Export PDF bientôt disponible sur iOS",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}
