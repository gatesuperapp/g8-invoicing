package com.a4a.g8invoicing.facturx

/**
 * IBAN + BIC detection results for a batch of free-text invoice footers.
 * Both fields are nullable — a scan may find an IBAN without a BIC (or
 * vice-versa), or nothing at all.
 */
data class DetectedBankInfo(val iban: String?, val bic: String?)

/**
 * Scan a list of invoice footer strings for IBAN and BIC patterns. Meant
 * to seed the "Coordonnées bancaires" step of the 1.9 migration wizard
 * with values pulled from the user's existing invoices — they used to
 * type these in the footer since there was no dedicated field for them.
 *
 * Rules:
 *  - Case-insensitive `IBAN` / `BIC` label, followed by any short run of
 *    non-letter separators ("IBAN :", "IBAN.", "IBAN N°2 :", …).
 *  - IBAN payload: strict ISO 13616 shape — 2 country letters + 2 check
 *    digits + 11 to 30 more chars from [A-Z0-9]. Users type them grouped
 *    ("FR76 3000 4008 …") so spaces inside are tolerated, but newlines
 *    are hard stops so a following "BIC …" line never bleeds into the
 *    captured IBAN. Whitespace is stripped on capture; the returned
 *    value is uppercase, space-free, 15 to 34 chars.
 *  - BIC payload: 8 to 11 alphanumeric characters, uppercased on return.
 *    Scanned on the same footer as the winning IBAN, so a BIC on its own
 *    line still gets picked up.
 *  - First footer with a matching IBAN wins.
 */
fun extractBankInfoFromFooters(footers: List<String>): DetectedBankInfo {
    // Note: character class uses a literal space, not \s, so newline / carriage
    // return / tab all terminate the capture — critical to keep a "BIC …" on
    // the next line from being appended to the IBAN.
    val ibanRegex = Regex("""(?i)\bIBAN\b[^A-Z\n\r]{0,10}?([A-Z]{2}\d{2}[A-Z0-9 ]{11,30})""")
    val bicRegex = Regex("""(?i)\bBIC\b[^A-Z\n\r]{0,10}?([A-Z0-9]{8,11})""")
    for (footer in footers) {
        if (footer.isBlank()) continue
        val ibanMatch = ibanRegex.find(footer) ?: continue
        val iban = ibanMatch.groupValues[1]
            .replace(" ", "")
            .uppercase()
            .takeIf { it.length in 15..34 }
            ?: continue
        val bic = bicRegex.find(footer)?.groupValues?.get(1)?.uppercase()
        return DetectedBankInfo(iban, bic)
    }
    return DetectedBankInfo(null, null)
}
