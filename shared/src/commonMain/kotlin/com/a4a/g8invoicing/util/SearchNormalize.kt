package com.a4a.g8invoicing.util

/**
 * Returns a form of the string suitable for case- and accent-insensitive comparison:
 * lowercased and with diacritics stripped ("Café" and "CAFE" both become "cafe").
 * Callers should normalize both sides of the comparison.
 */
expect fun String.normalizeForSearch(): String
