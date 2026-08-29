package com.a4a.g8invoicing.data.models

import com.a4a.g8invoicing.getSystemCountryCode

/**
 * ISO 3166-1 alpha-2 country codes used by the address form (Factur-X BT-40 / BT-55 /
 * BT-80). Deliberately curated rather than the full 249-entry ISO list: covers the EU,
 * European non-EU, main non-European trading partners, French-speaking / DROM-COM
 * territories that our indie users bill from or to. If a code is missing, the form
 * still accepts free-entry as a fallback so the list stays a UX shortcut, not a wall.
 *
 * Display names in French — this app is FR-first. Multi-locale display can be layered
 * later via stringResource lookup keyed on the code.
 */
object CountryCodes {

    /** All supported codes → French display name. */
    val ALL: Map<String, String> = linkedMapOf(
        // Europe (EU + neighbours)
        "FR" to "France",
        "BE" to "Belgique",
        "LU" to "Luxembourg",
        "CH" to "Suisse",
        "DE" to "Allemagne",
        "ES" to "Espagne",
        "IT" to "Italie",
        "NL" to "Pays-Bas",
        "PT" to "Portugal",
        "GB" to "Royaume-Uni",
        "IE" to "Irlande",
        "AT" to "Autriche",
        "DK" to "Danemark",
        "SE" to "Suède",
        "FI" to "Finlande",
        "NO" to "Norvège",
        "IS" to "Islande",
        "PL" to "Pologne",
        "CZ" to "Tchéquie",
        "SK" to "Slovaquie",
        "HU" to "Hongrie",
        "RO" to "Roumanie",
        "BG" to "Bulgarie",
        "GR" to "Grèce",
        "HR" to "Croatie",
        "SI" to "Slovénie",
        "EE" to "Estonie",
        "LV" to "Lettonie",
        "LT" to "Lituanie",
        "MT" to "Malte",
        "CY" to "Chypre",
        "LI" to "Liechtenstein",
        "MC" to "Monaco",
        "AD" to "Andorre",
        "SM" to "Saint-Marin",
        "VA" to "Vatican",

        // Amériques
        "US" to "États-Unis",
        "CA" to "Canada",
        "MX" to "Mexique",
        "BR" to "Brésil",
        "AR" to "Argentine",
        "CL" to "Chili",
        "CO" to "Colombie",
        "PE" to "Pérou",

        // Asie / Océanie
        "CN" to "Chine",
        "JP" to "Japon",
        "KR" to "Corée du Sud",
        "IN" to "Inde",
        "SG" to "Singapour",
        "AE" to "Émirats arabes unis",
        "IL" to "Israël",
        "TR" to "Turquie",
        "AU" to "Australie",
        "NZ" to "Nouvelle-Zélande",

        // Afrique / Maghreb / Moyen-Orient francophone
        "MA" to "Maroc",
        "DZ" to "Algérie",
        "TN" to "Tunisie",
        "SN" to "Sénégal",
        "CI" to "Côte d'Ivoire",
        "CM" to "Cameroun",
        "BF" to "Burkina Faso",
        "ML" to "Mali",
        "MG" to "Madagascar",
        "MU" to "Maurice",
        "LB" to "Liban",
        "EG" to "Égypte",
        "ZA" to "Afrique du Sud",

        // DROM-COM (facturation intra-France mais destinataire hors métropole)
        "RE" to "Réunion",
        "GP" to "Guadeloupe",
        "MQ" to "Martinique",
        "GF" to "Guyane française",
        "YT" to "Mayotte",
        "PF" to "Polynésie française",
        "NC" to "Nouvelle-Calédonie",
    )

    /**
     * Cascade for the default country of a newly-created address:
     * 1. [lastUsedCode] — the country of the most recently created address (from
     *    `ClientOrIssuerAddress.getLastCountryCode`), so someone who mostly bills
     *    French clients doesn't re-select FR on every new address.
     * 2. Device locale country via [getSystemCountryCode] — first-run behavior.
     * 3. Fallback "FR" — locale exposes no country (rare, e.g. locale = "und").
     * Result is always uppercase.
     */
    fun pickDefaultForNewAddress(lastUsedCode: String?): String {
        val fromLast = lastUsedCode?.trim().orEmpty()
        if (fromLast.isNotEmpty()) return fromLast.uppercase()
        val fromLocale = getSystemCountryCode()
        if (fromLocale.isNotEmpty() && fromLocale in ALL) return fromLocale
        return "FR"
    }

    /** Nom affiché pour un code (fallback = le code lui-même s'il n'est pas connu). */
    fun displayNameOf(code: String?): String {
        if (code.isNullOrBlank()) return ""
        return ALL[code.uppercase()] ?: code.uppercase()
    }

    /**
     * Union européenne — 27 États membres (post-Brexit, sans UK). Utilisé pour
     * conditionner l'affichage du toggle « Ventes hors de mon pays (UE) » sur le
     * profil émetteur : si l'émetteur n'est pas dans l'UE, la question n'a pas de
     * sens et on cache l'option.
     */
    private val EU_COUNTRIES: Set<String> = setOf(
        "AT", "BE", "BG", "CY", "CZ", "DE", "DK", "EE", "ES", "FI",
        "FR", "GR", "HR", "HU", "IE", "IT", "LT", "LU", "LV", "MT",
        "NL", "PL", "PT", "RO", "SE", "SI", "SK",
    )

    fun isInEU(code: String?): Boolean =
        code?.uppercase()?.let { it in EU_COUNTRIES } == true

    /**
     * ISO 3166-1 alpha-2 codes for countries that use IBAN as their standard
     * bank account identifier. Source: SWIFT/ECBS IBAN registry (~80 countries).
     * Used to switch the bank account form's identifier field between "IBAN"
     * (+ mod-97 validation) and a generic "Numéro de compte" fallback.
     */
    private val IBAN_COUNTRIES: Set<String> = setOf(
        "AD", "AE", "AL", "AT", "AZ", "BA", "BE", "BG", "BH", "BR",
        "BY", "CH", "CR", "CY", "CZ", "DE", "DK", "DO", "EE", "EG",
        "ES", "FI", "FO", "FR", "GB", "GE", "GI", "GL", "GR", "GT",
        "HR", "HU", "IE", "IL", "IQ", "IS", "IT", "JO", "KW", "KZ",
        "LB", "LC", "LI", "LT", "LU", "LV", "LY", "MC", "MD", "ME",
        "MK", "MR", "MT", "MU", "NL", "NO", "PK", "PL", "PS", "PT",
        "QA", "RO", "RS", "SA", "SC", "SE", "SI", "SK", "SM", "ST",
        "SV", "TL", "TN", "TR", "UA", "VA", "VG", "XK",
    )

    fun isIbanCountry(code: String?): Boolean =
        code?.uppercase()?.let { it in IBAN_COUNTRIES } == true

    /**
     * Countries where B2B invoices commonly carry a mandatory withholding tax
     * that the seller subtracts from the invoice total (IRPF in Spain, IRS
     * retenção in Portugal, ritenuta d'acconto in Italy, ISR/IVA retention in
     * Mexico, etc.). Gates the "Tax withholding" toggle on the issuer form —
     * hidden entirely outside this set so French / German / etc. users don't
     * see an option that doesn't apply to them.
     */
    private val RETENTION_COUNTRIES: Set<String> = setOf(
        "ES", "PT", "IT", "JP", "CL", "PE", "MX", "BR", "CO",
    )

    fun isRetentionCountry(code: String?): Boolean =
        code?.uppercase()?.let { it in RETENTION_COUNTRIES } == true

    /**
     * Default retention rate (as a percentage) proposed when the user first
     * adds a withholding line, per the issuer's country. Editable afterwards.
     * Falls back to 15 (the most common rate across the set) for unknown codes.
     *
     * Sources — commonly-cited standard rates:
     *   ES = 15 (IRPF, 7 for new autoentrepreneurs' 3 first years)
     *   PT = 25 (IRS retenção na fonte)
     *   IT = 20 (ritenuta d'acconto)
     *   JP = 10.21 (源泉徴収税額)
     *   CL = 14.5 (retención boleta de honorarios)
     *   PE =  8 (renta de 4ta categoría)
     *   MX = 10 (ISR — IVA retention separate, user adds a second line)
     *   BR = 15 (IRRF, variable — 15 is a common indicative rate)
     *   CO = 11 (retención en la fuente, variable — 11 common for services)
     */
    fun defaultRetentionRate(code: String?): Double = when (code?.uppercase()) {
        "ES" -> 15.0
        "PT" -> 25.0
        "IT" -> 20.0
        "JP" -> 10.21
        "CL" -> 14.5
        "PE" -> 8.0
        "MX" -> 10.0
        "BR" -> 15.0
        "CO" -> 11.0
        else -> 15.0
    }
}
