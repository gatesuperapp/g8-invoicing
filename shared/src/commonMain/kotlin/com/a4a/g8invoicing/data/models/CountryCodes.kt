package com.a4a.g8invoicing.data.models

import com.a4a.g8invoicing.data.AppLocaleHolder
import com.a4a.g8invoicing.getLocalizedCountryName
import com.a4a.g8invoicing.getSystemCountryCode

/**
 * ISO 3166-1 alpha-2 country codes used by the address form (Factur-X BT-40 / BT-55 /
 * BT-80). Covers the full ISO list so any client/issuer/bank country is selectable;
 * localised display names come from the platform (`getLocalizedCountryName`), the FR
 * names in this map are a fallback for codes the platform ICU table doesn't resolve.
 */
object CountryCodes {

    /** All supported codes → French display name (fallback when platform lookup fails). */
    val ALL: Map<String, String> = linkedMapOf(
        "AD" to "Andorre",
        "AE" to "Émirats arabes unis",
        "AF" to "Afghanistan",
        "AG" to "Antigua-et-Barbuda",
        "AI" to "Anguilla",
        "AL" to "Albanie",
        "AM" to "Arménie",
        "AO" to "Angola",
        "AQ" to "Antarctique",
        "AR" to "Argentine",
        "AS" to "Samoa américaines",
        "AT" to "Autriche",
        "AU" to "Australie",
        "AW" to "Aruba",
        "AX" to "Îles Åland",
        "AZ" to "Azerbaïdjan",
        "BA" to "Bosnie-Herzégovine",
        "BB" to "Barbade",
        "BD" to "Bangladesh",
        "BE" to "Belgique",
        "BF" to "Burkina Faso",
        "BG" to "Bulgarie",
        "BH" to "Bahreïn",
        "BI" to "Burundi",
        "BJ" to "Bénin",
        "BL" to "Saint-Barthélemy",
        "BM" to "Bermudes",
        "BN" to "Brunei",
        "BO" to "Bolivie",
        "BQ" to "Pays-Bas caribéens",
        "BR" to "Brésil",
        "BS" to "Bahamas",
        "BT" to "Bhoutan",
        "BV" to "Île Bouvet",
        "BW" to "Botswana",
        "BY" to "Biélorussie",
        "BZ" to "Belize",
        "CA" to "Canada",
        "CC" to "Îles Cocos",
        "CD" to "République démocratique du Congo",
        "CF" to "République centrafricaine",
        "CG" to "République du Congo",
        "CH" to "Suisse",
        "CI" to "Côte d'Ivoire",
        "CK" to "Îles Cook",
        "CL" to "Chili",
        "CM" to "Cameroun",
        "CN" to "Chine",
        "CO" to "Colombie",
        "CR" to "Costa Rica",
        "CU" to "Cuba",
        "CV" to "Cap-Vert",
        "CW" to "Curaçao",
        "CX" to "Île Christmas",
        "CY" to "Chypre",
        "CZ" to "Tchéquie",
        "DE" to "Allemagne",
        "DJ" to "Djibouti",
        "DK" to "Danemark",
        "DM" to "Dominique",
        "DO" to "République dominicaine",
        "DZ" to "Algérie",
        "EC" to "Équateur",
        "EE" to "Estonie",
        "EG" to "Égypte",
        "EH" to "Sahara occidental",
        "ER" to "Érythrée",
        "ES" to "Espagne",
        "ET" to "Éthiopie",
        "FI" to "Finlande",
        "FJ" to "Fidji",
        "FK" to "Îles Malouines",
        "FM" to "États fédérés de Micronésie",
        "FO" to "Îles Féroé",
        "FR" to "France",
        "GA" to "Gabon",
        "GB" to "Royaume-Uni",
        "GD" to "Grenade",
        "GE" to "Géorgie",
        "GF" to "Guyane française",
        "GG" to "Guernesey",
        "GH" to "Ghana",
        "GI" to "Gibraltar",
        "GL" to "Groenland",
        "GM" to "Gambie",
        "GN" to "Guinée",
        "GP" to "Guadeloupe",
        "GQ" to "Guinée équatoriale",
        "GR" to "Grèce",
        "GS" to "Géorgie du Sud-et-les îles Sandwich du Sud",
        "GT" to "Guatemala",
        "GU" to "Guam",
        "GW" to "Guinée-Bissau",
        "GY" to "Guyana",
        "HK" to "Hong Kong",
        "HM" to "Îles Heard-et-MacDonald",
        "HN" to "Honduras",
        "HR" to "Croatie",
        "HT" to "Haïti",
        "HU" to "Hongrie",
        "ID" to "Indonésie",
        "IE" to "Irlande",
        "IL" to "Israël",
        "IM" to "Île de Man",
        "IN" to "Inde",
        "IO" to "Territoire britannique de l'océan Indien",
        "IQ" to "Irak",
        "IR" to "Iran",
        "IS" to "Islande",
        "IT" to "Italie",
        "JE" to "Jersey",
        "JM" to "Jamaïque",
        "JO" to "Jordanie",
        "JP" to "Japon",
        "KE" to "Kenya",
        "KG" to "Kirghizistan",
        "KH" to "Cambodge",
        "KI" to "Kiribati",
        "KM" to "Comores",
        "KN" to "Saint-Christophe-et-Niévès",
        "KP" to "Corée du Nord",
        "KR" to "Corée du Sud",
        "KW" to "Koweït",
        "KY" to "Îles Caïmans",
        "KZ" to "Kazakhstan",
        "LA" to "Laos",
        "LB" to "Liban",
        "LC" to "Sainte-Lucie",
        "LI" to "Liechtenstein",
        "LK" to "Sri Lanka",
        "LR" to "Libéria",
        "LS" to "Lesotho",
        "LT" to "Lituanie",
        "LU" to "Luxembourg",
        "LV" to "Lettonie",
        "LY" to "Libye",
        "MA" to "Maroc",
        "MC" to "Monaco",
        "MD" to "Moldavie",
        "ME" to "Monténégro",
        "MF" to "Saint-Martin (partie française)",
        "MG" to "Madagascar",
        "MH" to "Îles Marshall",
        "MK" to "Macédoine du Nord",
        "ML" to "Mali",
        "MM" to "Birmanie (Myanmar)",
        "MN" to "Mongolie",
        "MO" to "Macao",
        "MP" to "Îles Mariannes du Nord",
        "MQ" to "Martinique",
        "MR" to "Mauritanie",
        "MS" to "Montserrat",
        "MT" to "Malte",
        "MU" to "Maurice",
        "MV" to "Maldives",
        "MW" to "Malawi",
        "MX" to "Mexique",
        "MY" to "Malaisie",
        "MZ" to "Mozambique",
        "NA" to "Namibie",
        "NC" to "Nouvelle-Calédonie",
        "NE" to "Niger",
        "NF" to "Île Norfolk",
        "NG" to "Nigeria",
        "NI" to "Nicaragua",
        "NL" to "Pays-Bas",
        "NO" to "Norvège",
        "NP" to "Népal",
        "NR" to "Nauru",
        "NU" to "Niue",
        "NZ" to "Nouvelle-Zélande",
        "OM" to "Oman",
        "PA" to "Panama",
        "PE" to "Pérou",
        "PF" to "Polynésie française",
        "PG" to "Papouasie-Nouvelle-Guinée",
        "PH" to "Philippines",
        "PK" to "Pakistan",
        "PL" to "Pologne",
        "PM" to "Saint-Pierre-et-Miquelon",
        "PN" to "Îles Pitcairn",
        "PR" to "Porto Rico",
        "PS" to "Palestine",
        "PT" to "Portugal",
        "PW" to "Palaos",
        "PY" to "Paraguay",
        "QA" to "Qatar",
        "RE" to "Réunion",
        "RO" to "Roumanie",
        "RS" to "Serbie",
        "RU" to "Russie",
        "RW" to "Rwanda",
        "SA" to "Arabie saoudite",
        "SB" to "Îles Salomon",
        "SC" to "Seychelles",
        "SD" to "Soudan",
        "SE" to "Suède",
        "SG" to "Singapour",
        "SH" to "Sainte-Hélène",
        "SI" to "Slovénie",
        "SJ" to "Svalbard et Jan Mayen",
        "SK" to "Slovaquie",
        "SL" to "Sierra Leone",
        "SM" to "Saint-Marin",
        "SN" to "Sénégal",
        "SO" to "Somalie",
        "SR" to "Suriname",
        "SS" to "Soudan du Sud",
        "ST" to "Sao Tomé-et-Principe",
        "SV" to "Salvador",
        "SX" to "Saint-Martin (partie néerlandaise)",
        "SY" to "Syrie",
        "SZ" to "Eswatini",
        "TC" to "Îles Turques-et-Caïques",
        "TD" to "Tchad",
        "TF" to "Terres australes et antarctiques françaises",
        "TG" to "Togo",
        "TH" to "Thaïlande",
        "TJ" to "Tadjikistan",
        "TK" to "Tokelau",
        "TL" to "Timor oriental",
        "TM" to "Turkménistan",
        "TN" to "Tunisie",
        "TO" to "Tonga",
        "TR" to "Turquie",
        "TT" to "Trinité-et-Tobago",
        "TV" to "Tuvalu",
        "TW" to "Taïwan",
        "TZ" to "Tanzanie",
        "UA" to "Ukraine",
        "UG" to "Ouganda",
        "UM" to "Îles mineures éloignées des États-Unis",
        "US" to "États-Unis",
        "UY" to "Uruguay",
        "UZ" to "Ouzbékistan",
        "VA" to "Vatican",
        "VC" to "Saint-Vincent-et-les-Grenadines",
        "VE" to "Venezuela",
        "VG" to "Îles Vierges britanniques",
        "VI" to "Îles Vierges américaines",
        "VN" to "Vietnam",
        "VU" to "Vanuatu",
        "WF" to "Wallis-et-Futuna",
        "WS" to "Samoa",
        "YE" to "Yémen",
        "YT" to "Mayotte",
        "ZA" to "Afrique du Sud",
        "ZM" to "Zambie",
        "ZW" to "Zimbabwe",
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

    /**
     * Localised display name for a country code, following the app's
     * current language (fr / en / de / es / …). Cascade:
     *   1. Platform ICU / NSLocale lookup — covers every ISO code Apple/JVM
     *      knows in the target locale.
     *   2. Curated FR map fallback — for exotic codes the platform doesn't
     *      have or when the app language layer is momentarily empty.
     *   3. Raw uppercase code — never returns blank for a non-blank input.
     */
    fun displayNameOf(code: String?): String {
        if (code.isNullOrBlank()) return ""
        val upper = code.uppercase()
        val language = AppLocaleHolder.languageCode.ifBlank { "en" }
        val localised = getLocalizedCountryName(upper, language)
        return localised ?: ALL[upper] ?: upper
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

    /**
     * Curated shortlist of the ~2-3 most-cited VAT rates for a country, seeded
     * into `TaxRate` at fresh-install onboarding once the user has picked
     * their country. Ordered low → high; the standard rate always closes the
     * list. Returns null for countries without a curated set — caller keeps
     * the global 5.5/10/20 fallback baked into TaxRate.sq.
     *
     * Sources: national tax administrations / EU VAT rates database (2024).
     * Curated, not exhaustive — reduced rates covering electronics/hospitality
     * that most micro-entreprises never touch are intentionally omitted.
     */
    fun defaultVatRatesForCountry(code: String?): List<Double>? = when (code?.uppercase()) {
        "FR", "MC" -> listOf(5.5, 10.0, 20.0)
        "BE" -> listOf(6.0, 12.0, 21.0)
        "LU" -> listOf(8.0, 14.0, 17.0)
        "CH", "LI" -> listOf(2.6, 3.8, 8.1)
        "DE" -> listOf(7.0, 19.0)
        "ES" -> listOf(4.0, 10.0, 21.0)
        "IT" -> listOf(5.0, 10.0, 22.0)
        "NL" -> listOf(9.0, 21.0)
        "PT" -> listOf(6.0, 13.0, 23.0)
        "GB" -> listOf(5.0, 20.0)
        "IE" -> listOf(9.0, 13.5, 23.0)
        "AT" -> listOf(10.0, 13.0, 20.0)
        "DK" -> listOf(25.0)
        "SE" -> listOf(6.0, 12.0, 25.0)
        "FI" -> listOf(10.0, 14.0, 25.5)
        "NO" -> listOf(12.0, 15.0, 25.0)
        "IS" -> listOf(11.0, 24.0)
        "PL" -> listOf(5.0, 8.0, 23.0)
        "CZ" -> listOf(12.0, 21.0)
        "SK" -> listOf(10.0, 23.0)
        "HU" -> listOf(5.0, 18.0, 27.0)
        "RO" -> listOf(5.0, 9.0, 19.0)
        "BG" -> listOf(9.0, 20.0)
        "GR" -> listOf(6.0, 13.0, 24.0)
        "HR" -> listOf(5.0, 13.0, 25.0)
        "SI" -> listOf(5.0, 9.5, 22.0)
        "EE" -> listOf(9.0, 22.0)
        "LV" -> listOf(5.0, 12.0, 21.0)
        "LT" -> listOf(5.0, 9.0, 21.0)
        "MT" -> listOf(5.0, 7.0, 18.0)
        "CY" -> listOf(5.0, 9.0, 19.0)
        "AD" -> listOf(1.0, 4.5, 9.5)
        "AU" -> listOf(10.0)
        "NZ" -> listOf(15.0)
        "JP" -> listOf(8.0, 10.0)
        "CA" -> listOf(5.0, 15.0)
        else -> null
    }
}
