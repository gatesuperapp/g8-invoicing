package com.a4a.g8invoicing.facturx

import com.a4a.g8invoicing.data.models.ClientType
import com.a4a.g8invoicing.data.models.ProductNature
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Behavioural spec for [TaxCategoryResolver]. Each test names the article
 * from the FR CGI / EU VAT Directive it maps to, so the intent survives
 * future reads even if the resolver rules shift.
 */
class TaxCategoryTest {

    // Helper to keep each case focused on the fields it actually cares
    // about — every unspecified input falls to a "national B2B services"
    // baseline (issuer FR, buyer FR, no exemption, no intra-EU, PRO client,
    // SERVICE at 20%). That baseline itself resolves to S so a broken test
    // that doesn't override anything meaningful still fails loudly.
    private fun ctx(
        issuerCountryCode: String? = "FR",
        buyerCountryCode: String? = "FR",
        issuerVatExempt: Boolean = false,
        issuerIntraEuSales: Boolean = false,
        clientType: ClientType? = ClientType.PROFESSIONAL,
        productNature: ProductNature? = ProductNature.SERVICE,
        taxRate: Double? = 20.0,
    ) = TaxContext(
        issuerCountryCode = issuerCountryCode,
        buyerCountryCode = buyerCountryCode,
        issuerVatExempt = issuerVatExempt,
        issuerIntraEuSales = issuerIntraEuSales,
        clientType = clientType,
        productNature = productNature,
        taxRate = taxRate,
    )

    // ---- E — franchise en base (art. 293B CGI) ----------------------------

    @Test
    fun `franchise en base overrides every other rule`() {
        // Even for an intra-EU B2B goods line that would normally resolve to
        // K, franchise en base wins: the issuer has no VAT collection
        // capacity at all.
        assertEquals(
            TaxCategory.E,
            TaxCategoryResolver.resolve(
                ctx(
                    issuerVatExempt = true,
                    buyerCountryCode = "DE",
                    issuerIntraEuSales = true,
                    productNature = ProductNature.GOODS,
                ),
            ),
        )
    }

    @Test
    fun `franchise en base beats zero rate and export`() {
        assertEquals(
            TaxCategory.E,
            TaxCategoryResolver.resolve(
                ctx(issuerVatExempt = true, taxRate = 0.0, buyerCountryCode = "US"),
            ),
        )
    }

    // ---- G — export outside EU --------------------------------------------

    @Test
    fun `sale to US resolves to G regardless of goods or services`() {
        assertEquals(
            TaxCategory.G,
            TaxCategoryResolver.resolve(
                ctx(buyerCountryCode = "US", productNature = ProductNature.SERVICE),
            ),
        )
        assertEquals(
            TaxCategory.G,
            TaxCategoryResolver.resolve(
                ctx(buyerCountryCode = "US", productNature = ProductNature.GOODS),
            ),
        )
    }

    @Test
    fun `sale to UK (post-Brexit non-EU) resolves to G`() {
        // UK dropped out of EU_COUNTRIES post-Brexit — good regression guard.
        assertEquals(
            TaxCategory.G,
            TaxCategoryResolver.resolve(ctx(buyerCountryCode = "GB")),
        )
    }

    @Test
    fun `export ignores the intra-EU sales opt-in`() {
        // The intra-EU checkbox is orthogonal to exports — G still applies
        // whether it's ticked or not.
        assertEquals(
            TaxCategory.G,
            TaxCategoryResolver.resolve(
                ctx(buyerCountryCode = "CH", issuerIntraEuSales = true),
            ),
        )
    }

    // ---- K — intra-EU B2B goods (art. 138) --------------------------------

    @Test
    fun `intra-EU B2B goods resolve to K`() {
        assertEquals(
            TaxCategory.K,
            TaxCategoryResolver.resolve(
                ctx(
                    buyerCountryCode = "DE",
                    issuerIntraEuSales = true,
                    clientType = ClientType.PROFESSIONAL,
                    productNature = ProductNature.GOODS,
                ),
            ),
        )
    }

    // ---- AE — intra-EU B2B services (art. 44) -----------------------------

    @Test
    fun `intra-EU B2B services resolve to AE`() {
        assertEquals(
            TaxCategory.AE,
            TaxCategoryResolver.resolve(
                ctx(
                    buyerCountryCode = "IT",
                    issuerIntraEuSales = true,
                    clientType = ClientType.PROFESSIONAL,
                    productNature = ProductNature.SERVICE,
                ),
            ),
        )
    }

    @Test
    fun `intra-EU B2B unknown nature defaults to AE not S`() {
        // Safer to over-report reverse-charge than to silently apply FR VAT
        // to an EU buyer entitled to art. 44 auto-liquidation.
        assertEquals(
            TaxCategory.AE,
            TaxCategoryResolver.resolve(
                ctx(
                    buyerCountryCode = "ES",
                    issuerIntraEuSales = true,
                    clientType = ClientType.PROFESSIONAL,
                    productNature = null,
                ),
            ),
        )
    }

    @Test
    fun `intra-EU without the sales opt-in falls back to national S`() {
        // Issuer that doesn't cross-border invoice treats every buyer as
        // domestic — even when the countries technically differ.
        assertEquals(
            TaxCategory.S,
            TaxCategoryResolver.resolve(
                ctx(
                    buyerCountryCode = "DE",
                    issuerIntraEuSales = false,
                    clientType = ClientType.PROFESSIONAL,
                    productNature = ProductNature.GOODS,
                ),
            ),
        )
    }

    @Test
    fun `intra-EU B2C stays on S regardless of goods or services`() {
        // OSS/IOSS thresholds mean B2C intra-EU is charged at either the
        // issuer's or the buyer's rate depending on volume — we don't try
        // to split that category-code-wise, we stay on S and let the user
        // manage the rate.
        assertEquals(
            TaxCategory.S,
            TaxCategoryResolver.resolve(
                ctx(
                    buyerCountryCode = "BE",
                    issuerIntraEuSales = true,
                    clientType = ClientType.INDIVIDUAL,
                    productNature = ProductNature.GOODS,
                ),
            ),
        )
    }

    @Test
    fun `intra-EU with null client type stays on S (no forced AE-K assumption)`() {
        // The export gate asks the user before generating Factur-X, so by
        // the time we resolve categories the client type is expected to be
        // set. If it isn't, treat as B2C to be safe — S beats a wrong
        // AE that would strip VAT from a private buyer.
        assertEquals(
            TaxCategory.S,
            TaxCategoryResolver.resolve(
                ctx(
                    buyerCountryCode = "PT",
                    issuerIntraEuSales = true,
                    clientType = null,
                    productNature = ProductNature.SERVICE,
                ),
            ),
        )
    }

    // ---- Z — zero-rated national (rare) -----------------------------------

    @Test
    fun `explicit 0 percent rate on national line resolves to Z`() {
        assertEquals(
            TaxCategory.Z,
            TaxCategoryResolver.resolve(ctx(taxRate = 0.0)),
        )
    }

    @Test
    fun `null tax rate on national line stays on S not Z`() {
        // Null = unknown ≠ 0. A blank rate field shouldn't silently emit a
        // zero-rated line the user didn't ask for.
        assertEquals(
            TaxCategory.S,
            TaxCategoryResolver.resolve(ctx(taxRate = null)),
        )
    }

    // ---- S — national default ---------------------------------------------

    @Test
    fun `standard national B2B invoice resolves to S`() {
        assertEquals(TaxCategory.S, TaxCategoryResolver.resolve(ctx()))
    }

    @Test
    fun `standard national B2C invoice resolves to S`() {
        assertEquals(
            TaxCategory.S,
            TaxCategoryResolver.resolve(ctx(clientType = ClientType.INDIVIDUAL)),
        )
    }

    @Test
    fun `same country but issuer country in lowercase still resolves nationally`() {
        // Country codes flow through .uppercase() so a lowercase entry
        // shouldn't accidentally trigger the cross-border branch.
        assertEquals(
            TaxCategory.S,
            TaxCategoryResolver.resolve(
                ctx(issuerCountryCode = "fr", buyerCountryCode = "FR"),
            ),
        )
    }
}
