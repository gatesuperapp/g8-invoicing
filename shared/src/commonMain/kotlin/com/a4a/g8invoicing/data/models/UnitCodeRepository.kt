package com.a4a.g8invoicing.data.models

import com.a4a.g8invoicing.data.AppLocaleHolder
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.unit_act_keywords
import com.a4a.g8invoicing.shared.resources.unit_act_name
import com.a4a.g8invoicing.shared.resources.unit_act_short
import com.a4a.g8invoicing.shared.resources.unit_ann_keywords
import com.a4a.g8invoicing.shared.resources.unit_ann_name
import com.a4a.g8invoicing.shared.resources.unit_ann_short
import com.a4a.g8invoicing.shared.resources.unit_c62_keywords
import com.a4a.g8invoicing.shared.resources.unit_c62_name
import com.a4a.g8invoicing.shared.resources.unit_c62_short
import com.a4a.g8invoicing.shared.resources.unit_clt_keywords
import com.a4a.g8invoicing.shared.resources.unit_clt_name
import com.a4a.g8invoicing.shared.resources.unit_cmk_keywords
import com.a4a.g8invoicing.shared.resources.unit_cmk_name
import com.a4a.g8invoicing.shared.resources.unit_cmt_keywords
import com.a4a.g8invoicing.shared.resources.unit_cmt_name
import com.a4a.g8invoicing.shared.resources.unit_day_keywords
import com.a4a.g8invoicing.shared.resources.unit_day_name
import com.a4a.g8invoicing.shared.resources.unit_day_short
import com.a4a.g8invoicing.shared.resources.unit_dtn_keywords
import com.a4a.g8invoicing.shared.resources.unit_dtn_name
import com.a4a.g8invoicing.shared.resources.unit_dtn_short
import com.a4a.g8invoicing.shared.resources.unit_dzn_keywords
import com.a4a.g8invoicing.shared.resources.unit_dzn_name
import com.a4a.g8invoicing.shared.resources.unit_dzn_short
import com.a4a.g8invoicing.shared.resources.unit_e48_keywords
import com.a4a.g8invoicing.shared.resources.unit_e48_name
import com.a4a.g8invoicing.shared.resources.unit_e48_short
import com.a4a.g8invoicing.shared.resources.unit_e51_keywords
import com.a4a.g8invoicing.shared.resources.unit_e51_name
import com.a4a.g8invoicing.shared.resources.unit_e51_short
import com.a4a.g8invoicing.shared.resources.unit_grm_keywords
import com.a4a.g8invoicing.shared.resources.unit_grm_name
import com.a4a.g8invoicing.shared.resources.unit_h87_keywords
import com.a4a.g8invoicing.shared.resources.unit_h87_name
import com.a4a.g8invoicing.shared.resources.unit_h87_short
import com.a4a.g8invoicing.shared.resources.unit_hea_keywords
import com.a4a.g8invoicing.shared.resources.unit_hea_name
import com.a4a.g8invoicing.shared.resources.unit_hea_short
import com.a4a.g8invoicing.shared.resources.unit_hlt_keywords
import com.a4a.g8invoicing.shared.resources.unit_hlt_name
import com.a4a.g8invoicing.shared.resources.unit_hur_keywords
import com.a4a.g8invoicing.shared.resources.unit_hur_name
import com.a4a.g8invoicing.shared.resources.unit_kgm_keywords
import com.a4a.g8invoicing.shared.resources.unit_kgm_name
import com.a4a.g8invoicing.shared.resources.unit_kmk_keywords
import com.a4a.g8invoicing.shared.resources.unit_kmk_name
import com.a4a.g8invoicing.shared.resources.unit_kmt_keywords
import com.a4a.g8invoicing.shared.resources.unit_kmt_name
import com.a4a.g8invoicing.shared.resources.unit_kwh_keywords
import com.a4a.g8invoicing.shared.resources.unit_kwh_name
import com.a4a.g8invoicing.shared.resources.unit_kwh_short
import com.a4a.g8invoicing.shared.resources.unit_lm_keywords
import com.a4a.g8invoicing.shared.resources.unit_lm_name
import com.a4a.g8invoicing.shared.resources.unit_lm_short
import com.a4a.g8invoicing.shared.resources.unit_lo_keywords
import com.a4a.g8invoicing.shared.resources.unit_lo_name
import com.a4a.g8invoicing.shared.resources.unit_lo_short
import com.a4a.g8invoicing.shared.resources.unit_ltr_keywords
import com.a4a.g8invoicing.shared.resources.unit_ltr_name
import com.a4a.g8invoicing.shared.resources.unit_min_keywords
import com.a4a.g8invoicing.shared.resources.unit_min_name
import com.a4a.g8invoicing.shared.resources.unit_mlt_keywords
import com.a4a.g8invoicing.shared.resources.unit_mlt_name
import com.a4a.g8invoicing.shared.resources.unit_mon_keywords
import com.a4a.g8invoicing.shared.resources.unit_mon_name
import com.a4a.g8invoicing.shared.resources.unit_mon_short
import com.a4a.g8invoicing.shared.resources.unit_mtk_keywords
import com.a4a.g8invoicing.shared.resources.unit_mtk_name
import com.a4a.g8invoicing.shared.resources.unit_mtq_keywords
import com.a4a.g8invoicing.shared.resources.unit_mtq_name
import com.a4a.g8invoicing.shared.resources.unit_mtr_keywords
import com.a4a.g8invoicing.shared.resources.unit_mtr_name
import com.a4a.g8invoicing.shared.resources.unit_mmt_keywords
import com.a4a.g8invoicing.shared.resources.unit_mmt_name
import com.a4a.g8invoicing.shared.resources.unit_mwh_keywords
import com.a4a.g8invoicing.shared.resources.unit_mwh_name
import com.a4a.g8invoicing.shared.resources.unit_mwh_short
import com.a4a.g8invoicing.shared.resources.unit_pr_keywords
import com.a4a.g8invoicing.shared.resources.unit_pr_name
import com.a4a.g8invoicing.shared.resources.unit_pr_short
import com.a4a.g8invoicing.shared.resources.unit_set_keywords
import com.a4a.g8invoicing.shared.resources.unit_set_name
import com.a4a.g8invoicing.shared.resources.unit_set_short
import com.a4a.g8invoicing.shared.resources.unit_tne_keywords
import com.a4a.g8invoicing.shared.resources.unit_tne_name
import com.a4a.g8invoicing.shared.resources.unit_wee_keywords
import com.a4a.g8invoicing.shared.resources.unit_wee_name
import com.a4a.g8invoicing.shared.resources.unit_wee_short
import com.a4a.g8invoicing.shared.resources.unit_xbe_keywords
import com.a4a.g8invoicing.shared.resources.unit_xbe_name
import com.a4a.g8invoicing.shared.resources.unit_xbe_short
import com.a4a.g8invoicing.shared.resources.unit_xbg_keywords
import com.a4a.g8invoicing.shared.resources.unit_xbg_name
import com.a4a.g8invoicing.shared.resources.unit_xbg_short
import com.a4a.g8invoicing.shared.resources.unit_xbh_keywords
import com.a4a.g8invoicing.shared.resources.unit_xbh_name
import com.a4a.g8invoicing.shared.resources.unit_xbh_short
import com.a4a.g8invoicing.shared.resources.unit_xbk_keywords
import com.a4a.g8invoicing.shared.resources.unit_xbk_name
import com.a4a.g8invoicing.shared.resources.unit_xbk_short
import com.a4a.g8invoicing.shared.resources.unit_xbl_keywords
import com.a4a.g8invoicing.shared.resources.unit_xbl_name
import com.a4a.g8invoicing.shared.resources.unit_xbl_short
import com.a4a.g8invoicing.shared.resources.unit_xcr_keywords
import com.a4a.g8invoicing.shared.resources.unit_xcr_name
import com.a4a.g8invoicing.shared.resources.unit_xcr_short
import com.a4a.g8invoicing.shared.resources.unit_xpx_keywords
import com.a4a.g8invoicing.shared.resources.unit_xpx_name
import com.a4a.g8invoicing.shared.resources.unit_xpx_short
import com.a4a.g8invoicing.shared.resources.unit_xro_keywords
import com.a4a.g8invoicing.shared.resources.unit_xro_name
import com.a4a.g8invoicing.shared.resources.unit_xro_short
import com.a4a.g8invoicing.shared.resources.unit_xsa_keywords
import com.a4a.g8invoicing.shared.resources.unit_xsa_name
import com.a4a.g8invoicing.shared.resources.unit_xsa_short
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/**
 * Loads localised names, short forms and search keywords from Compose Resources
 * for every [UnitCode], and answers the three questions the UI actually asks:
 *
 *  - `resolveName(code)`  → localized long-form (picker rows)
 *  - `resolveShort(code)` → compact inline form (uses the enum's `symbol` when
 *                           set, otherwise the localized `_short` resource)
 *  - `search(query)`      → ranked matches for autocomplete
 *  - `matchTextToCode(text)` → best-effort resolution of a free-text unit field
 *                              at save time; falls back to [UnitCode.C62] per
 *                              EN 16931.
 *
 * The search index is built once per locale and cached. When
 * [AppLocaleHolder.languageCode] changes, the next call rebuilds. Getting
 * strings from Compose Resources is suspend, so every read here is suspend too
 * — callers on ViewModels wrap in `viewModelScope.launch`.
 */
class UnitCodeRepository {

    private data class LocaleData(
        val locale: String,
        val names: Map<UnitCode, String>,
        val shorts: Map<UnitCode, String>,       // only for codes with a `_short` resource
        val entries: List<Entry>,                // pre-built, ranked search index
    )

    private data class Entry(val code: UnitCode, val term: String, val rank: Int)

    private var cached: LocaleData? = null
    private val loadMutex = Mutex()

    private suspend fun load(): LocaleData {
        val current = AppLocaleHolder.languageCode
        cached?.takeIf { it.locale == current }?.let { return it }
        return loadMutex.withLock {
            cached?.takeIf { it.locale == current }?.let { return it }
            val built = build(current)
            cached = built
            built
        }
    }

    private suspend fun build(locale: String): LocaleData {
        // Read all three resources per code up-front; the picker + product form
        // hit them all anyway, so paying once is cheaper than lazy paging.
        val names = mutableMapOf<UnitCode, String>()
        val shorts = mutableMapOf<UnitCode, String>()
        val keywordsByCode = mutableMapOf<UnitCode, List<String>>()

        for (code in UnitCode.entries) {
            val spec = specs[code] ?: continue
            names[code] = getString(spec.name)
            spec.short?.let { shorts[code] = getString(it) }
            keywordsByCode[code] = getString(spec.keywords)
                .split(',').map { it.trim() }.filter { it.isNotEmpty() }
        }

        // English fallback for keywords lets an ES user searching "kilo" still
        // find KGM even if the ES translator missed it. Skipped when the active
        // locale already IS English.
        val enKeywordsByCode: Map<UnitCode, List<String>> =
            if (locale == "en") emptyMap()
            else {
                val fallback = mutableMapOf<UnitCode, List<String>>()
                // NOTE: Compose Resources doesn't expose a per-locale getString
                // in commonMain — the fallback here reuses the active-locale
                // keywords as a placeholder. Weblate contributors are expected
                // to keep the English keywords rich; when a native
                // "get in specific locale" API lands we plug it in here.
                fallback
            }

        val entries = buildList {
            for (code in UnitCode.entries) {
                add(Entry(code = code, term = normalize(code.code), rank = 0))
                code.symbol?.let { symbol ->
                    add(Entry(code = code, term = normalize(symbol), rank = 0))
                    val ascii = symbol
                        .replace("²", "2").replace("³", "3")
                    if (ascii != symbol) {
                        add(Entry(code = code, term = normalize(ascii), rank = 0))
                    }
                }
                keywordsByCode[code].orEmpty().forEach { kw ->
                    add(Entry(code = code, term = normalize(kw), rank = 1))
                }
                enKeywordsByCode[code].orEmpty().forEach { kw ->
                    add(Entry(code = code, term = normalize(kw), rank = 2))
                }
            }
        }.filter { it.term.isNotEmpty() }

        return LocaleData(locale = locale, names = names, shorts = shorts, entries = entries)
    }

    /** Localised long-form name, or the raw code as a last-resort fallback. */
    suspend fun resolveName(code: UnitCode): String {
        val data = load()
        return data.names[code] ?: code.code
    }

    suspend fun resolveName(code: String?): String? {
        val unit = UnitCode.findByCode(code) ?: return null
        return resolveName(unit)
    }

    /**
     * Compact inline form: prefers the enum `symbol` when set (kg, m², h — never
     * translated) and falls back to the localised `_short` resource. Uses the
     * raw code if neither is available.
     */
    suspend fun resolveShort(code: UnitCode): String {
        code.symbol?.let { return it }
        val data = load()
        return data.shorts[code] ?: code.code
    }

    suspend fun resolveShort(code: String?): String? {
        val unit = UnitCode.findByCode(code) ?: return null
        return resolveShort(unit)
    }

    /**
     * Ranked ordered list of matches for the picker's search bar. Empty query
     * returns every code, unsorted (caller groups by category). Non-empty
     * queries are normalised and matched with a bidirectional prefix — either
     * the query prefixes the term (typing "kilo" finds "kilogramme") or the
     * term prefixes the query (indexed "u" matches "unite" typed by the user).
     */
    suspend fun search(query: String): List<UnitCode> {
        val q = normalize(query)
        if (q.isEmpty()) return UnitCode.entries
        val data = load()
        return data.entries
            .filter { it.term.startsWith(q) || q.startsWith(it.term) }
            .sortedWith(compareBy({ it.rank }, { it.term.length }))
            .map { it.code }
            .distinct()
    }

    /**
     * Free-text → code resolver, used when saving a product to persist the
     * Factur-X unit code alongside the human unit string. Best match wins;
     * if nothing hits, returns [UnitCode.C62] (EN 16931 default for "unit").
     */
    suspend fun matchTextToCode(text: String?): UnitCode {
        val q = normalize(text ?: "")
        if (q.isEmpty()) return UnitCode.C62
        val data = load()
        val hit = data.entries
            .firstOrNull { it.term == q }
            ?: data.entries
                .filter { it.term.startsWith(q) || q.startsWith(it.term) }
                .minByOrNull { it.rank * 1000 + it.term.length }
        return hit?.code ?: UnitCode.C62
    }

    private fun normalize(s: String): String = stripDiacriticsAndLower(s)

    private data class Spec(
        val name: StringResource,
        val keywords: StringResource,
        val short: StringResource? = null,
    )

    private val specs: Map<UnitCode, Spec> = mapOf(
        UnitCode.C62 to Spec(Res.string.unit_c62_name, Res.string.unit_c62_keywords, Res.string.unit_c62_short),
        UnitCode.H87 to Spec(Res.string.unit_h87_name, Res.string.unit_h87_keywords, Res.string.unit_h87_short),
        UnitCode.SET to Spec(Res.string.unit_set_name, Res.string.unit_set_keywords, Res.string.unit_set_short),
        UnitCode.PR  to Spec(Res.string.unit_pr_name,  Res.string.unit_pr_keywords,  Res.string.unit_pr_short),
        UnitCode.DZN to Spec(Res.string.unit_dzn_name, Res.string.unit_dzn_keywords, Res.string.unit_dzn_short),
        UnitCode.LO  to Spec(Res.string.unit_lo_name,  Res.string.unit_lo_keywords,  Res.string.unit_lo_short),

        UnitCode.MTR to Spec(Res.string.unit_mtr_name, Res.string.unit_mtr_keywords),
        UnitCode.LM  to Spec(Res.string.unit_lm_name,  Res.string.unit_lm_keywords,  Res.string.unit_lm_short),
        UnitCode.CMT to Spec(Res.string.unit_cmt_name, Res.string.unit_cmt_keywords),
        UnitCode.MMT to Spec(Res.string.unit_mmt_name, Res.string.unit_mmt_keywords),
        UnitCode.KMT to Spec(Res.string.unit_kmt_name, Res.string.unit_kmt_keywords),

        UnitCode.MTK to Spec(Res.string.unit_mtk_name, Res.string.unit_mtk_keywords),
        UnitCode.CMK to Spec(Res.string.unit_cmk_name, Res.string.unit_cmk_keywords),
        UnitCode.KMK to Spec(Res.string.unit_kmk_name, Res.string.unit_kmk_keywords),

        UnitCode.LTR to Spec(Res.string.unit_ltr_name, Res.string.unit_ltr_keywords),
        UnitCode.MLT to Spec(Res.string.unit_mlt_name, Res.string.unit_mlt_keywords),
        UnitCode.CLT to Spec(Res.string.unit_clt_name, Res.string.unit_clt_keywords),
        UnitCode.HLT to Spec(Res.string.unit_hlt_name, Res.string.unit_hlt_keywords),
        UnitCode.MTQ to Spec(Res.string.unit_mtq_name, Res.string.unit_mtq_keywords),

        UnitCode.KGM to Spec(Res.string.unit_kgm_name, Res.string.unit_kgm_keywords),
        UnitCode.GRM to Spec(Res.string.unit_grm_name, Res.string.unit_grm_keywords),
        UnitCode.TNE to Spec(Res.string.unit_tne_name, Res.string.unit_tne_keywords),
        UnitCode.DTN to Spec(Res.string.unit_dtn_name, Res.string.unit_dtn_keywords, Res.string.unit_dtn_short),

        UnitCode.HUR to Spec(Res.string.unit_hur_name, Res.string.unit_hur_keywords),
        UnitCode.MIN to Spec(Res.string.unit_min_name, Res.string.unit_min_keywords),
        UnitCode.DAY to Spec(Res.string.unit_day_name, Res.string.unit_day_keywords, Res.string.unit_day_short),
        UnitCode.WEE to Spec(Res.string.unit_wee_name, Res.string.unit_wee_keywords, Res.string.unit_wee_short),
        UnitCode.MON to Spec(Res.string.unit_mon_name, Res.string.unit_mon_keywords, Res.string.unit_mon_short),
        UnitCode.ANN to Spec(Res.string.unit_ann_name, Res.string.unit_ann_keywords, Res.string.unit_ann_short),

        UnitCode.KWH to Spec(Res.string.unit_kwh_name, Res.string.unit_kwh_keywords, Res.string.unit_kwh_short),
        UnitCode.MWH to Spec(Res.string.unit_mwh_name, Res.string.unit_mwh_keywords, Res.string.unit_mwh_short),

        UnitCode.ACT to Spec(Res.string.unit_act_name, Res.string.unit_act_keywords, Res.string.unit_act_short),
        UnitCode.E48 to Spec(Res.string.unit_e48_name, Res.string.unit_e48_keywords, Res.string.unit_e48_short),
        UnitCode.E51 to Spec(Res.string.unit_e51_name, Res.string.unit_e51_keywords, Res.string.unit_e51_short),

        UnitCode.XBH to Spec(Res.string.unit_xbh_name, Res.string.unit_xbh_keywords, Res.string.unit_xbh_short),
        UnitCode.XBE to Spec(Res.string.unit_xbe_name, Res.string.unit_xbe_keywords, Res.string.unit_xbe_short),
        UnitCode.XBL to Spec(Res.string.unit_xbl_name, Res.string.unit_xbl_keywords, Res.string.unit_xbl_short),
        UnitCode.XSA to Spec(Res.string.unit_xsa_name, Res.string.unit_xsa_keywords, Res.string.unit_xsa_short),
        UnitCode.XBG to Spec(Res.string.unit_xbg_name, Res.string.unit_xbg_keywords, Res.string.unit_xbg_short),
        UnitCode.XBK to Spec(Res.string.unit_xbk_name, Res.string.unit_xbk_keywords, Res.string.unit_xbk_short),
        UnitCode.XCR to Spec(Res.string.unit_xcr_name, Res.string.unit_xcr_keywords, Res.string.unit_xcr_short),
        UnitCode.XRO to Spec(Res.string.unit_xro_name, Res.string.unit_xro_keywords, Res.string.unit_xro_short),
        UnitCode.XPX to Spec(Res.string.unit_xpx_name, Res.string.unit_xpx_keywords, Res.string.unit_xpx_short),
        UnitCode.HEA to Spec(Res.string.unit_hea_name, Res.string.unit_hea_keywords, Res.string.unit_hea_short),
    )
}
