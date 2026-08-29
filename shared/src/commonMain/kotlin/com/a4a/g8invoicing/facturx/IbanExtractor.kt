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
 *  - Case-insensitive `IBAN` / `BIC` label prefix, optionally followed
 *    by `:`, `.`, or whitespace.
 *  - IBAN payload: 7 to 42 characters covering letters, digits and
 *    embedded whitespace (users type them grouped: "FR76 3000 4008 …").
 *    Whitespace is stripped on capture, so the returned string is always
 *    uppercase and space-free.
 *  - BIC payload: 8 to 11 alphanumeric characters, uppercased on return.
 *  - First footer with a matching IBAN wins. BIC is looked up in the
 *    same footer.
 */
fun extractBankInfoFromFooters(footers: List<String>): DetectedBankInfo {
    val ibanRegex = Regex("""(?i)IBAN\s*[:.]?\s*([A-Z0-9][A-Z0-9\s]{6,41})""")
    val bicRegex = Regex("""(?i)BIC\s*[:.]?\s*([A-Z0-9]{8,11})""")
    for (footer in footers) {
        if (footer.isBlank()) continue
        val ibanMatch = ibanRegex.find(footer) ?: continue
        val iban = ibanMatch.groupValues[1]
            .replace(Regex("""\s+"""), "")
            .uppercase()
            .takeIf { it.length in 7..34 }
            ?: continue
        val bic = bicRegex.find(footer)?.groupValues?.get(1)?.uppercase()
        return DetectedBankInfo(iban, bic)
    }
    return DetectedBankInfo(null, null)
}
