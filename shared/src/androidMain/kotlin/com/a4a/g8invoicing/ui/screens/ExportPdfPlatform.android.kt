package com.a4a.g8invoicing.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.alert_dialog_error
import com.a4a.g8invoicing.shared.resources.alert_dialog_error_confirm
import com.a4a.g8invoicing.shared.resources.credit_note_number
import com.a4a.g8invoicing.shared.resources.currency
import com.a4a.g8invoicing.shared.resources.delivery_note_number
import com.a4a.g8invoicing.shared.resources.document_date_label
import com.a4a.g8invoicing.shared.resources.document_reference_label
import com.a4a.g8invoicing.shared.resources.export_clickable_text
import com.a4a.g8invoicing.shared.resources.export_close
import com.a4a.g8invoicing.shared.resources.export_done
import com.a4a.g8invoicing.shared.resources.export_done_file_location
import com.a4a.g8invoicing.shared.resources.export_email_subject
import com.a4a.g8invoicing.shared.resources.export_email_subject_credit_note
import com.a4a.g8invoicing.shared.resources.export_email_subject_delivery_note
import com.a4a.g8invoicing.shared.resources.export_email_subject_invoice
import com.a4a.g8invoicing.shared.resources.export_error
import com.a4a.g8invoicing.shared.resources.export_error_sharing
import com.a4a.g8invoicing.shared.resources.export_info_popup
import com.a4a.g8invoicing.shared.resources.export_info_popup_validate
import com.a4a.g8invoicing.shared.resources.export_ongoing
import com.a4a.g8invoicing.shared.resources.export_permission_denied
import com.a4a.g8invoicing.shared.resources.export_send_file
import com.a4a.g8invoicing.shared.resources.export_send_file_content
import com.a4a.g8invoicing.shared.resources.export_share_file
import com.a4a.g8invoicing.shared.resources.export_waiting_permission
import com.a4a.g8invoicing.shared.resources.invoice_number
import com.a4a.g8invoicing.shared.resources.invoice_paid
import com.a4a.g8invoicing.shared.resources.invoice_pdf_due_date
import com.a4a.g8invoicing.shared.resources.label_separator
import com.a4a.g8invoicing.shared.resources.ok
import com.a4a.g8invoicing.shared.resources.table_description
import com.a4a.g8invoicing.shared.resources.table_quantity
import com.a4a.g8invoicing.shared.resources.table_tax_rate
import com.a4a.g8invoicing.shared.resources.table_total_price
import com.a4a.g8invoicing.shared.resources.table_unit
import com.a4a.g8invoicing.shared.resources.table_unit_price
import com.a4a.g8invoicing.shared.resources.total_with_tax
import com.a4a.g8invoicing.shared.resources.total_without_tax
import com.a4a.g8invoicing.shared.resources.vat
import com.a4a.g8invoicing.ui.shared.AlertDialogErrorOrInfo
import com.a4a.g8invoicing.ui.shared.AndroidPdfContext
import com.a4a.g8invoicing.ui.shared.DocumentType
import com.a4a.g8invoicing.ui.shared.PdfFileManager
import com.a4a.g8invoicing.ui.shared.PdfGenerator
import com.a4a.g8invoicing.ui.shared.PdfStrings
import com.a4a.g8invoicing.ui.states.DocumentState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

enum class ExportStatus {
    WAITING_PERMISSION, ONGOING, DONE, ERROR
}

/**
 * Android implementation of ExportPdfPlatform.
 * Uses the shared PdfGenerator from androidMain.
 */
@Composable
actual fun ExportPdfPlatform(
    document: DocumentState,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    var exportStatus by remember { mutableStateOf(ExportStatus.WAITING_PERMISSION) }
    var finalFileName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Set Android context for PdfFileManager
    LaunchedEffect(Unit) {
        AndroidPdfContext.context = context
    }

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

    // Strings for UI
    val strWaitingPermission = stringResource(Res.string.export_waiting_permission)
    val strOngoing = stringResource(Res.string.export_ongoing)
    val strDone = stringResource(Res.string.export_done)
    val strError = stringResource(Res.string.export_error)
    val strPermissionDenied = stringResource(Res.string.export_permission_denied)
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
    val strEmailSubjectDeliveryNote = stringResource(Res.string.export_email_subject_delivery_note)
    val strEmailSubjectCreditNote = stringResource(Res.string.export_email_subject_credit_note)
    val strEmailSubject = stringResource(Res.string.export_email_subject, "%1\$s", "%2\$s")
    val strEmailContent = stringResource(Res.string.export_send_file_content)
    val strOk = stringResource(Res.string.ok)

    val openErrorDialog = remember { mutableStateOf(false) }
    val openInfoDialog = remember { mutableStateOf(false) }
    var showShareError by remember { mutableStateOf(false) }

    // On Android 10+ (API 29+), no permissions needed (MediaStore)
    val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    // Check permissions (only for Android 9 and below)
    val hasPermissions = remember {
        if (!needsPermission) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Permission launcher (only for Android 9 and below)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            exportStatus = ExportStatus.ONGOING
        } else {
            errorMessage = strPermissionDenied
            openErrorDialog.value = true
            exportStatus = ExportStatus.ERROR
        }
    }

    // Request permissions or start export on launch
    LaunchedEffect(Unit) {
        if (hasPermissions) {
            exportStatus = ExportStatus.ONGOING
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    // Error dialog
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
            confirmationText = strAlertErrorConfirm
        )
    }

    // Info dialog
    if (openInfoDialog.value) {
        AlertDialogErrorOrInfo(
            onDismissRequest = { openInfoDialog.value = false },
            onConfirmation = { openInfoDialog.value = false },
            message = strInfoPopup,
            confirmationText = strInfoPopupValidate
        )
    }

    // Share error dialog
    if (showShareError) {
        AlertDialogErrorOrInfo(
            onDismissRequest = { showShareError = false },
            onConfirmation = { showShareError = false },
            message = strErrorSharing,
            confirmationText = strOk
        )
    }

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
                text = strClose,
                color = Color.White
            )
        }

        Text(
            modifier = Modifier.padding(bottom = 30.dp),
            text = when (exportStatus) {
                ExportStatus.WAITING_PERMISSION -> strWaitingPermission
                ExportStatus.ONGOING -> strOngoing
                ExportStatus.DONE -> strDone
                ExportStatus.ERROR -> strError
            },
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        // Launch export when permissions are granted
        if (exportStatus == ExportStatus.ONGOING) {
            val fileManager = remember { PdfFileManager() }

            LaunchedEffect(Unit) {
                launch(Dispatchers.Default) {
                    try {
                        val pdfGenerator = PdfGenerator(strings, fileManager)
                        finalFileName = pdfGenerator.generatePdf(document)
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
                color = Color.White
            )

            ClickableText(
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 50.dp),
                onClick = { openInfoDialog.value = true },
                style = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp
                ),
                text = AnnotatedString(strClickableText),
            )

            // Send email button
            SendEmailButton(
                context = context,
                document = document,
                finalFileName = finalFileName,
                strSendFile = strSendFile,
                strEmailSubjectInvoice = strEmailSubjectInvoice,
                strEmailSubjectDeliveryNote = strEmailSubjectDeliveryNote,
                strEmailSubjectCreditNote = strEmailSubjectCreditNote,
                strEmailSubject = strEmailSubject,
                strEmailContent = strEmailContent,
                onError = { showShareError = true }
            )

            // Share button
            ShareButton(
                context = context,
                finalFileName = finalFileName,
                strShareFile = strShareFile,
                onError = { showShareError = true }
            )
        }
    }
}

@Composable
private fun SendEmailButton(
    context: Context,
    document: DocumentState,
    finalFileName: String,
    strSendFile: String,
    strEmailSubjectInvoice: String,
    strEmailSubjectDeliveryNote: String,
    strEmailSubjectCreditNote: String,
    strEmailSubject: String,
    strEmailContent: String,
    onError: () -> Unit
) {
    val fileManager = remember { PdfFileManager() }

    Button(onClick = {
        try {
            val type = when (document.documentType) {
                DocumentType.INVOICE -> strEmailSubjectInvoice
                DocumentType.DELIVERY_NOTE -> strEmailSubjectDeliveryNote
                DocumentType.CREDIT_NOTE -> strEmailSubjectCreditNote
                else -> ""
            }

            val uri = fileManager.getFileUri(finalFileName)
            uri?.let {
                // Get all client email addresses
                val emailAddresses = document.documentClient?.emails
                    ?.map { email -> email.email.text }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()

                composeEmail(
                    addresses = emailAddresses,
                    documentNumber = document.documentNumber.text,
                    emailSubject = strEmailSubject
                        .replace("%1\$s", type)
                        .replace("%2\$s", document.documentNumber.text),
                    emailMessage = strEmailContent.replace("%1\$s", type),
                    attachedDocumentUri = it,
                    context = context
                )
            } ?: onError()
        } catch (e: Exception) {
            onError()
        }
    }) {
        Icon(imageVector = Icons.Outlined.Email, contentDescription = null)
        Text(
            strSendFile,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun composeEmail(
    addresses: List<String> = emptyList(),
    documentNumber: String? = null,
    emailSubject: String,
    emailMessage: String,
    attachedDocumentUri: Uri? = null,
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
        attachedDocumentUri?.let {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(listOf(it)))
        }

        ContextCompat.startActivity(context, intent, null)
    } catch (e: Exception) {
        // Silently fail - error will be shown via onError callback
    }
}

@Composable
private fun ShareButton(
    context: Context,
    finalFileName: String,
    strShareFile: String,
    onError: () -> Unit
) {
    val fileManager = remember { PdfFileManager() }

    Button(onClick = {
        try {
            val uri = fileManager.getFileUri(finalFileName)
            uri?.let {
                ShareCompat.IntentBuilder(context)
                    .setType("application/pdf")
                    .addStream(uri)
                    .setChooserTitle("Share document")
                    .setSubject("Shared document")
                    .startChooser()
            } ?: onError()
        } catch (e: Exception) {
            onError()
        }
    }) {
        Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
        Text(
            strShareFile,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
