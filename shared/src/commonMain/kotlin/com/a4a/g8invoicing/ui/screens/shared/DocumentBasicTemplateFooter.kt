package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_payment_section_title
import com.a4a.g8invoicing.shared.resources.invoice_pdf_due_date
import com.a4a.g8invoicing.shared.resources.issuer_bank_identifier_generic
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsBold
import org.jetbrains.compose.resources.stringResource

// Size ladder anchored to the totals block (which uses textForDocuments at
// 6sp): payment box shares the same base for header + values, terms +
// watermark drop one step below.
private val SmallSize = 5.sp
private val SignatureSize = 5.sp
private val MentionColor = Color(0xFF666666)
private val SignatureColor = Color(0xFF888888)
private val SeparatorColor = Color(0xFFE0E0E0)
private val PaymentBoxColor = Color(0xFFF5F5F5)

@Composable
fun DocumentBasicTemplateFooter(
    document: DocumentState,
    onClickElement: (ScreenElement) -> Unit,
    labels: Map<String, String>? = null,
) {
    // The watermark string is frozen at document creation in the DB (watermark_text
    // column). Display whatever is stored — toggling the module later doesn't change
    // existing docs. null/blank = no watermark on this doc.
    val watermark = document.watermarkText?.takeIf { it.isNotBlank() }

    val paymentMeansSegments: List<com.a4a.g8invoicing.data.models.PaymentLabelSegment> =
        when (document) {
            is InvoiceState -> document.paymentMeansSegments
            is com.a4a.g8invoicing.ui.states.CreditNoteState -> document.paymentMeansSegments
            else -> emptyList()
        }
    val paymentMeansHidden: Boolean = when (document) {
        is InvoiceState -> document.paymentMeansHidden
        is com.a4a.g8invoicing.ui.states.CreditNoteState -> document.paymentMeansHidden
        else -> false
    }
    val paymentBankHidden: Boolean = when (document) {
        is InvoiceState -> document.paymentBankHidden
        is com.a4a.g8invoicing.ui.states.CreditNoteState -> document.paymentBankHidden
        else -> false
    }
    val iban = document.documentIssuer?.paymentIban?.text?.trim().orEmpty()
    val bic = document.documentIssuer?.paymentBic?.text?.trim().orEmpty()

    val paymentMeansDisplay: String? = if (!paymentMeansHidden && paymentMeansSegments.isNotEmpty()) {
        val labelsByChip = com.a4a.g8invoicing.data.models.PaymentMeans.entries.associate {
            it.chipId to (labels?.get(it.labelKey) ?: "")
        }
        com.a4a.g8invoicing.data.models
            .flattenPaymentLabel(paymentMeansSegments, labelsByChip)
            .takeIf { it.isNotEmpty() }
    } else null

    val identifierLabel = if (com.a4a.g8invoicing.data.models.CountryCodes.isIbanCountry(
            document.documentIssuer?.paymentCountry
        ) || document.documentIssuer?.paymentCountry == null
    ) "IBAN" else stringResource(Res.string.issuer_bank_identifier_generic)
    val bankSegments: List<com.a4a.g8invoicing.data.models.PaymentBankSegment> = when (document) {
        is InvoiceState -> document.paymentBankSegments
        is com.a4a.g8invoicing.ui.states.CreditNoteState -> document.paymentBankSegments
        else -> emptyList()
    }
    val effectiveBankSegments = bankSegments.ifEmpty {
        com.a4a.g8invoicing.data.models.defaultPaymentBankSegments()
    }
    val bankRendered: String? = if (!paymentBankHidden && (iban.isNotEmpty() || bic.isNotEmpty())) {
        com.a4a.g8invoicing.data.models
            .flattenPaymentBank(effectiveBankSegments, identifierLabel, iban, bic)
            .takeIf { it.isNotEmpty() }
    } else null

    val showPaymentSection = paymentMeansDisplay != null || bankRendered != null

    val paymentTerms = (document as? InvoiceState)?.paymentTermsDescription?.text
        ?.takeIf { it.isNotBlank() }
    val footerText = document.footerText.text.takeIf { it.isNotBlank() }

    val bodyStyle = MaterialTheme.typography.textForDocuments

    Column(
        modifier = Modifier
            .fillMaxSize()
            .customCombinedClickable(
                onClick = {
                    onClickElement(ScreenElement.DOCUMENT_FOOTER)
                },
                onLongClick = {
                }
            )
    ) {
        if (showPaymentSection) {
            Spacer(Modifier.height(12.dp))
            // Box title is the invoice's due date ("À régler avant le
            // dd/mm/yyyy") when available; falls back to the generic
            // "Paiement" label for docs that don't carry a due date (credit
            // notes).
            val invoiceDueDate = (document as? InvoiceState)?.dueDate
                ?.substringBefore(" ")
                ?.takeIf { it.isNotBlank() }
            val boxTitle = if (invoiceDueDate != null) {
                documentLabel(labels, "invoice_pdf_due_date", Res.string.invoice_pdf_due_date) + invoiceDueDate
            } else {
                documentLabel(labels, "document_payment_section_title", Res.string.document_payment_section_title)
            }
            // No fillMaxWidth → the box wraps the widest inner line + its own
            // horizontal padding, so the grey background hugs the content
            // instead of spanning the whole page.
            Column(
                modifier = Modifier
                    .background(PaymentBoxColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text(
                    text = boxTitle,
                    style = MaterialTheme.typography.textForDocumentsBold,
                )
                Spacer(Modifier.height(4.dp))
                // Merged into a single Text so every internal line break shares
                // the same lineHeight — otherwise the gap between separate Text
                // composables (means vs bank) reads tighter than the "\n"
                // between IBAN and BIC inside bankRendered.
                val paymentLines = listOfNotNull(paymentMeansDisplay, bankRendered)
                    .joinToString("\n")
                Text(text = paymentLines, style = bodyStyle, lineHeight = 8.sp)
            }
        }

        // Terms + footer + watermark share the bottom band under a hairline.
        // The separator only shows when there's something below it — if all
        // three are absent (empty terms, no free-text footer, watermark
        // module off in gStore), it would dangle under empty space.
        if (paymentTerms != null || footerText != null || watermark != null) {
            Spacer(Modifier.height(if (showPaymentSection) 12.dp else 20.dp))
            HorizontalDivider(color = SeparatorColor, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))
            if (paymentTerms != null) {
                Text(
                    text = paymentTerms,
                    style = bodyStyle.copy(
                        color = MentionColor,
                        fontSize = SmallSize,
                    ),
                    lineHeight = 7.sp,
                )
            }
            if (footerText != null) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (paymentTerms != null) 6.dp else 0.dp),
                    textAlign = TextAlign.Center,
                    text = footerText,
                    style = bodyStyle.copy(fontSize = SmallSize),
                    lineHeight = 7.sp,
                )
            }
            if (watermark != null) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (paymentTerms != null || footerText != null) 6.dp else 0.dp),
                    textAlign = TextAlign.Center,
                    text = watermark,
                    color = SignatureColor,
                    fontSize = SignatureSize,
                    lineHeight = 7.sp,
                )
            }
        }
    }
}
