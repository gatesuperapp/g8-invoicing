package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.credit_note_number
import com.a4a.g8invoicing.shared.resources.currency
import com.a4a.g8invoicing.shared.resources.delivery_note_number
import com.a4a.g8invoicing.shared.resources.document_date_label
import com.a4a.g8invoicing.shared.resources.invoice_pdf_due_date
import com.a4a.g8invoicing.shared.resources.document_reference_label
import com.a4a.g8invoicing.shared.resources.invoice_number
import com.a4a.g8invoicing.shared.resources.invoice_paid
import com.a4a.g8invoicing.shared.resources.label_separator
import com.a4a.g8invoicing.shared.resources.table_description
import com.a4a.g8invoicing.shared.resources.table_quantity
import com.a4a.g8invoicing.shared.resources.table_tax_rate
import com.a4a.g8invoicing.shared.resources.table_total_price
import com.a4a.g8invoicing.shared.resources.table_unit
import com.a4a.g8invoicing.shared.resources.table_unit_price
import com.a4a.g8invoicing.shared.resources.total_without_tax
import com.a4a.g8invoicing.shared.resources.total_with_tax
import com.a4a.g8invoicing.shared.resources.vat
import com.a4a.g8invoicing.ui.shared.PdfFileManager
import com.a4a.g8invoicing.ui.shared.PdfGenerator
import com.a4a.g8invoicing.ui.shared.PdfStrings
import com.a4a.g8invoicing.ui.states.DocumentState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

enum class ExportStatusDesktop {
    ONGOING, DONE, ERROR
}

@Composable
actual fun ExportPdfPlatform(
    document: DocumentState,
    onDismissRequest: () -> Unit,
) {
    var exportStatus by remember { mutableStateOf(ExportStatusDesktop.ONGOING) }
    var finalFileName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Collect all strings from Compose Resources
    val strings = PdfStrings(
        invoiceNumber = stringResource(Res.string.invoice_number),
        deliveryNoteNumber = stringResource(Res.string.delivery_note_number),
        creditNoteNumber = stringResource(Res.string.credit_note_number),
        documentDate = stringResource(Res.string.document_date_label),
        documentReference = stringResource(Res.string.document_reference_label),
        tableDescription = stringResource(Res.string.table_description),
        tableQuantity = stringResource(Res.string.table_quantity),
        tableUnit = stringResource(Res.string.table_unit),
        tableTaxRate = stringResource(Res.string.table_tax_rate),
        tableUnitPrice = stringResource(Res.string.table_unit_price),
        tableTotalPrice = stringResource(Res.string.table_total_price),
        totalWithoutTax = stringResource(Res.string.total_without_tax),
        totalWithTax = stringResource(Res.string.total_with_tax),
        tax = stringResource(Res.string.vat),
        dueDate = stringResource(Res.string.invoice_pdf_due_date),
        currency = stringResource(Res.string.currency),
        invoicePaid = stringResource(Res.string.invoice_paid),
        labelSeparator = stringResource(Res.string.label_separator),
    )

    val fileManager = remember { PdfFileManager() }

    // Full screen semi-transparent background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 300.dp, max = 400.dp)
                .background(Color.DarkGray, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    modifier = Modifier
                        .clickable { onDismissRequest() }
                        .padding(8.dp),
                    text = "✕",
                    color = Color.White
                )
            }

            Text(
                text = when (exportStatus) {
                    ExportStatusDesktop.ONGOING -> "Export en cours..."
                    ExportStatusDesktop.DONE -> "Export terminé !"
                    ExportStatusDesktop.ERROR -> "Erreur lors de l'export"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )

            if (exportStatus == ExportStatusDesktop.ONGOING) {
                LaunchedEffect(Unit) {
                    launch(Dispatchers.Default) {
                        try {
                            val pdfGenerator = PdfGenerator(strings, fileManager)
                            finalFileName = pdfGenerator.generatePdf(document)
                            exportStatus = ExportStatusDesktop.DONE
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Unknown error"
                            exportStatus = ExportStatusDesktop.ERROR
                            e.printStackTrace()
                        }
                    }
                }

                LinearProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            if (exportStatus == ExportStatusDesktop.DONE) {
                Text(
                    text = "Fichier enregistré dans Documents/g8/",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )

                Text(
                    text = finalFileName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Button(onClick = { fileManager.openOrShare(finalFileName) }) {
                    Icon(imageVector = Icons.Outlined.FolderOpen, contentDescription = null)
                    Text(
                        "Ouvrir le dossier",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (exportStatus == ExportStatusDesktop.ERROR) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red
                )
            }
        }
    }
}
