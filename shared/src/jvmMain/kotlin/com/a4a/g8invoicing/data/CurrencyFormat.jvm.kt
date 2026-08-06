package com.a4a.g8invoicing.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import java.math.BigDecimal as JavaBigDecimal

// Resolve a BCP-47 code (fr/en/es/de) to the JVM Locale used for number
// formatting. Unknown codes fall back to French — matches the default in
// AppLocaleHolder and the FR-first orientation of the app.
private fun jvmLocaleFor(code: String): Locale = when (code) {
    "fr" -> Locale.FRENCH
    "en" -> Locale.ENGLISH
    "de" -> Locale.GERMAN
    "es" -> Locale("es")
    else -> Locale.FRENCH
}

actual fun formatAmount(
    amount: BigDecimal,
    currencyCode: String,
    languageCode: String?,
): String {
    return try {
        val javaAmount = JavaBigDecimal(amount.toPlainString())
        // Prefer the caller-provided locale (typically frozen on the doc so
        // format doesn't drift when the user switches app language). Fall back
        // to the current app language when caller doesn't know — legacy docs,
        // dashboard totals, product-catalog rows, etc.
        val locale = jvmLocaleFor(languageCode ?: AppLocaleHolder.languageCode)
        val currency = Currency.getInstance(currencyCode)

        // Ask ICU for the symbol the currency uses in its OWN home locale, not
        // the app's. USD in en_US → "$", not "$US" (which is what a fr locale
        // would give). EUR in a random EU locale → "€". BHD in ar_BH → Arabic
        // letters → detected → ISO fallback. This gets the recognisable,
        // universal glyph rather than a locale-specific abbreviation.
        val symbolLocale = jvmLocaleForCurrency(currencyCode) ?: Locale.ROOT
        val nativeSymbol = currency.getSymbol(symbolLocale) ?: currencyCode
        val displaySymbol = if (nativeSymbol.containsShapingScript()) currencyCode else nativeSymbol

        val nf = NumberFormat.getCurrencyInstance(locale) as DecimalFormat
        nf.currency = currency
        // The getter returns a defensive copy — mutate then reassign so the
        // change actually lands on the formatter.
        nf.decimalFormatSymbols = nf.decimalFormatSymbols.apply {
            currencySymbol = displaySymbol
        }
        nf.format(javaAmount)
    } catch (_: Exception) {
        "${amount.toPlainString()} $currencyCode"
    }
}

// Any character that requires text shaping to render correctly (RTL bidi,
// contextual joining, reordering, indic clusters, CJK ideographs). Presence
// of a single such char in the currency symbol is enough to force the
// ISO-code fallback.
private fun String.containsShapingScript(): Boolean = any { c ->
    val code = c.code
    // Hebrew (incl. presentation forms)
    (code in 0x0590..0x05FF) || (code in 0xFB1D..0xFB4F) ||
    // Arabic (base, supplement, extended-A, presentation forms A/B)
    (code in 0x0600..0x06FF) || (code in 0x0750..0x077F) ||
    (code in 0x08A0..0x08FF) || (code in 0xFB50..0xFDFF) || (code in 0xFE70..0xFEFF) ||
    // Indic block range: Devanagari, Bengali, Gurmukhi, Gujarati, Oriya,
    // Tamil, Telugu, Kannada, Malayalam, Sinhala
    (code in 0x0900..0x0DFF) ||
    // Thai, Lao, Tibetan
    (code in 0x0E00..0x0FFF) ||
    // Myanmar
    (code in 0x1000..0x109F) ||
    // Khmer
    (code in 0x1780..0x17FF) ||
    // Hiragana, Katakana, CJK Unified + Extension A, Hangul Syllables, CJK Compat
    (code in 0x3040..0x30FF) || (code in 0x3400..0x9FFF) ||
    (code in 0xAC00..0xD7AF) || (code in 0xF900..0xFAFF)
}
