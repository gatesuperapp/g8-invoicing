package com.a4a.g8invoicing.data.models

/**
 * BT-120 default exemption text seeded on newly-created Invoice / CreditNote
 * when the issuer is in the franchise en base regime (vatExempt=true → tax
 * category E). Bound to the issuer's country code — its legal regime dictates
 * the wording, not the reader's UI locale.
 *
 * Returns null when we have no reliable citation for the country. The user
 * must then fill the field via the text menu before exporting Factur-X;
 * writing a French mention on a Spanish invoice would be legally wrong.
 */
fun defaultVatExemptionText(issuerCountryCode: String?): String? =
    when (issuerCountryCode?.uppercase()) {
        "FR" -> "TVA non applicable, art. 293 B du CGI"
        "DE" -> "Steuerbefreit nach § 19 UStG (Kleinunternehmerregelung)"
        else -> null
    }
