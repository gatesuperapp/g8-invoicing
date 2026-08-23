package com.a4a.g8invoicing.facturx

import com.a4a.g8invoicing.data.models.ClientType
import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.data.models.ProductNature

/**
 * EN16931 tax category codes used by Factur-X for the line-level BT-151 and
 * the document-level BT-118 (issuer VAT exemption reason). We only ship the
 * codes we can drive deterministically from the app's data model — anything
 * more exotic (regional French DOM-TOM regimes, Canary Islands IGIC, Ceuta
 * IPSI, non-taxable supplies) is out of scope and falls back to [S].
 *
 * Reference: [EN16931-1:2017 Annex E] and [Factur-X 1.07.2 Extended §
 * BT-151 code lists].
 */
enum class TaxCategory {
    /** Standard rate — the default. National supply, or intra-EU B2C. */
    S,

    /**
     * Reverse-charge on intra-EU B2B **services** (art. 44 EU VAT Directive,
     * art. 259-1 CGI FR). The buyer self-assesses VAT in their country;
     * issuer emits without VAT with the "Autoliquidation" mention.
     */
    AE,

    /**
     * Intra-EU B2B supply of **goods** (art. 138 EU VAT Directive, art.
     * 262 ter CGI FR). Exempt on the issuer's side, taxed on arrival in
     * the buyer's country. Requires a valid VAT number from the buyer.
     */
    K,

    /** Zero-rated national supply — rare, e.g. some ES books/newspapers at 0%. */
    Z,

    /**
     * Émetteur en franchise en base (art. 293B CGI FR) or German
     * Kleinunternehmer (§19 UStG). No VAT collected regardless of client.
     */
    E,

    /** Export outside the EU — goods or services to a non-EU country. */
    G,
}

/**
 * Inputs the resolver needs to pick a category. All optional-ish fields are
 * nullable so a partially-filled invoice still resolves to a sane default
 * (fallback: [TaxCategory.S]).
 */
data class TaxContext(
    /** ISO 3166-1 alpha-2 of the issuer's country. Null → treat as national. */
    val issuerCountryCode: String?,
    /** ISO 3166-1 alpha-2 of the buyer's country. */
    val buyerCountryCode: String?,
    /** Émetteur en franchise en base / Kleinunternehmer. */
    val issuerVatExempt: Boolean,
    /**
     * Whether the issuer opted in to intra-EU cross-border invoicing. When
     * false, we treat every transaction as national (fallback [S]) even if
     * the countries differ — matches the "je ne facture qu'en France" case.
     */
    val issuerIntraEuSales: Boolean,
    /** Client's B2B/B2C nature. Null = unknown → treated as B2C (no K/AE). */
    val clientType: ClientType?,
    /** Line-level GOODS vs SERVICE. Null = unknown → treated as SERVICE. */
    val productNature: ProductNature?,
    /** Line VAT rate, e.g. 20.0 for 20%. Null = unknown → non-zero assumed. */
    val taxRate: Double?,
)

object TaxCategoryResolver {
    /**
     * Deterministic mapping — same inputs always produce the same code.
     * Rules are ordered from most-specific (issuer-wide overrides) to
     * least-specific (national fallback).
     */
    fun resolve(ctx: TaxContext): TaxCategory {
        // 1. Franchise en base takes priority over everything else — no VAT
        //    is collected regardless of who the buyer is or what the line
        //    sells. This is a per-issuer legal status, not a per-line choice.
        if (ctx.issuerVatExempt) return TaxCategory.E

        val issuer = ctx.issuerCountryCode?.uppercase()
        val buyer = ctx.buyerCountryCode?.uppercase()
        val crossBorder = issuer != null && buyer != null && issuer != buyer

        // 2. Cross-border to a non-EU country → export.
        if (crossBorder && !CountryCodes.isInEU(buyer)) return TaxCategory.G

        // 3. Cross-border within the EU — only meaningful when the issuer
        //    has enabled intra-EU sales (checkbox on their form). Without
        //    that opt-in, we treat the line as if it were national.
        if (crossBorder && CountryCodes.isInEU(buyer) && ctx.issuerIntraEuSales) {
            // Split B2B goods (K, art. 138) from B2B services (AE, art. 44).
            // B2C intra-EU stays on S: the OSS/IOSS thresholds decide whether
            // the issuer's country or the buyer's country rate applies, and
            // the user manages the rate manually per line — we don't switch
            // category code for it.
            if (ctx.clientType == ClientType.PROFESSIONAL) {
                return when (ctx.productNature) {
                    ProductNature.GOODS -> TaxCategory.K
                    // Null nature defaults to service — the safer bet since
                    // AE keeps the invoice VAT-free; picking S would silently
                    // charge FR VAT to an EU buyer entitled to reverse-charge.
                    ProductNature.SERVICE, null -> TaxCategory.AE
                }
            }
            // B2C or unknown client type: fall through to national rules.
        }

        // 4. Explicit 0% on a national line → Z (rare but legal, e.g. some
        //    ES books/newspapers). We only pick Z when the rate is exactly
        //    0.0 — a null rate is treated as "unknown" and stays on S.
        if (ctx.taxRate == 0.0) return TaxCategory.Z

        // 5. Default: standard national rate.
        return TaxCategory.S
    }
}
