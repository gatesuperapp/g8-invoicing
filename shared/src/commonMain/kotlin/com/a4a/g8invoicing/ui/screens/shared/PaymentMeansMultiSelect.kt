package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.models.PaymentMeans
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_payment_bank_edit_prefix
import com.a4a.g8invoicing.shared.resources.document_payment_bank_iban_empty
import com.a4a.g8invoicing.shared.resources.document_payment_bank_iban_empty_no_issuer
import com.a4a.g8invoicing.shared.resources.document_payment_bank_show_switch
import com.a4a.g8invoicing.shared.resources.document_payment_means_edit_prefix
import com.a4a.g8invoicing.shared.resources.document_payment_means_show_switch
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.theme.textBodyBold
import org.jetbrains.compose.resources.stringResource

/**
 * Auto-save payment-means picker for invoice + credit note.
 *
 * 1. Chips (8 modes) — left-aligned, wrapped 3 per row via [FlowRow]. Tap
 *    toggles active/inactive. 5 real UN/CEFACT 4461 modes (TRANSFER, CHEQUE,
 *    CARD, SEPA, CASH) + 2 online-payment brands sharing code 68 (PAYPAL,
 *    STRIPE) + OTHER (UI-only, never rendered on the invoice, never exported
 *    to Factur-X). Toggling a "real" chip mutates the label segments; OTHER
 *    toggles a separate flag. The joined mode list composes at render time
 *    by the footer/PDF, so chip changes show up on preview immediately.
 * 2. "Afficher les moyens de paiements acceptés sur la facture" row with a
 *    Switch. Off = the whole payment-means block is skipped on preview + PDF
 *    (selections stay persisted so Factur-X BT-81 survives).
 * 3. When the switch is ON, an underlined "Modifier le txt" link appears
 *    below it — fires [onClickEditPrefix] which opens a free-text modal in
 *    the caller for editing just the prefix (default "moyens de paiement
 *    acceptés :"). The joined mode list still auto-appends after whatever
 *    the user types.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentMeansMultiSelect(
    selectedChips: Set<String>,
    otherChecked: Boolean,
    // Fired for the 7 "real" chips (TRANSFER, CHEQUE, CARD, SEPA, CASH,
    // PAYPAL, STRIPE) — those that mutate label segments.
    onToggleChip: (String) -> Unit,
    // Separate handler for OTHER — it doesn't touch segments, only the flag.
    onToggleOther: () -> Unit,
    hidden: Boolean,
    onToggleHidden: () -> Unit,
    onClickEditPrefix: () -> Unit,
    // Bank accounts of the doc's master issuer. Populated by the caller via a
    // suspend fetch on modal open. Empty list = show the empty-state hint in
    // the dropdown instead of hiding it (user needs a visual reminder to go
    // add an account in the master form).
    issuerBanks: List<com.a4a.g8invoicing.ui.states.IssuerBankState> = emptyList(),
    // IBAN currently frozen on the doc (from DocumentClientOrIssuer). Matched
    // by text against [issuerBanks] to pre-select the right dropdown option.
    selectedBankIban: String? = null,
    onBankPicked: (com.a4a.g8invoicing.ui.states.IssuerBankState) -> Unit = {},
    // Second visibility toggle — the "Afficher les coordonnées bancaires"
    // switch below the IBAN dropdown. When on, the block renders on the
    // preview + PDF (with BIC line if BIC is present).
    bankHidden: Boolean = false,
    onToggleBankHidden: () -> Unit = {},
    onClickEditBankPrefix: () -> Unit = {},
    // Whether an issuer has been picked on the invoice yet. Controls which
    // empty-state hint the IBAN dropdown shows: "Ajoutez un émetteur…" when
    // no issuer, "Ajoutez un compte bancaire…" when there's an issuer but
    // no bank on file.
    hasIssuer: Boolean = true,
) {
    Column(
        modifier = Modifier
            .background(AppColors.surface)
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 100.dp)
    ) {
        // Chips — natural flow-wrap. Row 1 fits Virement / Chèque / CB / SEPA
        // thanks to the short SEPA label; row 2 gets the longer online-brand
        // chips + Autre.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            PaymentMeans.UI_ORDER.forEach { means ->
                if (means == PaymentMeans.OTHER) {
                    PaymentMeansChip(
                        text = stringResource(means.labelRes),
                        active = otherChecked,
                        onClick = { onToggleOther() },
                    )
                } else {
                    val checked = means.chipId in selectedChips
                    PaymentMeansChip(
                        text = stringResource(means.labelRes),
                        active = checked,
                        onClick = { onToggleChip(means.chipId) },
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // Show/hide row — sync-to-master switch pattern (textBodyBold label,
        // Switch scale 0.8f + ColorVioletLink track).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.document_payment_means_show_switch),
                style = MaterialTheme.typography.textBodyBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 15.dp),
            )
            Switch(
                checked = !hidden,
                onCheckedChange = { onToggleHidden() },
                modifier = Modifier.scale(0.8f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ColorVioletLink,
                    checkedBorderColor = Color.Transparent,
                    uncheckedBorderColor = Color.Transparent,
                ),
            )
        }

        // "Modifier le txt" — only visible when the block is set to show.
        // No point exposing the label editor when the block itself is hidden.
        if (!hidden) {
            Text(
                text = stringResource(Res.string.document_payment_means_edit_prefix),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ColorVioletLink,
                    textDecoration = TextDecoration.Underline,
                ),
                modifier = Modifier
                    .padding(start = 4.dp, top = 5.dp)
                    .clickable { onClickEditPrefix() },
            )
        }

        Spacer(Modifier.height(24.dp))

        // Coordonnées bancaires switch. Mirrors the "Afficher les modes de
        // paiement acceptés" row above (same textBodyBold label + scale 0.8f
        // Switch). Off = the IBAN/BIC line is skipped on preview + PDF.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.document_payment_bank_show_switch),
                style = MaterialTheme.typography.textBodyBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 15.dp),
            )
            Switch(
                checked = !bankHidden,
                onCheckedChange = { onToggleBankHidden() },
                modifier = Modifier.scale(0.8f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ColorVioletLink,
                    checkedBorderColor = Color.Transparent,
                    uncheckedBorderColor = Color.Transparent,
                ),
            )
        }

        if (!bankHidden) {
            // IBAN dropdown appears only when there's an actual choice to make
            // — 2+ banks. With a single bank there's nothing to pick between,
            // so we skip the dropdown entirely (the IBAN still renders on the
            // invoice via the frozen payment_iban).
            // The empty-state variant (0 banks) also renders as a static hint
            // to nudge the user back to "Mon entreprise" (or emitter pick).
            if (issuerBanks.size != 1) {
                Spacer(Modifier.height(12.dp))
                IssuerBankDropdown(
                    banks = issuerBanks,
                    selectedIban = selectedBankIban,
                    onBankPicked = onBankPicked,
                    hasIssuer = hasIssuer,
                )
            }
            if (issuerBanks.isNotEmpty()) {
                // 5 dp when link sits directly under the switch (no dropdown),
                // 8 dp when a dropdown separates them.
                val topPad = if (issuerBanks.size == 1) 5.dp else 8.dp
                Text(
                    text = stringResource(Res.string.document_payment_bank_edit_prefix),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ColorVioletLink,
                        textDecoration = TextDecoration.Underline,
                    ),
                    modifier = Modifier
                        .padding(start = 4.dp, top = topPad)
                        .clickable { onClickEditBankPrefix() },
                )
            }
        }
    }
}

@Composable
private fun PaymentMeansChip(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (active) ColorVioletLink.copy(alpha = 0.12f) else AppColors.surfaceMuted
    val fg = if (active) ColorVioletLink else AppColors.textMuted
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = fg,
        )
    }
}

/**
 * Dropdown surfaced under the chips when the TRANSFER chip is active. Lets
 * the user pick which of the issuer's bank accounts freezes on this doc.
 * Display: the account's [label] if set, else its IBAN. On pick, fires
 * [onBankPicked] which the caller translates into a DOCUMENT_ISSUER_BANK_PICKED
 * event so the ViewModel can update state + persist.
 */
@Composable
private fun IssuerBankDropdown(
    banks: List<com.a4a.g8invoicing.ui.states.IssuerBankState>,
    selectedIban: String?,
    onBankPicked: (com.a4a.g8invoicing.ui.states.IssuerBankState) -> Unit,
    hasIssuer: Boolean = true,
) {
    // Match the doc's frozen IBAN back to a master bank; fallback to the first
    // bank so the field never renders "no selection" — that would confuse users
    // into thinking their pick didn't stick.
    val selected = banks.firstOrNull { it.identifier.text == selectedIban }
        ?: banks.firstOrNull()
    var expanded by remember { mutableStateOf(false) }
    val displayFor: (com.a4a.g8invoicing.ui.states.IssuerBankState) -> String = { bank ->
        bank.label?.text?.trim().orEmpty().ifEmpty { bank.identifier.text }
    }
    // Two distinct empty-state hints depending on the missing piece:
    //  - no issuer chosen on the doc yet → user needs to pick an emitter first
    //  - issuer chosen but no bank on file → user needs to go add one in
    //    "Mon entreprise"
    val emptyStateHint = stringResource(
        if (!hasIssuer) Res.string.document_payment_bank_iban_empty_no_issuer
        else Res.string.document_payment_bank_iban_empty
    )
    val displayText = selected?.let(displayFor)?.takeIf { it.isNotEmpty() } ?: emptyStateHint
    val isEmptyState = banks.isEmpty()

    // Plain-Compose dropdown: outlined-looking Box + anchored DropdownMenu.
    // The Material3 ExposedDropdownMenuBox isn't available in Compose
    // Multiplatform 1.8.x — this shape matches the codebase's other picker
    // fields (see CurrencyPicker / CountryPicker) and stays lightweight.
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(AppColors.surfaceMuted)
                // Don't open a dropdown that has nothing to pick — swallow the
                // click in empty-state so the user's next action is heading
                // back to "Mon entreprise" (following the hint text).
                .clickable(enabled = !isEmptyState) { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isEmptyState) AppColors.textMuted else AppColors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            if (!isEmptyState) {
                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textMuted,
                )
            }
        }
        androidx.compose.material3.DropdownMenu(
            expanded = expanded && !isEmptyState,
            onDismissRequest = { expanded = false },
        ) {
            banks.forEach { bank ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(displayFor(bank)) },
                    onClick = {
                        expanded = false
                        onBankPicked(bank)
                    },
                )
            }
        }
    }
}
