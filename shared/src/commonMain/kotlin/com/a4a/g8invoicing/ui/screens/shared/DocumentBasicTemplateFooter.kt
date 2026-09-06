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
import com.a4a.g8invoicing.ui.states.QuoteState
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

    // Payment section (means + IBAN/BIC + terms) applies to INVOICES + QUOTES.
    // A devis is a commercial proposal the client will sign, so it needs
    // the same payment context (how to pay, terms) as the eventual invoice.
    // An avoir reverses the flow (seller owes buyer, no payment for the
    // buyer to make) — kept off. BLs likewise skip the block.
    val payingDoc: DocumentState? = (document as? InvoiceState) ?: (document as? QuoteState)
    val paymentMeansSegments = when (payingDoc) {
        is InvoiceState -> payingDoc.paymentMeansSegments
        is QuoteState -> payingDoc.paymentMeansSegments
        else -> emptyList()
    }
    val paymentMeansHidden = when (payingDoc) {
        is InvoiceState -> payingDoc.paymentMeansHidden
        is QuoteState -> payingDoc.paymentMeansHidden
        else -> true
    }
    val paymentBankHidden = when (payingDoc) {
        is InvoiceState -> payingDoc.paymentBankHidden
        is QuoteState -> payingDoc.paymentBankHidden
        else -> true
    }
    val iban = if (payingDoc != null) {
        document.documentIssuer?.paymentIban?.text?.trim().orEmpty()
    } else ""
    val bic = if (payingDoc != null) {
        document.documentIssuer?.paymentBic?.text?.trim().orEmpty()
    } else ""

    val paymentMeansDisplay: String? = if (!paymentMeansHidden && paymentMeansSegments.isNotEmpty()) {
        // Chip label lookup — prefer the doc's frozen labelsSnapshot so a
        // FR-created doc keeps "Virement" even after the app switches to EN.
        // Legacy docs (pre-1.9) have no snapshot for the payment-means chips
        // added post-migration, so fall back to stringResource(labelRes)
        // (current app locale) instead of the empty string that used to
        // leave the chip rendering blank in the preview — the PDF was fine
        // because PdfStrings pre-resolves the same map via stringResource.
        val labelsByChip = com.a4a.g8invoicing.data.models.PaymentMeans.entries.associate {
            val snapshotLabel = labels?.get(it.labelKey)?.takeIf { s -> s.isNotBlank() }
            it.chipId to (snapshotLabel ?: stringResource(it.labelRes))
        }
        com.a4a.g8invoicing.data.models
            .flattenPaymentLabel(paymentMeansSegments, labelsByChip)
            .takeIf { it.isNotEmpty() }
    } else null

    val identifierLabel = if (com.a4a.g8invoicing.data.models.CountryCodes.isIbanCountry(
            document.documentIssuer?.paymentCountry
        ) || document.documentIssuer?.paymentCountry == null
    ) "IBAN" else stringResource(Res.string.issuer_bank_identifier_generic)
    val bankSegments: List<com.a4a.g8invoicing.data.models.PaymentBankSegment> = when (payingDoc) {
        is InvoiceState -> payingDoc.paymentBankSegments
        is QuoteState -> payingDoc.paymentBankSegments
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

    // BT-20 = concat of the 3 subject-coded fields (PMT/PMD/AAB), rendered
    // as one flowing paragraph — sentences joined with a single space, no
    // newlines, so the block matches the PDF and stays visually tight.
    // Invoice-only: the 3 legal mentions attach to the payment obligation
    // itself, not to the offer. Devis renders bank / payment means but
    // never the recovery-fees / late-fees / discount block — matches the
    // PDF renderer (createPaymentTermsBlock is likewise Invoice-only).
    // Legacy Quotes that had these fields populated stop rendering them.
    val paymentTerms = (payingDoc as? InvoiceState)?.let { inv ->
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
                // end=3.dp matches DocumentBasicTemplateTotalPrices' right
                // padding, so the mention's right edge lands on the same
                // vertical as the € column of the totals block above.
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 3.dp),
                text = vatExemptionMention,
                style = bodyStyle.copy(
                    color = MentionColor,
                    fontSize = SmallSize,
                ),
                textAlign = TextAlign.End,
                lineHeight = 7.sp,
            )
        }
        // "À régler avant le dd/mm/yyyy" (invoices) or the generic "Paiement"
        // fallback (credit notes). Computed once and reused: it's the grey
        // box's title when the payment section renders, and a standalone
        // bold line when the user has hidden both payment means and bank —
        // in that case the due date still needs to show, just without the
        // grey background.
        val invoiceDueDate = (document as? InvoiceState)?.dueDate
            ?.substringBefore(" ")
            ?.takeIf { it.isNotBlank() }
        val paymentBlockTitle = when {
            invoiceDueDate != null ->
                documentLabel(labels, "invoice_pdf_due_date", Res.string.invoice_pdf_due_date) + invoiceDueDate
            showPaymentSection ->
                documentLabel(labels, "document_payment_section_title", Res.string.document_payment_section_title)
            else -> null
        }
        if (showPaymentSection) {
            Spacer(Modifier.height(12.dp))
            // No fillMaxWidth → the box wraps the widest inner line + its own
            // horizontal padding, so the grey background hugs the content
            // instead of spanning the whole page.
            Column(
                modifier = Modifier
                    .background(PaymentBoxColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text(
                    text = paymentBlockTitle.orEmpty(),
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
        } else if (invoiceDueDate != null && paymentBlockTitle != null) {
            // Both payment means and bank are hidden but this is still an
            // invoice with a due date — surface it on a plain, centered
            // line so the client can see when the invoice needs to be paid.
            // Centering (rather than the left-alignment used inside the grey
            // box) anchors the line as a standalone reminder rather than a
            // would-be header of the missing payment block.
            Spacer(Modifier.height(12.dp))
            Text(
                text = paymentBlockTitle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.textForDocumentsBold,
            )
        }

        // Terms + footer + watermark share the bottom band. The hairline
        // separator only shows when there's a payment section (grey box or
        // standalone due-date line) above it — on avoirs / quotes / delivery
        // notes the payment section is suppressed, so the divider would sit
        // under empty space. Legacy docs (pre-1.8, showCurrencyAndAutoTaxColumn
        // = false) never carried the separator either — a re-render of an old
        // invoice must stay pixel-identical, so we suppress the line there too.
        val hasPaymentHeader = document.showCurrencyAndAutoTaxColumn &&
            (showPaymentSection ||
                (invoiceDueDate != null && paymentBlockTitle != null))
        // Standalone due-date line with no grey box + no payment-terms prose:
        // fall back to the tight ~6dp gap master used to sit the footer
        // directly under the due date (barely more than a normal interline).
        // The 20dp default only applies when the payment box or terms above
        // introduce a real visual break the reader needs to cross.
        val isStandaloneDueDateOnly = !showPaymentSection &&
            invoiceDueDate != null && paymentBlockTitle != null &&
            paymentTerms == null
        val topBandSpacer = when {
            hasPaymentHeader -> 12.dp
            isStandaloneDueDateOnly -> 2.dp
            else -> 20.dp
        }
        if (paymentTerms != null || footerText != null || watermark != null) {
            Spacer(Modifier.height(topBandSpacer))
            // Skip the separator in the standalone-due-date case even
            // though hasPaymentHeader is still true — the tight 6dp gap
            // above already reads as a plain interline continuation, no
            // divider needed. Matches master's pre-1.8 look on invoices
            // with only the due-date line visible above the footer.
            if (hasPaymentHeader && !isStandaloneDueDateOnly) {
                HorizontalDivider(color = SeparatorColor, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))
            }
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
