package com.a4a.g8invoicing.data.models

/**
 * Locale-agnostic search normalisation: trim, lowercase, and strip diacritics
 * (NFD decomposition + drop combining marks). Applied on BOTH sides of the
 * search index (indexed term AND user query) so "unite"/"unité", "metre"/"mètre",
 * "année"/"annee", "pièces"/"pieces" all collapse to the same token.
 *
 * Kotlin common has no NFD normalizer, so this delegates to a platform expect —
 * java.text.Normalizer on JVM (Android + Desktop) and NSString on iOS.
 */
expect fun stripDiacriticsAndLower(s: String): String
