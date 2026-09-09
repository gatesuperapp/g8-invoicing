package com.a4a.g8invoicing.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Structured representation of the payment-means label — a list of segments
 * alternating between free-form text and locked mode tokens. Tokens store the
 * chip identity (e.g. "TRANSFER", "PAYPAL") so PayPal and Stripe (both sharing
 * UN/CEFACT 4461 code 68) stay distinguishable. The visible label localises at
 * flatten time (see [flattenPaymentLabel]).
 *
 * Persisted as JSON in the `payment_means_label` column. A defensive fallback
 * on parse failure yields one Free segment holding the raw string — cheap
 * safety net against dev-DB rows that predate this data-shape.
 */
@Serializable
sealed interface PaymentLabelSegment {
    @Serializable
    @SerialName("free")
    data class Free(val text: String) : PaymentLabelSegment

    @Serializable
    @SerialName("token")
    data class Token(val chipId: String) : PaymentLabelSegment
}

private val paymentLabelJson = Json {
    classDiscriminator = "kind"
    ignoreUnknownKeys = true
}
private val paymentLabelListSerializer = ListSerializer(PaymentLabelSegment.serializer())

/** Serialize to a JSON array. Empty list serialises to "[]". */
fun serializePaymentLabel(segments: List<PaymentLabelSegment>): String =
    paymentLabelJson.encodeToString(paymentLabelListSerializer, segments)

/**
 * Deserialize the persisted string. null / empty → empty list. Malformed
 * JSON → one Free segment carrying the raw string (defensive fallback).
 */
fun parsePaymentLabel(raw: String?): List<PaymentLabelSegment> {
    if (raw.isNullOrEmpty()) return emptyList()
    return runCatching {
        paymentLabelJson.decodeFromString(paymentLabelListSerializer, raw)
    }.getOrElse { listOf(PaymentLabelSegment.Free(raw)) }
}

/**
 * Flatten segments to the string rendered on preview + PDF. Free segments
 * contribute their raw text; Token segments resolve via [labelsByChip] with
 * the first char lowercased unless the whole label is uppercase (SEPA, CB) or
 * the chip is a brand name with [PaymentMeans.preserveCase] set. Chip ids
 * missing from the map are silently dropped so a legacy segment for a
 * discontinued mode won't crash the render.
 */
fun flattenPaymentLabel(
    segments: List<PaymentLabelSegment>,
    labelsByChip: Map<String, String>,
): String = buildString {
    for (seg in segments) {
        when (seg) {
            is PaymentLabelSegment.Free -> append(seg.text)
            is PaymentLabelSegment.Token -> {
                val label = labelsByChip[seg.chipId] ?: continue
                val preserve = PaymentMeans.fromChipId(seg.chipId)?.preserveCase == true
                append(if (preserve) label else lowercaseTokenLabel(label))
            }
        }
    }
}

/**
 * Byte-range of each Token inside the flattened string so the editor can
 * apply a SpanStyle to the exact protected substrings.
 */
data class PaymentLabelTokenSpan(val range: IntRange, val chipId: String)

fun paymentLabelTokenSpans(
    segments: List<PaymentLabelSegment>,
    labelsByChip: Map<String, String>,
): List<PaymentLabelTokenSpan> {
    val out = mutableListOf<PaymentLabelTokenSpan>()
    var cursor = 0
    for (seg in segments) {
        when (seg) {
            is PaymentLabelSegment.Free -> cursor += seg.text.length
            is PaymentLabelSegment.Token -> {
                val label = labelsByChip[seg.chipId] ?: continue
                val preserve = PaymentMeans.fromChipId(seg.chipId)?.preserveCase == true
                val rendered = if (preserve) label else lowercaseTokenLabel(label)
                out.add(PaymentLabelTokenSpan(cursor..(cursor + rendered.length), seg.chipId))
                cursor += rendered.length
            }
        }
    }
    return out
}

/** Chip ids currently referenced by Token segments. */
fun chipIdsFromSegments(segments: List<PaymentLabelSegment>): Set<String> =
    segments.filterIsInstance<PaymentLabelSegment.Token>().map { it.chipId }.toSet()

/**
 * Append a Token for [chipId] at the end, with a ", " separator when another
 * token is already present. No-op if the chip is already there.
 */
fun addTokenToSegments(
    segments: List<PaymentLabelSegment>,
    chipId: String,
): List<PaymentLabelSegment> {
    if (segments.any { it is PaymentLabelSegment.Token && it.chipId == chipId }) return segments
    val hasToken = segments.any { it is PaymentLabelSegment.Token }
    return mergeAdjacentFree(
        if (hasToken) segments + PaymentLabelSegment.Free(", ") + PaymentLabelSegment.Token(chipId)
        else segments + PaymentLabelSegment.Token(chipId)
    )
}

/**
 * Remove the Token with matching [chipId] and strip one adjacent ", " so the
 * sentence stays natural. Prefers the preceding separator; falls back to the
 * following one when the removed token was the first.
 */
fun removeTokenFromSegments(
    segments: List<PaymentLabelSegment>,
    chipId: String,
): List<PaymentLabelSegment> {
    val idx = segments.indexOfFirst { it is PaymentLabelSegment.Token && it.chipId == chipId }
    if (idx < 0) return segments
    val result = segments.toMutableList()
    result.removeAt(idx)
    val prevIdx = idx - 1
    val nextIdx = idx
    when {
        prevIdx >= 0 && result.getOrNull(prevIdx) is PaymentLabelSegment.Free &&
            (result[prevIdx] as PaymentLabelSegment.Free).text.endsWith(", ") -> {
            val text = (result[prevIdx] as PaymentLabelSegment.Free).text
            result[prevIdx] = PaymentLabelSegment.Free(text.dropLast(2))
        }
        nextIdx < result.size && result[nextIdx] is PaymentLabelSegment.Free &&
            (result[nextIdx] as PaymentLabelSegment.Free).text.startsWith(", ") -> {
            val text = (result[nextIdx] as PaymentLabelSegment.Free).text
            result[nextIdx] = PaymentLabelSegment.Free(text.drop(2))
        }
    }
    return mergeAdjacentFree(result)
}

/**
 * Rebuild segments from a new flattened text. Walks the old token list in
 * order and locates each label inside [newText]; if any token is missing —
 * or reordered — the edit crossed a protected span and callers should reject
 * it (returns null). Everything between / around the located tokens becomes
 * fresh Free segments, so arbitrary insertions/deletions in the free portions
 * flow through cleanly.
 */
fun rebuildSegmentsFromEditedText(
    newText: String,
    previousSegments: List<PaymentLabelSegment>,
    labelsByChip: Map<String, String>,
): List<PaymentLabelSegment>? {
    val tokens = previousSegments.filterIsInstance<PaymentLabelSegment.Token>()
    val result = mutableListOf<PaymentLabelSegment>()
    var cursor = 0
    for (token in tokens) {
        val rawLabel = labelsByChip[token.chipId] ?: return null
        val preserve = PaymentMeans.fromChipId(token.chipId)?.preserveCase == true
        val label = if (preserve) rawLabel else lowercaseTokenLabel(rawLabel)
        val idx = newText.indexOf(label, cursor)
        if (idx < 0) return null
        if (idx > cursor) result.add(PaymentLabelSegment.Free(newText.substring(cursor, idx)))
        result.add(token)
        cursor = idx + label.length
    }
    if (cursor < newText.length) result.add(PaymentLabelSegment.Free(newText.substring(cursor)))
    return mergeAdjacentFree(result)
}

/**
 * Default segments for a newly-created invoice / credit note. [prefixText] is
 * the localised "modes de paiement acceptés :" — a trailing space is appended
 * when missing so the join with the first token reads naturally.
 */
fun defaultPaymentSegments(prefixText: String): List<PaymentLabelSegment> {
    val prefixNormalised = if (prefixText.endsWith(" ")) prefixText else "$prefixText "
    return listOf(
        PaymentLabelSegment.Free(prefixNormalised),
        PaymentLabelSegment.Token(PaymentMeans.TRANSFER.chipId),
        PaymentLabelSegment.Free(", "),
        PaymentLabelSegment.Token(PaymentMeans.CHEQUE.chipId),
        PaymentLabelSegment.Free(", "),
        PaymentLabelSegment.Token(PaymentMeans.CASH.chipId),
    )
}

// ---- internal helpers ---------------------------------------------------

private fun lowercaseTokenLabel(label: String): String {
    return if (label == label.uppercase()) label
    else label.replaceFirstChar { it.lowercase() }
}

private fun mergeAdjacentFree(segments: List<PaymentLabelSegment>): List<PaymentLabelSegment> {
    val out = mutableListOf<PaymentLabelSegment>()
    for (seg in segments) {
        val last = out.lastOrNull()
        if (last is PaymentLabelSegment.Free && seg is PaymentLabelSegment.Free) {
            out[out.size - 1] = PaymentLabelSegment.Free(last.text + seg.text)
        } else {
            out.add(seg)
        }
    }
    return out
}
