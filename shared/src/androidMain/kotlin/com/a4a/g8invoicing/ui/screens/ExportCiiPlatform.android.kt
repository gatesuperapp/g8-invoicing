package com.a4a.g8invoicing.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.data.models.PaymentMeans
import com.a4a.g8invoicing.data.models.defaultPaymentBankSegments
import com.a4a.g8invoicing.data.models.flattenPaymentBank
import com.a4a.g8invoicing.facturx.CiiXmlBuilder
import com.a4a.g8invoicing.facturx.CiiXmlFileManager
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.alert_dialog_error
import com.a4a.g8invoicing.shared.resources.alert_dialog_error_confirm
import com.a4a.g8invoicing.shared.resources.export_clickable_text
import com.a4a.g8invoicing.shared.resources.export_close
import com.a4a.g8invoicing.shared.resources.export_done
import com.a4a.g8invoicing.shared.resources.export_done_file_location
import com.a4a.g8invoicing.shared.resources.export_email_subject
import com.a4a.g8invoicing.shared.resources.export_email_subject_invoice
import com.a4a.g8invoicing.shared.resources.export_error
import com.a4a.g8invoicing.shared.resources.export_error_sharing
import com.a4a.g8invoicing.shared.resources.export_info_popup
import com.a4a.g8invoicing.shared.resources.export_info_popup_validate
import com.a4a.g8invoicing.shared.resources.export_ongoing
import com.a4a.g8invoicing.shared.resources.export_send_file
import com.a4a.g8invoicing.shared.resources.export_send_file_content
import com.a4a.g8invoicing.shared.resources.export_share_file
import com.a4a.g8invoicing.shared.resources.issuer_bank_identifier_generic
import com.a4a.g8invoicing.shared.resources.issuer_bank_identifier_iban
import com.a4a.g8invoicing.shared.resources.ok
import com.a4a.g8invoicing.ui.shared.AlertDialogErrorOrInfo
import com.a4a.g8invoicing.ui.shared.AndroidPdfContext
import com.a4a.g8invoicing.ui.states.InvoiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Android CII XML export screen. Same three-state black-screen UX as
 * [ExportPdfPlatform] (ONGOING → DONE + file location + email/share
 * buttons), but skips the PDF/permission code path since MediaStore
 * writes on API 29+ don't need runtime permissions and the pre-29
 * external-storage write path is guarded inside CiiXmlFileManager.
 */
@Composable
actual fun ExportCiiPlatform(
    invoice: InvoiceState,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    var exportStatus by remember { mutableStateOf(ExportStatus.ONGOING) }
    var finalFileName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        AndroidPdfContext.context = context
    }

    BackHandler(onBack = onDismissRequest)

    // Locale-resolved labels the CII builder needs to flatten the two
     // payment-related text blocks (accepted-methods list + bank details)
     // into <ram:Information>. Same lookup pattern as the PDF path.
    val paymentMeansLabels: Map<String, String> = PaymentMeans.entries.associate {
        it.chipId to stringResource(it.labelRes)
    }
    val bankIbanLabel = stringResource(Res.string.issuer_bank_identifier_iban)
    val bankGenericLabel = stringResource(Res.string.issuer_bank_identifier_generic)

    val strOngoing = stringResource(Res.string.export_ongoing)
    val strDone = stringResource(Res.string.export_done)
    val strError = stringResource(Res.string.export_error)
    val strFileLocation = stringResource(Res.string.export_done_file_location)
    val strClickableText = stringResource(Res.string.export_clickable_text)
    val strInfoPopup = stringResource(Res.string.export_info_popup)
    val strInfoPopupValidate = stringResource(Res.string.export_info_popup_validate)
    val strSendFile = stringResource(Res.string.export_send_file)
    val strShareFile = stringResource(Res.string.export_share_file)
    val strClose = stringResource(Res.string.export_close)
    val strErrorSharing = stringResource(Res.string.export_error_sharing)
    val strAlertError = stringResource(Res.string.alert_dialog_error)
    val strAlertErrorConfirm = stringResource(Res.string.alert_dialog_error_confirm)
    val strEmailSubjectInvoice = stringResource(Res.string.export_email_subject_invoice)
    val strEmailSubject = stringResource(Res.string.export_email_subject, "%1\$s", "%2\$s")
    val strEmailContent = stringResource(Res.string.export_send_file_content)
    val strOk = stringResource(Res.string.ok)

    val openErrorDialog = remember { mutableStateOf(false) }
    val openInfoDialog = remember { mutableStateOf(false) }
    var showShareError by remember { mutableStateOf(false) }

    if (openErrorDialog.value) {
        AlertDialogErrorOrInfo(
            onDismissRequest = {
                openErrorDialog.value = false
                onDismissRequest()
            },
            onConfirmation = {
                openErrorDialog.value = false
                onDismissRequest()
            },
            message = strAlertError + errorMessage,
            confirmationText = strAlertErrorConfirm,
        )
    }

    if (openInfoDialog.value) {
        AlertDialogErrorOrInfo(
            onDismissRequest = { openInfoDialog.value = false },
            onConfirmation = { openInfoDialog.value = false },
            message = strInfoPopup,
            confirmationText = strInfoPopupValidate,
        )
    }

    if (showShareError) {
        AlertDialogErrorOrInfo(
            onDismissRequest = { showShareError = false },
            onConfirmation = { showShareError = false },
            message = strErrorSharing,
            confirmationText = strOk,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Spacer(Modifier.weight(1f))
            Text(
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .clickable { onDismissRequest() },
                text = strClose,
                color = Color.White,
            )
        }

        Text(
            modifier = Modifier.padding(bottom = 30.dp),
            text = when (exportStatus) {
                ExportStatus.WAITING_PERMISSION -> strOngoing
                ExportStatus.ONGOING -> strOngoing
                ExportStatus.DONE -> strDone
                ExportStatus.ERROR -> strError
            },
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
        )

        if (exportStatus == ExportStatus.ONGOING) {
            val fileManager = remember { CiiXmlFileManager() }

            LaunchedEffect(Unit) {
                launch(Dispatchers.Default) {
                    try {
                        val bankInfoText = buildBankInfoText(
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
                        exportStatus = ExportStatus.DONE
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Unknown error"
                        exportStatus = ExportStatus.ERROR
                        openErrorDialog.value = true
                    }
                }
            }

            LinearProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        if (exportStatus == ExportStatus.DONE) {
            Text(
                modifier = Modifier.padding(bottom = 5.dp),
                text = strFileLocation,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
            )

            ClickableText(
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 50.dp),
                onClick = { openInfoDialog.value = true },
                style = TextStyle(color = Color.White, fontSize = 18.sp),
                text = AnnotatedString(strClickableText),
            )

            CiiSendEmailButton(
                context = context,
                invoice = invoice,
                finalFileName = finalFileName,
                strSendFile = strSendFile,
                strEmailSubjectInvoice = strEmailSubjectInvoice,
                strEmailSubject = strEmailSubject,
                strEmailContent = strEmailContent,
                onError = { showShareError = true },
            )

            CiiShareButton(
                context = context,
                finalFileName = finalFileName,
                strShareFile = strShareFile,
                onError = { showShareError = true },
            )
        }
    }
}

@Composable
private fun CiiSendEmailButton(
    context: Context,
    invoice: InvoiceState,
    finalFileName: String,
    strSendFile: String,
    strEmailSubjectInvoice: String,
    strEmailSubject: String,
    strEmailContent: String,
    onError: () -> Unit,
) {
    val fileManager = remember { CiiXmlFileManager() }

    Button(onClick = {
        try {
            val uri = fileManager.getFileUri(finalFileName)
            uri?.let {
                val emailAddresses = invoice.documentClient?.emails
                    ?.map { email -> email.email.text }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()

                composeCiiEmail(
                    addresses = emailAddresses,
                    documentNumber = invoice.documentNumber.text,
                    emailSubject = strEmailSubject
                        .replace("%1\$s", strEmailSubjectInvoice)
                        .replace("%2\$s", invoice.documentNumber.text),
                    emailMessage = strEmailContent.replace("%1\$s", strEmailSubjectInvoice),
                    attachedDocumentUri = it,
                    context = context,
                )
            } ?: onError()
        } catch (_: Exception) {
            onError()
        }
    }) {
        Icon(imageVector = Icons.Outlined.Email, contentDescription = null)
        Text(strSendFile, modifier = Modifier.padding(start = 8.dp))
    }
}

private fun composeCiiEmail(
    addresses: List<String>,
    documentNumber: String?,
    emailSubject: String,
    emailMessage: String,
    attachedDocumentUri: Uri,
    context: Context,
) {
    try {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.selector = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
        intent.putExtra(Intent.EXTRA_EMAIL, addresses.toTypedArray())
        if (documentNumber != null) {
            intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
        }
        intent.putExtra(Intent.EXTRA_TEXT, emailMessage)
        intent.putParcelableArrayListExtra(
            Intent.EXTRA_STREAM,
            ArrayList(listOf(attachedDocumentUri)),
        )
        ContextCompat.startActivity(context, intent, null)
    } catch (_: Exception) {
        // Silent — the caller shows an error dialog via onError.
    }
}

@Composable
private fun CiiShareButton(
    context: Context,
    finalFileName: String,
    strShareFile: String,
    onError: () -> Unit,
) {
    val fileManager = remember { CiiXmlFileManager() }

    Button(onClick = {
        try {
            val uri = fileManager.getFileUri(finalFileName)
            uri?.let {
                ShareCompat.IntentBuilder(context)
                    .setType("application/xml")
                    .addStream(uri)
                    .setChooserTitle("Share CII XML")
                    .setSubject("Shared invoice (CII XML)")
                    .startChooser()
            } ?: onError()
        } catch (_: Exception) {
            onError()
        }
    }) {
        Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
        Text(strShareFile, modifier = Modifier.padding(start = 8.dp))
    }
}

/**
 * Flatten the invoice's bank-details block into a plain string the CII
 * builder can drop into `<ram:Information>`. Mirrors PdfGeneratorImpl's
 * `buildBankInfoText` — same identifier-label selection (IBAN vs generic
 * account label based on the payment country), same default-segments
 * fallback when the user hasn't customised the text. Returns null when
 * the block is hidden, empty, or the issuer has no IBAN/BIC on file so
 * the caller doesn't emit an empty Information block.
 */
private fun buildBankInfoText(
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
