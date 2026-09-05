package com.a4a.g8invoicing.data.models

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.vat_exempt_DE
import com.a4a.g8invoicing.shared.resources.vat_exempt_FR
import com.a4a.g8invoicing.shared.resources.vat_exempt_IT
import com.a4a.g8invoicing.shared.resources.vat_exempt_fallback_eu
import com.a4a.g8invoicing.shared.resources.vat_exempt_fallback_generic
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import org.jetbrains.compose.resources.getString

/**
 * BT-120 default VAT-exempt mention resolved from the issuer's country code.
 * Three-layer waterfall:
 *   1. Country-specific citation, kept verbatim across all locales (a legal
 *      citation isn't paraphrased — § 19 UStG stays German on an EN invoice).
 *      Covers FR + MC (Monaco = French tax union), DE, IT so far. Add a case
 *      here + a `vat_exempt_<CC>` key on the translations branch to extend.
 *   2. EU 27 fallback — Article 284 of directive 2006/112/CE. Translated per
 *      display locale (values-en / -de / -es) so the reader sees it in their
 *      language.
 *   3. Everything else — generic "Not registered for VAT". Also translated
 *      per display locale.
 *
 * Compose Multiplatform doesn't support dynamic Res.string.byName(String),
 * hence the explicit when-branch. User can always overwrite the default via
 * the text menu — even inside one country several regimes coexist (franchise
 * en base, exonération d'activité, exonération art. 261…) and the app can't
 * guess which one applies.
 */
suspend fun resolveVatExemptionText(issuerCountryCode: String?): String? {
    val code = issuerCountryCode?.trim()?.takeIf { it.isNotEmpty() }?.uppercase()
        ?: return null
    return when (code) {
        "FR", "MC" -> getString(Res.string.vat_exempt_FR)
        "DE" -> getString(Res.string.vat_exempt_DE)
        "IT" -> getString(Res.string.vat_exempt_IT)
        else -> if (CountryCodes.isInEU(code)) {
            getString(Res.string.vat_exempt_fallback_eu)
        } else {
            getString(Res.string.vat_exempt_fallback_generic)
        }
    }
}

/**
 * Seed the BT-120 mention on a brand-new document (Invoice or CreditNote).
 * Rules:
 *   • Returns null when the issuer isn't vat-exempt (nothing to seed).
 *   • Reuses the previous-doc wording only when the master issuer's country
 *     hasn't changed between docs — otherwise the frozen text is now stale
 *     (previous mention referenced the OLD country's regime) and we let the
 *     resolver pick the current country's default.
 *   • Falls through to [resolveVatExemptionText] for the 3-layer defaults.
 * User can always overwrite via the text menu.
 */
suspend fun resolveVatExemptionForNewDoc(
    issuer: ClientOrIssuerState?,
    previousVatText: String?,
    previousIssuerCountry: String?,
): TextFieldValue? {
    if (issuer?.vatExempt != true) return null
    val currentCountry = issuer.addresses?.firstOrNull()?.countryCode
    val sameCountry = !currentCountry.isNullOrBlank()
        && !previousIssuerCountry.isNullOrBlank()
        && currentCountry.equals(previousIssuerCountry, ignoreCase = true)
    val reused = previousVatText
        ?.trim()
        ?.takeIf { it.isNotEmpty() && sameCountry }
    val text = reused ?: resolveVatExemptionText(currentCountry)
    return text?.let { TextFieldValue(it) }
}
