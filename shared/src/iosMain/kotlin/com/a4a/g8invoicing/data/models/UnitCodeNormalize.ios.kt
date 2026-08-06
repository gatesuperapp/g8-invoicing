package com.a4a.g8invoicing.data.models

// iOS isn't shipped today (Android + Desktop only). Pragmatic fallback: manual
// mapping of the accented characters we actually see in the 4 supported UI
// languages (fr / en / es / de). If iOS ever ships, swap this for NSString
// decomposedStringWithCanonicalMapping + Mn-category drop.
private val diacriticMap: Map<Char, Char> = buildMap {
    "àáâãäåÀÁÂÃÄÅ".forEach { put(it, 'a') }
    "èéêëÈÉÊË".forEach { put(it, 'e') }
    "ìíîïÌÍÎÏ".forEach { put(it, 'i') }
    "òóôõöÒÓÔÕÖ".forEach { put(it, 'o') }
    "ùúûüÙÚÛÜ".forEach { put(it, 'u') }
    "ýÿÝŸ".forEach { put(it, 'y') }
    put('ñ', 'n'); put('Ñ', 'n')
    put('ç', 'c'); put('Ç', 'c')
    put('ß', 's')
}

actual fun stripDiacriticsAndLower(s: String): String {
    val trimmed = s.trim()
    if (trimmed.isEmpty()) return ""
    val sb = StringBuilder(trimmed.length)
    for (ch in trimmed) sb.append(diacriticMap[ch] ?: ch)
    return sb.toString().lowercase()
}
