package com.a4a.g8invoicing.data.models

/**
 * Catalog of UNECE Rec 20 / Rec 21 unit codes accepted by Factur-X (BT-130) and UBL Peppol,
 * curated for typical indépendant / artisan / farmer use cases. The full UN list has ~2100
 * codes, most are scientific — this subset is the ~40 that a real invoice will actually use.
 *
 * If the user's text doesn't match any alias, the generator falls back to H87 (piece), which
 * remains a valid Factur-X value — the human-facing label from the free-text `unit` field
 * on the PDF is unchanged, only the XML underneath is affected.
 *
 * Display labels in French — matches the CountryCodes convention. Multi-locale layered later.
 */
object UnitCodes {

    enum class Category { COUNT, LENGTH, AREA, VOLUME, WEIGHT, TIME, ENERGY, SERVICE, PACKAGING }

    data class UnitCode(
        val code: String,
        val labelFr: String,
        val labelEn: String,
        val category: Category,
    )

    val ALL: List<UnitCode> = listOf(
        // Comptage / abstrait
        UnitCode("C62", "pièce", "one", Category.COUNT),
        UnitCode("H87", "pièce", "piece", Category.COUNT),
        UnitCode("SET", "ensemble", "set", Category.COUNT),
        UnitCode("PR",  "paire", "pair", Category.COUNT),
        UnitCode("DZN", "douzaine", "dozen", Category.COUNT),
        UnitCode("LO",  "lot", "lot", Category.COUNT),

        // Longueur
        UnitCode("MTR", "mètre", "metre", Category.LENGTH),
        UnitCode("LM",  "mètre linéaire", "linear metre", Category.LENGTH),
        UnitCode("CMT", "centimètre", "centimetre", Category.LENGTH),
        UnitCode("MMT", "millimètre", "millimetre", Category.LENGTH),
        UnitCode("KMT", "kilomètre", "kilometre", Category.LENGTH),

        // Surface
        UnitCode("MTK", "mètre carré", "square metre", Category.AREA),
        UnitCode("CMK", "centimètre carré", "square centimetre", Category.AREA),
        UnitCode("KMK", "kilomètre carré", "square kilometre", Category.AREA),

        // Volume
        UnitCode("LTR", "litre", "litre", Category.VOLUME),
        UnitCode("MLT", "millilitre", "millilitre", Category.VOLUME),
        UnitCode("CLT", "centilitre", "centilitre", Category.VOLUME),
        UnitCode("HLT", "hectolitre", "hectolitre", Category.VOLUME),
        UnitCode("MTQ", "mètre cube", "cubic metre", Category.VOLUME),

        // Poids
        UnitCode("KGM", "kilogramme", "kilogram", Category.WEIGHT),
        UnitCode("GRM", "gramme", "gram", Category.WEIGHT),
        UnitCode("TNE", "tonne", "tonne", Category.WEIGHT),
        UnitCode("DTN", "décitonne", "decitonne", Category.WEIGHT),

        // Temps
        UnitCode("HUR", "heure", "hour", Category.TIME),
        UnitCode("MIN", "minute", "minute", Category.TIME),
        UnitCode("DAY", "jour", "day", Category.TIME),
        UnitCode("WEE", "semaine", "week", Category.TIME),
        UnitCode("MON", "mois", "month", Category.TIME),
        UnitCode("ANN", "année", "year", Category.TIME),

        // Énergie
        UnitCode("KWH", "kilowatt-heure", "kilowatt hour", Category.ENERGY),
        UnitCode("MWH", "mégawatt-heure", "megawatt hour", Category.ENERGY),

        // Service
        UnitCode("ACT", "activité", "activity", Category.SERVICE),
        UnitCode("E48", "unité de service", "service unit", Category.SERVICE),
        UnitCode("E51", "prestation", "job", Category.SERVICE),

        // Emballage / vrac
        UnitCode("XBH", "botte", "bunch", Category.PACKAGING),
        UnitCode("XBE", "fagot", "bundle", Category.PACKAGING),
        UnitCode("XBL", "balle compressée", "bale, compressed", Category.PACKAGING),
        UnitCode("XSA", "sac", "sack", Category.PACKAGING),
        UnitCode("XBG", "sachet", "bag", Category.PACKAGING),
        UnitCode("XBK", "panier", "basket", Category.PACKAGING),
        UnitCode("XCR", "cageot", "crate", Category.PACKAGING),
        UnitCode("XRO", "rouleau", "roll", Category.PACKAGING),
        UnitCode("XPX", "palette", "pallet", Category.PACKAGING),
        UnitCode("HEA", "tête (bétail)", "head", Category.PACKAGING),
    )

    private val byCode: Map<String, UnitCode> = ALL.associateBy { it.code }

    /** Alias dictionary — normalized user input → UNECE code. Order matters for
     * disambiguation: earlier entries win when a key matches multiple. */
    private val aliases: Map<String, String> = linkedMapOf(
        // Comptage
        "pièce" to "H87", "piece" to "H87", "pieces" to "H87", "pièces" to "H87",
        "unité" to "H87", "unite" to "H87", "unités" to "H87", "unites" to "H87",
        "u" to "H87", "un" to "H87",
        "paire" to "PR", "paires" to "PR",
        "douzaine" to "DZN", "douzaines" to "DZN",
        "ensemble" to "SET", "set" to "SET", "kit" to "SET",
        "lot" to "LO", "lots" to "LO",
        // Longueur
        "m" to "MTR", "mètre" to "MTR", "metre" to "MTR", "mètres" to "MTR", "metres" to "MTR",
        "cm" to "CMT", "centimètre" to "CMT", "centimetre" to "CMT",
        "mm" to "MMT", "millimètre" to "MMT", "millimetre" to "MMT",
        "km" to "KMT", "kilomètre" to "KMT", "kilometre" to "KMT",
        "mètre linéaire" to "LM", "metre lineaire" to "LM", "linéaire" to "LM", "lineaire" to "LM",
        // Surface
        "m²" to "MTK", "m2" to "MTK", "mètre carré" to "MTK", "metre carre" to "MTK",
        "cm²" to "CMK", "cm2" to "CMK",
        "km²" to "KMK", "km2" to "KMK",
        // Volume
        "l" to "LTR", "litre" to "LTR", "litres" to "LTR",
        "ml" to "MLT", "millilitre" to "MLT",
        "cl" to "CLT", "centilitre" to "CLT",
        "hl" to "HLT", "hectolitre" to "HLT",
        "m³" to "MTQ", "m3" to "MTQ", "mètre cube" to "MTQ", "metre cube" to "MTQ",
        // Poids
        "kg" to "KGM", "kilo" to "KGM", "kilos" to "KGM", "kilogramme" to "KGM", "kilogrammes" to "KGM",
        "g" to "GRM", "gr" to "GRM", "gramme" to "GRM", "grammes" to "GRM",
        "t" to "TNE", "tonne" to "TNE", "tonnes" to "TNE",
        "dt" to "DTN", "décitonne" to "DTN", "decitonne" to "DTN",
        // Temps
        "h" to "HUR", "heure" to "HUR", "heures" to "HUR", "hr" to "HUR",
        "min" to "MIN", "minute" to "MIN", "minutes" to "MIN",
        "j" to "DAY", "jour" to "DAY", "jours" to "DAY", "jr" to "DAY",
        "sem" to "WEE", "semaine" to "WEE", "semaines" to "WEE",
        "mois" to "MON",
        "an" to "ANN", "année" to "ANN", "annee" to "ANN", "années" to "ANN", "ans" to "ANN",
        // Énergie
        "kwh" to "KWH",
        "mwh" to "MWH",
        // Service
        "activité" to "ACT", "activite" to "ACT",
        "prestation" to "E51", "prestations" to "E51",
        "service" to "E48", "services" to "E48",
        // Emballage
        "botte" to "XBH", "bottes" to "XBH",
        "fagot" to "XBE", "fagots" to "XBE",
        "balle" to "XBL", "balles" to "XBL",
        "sac" to "XSA", "sacs" to "XSA",
        "sachet" to "XBG", "sachets" to "XBG",
        "panier" to "XBK", "paniers" to "XBK",
        "cageot" to "XCR", "cageots" to "XCR",
        "rouleau" to "XRO", "rouleaux" to "XRO",
        "palette" to "XPX", "palettes" to "XPX",
        "tête" to "HEA", "tete" to "HEA", "têtes" to "HEA", "tetes" to "HEA",
    )

    /** Match free-text input to a UNECE code via the alias dictionary. Returns null if no
     * match — the caller decides whether to fall back to H87 or leave it null. Trims and
     * lowercases the input, but the dictionary itself is expected to already be lowercase. */
    fun matchTextToCode(text: String): String? {
        val normalized = text.trim().lowercase()
        if (normalized.isEmpty()) return null
        return aliases[normalized]
    }

    /** Default display label for a code, in French. Used when the user picks from the
     * bottom sheet and we prefill the free-text `unit` field. */
    fun codeToDefaultLabel(code: String): String? = byCode[code]?.labelFr

    fun findByCode(code: String): UnitCode? = byCode[code]
}
