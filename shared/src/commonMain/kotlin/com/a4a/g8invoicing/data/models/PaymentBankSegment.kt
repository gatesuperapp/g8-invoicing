package com.a4a.g8invoicing.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Structured representation of the bank-details label — mirrors
 * [PaymentLabelSegment] but for the IBAN/BIC block. Two locked token kinds:
 * - [IbanToken] renders as `"$identifierLabel : $iban"` where the label swaps
 *   between "IBAN" and "N° de compte" based on the picked bank's country.
 * - [BicToken] renders as `"BIC : $bic"`. Skipped if the doc has no BIC.
 *
 * Persisted as JSON on `payment_bank_label` (Invoice + CreditNote). Defensive
 * fallback: unparseable string yields one Free segment.
 */
@Serializable
sealed interface PaymentBankSegment {
    @Serializable
    @SerialName("free")
    data class Free(val text: String) : PaymentBankSegment

    @Serializable
    @SerialName("iban")
    data object IbanToken : PaymentBankSegment

    @Serializable
    @SerialName("bic")
    data object BicToken : PaymentBankSegment
}

private val paymentBankJson = Json {
    classDiscriminator = "kind"
    ignoreUnknownKeys = true
}
private val paymentBankListSerializer = ListSerializer(PaymentBankSegment.serializer())

fun serializePaymentBankLabel(segments: List<PaymentBankSegment>): String =
    paymentBankJson.encodeToString(paymentBankListSerializer, segments)

fun parsePaymentBankLabel(raw: String?): List<PaymentBankSegment> {
    if (raw.isNullOrEmpty()) return emptyList()
    return runCatching {
        paymentBankJson.decodeFromString(paymentBankListSerializer, raw)
    }.getOrElse { listOf(PaymentBankSegment.Free(raw)) }
}

/** Render one token as it appears in the flattened string. Empty value → empty. */
private fun renderToken(
    token: PaymentBankSegment,
    identifierLabel: String,
    iban: String,
    bic: String,
): String = when (token) {
    is PaymentBankSegment.IbanToken -> if (iban.isEmpty()) "" else "$identifierLabel : $iban"
    is PaymentBankSegment.BicToken -> if (bic.isEmpty()) "" else "BIC : $bic"
    is PaymentBankSegment.Free -> token.text
}

/**
 * Flatten to the rendered string. When a token has no value, its surrounding
 * whitespace-only Free segments are also dropped so the block doesn't leave
 * a lonely "\n" hanging.
 */
fun flattenPaymentBank(
    segments: List<PaymentBankSegment>,
    identifierLabel: String,
    iban: String,
    bic: String,
): String {
    val kept = filterRenderable(segments, identifierLabel, iban, bic)
        .filter { it !is PaymentBankSegment.Free || it.text.isNotEmpty() }
    return buildString {
        kept.forEachIndexed { i, seg ->
            val rendered = renderToken(seg, identifierLabel, iban, bic)
            if (i > 0 && seg !is PaymentBankSegment.Free) {
                // Two consecutive tokens with no Free between them → newline.
                val prev = kept[i - 1]
                if (prev !is PaymentBankSegment.Free) append("\n")
            }
            append(rendered)
        }
    }
}

private fun isWhitespaceBlockedByEmptyToken(
    neighbour: PaymentBankSegment?,
    identifierLabel: String,
    iban: String,
    bic: String,
): Boolean = neighbour != null && neighbour !is PaymentBankSegment.Free &&
    renderToken(neighbour, identifierLabel, iban, bic).isEmpty()

data class PaymentBankTokenSpan(val range: IntRange, val kind: PaymentBankSegment)

fun paymentBankTokenSpans(
    segments: List<PaymentBankSegment>,
    identifierLabel: String,
    iban: String,
    bic: String,
): List<PaymentBankTokenSpan> {
    val out = mutableListOf<PaymentBankTokenSpan>()
    var cursor = 0
    val kept = filterRenderable(segments, identifierLabel, iban, bic)
        .filter { it !is PaymentBankSegment.Free || it.text.isNotEmpty() }
    kept.forEachIndexed { i, seg ->
        val rendered = renderToken(seg, identifierLabel, iban, bic)
        if (i > 0 && seg !is PaymentBankSegment.Free) {
            val prev = kept[i - 1]
            if (prev !is PaymentBankSegment.Free) cursor += 1
        }
        if (seg !is PaymentBankSegment.Free) {
            out.add(PaymentBankTokenSpan(cursor..(cursor + rendered.length), seg))
        }
        cursor += rendered.length
    }
    return out
}

private fun filterRenderable(
    segments: List<PaymentBankSegment>,
    identifierLabel: String,
    iban: String,
    bic: String,
): List<PaymentBankSegment> {
    val kept = mutableListOf<PaymentBankSegment>()
    segments.forEachIndexed { i, seg ->
        val rendered = renderToken(seg, identifierLabel, iban, bic)
        val prev = segments.getOrNull(i - 1)
        val next = segments.getOrNull(i + 1)
        val isEmptyToken = seg !is PaymentBankSegment.Free && rendered.isEmpty()
        val isDanglingSeparator = seg is PaymentBankSegment.Free &&
            seg.text.isBlank() &&
            (isWhitespaceBlockedByEmptyToken(prev, identifierLabel, iban, bic) ||
                isWhitespaceBlockedByEmptyToken(next, identifierLabel, iban, bic))
        if (!isEmptyToken && !isDanglingSeparator) kept.add(seg)
    }
    return kept
}

/**
 * Rebuild segments from an edited flattened string. Walks the previous token
 * list in order and locates each token's rendered form in [newText]; missing
 * or reordered tokens = the edit crossed a protected span → return null and
 * the caller rejects the input. Free portions between/around the tokens
 * become fresh Free segments so arbitrary insertions flow through cleanly.
 */
fun rebuildPaymentBankSegments(
    newText: String,
    previousSegments: List<PaymentBankSegment>,
    identifierLabel: String,
    iban: String,
    bic: String,
): List<PaymentBankSegment>? {
    val previousTokens = previousSegments.filter { it !is PaymentBankSegment.Free }
    val visibleTokens = previousTokens.filter { renderToken(it, identifierLabel, iban, bic).isNotEmpty() }
    val invisibleTokens = previousTokens.filter { renderToken(it, identifierLabel, iban, bic).isEmpty() }
    val result = mutableListOf<PaymentBankSegment>()
    var cursor = 0
    for (token in visibleTokens) {
        val rendered = renderToken(token, identifierLabel, iban, bic)
        val idx = newText.indexOf(rendered, cursor)
        if (idx < 0) return null
        if (idx > cursor) result.add(PaymentBankSegment.Free(newText.substring(cursor, idx)))
        result.add(token)
        cursor = idx + rendered.length
    }
    if (cursor < newText.length) result.add(PaymentBankSegment.Free(newText.substring(cursor)))
    // Invisible tokens carried at the end so they still render when their
    // value gets filled in later. Force a "\n" separator unless the trailing
    // Free already ends with one — otherwise the future value would glue to
    // whatever text the user typed (e.g. IBAN + free text + BIC on one line).
    for (token in invisibleTokens) {
        val last = result.lastOrNull()
        val alreadyEndsWithNewline = last is PaymentBankSegment.Free && last.text.endsWith("\n")
        if (result.isNotEmpty() && !alreadyEndsWithNewline) {
            result.add(PaymentBankSegment.Free("\n"))
        }
        result.add(token)
    }
    return mergeAdjacentFree(result)
}

/** Default segments seeded on a fresh invoice: both tokens on separate lines. */
fun defaultPaymentBankSegments(): List<PaymentBankSegment> = listOf(
    PaymentBankSegment.IbanToken,
    PaymentBankSegment.Free("\n"),
    PaymentBankSegment.BicToken,
)

private fun mergeAdjacentFree(segments: List<PaymentBankSegment>): List<PaymentBankSegment> {
    val out = mutableListOf<PaymentBankSegment>()
    for (seg in segments) {
        val last = out.lastOrNull()
        if (last is PaymentBankSegment.Free && seg is PaymentBankSegment.Free) {
            out[out.size - 1] = PaymentBankSegment.Free(last.text + seg.text)
        } else {
            out.add(seg)
        }
    }
    return out
}
