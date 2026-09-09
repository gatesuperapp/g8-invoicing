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
import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.data.models.PaymentMeans
import com.a4a.g8invoicing.data.models.defaultPaymentBankSegments
import com.a4a.g8invoicing.data.models.flattenPaymentBank
import com.a4a.g8invoicing.facturx.CiiXmlBuilder
import com.a4a.g8invoicing.facturx.CiiXmlFileManager
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.issuer_bank_identifier_generic
import com.a4a.g8invoicing.shared.resources.issuer_bank_identifier_iban
import com.a4a.g8invoicing.ui.states.InvoiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import java.awt.Desktop
import java.io.File

@Composable
actual fun ExportCiiPlatform(
    invoice: InvoiceState,
    onDismissRequest: () -> Unit,
) {
    var exportStatus by remember { mutableStateOf(ExportStatusDesktop.ONGOING) }
    var finalFileName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val fileManager = remember { CiiXmlFileManager() }
    val paymentMeansLabels: Map<String, String> = PaymentMeans.entries.associate {
        it.chipId to stringResource(it.labelRes)
    }
    val bankIbanLabel = stringResource(Res.string.issuer_bank_identifier_iban)
    val bankGenericLabel = stringResource(Res.string.issuer_bank_identifier_generic)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 300.dp, max = 400.dp)
                .background(Color.DarkGray, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(modifier = Modifier.align(Alignment.End)) {
                Text(
                    modifier = Modifier
                        .clickable { onDismissRequest() }
                        .padding(8.dp),
                    text = "✕",
                    color = Color.White,
                )
            }

            Text(
                text = when (exportStatus) {
                    ExportStatusDesktop.ONGOING -> "Export en cours..."
                    ExportStatusDesktop.DONE -> "Export terminé !"
                    ExportStatusDesktop.ERROR -> "Erreur lors de l'export"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )

            if (exportStatus == ExportStatusDesktop.ONGOING) {
                LaunchedEffect(Unit) {
                    launch(Dispatchers.Default) {
                        try {
                            val bankInfoText = buildBankInfoTextDesktop(
                                invoice = invoice,
                                ibanLabel = bankIbanLabel,
                                genericLabel = bankGenericLabel,
                            )
                            val xml = CiiXmlBuilder.build(
                                invoice = invoice,
                                paymentMeansLabels = paymentMeansLabels,
                                bankInfoText = bankInfoText,
                            )
                            val baseName = invoice.documentNumber.text
                                .trim()
                                .ifEmpty { "invoice" }
                                .replace(Regex("[^A-Za-z0-9._-]"), "_")
                            finalFileName = fileManager.writeXml("$baseName.xml", xml)
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
                    color = Color.LightGray,
                )

                Text(
                    text = finalFileName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )

                Button(onClick = {
                    try {
                        val folder = File(System.getProperty("user.home"), "Documents/g8")
                        if (folder.exists() && Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(folder)
                        }
                    } catch (_: Throwable) {
                        // No-op — the file is on disk regardless.
                    }
                }) {
                    Icon(imageVector = Icons.Outlined.FolderOpen, contentDescription = null)
                    Text("Ouvrir le dossier", modifier = Modifier.padding(start = 8.dp))
                }
            }

            if (exportStatus == ExportStatusDesktop.ERROR) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red,
                )
            }
        }
    }
}

/**
 * Flatten the invoice's bank-details block so CiiXmlBuilder can drop it
 * into `<ram:Information>`. Mirrors the Android helper + PdfGeneratorImpl:
 * picks the IBAN or generic account label based on the account country,
 * falls back to the default segments when the user hasn't customised the
 * text, returns null when there's nothing to say (hidden, no IBAN/BIC).
 */
private fun buildBankInfoTextDesktop(
    invoice: InvoiceState,
    ibanLabel: String,
    genericLabel: String,
): String? {
    if (invoice.paymentBankHidden) return null
    val issuer = invoice.documentIssuer ?: return null
    val iban = issuer.paymentIban?.text?.trim().orEmpty()
    val bic = issuer.paymentBic?.text?.trim().orEmpty()
    if (iban.isEmpty() && bic.isEmpty()) return null
    val identifierLabel = if (
        CountryCodes.isIbanCountry(issuer.paymentCountry) || issuer.paymentCountry == null
    ) ibanLabel else genericLabel
    val segments = invoice.paymentBankSegments.ifEmpty { defaultPaymentBankSegments() }
    return flattenPaymentBank(segments, identifierLabel, iban, bic)
        .takeIf { it.isNotEmpty() }
}
