package com.a4a.g8invoicing.facturx

/**
 * Classification of the 3 free-label "company identifier" slots
 * ([`companyId1/2/3Label`] + [`companyId1/2/3Number`]) on a party towards
 * the CII / Factur-X mapping.
 *
 * The user is free to relabel each slot ("SIRET", "TVA", "RCS" by default
 * on FR — but they can rename any of them). The XML export can't trust
 * a positional convention (was: `companyId1` → SIREN, `companyId2` → VAT)
 * so we classify at export time using two signals in order:
 *
 * 1. **Label-first** — if the user-typed label contains a well-known keyword
 *    ("siret", "tva", "ein", "cuit"…), we trust that keyword. The value is
 *    then validated against the expected format for that keyword; a mismatch
 *    surfaces as a modale Oups warning (via
 *    [CiiValidationIssue.IssuerCompanyIdLabelMismatch]) and the slot is
 *    skipped from the XML.
 * 2. **Content regex** — if no keyword is recognised, we run a regex sweep
 *    scoped by the issuer's country (`REGEX_PATTERNS_BY_COUNTRY[country]`)
 *    plus the universal EU VAT pattern. First match wins.
 * 3. **Skip** — nothing matches → the value stays on the PDF but is not
 *    exported to CII. If a whole party ends up with no
 *    [CiiTarget.LegalOrg] / [CiiTarget.VatRegistration] /
 *    [CiiTarget.FiscalRegistration], `BR-CO-26` requires we still emit
 *    something under BT-29 (SellerTradeParty/ID, no schemeID) as a
 *    last-resort fallback — see [CiiXmlBuilder].
 */

// Shared preClean functions to keep the enum declarations below terse.
// Declared before the enum so the enum entries can capture them safely
// (top-level val init runs before enum-entry construction at class load).
private val digitsOnly: (String) -> String = { s -> s.filter { c -> c.isDigit() } }
private val alnumUpper: (String) -> String = { s -> s.filter { c -> c.isLetterOrDigit() }.uppercase() }

/** Where in the CII XML a classified value lands. */
enum class CiiTarget {
    /** BT-30 — SpecifiedLegalOrganization/ID with an ICD 4-digit schemeID. */
    LegalOrg,

    /** BT-31 — SpecifiedTaxRegistration/ID schemeID="VA" (EU VAT). */
    VatRegistration,

    /** BT-32 — SpecifiedTaxRegistration/ID schemeID="FC" (non-EU tax id). */
    FiscalRegistration,
}

/**
 * A concrete identifier type we know how to map. Each entry carries:
 * - [target] — the CII slot it lands in
 * - [schemeId] — the attribute value on the emitted `<ram:ID>` tag
 * - [displayName] — human-readable name (used in the modale Oups warning)
 * - [expectedFormat] — plain-language format hint (used in the same warning)
 * - [preClean] — normaliser applied before both matching and XML emit:
 *   strips separators, uppercases alnum content. The regex matches the
 *   pre-cleaned form.
 * - [pattern] — the regex the [preClean]-ed value has to match.
 * - [toXml] — final transform on the pre-cleaned value for the XML tag
 *   contents (e.g. SIRET truncates to its 9-digit SIREN prefix because
 *   schemeID "0002" requires exactly 9 digits per BR-FR-10 / BR-FR-32).
 *   Defaults to identity — most kinds emit the pre-cleaned value as-is.
 */
enum class CompanyIdKind(
    val target: CiiTarget,
    val schemeId: String,
    val displayName: String,
    val expectedFormat: String,
    val preClean: (String) -> String,
    val pattern: Regex,
    val toXml: (String) -> String = { it },
) {
    // ------- BT-30 (registre légal) — codes ISO 6523 -------
    // The FR Factur-X national profile (BR-FR-10 / BR-FR-32) pins BT-30 =
    // SIREN = exactly 9 digits under schemeID "0002" (INSEE SIREN). SIRET
    // (14 digits, schemeID "0009") is not a valid BT-30 value on that
    // profile — Chorus Pro / veraPDF reject it with "SIREN empty / must be
    // 9 digits". So we announce SIRET as 0002 and truncate at emit time to
    // its 9-digit SIREN prefix (the last 5 digits are the establishment
    // code, kept on the visible PDF but dropped from the CII XML).
    SIRET(
        CiiTarget.LegalOrg, "0002", "SIRET", "14 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{14}$"""),
        toXml = { it.take(9) },
    ),
    SIREN(
        CiiTarget.LegalOrg, "0002", "SIREN", "9 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{9}$"""),
    ),
    KVK(
        CiiTarget.LegalOrg, "0106", "KvK", "8 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{8}$"""),
    ),
    BCE(
        CiiTarget.LegalOrg, "0208", "N° d'entreprise (BCE)", "10 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{10}$"""),
    ),
    CODICE_FISCALE(
        CiiTarget.LegalOrg, "0210", "Codice Fiscale", "11 à 16 caractères alphanumériques",
        preClean = alnumUpper,
        pattern = Regex("""^[A-Z0-9]{11,16}$"""),
    ),
    CVR(
        CiiTarget.LegalOrg, "0184", "CVR", "8 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{8}$"""),
    ),
    SE_ORGNR(
        CiiTarget.LegalOrg, "0007", "Organisationsnummer", "10 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{10}$"""),
    ),
    NO_ORGNR(
        CiiTarget.LegalOrg, "0192", "Organisasjonsnummer", "9 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{9}$"""),
    ),
    LEI(
        CiiTarget.LegalOrg, "0199", "LEI", "20 caractères alphanumériques",
        preClean = alnumUpper,
        pattern = Regex("""^[A-Z0-9]{18}\d{2}$"""),
    ),
    DUNS(
        CiiTarget.LegalOrg, "0060", "DUNS", "9 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{9}$"""),
    ),

    // ------- BT-31 (TVA intracom EU) -------
    // Broad shape regex — a stricter per-country match runs in the
    // pre-flight validator. Accepts already-prefixed (FR12345678901) or
    // bare digits (12345678901); [prefixEuVatIfMissing] adds the country
    // prefix at emit time when the user didn't type it.
    EU_VAT(
        CiiTarget.VatRegistration, "VA", "N° TVA intracom",
        "code pays ISO (FR, DE, IT…) + 8 à 12 caractères",
        preClean = alnumUpper,
        pattern = Regex("""^([A-Z]{2}[A-Z0-9]{2,13}|\d{8,13})$"""),
    ),

    // ------- BT-32 (fiscal local non-UE) — schemeID "FC" -------
    US_EIN(
        CiiTarget.FiscalRegistration, "FC", "EIN",
        "9 chiffres au format XX-XXXXXXX",
        preClean = { it.trim() },
        pattern = Regex("""^\d{2}-\d{7}$"""),
    ),
    AR_CUIT(
        CiiTarget.FiscalRegistration, "FC", "CUIT / CUIL",
        "11 chiffres commençant par 20/23/24/27/30/33/34",
        preClean = digitsOnly,
        pattern = Regex("""^(20|23|24|27|30|33|34)\d{9}$"""),
    ),
    BR_CNPJ(
        CiiTarget.FiscalRegistration, "FC", "CNPJ",
        "14 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{14}$"""),
    ),
    MX_RFC(
        CiiTarget.FiscalRegistration, "FC", "RFC",
        "12 (personne morale) ou 13 (personne physique) caractères",
        preClean = { it.filter { c -> !c.isWhitespace() }.uppercase() },
        pattern = Regex("""^[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{3}$"""),
    ),
    CL_RUT(
        CiiTarget.FiscalRegistration, "FC", "RUT",
        "7 à 8 chiffres + '-' + clé (0-9 ou K)",
        preClean = { it.filter { c -> !c.isWhitespace() && c != '.' }.uppercase() },
        pattern = Regex("""^\d{7,8}-[\dK]$"""),
    ),
    CO_NIT(
        CiiTarget.FiscalRegistration, "FC", "NIT",
        "9 à 10 chiffres + clé",
        preClean = { it.filter { c -> !c.isWhitespace() && c != '.' } },
        pattern = Regex("""^\d{9,10}-\d$"""),
    ),
    PE_RUC(
        CiiTarget.FiscalRegistration, "FC", "RUC",
        "11 chiffres commençant par 10/15/17/20",
        preClean = digitsOnly,
        pattern = Regex("""^(10|15|17|20)\d{9}$"""),
    ),
    UY_RUT(
        CiiTarget.FiscalRegistration, "FC", "RUT",
        "12 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{12}$"""),
    ),
    EC_RUC(
        CiiTarget.FiscalRegistration, "FC", "RUC",
        "13 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{13}$"""),
    ),
    PY_RUC(
        CiiTarget.FiscalRegistration, "FC", "RUC",
        "6 à 8 chiffres + '-' + DV",
        preClean = { it.filter { c -> !c.isWhitespace() } },
        pattern = Regex("""^\d{6,8}-\d$"""),
    ),
    CR_CEDULA(
        CiiTarget.FiscalRegistration, "FC", "Cédula jurídica",
        "10 à 12 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{10,12}$"""),
    ),
    BO_NIT(
        CiiTarget.FiscalRegistration, "FC", "NIT",
        "7 à 12 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{7,12}$"""),
    ),
    DO_RNC(
        CiiTarget.FiscalRegistration, "FC", "RNC",
        "9 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{9}$"""),
    ),
    TR_VKN(
        CiiTarget.FiscalRegistration, "FC", "VKN",
        "10 chiffres",
        preClean = digitsOnly,
        pattern = Regex("""^\d{10}$"""),
    ),
    IN_GSTIN(
        CiiTarget.FiscalRegistration, "FC", "GSTIN",
        "15 caractères (état + PAN + check)",
        preClean = { it.filter { c -> !c.isWhitespace() }.uppercase() },
        pattern = Regex("""^\d{2}[A-Z]{5}\d{4}[A-Z]\d[A-Z0-9]{2}$"""),
    ),
    ;

    /** Format check against the pre-cleaned form. */
    fun matches(value: String): Boolean = pattern.matches(preClean(value))

    /** Full clean → emit-transform pipeline. Result lands in the XML tag. */
    fun normalize(value: String): String = toXml(preClean(value))
}

/**
 * Result of [classifyCompanyId] for a single `(label, value)` tuple.
 */
sealed class CompanyIdClassification {
    /** Value cleanly maps to a target CII field. */
    data class Match(
        val kind: CompanyIdKind,
        /** The value after [CompanyIdKind.normalize] — ready for XML emit. */
        val normalizedValue: String,
    ) : CompanyIdClassification()

    /**
     * Label carries a known keyword ("siret", "cuit", …) but the value
     * doesn't match the format that keyword expects. The value is skipped
     * from the XML; the pre-flight validator surfaces a warning so the
     * user corrects it before export.
     */
    data class LabelMismatch(
        val expected: CompanyIdKind,
        /** The user-typed label as-is (for display in the warning). */
        val fieldLabel: String,
        val rawValue: String,
    ) : CompanyIdClassification()

    /**
     * Value matches a known kind's regex, but that kind isn't valid for the
     * party's country (e.g. a 9-digit "SIREN" typed on a ZA-country issuer).
     * Skipped from the XML — no valid schemeID we can attach to a foreign
     * SIRET / SIREN under this country — so the pre-flight validator raises
     * an informational warning and the export goes on with the value simply
     * absent from the routing block.
     *
     * [suggestedCountry] is the country the classifier's fallback sweep
     * associated with the matched kind (best-effort, may be null when the
     * kind isn't country-scoped). Kept so the Oups message can hint at the
     * mismatch when useful.
     */
    data class CountryMismatch(
        val kind: CompanyIdKind,
        val fieldLabel: String,
        val rawValue: String,
        val partyCountry: String?,
    ) : CompanyIdClassification()

    /** Neither label nor content pattern gave us a mapping. */
    object Skip : CompanyIdClassification()
}

/**
 * Label keyword → candidate [CompanyIdKind]s lookup. Match uses `contains`
 * on the lower-cased + accent-stripped label, so "N° SIRET (société mère)"
 * still hits the SIRET entry.
 *
 * Order of keywords matters when a label contains several — first-listed
 * wins ("codice fiscale" before "iva" so an IT user with a `Codice Fiscale`
 * labelled slot doesn't misclassify as VAT).
 *
 * Order inside a value list matters for ambiguous keywords like `rut` (used
 * in CL, UY): we walk the list and keep the first kind whose format-check
 * accepts the value. If none does, we emit [LabelMismatch] with the first
 * kind as the "expected format" for the user warning.
 *
 * German legal register (`HRB`, `HRA`, `Handelsregister`, `Steuer-Nr.`) is
 * intentionally absent — none has a standardised ICD schemeID, so we let
 * the value fall through to the country regex fallback (which for DE only
 * tries EU_VAT).
 */
private val LABEL_KEYWORDS: List<Pair<String, List<CompanyIdKind>>> = listOf(
    // BT-30 first — the more specific labels tend to be legal registers.
    "codice fiscale" to listOf(CompanyIdKind.CODICE_FISCALE),
    "kvk" to listOf(CompanyIdKind.KVK),
    "bce" to listOf(CompanyIdKind.BCE),
    "kbo" to listOf(CompanyIdKind.BCE),
    "cvr" to listOf(CompanyIdKind.CVR),
    "organisasjonsnummer" to listOf(CompanyIdKind.NO_ORGNR),
    "organisationsnummer" to listOf(CompanyIdKind.SE_ORGNR),
    "orgnr" to listOf(CompanyIdKind.NO_ORGNR, CompanyIdKind.SE_ORGNR),
    "siret" to listOf(CompanyIdKind.SIRET),
    "siren" to listOf(CompanyIdKind.SIREN),
    // RCS mention on a French invoice carries the SIREN — same schemeID 0002
    // emission path, so map here.
    "rcs" to listOf(CompanyIdKind.SIREN),
    "lei" to listOf(CompanyIdKind.LEI),
    "duns" to listOf(CompanyIdKind.DUNS),

    // BT-32 (non-EU fiscal) — declared before VAT so `rfc` etc. don't collide.
    "ein" to listOf(CompanyIdKind.US_EIN),
    "cuit" to listOf(CompanyIdKind.AR_CUIT),
    "cuil" to listOf(CompanyIdKind.AR_CUIT),
    "cnpj" to listOf(CompanyIdKind.BR_CNPJ),
    "rfc" to listOf(CompanyIdKind.MX_RFC),
    "rut" to listOf(CompanyIdKind.CL_RUT, CompanyIdKind.UY_RUT),
    "nit" to listOf(CompanyIdKind.CO_NIT, CompanyIdKind.BO_NIT),
    "ruc" to listOf(CompanyIdKind.PE_RUC, CompanyIdKind.EC_RUC, CompanyIdKind.PY_RUC),
    "rnc" to listOf(CompanyIdKind.DO_RNC),
    "cedula" to listOf(CompanyIdKind.CR_CEDULA),
    "vkn" to listOf(CompanyIdKind.TR_VKN),
    "gstin" to listOf(CompanyIdKind.IN_GSTIN),

    // BT-31 (VAT) — last so specific labels above win.
    "n° tva" to listOf(CompanyIdKind.EU_VAT),
    "tva" to listOf(CompanyIdKind.EU_VAT),
    "vat" to listOf(CompanyIdKind.EU_VAT),
    "ust" to listOf(CompanyIdKind.EU_VAT),
    "iva" to listOf(CompanyIdKind.EU_VAT),
    "btw" to listOf(CompanyIdKind.EU_VAT),
)

/**
 * Content-based fallback: which kinds to try, in order, when the label
 * didn't produce a keyword hit. Keyed by the issuer's country so we don't
 * accidentally classify a French phone number as a Turkish VKN.
 * Every list ends with [CompanyIdKind.EU_VAT] because a user in any
 * country might have typed a prefixed EU VAT number.
 */
private val REGEX_PATTERNS_BY_COUNTRY: Map<String, List<CompanyIdKind>> = mapOf(
    // SIRET first (strict 14) then SIREN (strict 9) so a 14-digit value
    // hits SIRET, a 9-digit value hits SIREN — mutually exclusive by length.
    "FR" to listOf(CompanyIdKind.SIRET, CompanyIdKind.SIREN, CompanyIdKind.EU_VAT),
    "DE" to listOf(CompanyIdKind.EU_VAT),
    "IT" to listOf(CompanyIdKind.CODICE_FISCALE, CompanyIdKind.EU_VAT),
    "ES" to listOf(CompanyIdKind.EU_VAT),
    "BE" to listOf(CompanyIdKind.BCE, CompanyIdKind.EU_VAT),
    "NL" to listOf(CompanyIdKind.KVK, CompanyIdKind.EU_VAT),
    "SE" to listOf(CompanyIdKind.SE_ORGNR, CompanyIdKind.EU_VAT),
    "FI" to listOf(CompanyIdKind.EU_VAT),
    "DK" to listOf(CompanyIdKind.CVR, CompanyIdKind.EU_VAT),
    "NO" to listOf(CompanyIdKind.NO_ORGNR),
    "GB" to listOf(CompanyIdKind.EU_VAT),
    "CH" to listOf(CompanyIdKind.EU_VAT),

    // Non-EU (fiscal FC + LEI/DUNS universels)
    "US" to listOf(CompanyIdKind.US_EIN, CompanyIdKind.LEI, CompanyIdKind.DUNS),
    "AR" to listOf(CompanyIdKind.AR_CUIT, CompanyIdKind.LEI),
    "BR" to listOf(CompanyIdKind.BR_CNPJ, CompanyIdKind.LEI),
    "MX" to listOf(CompanyIdKind.MX_RFC, CompanyIdKind.LEI),
    "CL" to listOf(CompanyIdKind.CL_RUT, CompanyIdKind.LEI),
    "CO" to listOf(CompanyIdKind.CO_NIT, CompanyIdKind.LEI),
    "PE" to listOf(CompanyIdKind.PE_RUC, CompanyIdKind.LEI),
    "UY" to listOf(CompanyIdKind.UY_RUT, CompanyIdKind.LEI),
    "EC" to listOf(CompanyIdKind.EC_RUC, CompanyIdKind.LEI),
    "PY" to listOf(CompanyIdKind.PY_RUC, CompanyIdKind.LEI),
    "CR" to listOf(CompanyIdKind.CR_CEDULA, CompanyIdKind.LEI),
    "BO" to listOf(CompanyIdKind.BO_NIT, CompanyIdKind.LEI),
    "DO" to listOf(CompanyIdKind.DO_RNC, CompanyIdKind.LEI),
    "TR" to listOf(CompanyIdKind.TR_VKN, CompanyIdKind.LEI),
    "IN" to listOf(CompanyIdKind.IN_GSTIN, CompanyIdKind.LEI),
)

/** Countries whose "unknown label" fallback still checks EU VAT. */
private val EU_COUNTRIES = setOf(
    "AT", "BE", "BG", "CY", "CZ", "DE", "DK", "EE", "EL", "GR", "ES", "FI",
    "FR", "HR", "HU", "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PL", "PT",
    "RO", "SE", "SI", "SK", "XI",
)

/**
 * Structural EU VAT patterns per country. Used to tighten
 * [CompanyIdKind.EU_VAT] classification when the party's country is known:
 * a "TVA" labelled slot on an FR issuer must match `FR[A-Z0-9]{2}\d{9}`
 * exactly, not just the broad EU shape. Unknown countries fall back to
 * the broad [CompanyIdKind.EU_VAT] pattern.
 *
 * Country codes are ISO 3166-1 alpha-2 (matching AddressState.countryCode)
 * with a Greek nuance: EL is the EU VAT prefix, GR the ISO country code —
 * both map to the same rule. Values are pre-cleaned (alnum + upper) before
 * matching.
 */
internal val EU_VAT_STRICT_PATTERNS: Map<String, Regex> = mapOf(
    "AT" to Regex("^ATU[0-9]{8}$"),
    "BE" to Regex("^BE[01][0-9]{9}$"),
    "BG" to Regex("^BG[0-9]{9,10}$"),
    "CY" to Regex("^CY[0-9]{8}[A-Z]$"),
    "CZ" to Regex("^CZ[0-9]{8,10}$"),
    "DE" to Regex("^DE[0-9]{9}$"),
    "DK" to Regex("^DK[0-9]{8}$"),
    "EE" to Regex("^EE[0-9]{9}$"),
    "EL" to Regex("^EL[0-9]{9}$"),
    "GR" to Regex("^EL[0-9]{9}$"),
    "ES" to Regex("^ES[A-Z0-9][0-9]{7}[A-Z0-9]$"),
    "FI" to Regex("^FI[0-9]{8}$"),
    "FR" to Regex("^FR[A-Z0-9]{2}[0-9]{9}$"),
    "HR" to Regex("^HR[0-9]{11}$"),
    "HU" to Regex("^HU[0-9]{8}$"),
    "IE" to Regex("^IE([0-9]{7}[A-Z]{1,2}|[0-9][A-Z*+][0-9]{5}[A-Z])$"),
    "IT" to Regex("^IT[0-9]{11}$"),
    "LT" to Regex("^LT([0-9]{9}|[0-9]{12})$"),
    "LU" to Regex("^LU[0-9]{8}$"),
    "LV" to Regex("^LV[0-9]{11}$"),
    "MT" to Regex("^MT[0-9]{8}$"),
    "NL" to Regex("^NL[0-9]{9}B[0-9]{2}$"),
    "PL" to Regex("^PL[0-9]{10}$"),
    "PT" to Regex("^PT[0-9]{9}$"),
    "RO" to Regex("^RO[0-9]{2,10}$"),
    "SE" to Regex("^SE[0-9]{12}$"),
    "SI" to Regex("^SI[0-9]{8}$"),
    "SK" to Regex("^SK[0-9]{10}$"),
    "XI" to Regex("^XI([0-9]{9}|[0-9]{12}|GD[0-9]{3}|HA[0-9]{3})$"),
)

/**
 * [CompanyIdKind.matches] but with a country-aware tightening for EU VAT:
 * when the country has a specific pattern in [EU_VAT_STRICT_PATTERNS], the
 * pre-cleaned value must match that exact pattern (BR-CO-09 + national
 * rules). Otherwise falls back to the broad kind pattern.
 */
private fun matchesForKind(kind: CompanyIdKind, value: String, country: String?): Boolean {
    if (kind == CompanyIdKind.EU_VAT && country != null) {
        EU_VAT_STRICT_PATTERNS[country.uppercase()]?.let { strict ->
            return strict.matches(kind.preClean(value))
        }
    }
    return kind.matches(value)
}

/**
 * Country-specific format hint used in the Oups modal when a VAT slot's
 * value doesn't match the strict per-country pattern. Falls back to the
 * generic EU_VAT.expectedFormat when the party's country isn't in the
 * strict table (unknown / non-EU country → the broad "8-12 caractères"
 * shape is the best we can say). Kept in French — same convention as
 * [CompanyIdKind.expectedFormat]; migrated to strings.xml alongside the
 * rest of the CII modal wording.
 */
internal fun euVatFormatHintForCountry(country: String?): String {
    val code = country?.trim()?.uppercase()?.takeIf { it.isNotBlank() }
        ?: return CompanyIdKind.EU_VAT.expectedFormat
    return EU_VAT_FORMAT_HINTS[code] ?: CompanyIdKind.EU_VAT.expectedFormat
}

private val EU_VAT_FORMAT_HINTS: Map<String, String> = mapOf(
    "AT" to "AT + U + 8 chiffres (ex. ATU12345678)",
    "BE" to "BE + 10 chiffres commençant par 0 ou 1 (ex. BE0123456789)",
    "BG" to "BG + 9 ou 10 chiffres",
    "CY" to "CY + 8 chiffres + 1 lettre",
    "CZ" to "CZ + 8, 9 ou 10 chiffres",
    "DE" to "DE + 9 chiffres (ex. DE123456789)",
    "DK" to "DK + 8 chiffres",
    "EE" to "EE + 9 chiffres",
    "EL" to "EL + 9 chiffres (Grèce)",
    "GR" to "EL + 9 chiffres (Grèce)",
    "ES" to "ES + 9 caractères alphanumériques (ex. ESX1234567X)",
    "FI" to "FI + 8 chiffres",
    "FR" to "FR + 2 caractères alphanumériques + 9 chiffres (ex. FR32123456789)",
    "HR" to "HR + 11 chiffres",
    "HU" to "HU + 8 chiffres",
    "IE" to "IE + 8 ou 9 caractères (ex. IE1234567X ou IE1X23456X)",
    "IT" to "IT + 11 chiffres",
    "LT" to "LT + 9 ou 12 chiffres",
    "LU" to "LU + 8 chiffres",
    "LV" to "LV + 11 chiffres",
    "MT" to "MT + 8 chiffres",
    "NL" to "NL + 9 chiffres + B + 2 chiffres (ex. NL123456789B01)",
    "PL" to "PL + 10 chiffres",
    "PT" to "PT + 9 chiffres",
    "RO" to "RO + 2 à 10 chiffres",
    "SE" to "SE + 12 chiffres",
    "SI" to "SI + 8 chiffres",
    "SK" to "SK + 10 chiffres",
    "XI" to "XI + 9 ou 12 chiffres (Irlande du Nord)",
)

/**
 * Normalise a label for keyword matching: lower-case, ASCII-fold the
 * common French accents, collapse whitespace. Kept minimal (no
 * java.text.Normalizer — not on iOS) since the keywords themselves are
 * short.
 */
private fun normalizeLabel(label: String): String = label
    .lowercase()
    .replace('é', 'e').replace('è', 'e').replace('ê', 'e').replace('ë', 'e')
    .replace('à', 'a').replace('â', 'a').replace('ä', 'a')
    .replace('î', 'i').replace('ï', 'i')
    .replace('ô', 'o').replace('ö', 'o')
    .replace('ù', 'u').replace('û', 'u').replace('ü', 'u')
    .replace('ç', 'c')
    .trim()

/**
 * Classify one company_id slot. See the file-level KDoc for the algorithm.
 *
 * @param label the user-typed label ("SIRET", "N° TVA", "Autre 1", …)
 * @param value the user-typed value
 * @param issuerCountry ISO 3166-1 alpha-2 country of the party (upper-cased)
 * @return [CompanyIdClassification.Match] when we know where the value goes,
 *   [CompanyIdClassification.LabelMismatch] when the label promises a kind
 *   the value doesn't fit, or [CompanyIdClassification.Skip] when nothing
 *   matches.
 */
fun classifyCompanyId(
    label: String?,
    value: String?,
    issuerCountry: String?,
): CompanyIdClassification {
    val rawValue = value?.trim().orEmpty()
    if (rawValue.isEmpty()) return CompanyIdClassification.Skip

    val country = issuerCountry?.uppercase()

    // Étage 1 — label-first. Walk the candidate list for the matched
    // keyword; keep the first kind whose format-check accepts the value
    // (disambiguates rut = CL/UY, nit = CO/BO, ruc = PE/EC/PY). EU VAT
    // matching is tightened per-country by [matchesForKind].
    val normalizedLabel = label?.let { normalizeLabel(it) }.orEmpty()
    if (normalizedLabel.isNotEmpty()) {
        val candidates = LABEL_KEYWORDS.firstOrNull { (kw, _) -> kw in normalizedLabel }?.second
        if (candidates != null) {
            val hit = candidates.firstOrNull { matchesForKind(it, rawValue, country) }
            return if (hit != null) {
                CompanyIdClassification.Match(hit, hit.normalize(rawValue))
            } else {
                CompanyIdClassification.LabelMismatch(
                    expected = candidates.first(),
                    fieldLabel = label?.trim().orEmpty(),
                    rawValue = rawValue,
                )
            }
        }
    }

    // Étage 2 — regex par pays
    val candidates: List<CompanyIdKind> = country
        ?.let { REGEX_PATTERNS_BY_COUNTRY[it] }
        ?: if (country in EU_COUNTRIES) listOf(CompanyIdKind.EU_VAT) else emptyList()

    val match = candidates.firstOrNull { matchesForKind(it, rawValue, country) }
    if (match != null) {
        return CompanyIdClassification.Match(match, match.normalize(rawValue))
    }

    // Étage 2 bis — country mismatch. When no country-scoped kind matched,
    // sweep every kind + every strict EU VAT pattern to detect a value that
    // looks like a valid ID for a DIFFERENT country. Lets us warn "this
    // looks like a SIREN but the party is in ZA" instead of silently
    // dropping the value from the CII XML. Skipped when the party has no
    // country set (we have no baseline to declare a mismatch).
    if (country != null) {
        val strayKind = CompanyIdKind.entries.firstOrNull { kind ->
            // Country-specific EU VAT patterns override the broad EU_VAT
            // regex — a "FR12345…" value on a DE issuer still hits the
            // sweep here via the FR strict pattern below.
            kind != CompanyIdKind.EU_VAT && kind.matches(rawValue)
        } ?: strayEuVatCountry(rawValue)?.let { CompanyIdKind.EU_VAT }
        if (strayKind != null) {
            return CompanyIdClassification.CountryMismatch(
                kind = strayKind,
                fieldLabel = label?.trim().orEmpty(),
                rawValue = rawValue,
                partyCountry = country,
            )
        }
    }

    // Étage 3 — skip
    return CompanyIdClassification.Skip
}

/** Returns the country whose strict EU VAT pattern accepts [rawValue], or
 *  null when no country pattern matches. Used by the country-mismatch sweep
 *  to catch a "FR…" value typed on a DE issuer or vice versa. */
private fun strayEuVatCountry(rawValue: String): String? {
    val cleaned = CompanyIdKind.EU_VAT.preClean(rawValue)
    return EU_VAT_STRICT_PATTERNS.entries.firstOrNull { (_, regex) -> regex.matches(cleaned) }?.key
}

/**
 * BR-CO-09 requires an EU VAT identifier to start with an ISO 3166-1
 * alpha-2 country prefix. Applied at emit time by [CiiXmlBuilder] on the
 * normalised value returned by [CompanyIdKind.EU_VAT]'s normaliser.
 */
internal fun prefixEuVatIfMissing(normalized: String, partyCountry: String?): String {
    if (normalized.isEmpty()) return normalized
    val first = normalized.take(2)
    val alreadyPrefixed = first.length == 2 && first.all { it.isLetter() }
    return if (alreadyPrefixed) normalized
    else (partyCountry?.uppercase() ?: "FR") + normalized
}
