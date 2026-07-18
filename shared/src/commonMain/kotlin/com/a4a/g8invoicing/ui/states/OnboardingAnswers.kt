package com.a4a.g8invoicing.ui.states

import com.a4a.g8invoicing.data.models.ProductNature

/**
 * Sealed model of one step in the 1.8 onboarding wizard. The wizard is a linear
 * state machine (not a HorizontalPager) because branches skip whole steps based
 * on user answers.
 *
 * Per-issuer steps carry an [issuerIndex] into the current cursor over the
 * (possibly-cleaned-up) list of issuers. The country picker replaces the old
 * "Are you EU-based?" yes/no — EU-ness is derived from the country the user
 * picks (see CountryCodes.isInEU).
 */
sealed class OnboardingStep {
    object Welcome : OnboardingStep()
    object DevisIntro : OnboardingStep()
    object FacturXIntro : OnboardingStep()
    /** Privacy / local-storage reassurance + optional DB backup CTA. Sits
     *  between FacturXIntro and the per-issuer loop so the "your data stays
     *  on your phone" reassurance is stated before we start asking questions. */
    object Privacy : OnboardingStep()
    object IssuerCleanup : OnboardingStep()
    data class IssuerCountry(val issuerIndex: Int) : OnboardingStep()
    data class VatExempt(val issuerIndex: Int) : OnboardingStep()
    data class VatDontKnowHint(val issuerIndex: Int) : OnboardingStep()
    data class IntraEu(val issuerIndex: Int) : OnboardingStep()
    /** Per-issuer product nature question — only shown for issuers who are
     *  EU-based AND sell intra-EU. Appears right after that issuer's IntraEu
     *  question, before moving on to the next issuer. */
    data class ProductNature(val issuerIndex: Int) : OnboardingStep()
    /** Per-issuer follow-up when the ProductNature answer was MIXED — explains
     *  that the type will be set per document product. */
    data class MixedHint(val issuerIndex: Int) : OnboardingStep()
    /** Confirmation shown after the per-issuer loop completes — reassures the
     *  user that their issuer answers are captured before diving into the
     *  intra-EU-driven tail (or straight to unit code). Only shown when the
     *  list of issuers is non-empty. */
    object IssuersDone : OnboardingStep()
    object ClientCountryQuestion : OnboardingStep()
    object ClientCountryPicker : OnboardingStep()
    /** Confirmation after user picked a bulk country for clients (Yes branch). */
    object ClientCountryDoneApplied : OnboardingStep()
    /** Reassurance after user chose "mixed countries" — nothing to bulk-apply. */
    object ClientCountryDoneSkipped : OnboardingStep()
    object UnitCodeInfo : OnboardingStep()
    object ThankYou : OnboardingStep()
}

/** VAT status answer per issuer. FRANCHISE persists as vatExempt=true; TVA and
 *  DONT_KNOW both persist as vatExempt=false (DONT_KNOW just also shows a hint
 *  screen). */
enum class VatAnswer { FRANCHISE, TVA, DONT_KNOW }

/**
 * Per-issuer answers accumulated during the wizard. Committed to DB at the end
 * only — the user can go back and edit any answer before the final "Terminer".
 */
data class OnboardingIssuerAnswers(
    val issuerId: Int,
    val countryCode: String? = null,
    val vatAnswer: VatAnswer? = null,
    val intraEuSales: Boolean? = null,
    val productNature: ProductNatureAnswer? = null,
)

/** Product-nature answer, captured per-issuer. Bulk-applies to every Product
 *  row on commit only if every relevant issuer picked the same non-MIXED value
 *  (products aren't scoped to a specific issuer in 1.8 — a per-issuer bulk
 *  would need per-product scoping, planned for 1.9). */
enum class ProductNatureAnswer {
    ONLY_SERVICES,
    ONLY_GOODS,
    MIXED;

    fun toBulkType(): ProductNature? = when (this) {
        ONLY_SERVICES -> ProductNature.SERVICE
        ONLY_GOODS -> ProductNature.GOODS
        MIXED -> null
    }
}
