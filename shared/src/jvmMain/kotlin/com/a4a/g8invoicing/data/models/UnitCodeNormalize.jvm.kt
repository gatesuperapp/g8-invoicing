package com.a4a.g8invoicing.data.models

import java.text.Normalizer

// NFD splits accented chars into base + combining mark, then the regex removes
// the marks (\p{Mn} = "Mark, nonspacing"). Locale-agnostic lowercasing at the
// end because we don't want locale-specific casing rules (e.g. Turkish dotless i)
// leaking into what's meant to be a pure ASCII match.
actual fun stripDiacriticsAndLower(s: String): String {
    val trimmed = s.trim()
    if (trimmed.isEmpty()) return ""
    val nfd = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
    val stripped = nfd.replace(Regex("\\p{Mn}+"), "")
    return stripped.lowercase()
}
