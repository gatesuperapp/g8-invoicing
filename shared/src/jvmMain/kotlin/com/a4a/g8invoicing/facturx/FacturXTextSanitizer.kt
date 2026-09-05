package com.a4a.g8invoicing.facturx

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.QuoteState
import com.itextpdf.io.font.FontProgram
import com.itextpdf.io.font.FontProgramFactory

/**
 * Silently strips code points not covered by the embedded PDF font stack
 * (Arimo + Noto Sans Regular + Noto Sans Bold) before a Factur-X export.
 *
 * PDF/A-3B §6.3.4 requires every rendered glyph to come from an embedded
 * font — iText raises `PdfAConformanceException` mid-render on the first
 * uncovered glyph and drops the whole export. Standard PDF is lenient (a
 * missing glyph paints as `.notdef`), which is why classic export works
 * with the same document. Rather than block export or make the user hunt
 * for the offending character, we mutate the state in place before iText
 * sees it, then restore the original values via the returned lambda so
 * the UI never notices.
 *
 * Coverage is checked against the actual embedded fonts (not a hardcoded
 * Unicode range list) so emojis, CJK, exotic symbols — anything Arimo
 * and Noto Sans don't cover — all get dropped in a single pass.
 */
class FacturXTextSanitizer(
    private val loadFontBytes: (String) -> ByteArray?,
) {
    private val fontAssetPaths = listOf(
        "composeResources/com.a4a.g8invoicing.shared.resources/font/arimo.ttf",
        "composeResources/com.a4a.g8invoicing.shared.resources/font/notosansregular.ttf",
        "composeResources/com.a4a.g8invoicing.shared.resources/font/notosansbold.ttf",
    )

    private val fontPrograms: List<FontProgram> by lazy {
        fontAssetPaths.mapNotNull { path ->
            try {
                loadFontBytes(path)?.let { FontProgramFactory.createFont(it) }
            } catch (_: Throwable) {
                null
            }
        }
    }

    // Per-invocation cache — code points repeat massively across a document
    // (spaces, digits, common letters) and getGlyph() walks the font's cmap
    // each call. HashMap here, not thread-safe by design: sanitisation runs
    // start-to-finish on one thread inside generateFacturX.
    private val coverageCache = HashMap<Int, Boolean>()

    fun sanitize(text: String): String {
        if (text.isEmpty()) return text
        var mutated = false
        val out = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            val step = Character.charCount(cp)
            if (isCovered(cp)) {
                out.appendCodePoint(cp)
            } else {
                mutated = true
            }
            i += step
        }
        return if (mutated) out.toString() else text
    }

    private fun isCovered(cp: Int): Boolean {
        // Whitespace + control chars iText treats as layout, not glyphs.
        // Newline and tab in particular never route through cmap lookup,
        // so exempting them is both correct and avoids a false negative
        // when a font's cmap has no entry for U+000A / U+0009.
        if (cp == ' '.code || cp == '\n'.code || cp == '\t'.code || cp == '\r'.code) return true
        val cached = coverageCache[cp]
        if (cached != null) return cached
        val supported = fontPrograms.any { program ->
            try { program.getGlyph(cp) != null } catch (_: Throwable) { false }
        }
        coverageCache[cp] = supported
        return supported
    }

    /**
     * Walk every user-editable text field on [state] (and its issuer / client
     * / products / banks / addresses / emails) and rewrite each in place
     * with the sanitized value. Returns a lambda that restores the original
     * values — call it in a `finally` block so a mid-export throw doesn't
     * leave the caller's DocumentState pointing at stripped text.
     */
    fun applyToDocumentInPlace(state: DocumentState): () -> Unit {
        val restore = mutableListOf<() -> Unit>()

        rewriteTfv(state.documentNumber, { state.documentNumber = it }, restore)
        rewriteNullableTfv(state.reference, { state.reference = it }, restore)
        rewriteNullableTfv(state.freeField, { state.freeField = it }, restore)
        rewriteTfv(state.currency, { state.currency = it }, restore)
        rewriteTfv(state.footerText, { state.footerText = it }, restore)
        rewriteNullableTfv(state.vatExemptionText, { state.vatExemptionText = it }, restore)

        state.watermarkText?.let { orig ->
            val cleaned = sanitize(orig)
            if (cleaned != orig) {
                state.watermarkText = cleaned
                restore += { state.watermarkText = orig }
            }
        }

        state.documentIssuer?.let { rewriteParty(it, restore) }
        state.documentClient?.let { rewriteParty(it, restore) }
        state.documentProducts?.forEach { rewriteProduct(it, restore) }

        // Payment-terms fields exist on Invoice + Quote only — CreditNote /
        // DeliveryNote don't carry them.
        when (state) {
            is InvoiceState -> {
                rewriteTfv(state.paymentTermsRecoveryFees, { state.paymentTermsRecoveryFees = it }, restore)
                rewriteTfv(state.paymentTermsLateFees, { state.paymentTermsLateFees = it }, restore)
                rewriteTfv(state.paymentTermsDiscount, { state.paymentTermsDiscount = it }, restore)
            }
            is QuoteState -> {
                rewriteTfv(state.paymentTermsRecoveryFees, { state.paymentTermsRecoveryFees = it }, restore)
                rewriteTfv(state.paymentTermsLateFees, { state.paymentTermsLateFees = it }, restore)
                rewriteTfv(state.paymentTermsDiscount, { state.paymentTermsDiscount = it }, restore)
            }
        }

        return { restore.forEach { it() } }
    }

    private fun rewriteParty(party: ClientOrIssuerState, restore: MutableList<() -> Unit>) {
        rewriteNullableTfv(party.firstName, { party.firstName = it }, restore)
        rewriteTfv(party.name, { party.name = it }, restore)
        rewriteNullableTfv(party.phone, { party.phone = it }, restore)
        rewriteNullableTfv(party.notes, { party.notes = it }, restore)
        rewriteNullableTfv(party.companyId1Label, { party.companyId1Label = it }, restore)
        rewriteNullableTfv(party.companyId1Number, { party.companyId1Number = it }, restore)
        rewriteNullableTfv(party.companyId2Label, { party.companyId2Label = it }, restore)
        rewriteNullableTfv(party.companyId2Number, { party.companyId2Number = it }, restore)
        rewriteNullableTfv(party.companyId3Label, { party.companyId3Label = it }, restore)
        rewriteNullableTfv(party.companyId3Number, { party.companyId3Number = it }, restore)
        rewriteNullableTfv(party.paymentIban, { party.paymentIban = it }, restore)
        rewriteNullableTfv(party.paymentBic, { party.paymentBic = it }, restore)

        party.addresses?.forEach { address ->
            rewriteNullableTfv(address.addressTitle, { address.addressTitle = it }, restore)
            rewriteNullableTfv(address.addressLine1, { address.addressLine1 = it }, restore)
            rewriteNullableTfv(address.addressLine2, { address.addressLine2 = it }, restore)
            rewriteNullableTfv(address.zipCode, { address.zipCode = it }, restore)
            rewriteNullableTfv(address.city, { address.city = it }, restore)
        }

        party.emails?.forEach { email ->
            rewriteTfv(email.email, { email.email = it }, restore)
        }

        party.banks.forEach { bank ->
            rewriteNullableTfv(bank.label, { bank.label = it }, restore)
            rewriteTfv(bank.identifier, { bank.identifier = it }, restore)
            rewriteTfv(bank.bic, { bank.bic = it }, restore)
        }
    }

    private fun rewriteProduct(product: DocumentProductState, restore: MutableList<() -> Unit>) {
        rewriteTfv(product.name, { product.name = it }, restore)
        rewriteNullableTfv(product.description, { product.description = it }, restore)
        rewriteNullableTfv(product.unit, { product.unit = it }, restore)
    }

    private fun rewriteTfv(
        current: TextFieldValue,
        set: (TextFieldValue) -> Unit,
        restore: MutableList<() -> Unit>,
    ) {
        val cleaned = sanitize(current.text)
        if (cleaned != current.text) {
            set(current.copy(text = cleaned))
            restore += { set(current) }
        }
    }

    private fun rewriteNullableTfv(
        current: TextFieldValue?,
        set: (TextFieldValue?) -> Unit,
        restore: MutableList<() -> Unit>,
    ) {
        if (current == null) return
        val cleaned = sanitize(current.text)
        if (cleaned != current.text) {
            set(current.copy(text = cleaned))
            restore += { set(current) }
        }
    }
}
