package com.a4a.g8invoicing.facturx

import com.a4a.g8invoicing.ui.states.AddressState

/**
 * A party carries a free-label list of addresses. For CII / Factur-X routing
 * we need to know which is:
 *  - the DELIVERY address (→ ram:ShipToTradeParty under HeaderTradeDelivery)
 *  - the BILLING address (→ ram:InvoiceeTradeParty under HeaderTradeSettlement)
 *  - the BASE / siège (→ SellerTradeParty or BuyerTradeParty PostalTradeAddress)
 *
 * The user labels each address in their own words in whichever language the
 * doc was written; we detect DELIVERY / BILLING by keyword scan against a
 * multilingual bag (FR/EN/DE/ES cover the app's shipping locales). Anything
 * that doesn't hit either keyword is treated as BASE.
 *
 * With 1 address: BASE only.
 * With 2 addresses: 1 identified (usually DELIVERY) + 1 BASE — the user says
 *   the second slot is nearly always the shipping address, so if only one
 *   keyword hits it usually reads as DELIVERY.
 * With 3 addresses: 2 identified (DELIVERY + BILLING) + 1 BASE.
 *
 * The preflight validator warns via [addressPurposeAmbiguous] when we can't
 * cover the (N - 1) required identifications — the user then edits the labels
 * to disambiguate before export.
 */

enum class AddressPurpose { DELIVERY, BILLING, BASE }

/**
 * Grouped view of a party's addresses after keyword classification. `base`
 * always exists (falls back to `addresses.first()` when every slot got labeled
 * as delivery/billing). `delivery` / `billing` are null when no address in
 * the list matches their respective keyword set.
 */
data class AddressClassification(
    val base: AddressState?,
    val delivery: AddressState?,
    val billing: AddressState?,
)

/**
 * True when [addresses] carries more than one entry AND the user hasn't
 * labelled enough of them to route — i.e. we'd end up with more than one
 * unidentified "base" and no way to tell which is the siège vs which was
 * meant to be delivery / billing. Fired from the preflight validator as an
 * [CiiValidationIssue] so the Oups modal nudges the user to fix labels.
 */
fun addressPurposeAmbiguous(addresses: List<AddressState>?): Boolean {
    val list = addresses ?: return false
    if (list.size <= 1) return false
    val classification = classifyAddresses(list)
    val identified = listOfNotNull(classification.delivery, classification.billing).size
    // N-1 addresses must have a specific purpose; the last one plays the
    // BASE role by default. So identified >= N-1 = OK, less = warn.
    return identified < list.size - 1
}

fun classifyAddresses(addresses: List<AddressState>): AddressClassification {
    if (addresses.isEmpty()) return AddressClassification(null, null, null)

    // Score each address against both keyword sets; keep the first hit per
    // purpose so a user who accidentally puts "livraison" in two labels
    // doesn't emit two ShipToTradeParty blocks.
    var delivery: AddressState? = null
    var billing: AddressState? = null
    for (address in addresses) {
        val label = address.addressTitle?.text
        val purpose = classifyAddressPurpose(label)
        if (purpose == AddressPurpose.DELIVERY && delivery == null) delivery = address
        if (purpose == AddressPurpose.BILLING && billing == null) billing = address
    }
    // Base = first address that isn't already delivery / billing. Falls back
    // to the first address so we always have SOMETHING for SellerTradeParty /
    // BuyerTradeParty (BR-CO-14 requires an address on the primary party).
    val base = addresses.firstOrNull { it !== delivery && it !== billing }
        ?: addresses.first()
    return AddressClassification(base = base, delivery = delivery, billing = billing)
}

/** Classify a single free-label address title. Case-, accent- and
 *  whitespace-insensitive; a label like "N° 2 — Adresse de LIVRAISON" hits
 *  the DELIVERY branch. */
fun classifyAddressPurpose(label: String?): AddressPurpose {
    val normalized = normalizeAddressLabel(label) ?: return AddressPurpose.BASE
    if (DELIVERY_KEYWORDS.any { it in normalized }) return AddressPurpose.DELIVERY
    if (BILLING_KEYWORDS.any { it in normalized }) return AddressPurpose.BILLING
    return AddressPurpose.BASE
}

/** Normalise the label for keyword scanning: lowercase, strip common Latin
 *  accents, collapse whitespace. Same intent as the label normaliser used in
 *  [CompanyIdMapping] — keeps a "Facturation N°2" label matching "facturation". */
private fun normalizeAddressLabel(label: String?): String? {
    val trimmed = label?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return trimmed.lowercase()
        .replace('à', 'a').replace('â', 'a').replace('ä', 'a')
        .replace('é', 'e').replace('è', 'e').replace('ê', 'e').replace('ë', 'e')
        .replace('î', 'i').replace('ï', 'i')
        .replace('ô', 'o').replace('ö', 'o')
        .replace('ù', 'u').replace('û', 'u').replace('ü', 'u')
        .replace('ç', 'c')
        .replace('ñ', 'n')
        .replace('ß', 's')
}

// Multilingual keyword bags. Kept as `contains` substrings so a compound
// label ("Adresse de livraison N°2") still hits. Add new entries here when a
// new UI locale ships — the classifier accepts hits from ANY language
// regardless of the doc's formatLocale, so a FR user who types the English
// "delivery" still gets it routed as DELIVERY.
private val DELIVERY_KEYWORDS = setOf(
    // FR
    "livraison", "livrer", "expedition",
    // EN
    "delivery", "shipping", "ship to", "shipto",
    // DE — "Lieferung" (delivery), "Versand" (shipping)
    "lieferung", "versand",
    // ES — "entrega" (delivery), "envio" (shipping) [normalised from "envío"]
    "entrega", "envio",
)

private val BILLING_KEYWORDS = setOf(
    // FR — "facturation" first so "facture" doesn't shadow the more precise
    // "facturation" label (both hit though — contains matches either).
    "facturation", "facture",
    // EN — "billing" and "invoice/invoicing"
    "billing", "bill to", "billto", "invoice", "invoicing",
    // DE — "Rechnung" (invoice), "Rechnungsadresse" (billing address)
    "rechnung",
    // ES — "facturacion" (normalised from "facturación"), "factura"
    "facturacion", "factura",
)
