package com.a4a.g8invoicing.util

import java.text.Normalizer
import java.util.Locale

private val COMBINING_MARKS = "\\p{InCombiningDiacriticalMarks}+".toRegex()

actual fun String.normalizeForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(COMBINING_MARKS, "")
        .lowercase(Locale.ROOT)
