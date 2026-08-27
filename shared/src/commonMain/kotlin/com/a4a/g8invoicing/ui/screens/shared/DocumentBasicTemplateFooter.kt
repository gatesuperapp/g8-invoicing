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

    // Payment section (means + IBAN/BIC + terms) applies to INVOICES ONLY.
    // A quote is a commercial proposal (no payment context); an avoir
    // reverses the flow (seller owes buyer, no payment for the buyer to
    // make). Bailing out on non-invoice types keeps the grey Paiement box
    // off the PDF for those docs.
    val invoice = document as? InvoiceState
    val paymentMeansSegments = invoice?.paymentMeansSegments.orEmpty()
    val paymentMeansHidden = invoice?.paymentMeansHidden ?: true
    val paymentBankHidden = invoice?.paymentBankHidden ?: true
    val iban = if (invoice != null) {
        document.documentIssuer?.paymentIban?.text?.trim().orEmpty()
    } else ""
    val bic = if (invoice != null) {
        document.documentIssuer?.paymentBic?.text?.trim().orEmpty()
    } else ""

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
    val bankSegments: List<com.a4a.g8invoicing.data.models.PaymentBankSegment> =
        invoice?.paymentBankSegments.orEmpty()
    val effectiveBankSegments = bankSegments.ifEmpty {
        com.a4a.g8invoicing.data.models.defaultPaymentBankSegments()
    }
    val bankRendered: String? = if (!paymentBankHidden && (iban.isNotEmpty() || bic.isNotEmpty())) {
        com.a4a.g8invoicing.data.models
            .flattenPaymentBank(effectiveBankSegments, identifierLabel, iban, bic)
            .takeIf { it.isNotEmpty() }
    } else null

    val showPaymentSection = paymentMeansDisplay != null || bankRendered != null

    // BT-20 = concat of the 3 subject-coded fields (PMT/PMD/AAB), rendered
    // as one flowing paragraph — sentences joined with a single space, no
    // newlines, so the block matches the PDF and stays visually tight.
    val paymentTerms = (document as? InvoiceState)?.let { inv ->
        listOf(
            inv.paymentTermsRecoveryFees.text.trim(),
            inv.paymentTermsLateFees.text.trim(),
            inv.paymentTermsDiscount.text.trim(),
        ).filter { it.isNotEmpty() }.joinToString(" ").takeIf { it.isNotEmpty() }
    }
    val footerText = document.footerText.text.takeIf { it.isNotBlank() }
    // BT-120 legal mention — only surfaced when the issuer is in franchise
    // en base and the user has entered a wording in the text menu.
    val vatExemptionMention = if (document.documentIssuer?.vatExempt == true) {
        document.vatExemptionText?.text?.takeIf { it.isNotBlank() }
    } else null

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
        if (vatExemptionMention != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = vatExemptionMention,
                style = bodyStyle.copy(
                    color = MentionColor,
                    fontSize = SmallSize,
                ),
                textAlign = TextAlign.End,
                lineHeight = 7.sp,
            )
        }
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
