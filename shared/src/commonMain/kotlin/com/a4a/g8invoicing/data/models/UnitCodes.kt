package com.a4a.g8invoicing.data.models

/**
 * UNECE Rec 20 / Rec 21 unit codes accepted by Factur-X (BT-130) and UBL Peppol,
 * curated for typical indépendant / artisan / farmer use cases. The full UN list
 * has ~2100 codes, most are scientific — this subset is the ~44 that a real
 * invoice will actually use.
 *
 * Codes are language-neutral by construction. All display and search terms come
 * from Weblate-translated resources (unit_<code>_name, _short, _keywords) —
 * NEVER hardcode a label here. The one exception is [symbol]: SI / international
 * units like kg, m², h are written the same way in every locale, so the picker
 * and inline forms pull the symbol from this enum directly and translators
 * can't accidentally "localise" them.
 *
 * When [symbol] is null, the compact inline form must come from the
 * `unit_<code>_short` resource. When [symbol] is non-null, that resource is
 * omitted and the symbol wins.
 *
 * If free-text matching returns no code, the generator falls back to C62 (EN
 * 16931 default). H87 (piece) is reserved for actual "piece" input.
 */
enum class UnitCode(
    val code: String,
    val category: Category,
    val symbol: String? = null,
) {
    // Comptage / abstrait
    C62("C62", Category.COUNT),
    H87("H87", Category.COUNT),
    SET("SET", Category.COUNT),
    PR("PR", Category.COUNT),
    DZN("DZN", Category.COUNT),
    LO("LO", Category.COUNT),

    // Longueur
    MTR("MTR", Category.LENGTH, symbol = "m"),
    LM("LM", Category.LENGTH),
    CMT("CMT", Category.LENGTH, symbol = "cm"),
    MMT("MMT", Category.LENGTH, symbol = "mm"),
    KMT("KMT", Category.LENGTH, symbol = "km"),

    // Surface
    MTK("MTK", Category.AREA, symbol = "m²"),
    CMK("CMK", Category.AREA, symbol = "cm²"),
    KMK("KMK", Category.AREA, symbol = "km²"),

    // Volume
    LTR("LTR", Category.VOLUME, symbol = "L"),
    MLT("MLT", Category.VOLUME, symbol = "mL"),
    CLT("CLT", Category.VOLUME, symbol = "cL"),
    HLT("HLT", Category.VOLUME, symbol = "hL"),
    MTQ("MTQ", Category.VOLUME, symbol = "m³"),

    // Poids
    KGM("KGM", Category.WEIGHT, symbol = "kg"),
    GRM("GRM", Category.WEIGHT, symbol = "g"),
    TNE("TNE", Category.WEIGHT, symbol = "t"),
    DTN("DTN", Category.WEIGHT),

    // Temps
    HUR("HUR", Category.TIME, symbol = "h"),
    MIN("MIN", Category.TIME, symbol = "min"),
    DAY("DAY", Category.TIME),
    WEE("WEE", Category.TIME),
    MON("MON", Category.TIME),
    ANN("ANN", Category.TIME),

    // Énergie
    KWH("KWH", Category.ENERGY),
    MWH("MWH", Category.ENERGY),

    // Service
    ACT("ACT", Category.SERVICE),
    E48("E48", Category.SERVICE),
    E51("E51", Category.SERVICE),

    // Emballage / vrac
    XBH("XBH", Category.PACKAGING),
    XBE("XBE", Category.PACKAGING),
    XBL("XBL", Category.PACKAGING),
    XSA("XSA", Category.PACKAGING),
    XBG("XBG", Category.PACKAGING),
    XBK("XBK", Category.PACKAGING),
    XCR("XCR", Category.PACKAGING),
    XRO("XRO", Category.PACKAGING),
    XPX("XPX", Category.PACKAGING),
    HEA("HEA", Category.PACKAGING);

    enum class Category { COUNT, LENGTH, AREA, VOLUME, WEIGHT, TIME, ENERGY, SERVICE, PACKAGING }

    companion object {
        private val byCode: Map<String, UnitCode> = entries.associateBy { it.code }

        /** Case-insensitive lookup, null when the code isn't in our subset. */
        fun findByCode(code: String?): UnitCode? =
            code?.trim()?.uppercase()?.let { byCode[it] }
    }
}
