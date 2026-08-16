package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.invoice_pdf_due_date
import com.a4a.g8invoicing.shared.resources.issuer_bank_identifier_generic
import com.a4a.g8invoicing.shared.resources.pdf_currency_notice
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsBold
import org.jetbrains.compose.resources.stringResource

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

    val currencyCode = document.currency.text
    val showCurrencyNoticeLine = document.showCurrencyAndAutoTaxColumn &&
        currencyCode.isNotEmpty() && currencyCode != "EUR"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
            .customCombinedClickable(
                onClick = {
                    onClickElement(ScreenElement.DOCUMENT_FOOTER)
                },
                onLongClick = {
                }
            )
    ) {
        // "Devise : USD" — placed just above the due-date line so it sits with
        // the payment info, not the header. Bold + centered to match the
        // due-date visual weight, so the two read as one info block.
        // Snapshot first, then a locale-specific hardcoded fallback for docs
        // predating the addition of pdf_currency_notice to DocumentLabels.keys,
        // then stringResource as last resort (app-current locale).
        if (showCurrencyNoticeLine) {
            val snapshotValue = labels?.get("pdf_currency_notice")
                ?: DocumentLabels.localeFallback("pdf_currency_notice", document.formatLocale)
            val pattern = snapshotValue ?: stringResource(Res.string.pdf_currency_notice)
            Row(
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    style = MaterialTheme.typography.textForDocumentsBold,
                    text = pattern.replace("%1\$s", currencyCode),
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(bottom = 6.dp)
        ) {
            if(document is InvoiceState) {
                Text(
                    style = MaterialTheme.typography.textForDocumentsBold,
                    text = documentLabel(labels, "invoice_pdf_due_date", Res.string.invoice_pdf_due_date) + document.dueDate.substringBefore(" ")
                )
            }
        }
        // Payment terms block (BT-20) — invoice only, free text. Rendered just
        // above the payment-means line. Sits at whatever the user typed on the
        // doc; if the field is empty (user cleared it), we skip the row entirely
        // so we don't leave a phantom vertical gap.
        val paymentTerms = (document as? InvoiceState)?.paymentTermsDescription?.text
            ?.takeIf { it.isNotBlank() }
        if (paymentTerms != null) {
            Row {
                Text(
                    modifier = Modifier.padding(bottom = 4.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.textForDocuments,
                    text = paymentTerms,
                    lineHeight = 7.sp,
                )
            }
        }
        // Payment means block (BT-81) — invoice + credit note only. Segments
        // (Free text + locked Token codes) flatten to a plain string, using
        // the doc's frozen labels snapshot so mode names stay in the doc's
        // original locale after the app switches language. Hidden flag opts
        // the whole block out; empty flatten (no chips + no free text) gives
        // the same effect.
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
        if (!paymentMeansHidden && paymentMeansSegments.isNotEmpty()) {
            val labelsByChip = com.a4a.g8invoicing.data.models.PaymentMeans.entries.associate {
                it.chipId to (labels?.get(it.labelKey) ?: "")
            }
            val display = com.a4a.g8invoicing.data.models
                .flattenPaymentLabel(paymentMeansSegments, labelsByChip)
            if (display.isNotEmpty()) {
                Row {
                    Text(
                        modifier = Modifier.padding(bottom = 4.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.textForDocuments,
                        text = display,
                        lineHeight = 7.sp,
                    )
                }
            }
        }
        // IBAN + BIC block, sourced from the frozen documentIssuer snapshot.
        // Skipped when the "Afficher les coordonnées bancaires" switch is off
        // (paymentBankHidden). The frozen bank fields on DocumentClientOrIssuer
        // stay populated regardless — BT-84/86 preserved for Factur-X export
        // even when the human-readable block is hidden.
        val paymentBankHidden: Boolean = when (document) {
            is InvoiceState -> document.paymentBankHidden
            is com.a4a.g8invoicing.ui.states.CreditNoteState -> document.paymentBankHidden
            else -> false
        }
        val iban = document.documentIssuer?.paymentIban?.text?.trim().orEmpty()
        val bic = document.documentIssuer?.paymentBic?.text?.trim().orEmpty()
        val paymentCountry = document.documentIssuer?.paymentCountry
        val identifierLabel = if (com.a4a.g8invoicing.data.models.CountryCodes.isIbanCountry(paymentCountry)
            || paymentCountry == null
        ) "IBAN" else stringResource(Res.string.issuer_bank_identifier_generic)
        val bankSegments: List<com.a4a.g8invoicing.data.models.PaymentBankSegment> = when (document) {
            is InvoiceState -> document.paymentBankSegments
            is com.a4a.g8invoicing.ui.states.CreditNoteState -> document.paymentBankSegments
            else -> emptyList()
        }
        val effectiveBankSegments = bankSegments.ifEmpty {
            com.a4a.g8invoicing.data.models.defaultPaymentBankSegments()
        }
        if (!paymentBankHidden && (iban.isNotEmpty() || bic.isNotEmpty())) {
            val bankRendered = com.a4a.g8invoicing.data.models.flattenPaymentBank(
                effectiveBankSegments, identifierLabel, iban, bic,
            )
            if (bankRendered.isNotEmpty()) {
                Row {
                    Text(
                        modifier = Modifier.padding(bottom = 6.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.textForDocuments,
                        text = bankRendered,
                        lineHeight = 7.sp,
                    )
                }
            }
        }
        Row {
            Text(
                modifier = Modifier
                    .padding(bottom = 6.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.textForDocuments,
                text = document.footerText.text,
                // 7sp on a 6sp font ≈ 1.15 line-height — the previous 10sp
                // rendered a user-typed blank line as ~2 blank lines. Halving
                // would clip; 7 gets the same visual "one blank line" effect
                // without eating into the glyph metrics.
                lineHeight = 7.sp
            )
        }
        if (watermark != null) {
            // Tiny watermark, smaller than the address text. Non-interactive in the
            // in-app preview to avoid accidental taps launching the browser; the PDF
            // renders the same watermark with a live hyperlink (PdfGeneratorImpl).
            Text(
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 6.dp),
                textAlign = TextAlign.Center,
                text = watermark,
                color = Color(0xFF888888),
                fontSize = 5.sp,
                lineHeight = 7.sp,
            )
        }
    }
}

