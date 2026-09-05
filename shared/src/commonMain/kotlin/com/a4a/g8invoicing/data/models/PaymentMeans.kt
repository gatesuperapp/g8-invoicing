package com.a4a.g8invoicing.data.models

import androidx.compose.runtime.Composable
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.payment_means_10
import com.a4a.g8invoicing.shared.resources.payment_means_30
import com.a4a.g8invoicing.shared.resources.payment_means_42
import com.a4a.g8invoicing.shared.resources.payment_means_48
import com.a4a.g8invoicing.shared.resources.payment_means_other
import com.a4a.g8invoicing.shared.resources.payment_means_paypal
import com.a4a.g8invoicing.shared.resources.payment_means_stripe
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// Chips offered in the payment-means picker. Persisted via [chipId] (enum name)
// because PAYPAL and STRIPE share UN/CEFACT 4461 code 68 — code alone can't tell
// them apart, so the chip identity is what round-trips to the DB.
//
// [code] is the UN/CEFACT 4461 payment means code exposed for Factur-X BT-81.
// Nullable for OTHER, which is a UI-only chip that doesn't map to any 4461
// entry and is never emitted on the invoice or in exports. The "code 1 =
// Instrument not defined" fallback is applied at export time when the derived
// code set is empty (see [unCefactCodesForExport]).
//
// [preserveCase] keeps brand names (PayPal, Stripe) uncasted mid-sentence — the
// default lowercase-first-char rule would butcher "PayPal" to "payPal". Non-brand
// modes stay under the FR "after-colon lowercase" convention.
//
// [labelKey] is the key used in [DocumentLabels] snapshot map so a FR-created
// invoice keeps FR labels after the app switches locale.
enum class PaymentMeans(
    val code: Int?,
    val labelRes: StringResource,
    val labelKey: String,
    val preserveCase: Boolean = false,
) {
    TRANSFER(30, Res.string.payment_means_30, "payment_means_30"),
    CHEQUE(42, Res.string.payment_means_42, "payment_means_42"),
    CARD(48, Res.string.payment_means_48, "payment_means_48"),
    // SEPA direct debit (was code 58 mislabeled "Prélèvement SEPA") retired
    // per product decision — the SEPA direct-debit flow requires a signed
    // mandate we don't manage. Use TRANSFER for SEPA credit transfer.
    CASH(10, Res.string.payment_means_10, "payment_means_10"),
    PAYPAL(68, Res.string.payment_means_paypal, "payment_means_paypal", preserveCase = true),
    STRIPE(68, Res.string.payment_means_stripe, "payment_means_stripe", preserveCase = true),
    OTHER(null, Res.string.payment_means_other, "payment_means_other");

    /** Stable identity used for persistence and Token(chipId). Same as [name]. */
    val chipId: String get() = name

    companion object {
        /** Order shown in the picker + used to build the comma-joined display. */
        val UI_ORDER: List<PaymentMeans> = listOf(
            TRANSFER, CHEQUE, CARD, CASH, PAYPAL, STRIPE, OTHER,
        )

        /** Chip identities that actually render as tokens on the invoice.
         *  OTHER is filtered out — it's a UI-only marker. */
        val RENDERABLE: List<PaymentMeans> = UI_ORDER.filter { it != OTHER }

        fun fromChipId(chipId: String): PaymentMeans? =
            entries.firstOrNull { it.name == chipId }
    }
}

/**
 * Derive the Factur-X BT-81 code set to emit for a given selection. Applies the
 * "empty → [1] Instrument not defined" fallback so the CII XML always carries a
 * valid PaymentMeansCode. OTHER contributes nothing (no 4461 mapping); PAYPAL
 * and STRIPE both contribute 68 (dedup'd by Set).
 *
 * Not called by any live code today (Factur-X export is future work) but
 * documents the contract so the export code path just calls this helper.
 */
fun unCefactCodesForExport(selections: Set<String>?): Set<Int> {
    val codes = selections
        ?.mapNotNull { PaymentMeans.fromChipId(it)?.code }
        ?.toSet()
        .orEmpty()
    return codes.ifEmpty { setOf(1) }
}

/**
 * Composable helper — resolves the chip ids to a stable, comma-joined localized
 * label like "Virement, chèque". Uses the CURRENT app locale.
 * Sorted by [PaymentMeans.UI_ORDER] so the display order is stable regardless
 * of the underlying set iteration. OTHER never appears in the joined string
 * (filtered out via RENDERABLE).
 *
 * Non-first entries are lowercased (except when the label is a full-uppercase
 * acronym like "SEPA" or "CB", or when the chip has [preserveCase] set for a
 * brand name like "PayPal"/"Stripe").
 */
@Composable
fun joinPaymentMeansLabels(chipIds: Set<String>?): String {
    if (chipIds.isNullOrEmpty()) return ""
    val entries = PaymentMeans.RENDERABLE.filter { it.chipId in chipIds }
    val labels = entries.map { stringResource(it.labelRes) }
    val preserve = entries.map { it.preserveCase }
    return joinPreservingAcronyms(labels, preserve)
}

/**
 * Non-composable variant — resolves labels from a frozen labels snapshot map
 * (locale of the doc at creation). Used by the in-app footer preview so a FR
 * invoice keeps "virement" after the user switches the app to EN.
 */
fun joinPaymentMeansLabelsFromSnapshot(
    chipIds: Set<String>?,
    labelsSnapshot: Map<String, String>?,
): String {
    if (chipIds.isNullOrEmpty()) return ""
    val pairs = PaymentMeans.RENDERABLE
        .filter { it.chipId in chipIds }
        .mapNotNull { pm -> labelsSnapshot?.get(pm.labelKey)?.let { it to pm.preserveCase } }
    return joinPreservingAcronyms(pairs.map { it.first }, pairs.map { it.second })
}

/**
 * Non-composable variant used inside picker callbacks where a composable
 * `stringResource` call isn't available. The [labelsByChip] map is built once
 * in the composable body from `stringResource(labelRes)` for each mode.
 */
fun joinPaymentMeansLabelsFromMap(
    chipIds: Set<String>?,
    labelsByChip: Map<String, String>,
): String {
    if (chipIds.isNullOrEmpty()) return ""
    val entries = PaymentMeans.RENDERABLE.filter { it.chipId in chipIds }
    val labels = entries.mapNotNull { labelsByChip[it.chipId]?.let { s -> s to it.preserveCase } }
    return joinPreservingAcronyms(labels.map { it.first }, labels.map { it.second })
}

/** Join with lowercase-first-char on every entry except full-uppercase acronyms
 *  (SEPA, CB…) and brand names flagged [preserveCase]. Position-parallel with
 *  [preserve] so we know whether to touch each label. */
private fun joinPreservingAcronyms(labels: List<String>, preserve: List<Boolean>): String {
    return labels.mapIndexed { i, s ->
        val keep = preserve.getOrNull(i) == true || s == s.uppercase()
        if (keep) s else s.replaceFirstChar { it.lowercase() }
    }.joinToString(", ")
}
