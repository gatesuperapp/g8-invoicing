package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.a4a.g8invoicing.ui.states.DocumentState

/**
 * Android implementation of ExportPdfPlatform.
 * Note: This is a placeholder - the actual Android app uses ExportPdf from the app module,
 * which is passed via the exportPdfContent parameter in NavGraph.
 * This actual function is only here to satisfy the expect/actual contract.
 */
@Composable
actual fun ExportPdfPlatform(
    document: DocumentState,
    onDismissRequest: () -> Unit,
) {
    // This should never be shown since Android app uses ExportPdf from app module
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Export PDF - Use app module implementation",
            color = Color.White
        )
    }
}
