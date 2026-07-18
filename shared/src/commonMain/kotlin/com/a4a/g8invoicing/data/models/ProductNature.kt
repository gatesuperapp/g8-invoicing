package com.a4a.g8invoicing.data.models

// Business nature of the product line, used by the Factur-X / UBL serializers to pick the
// right VAT category code (BT-151) — SERVICE → AE (auto-liquidation) for intra-EU B2B,
// GOODS → K (intra-EU goods) for the same. Not to be confused with the enum
// `com.a4a.g8invoicing.ui.viewmodels.ProductType` (PRODUCT / DOCUMENT_PRODUCT) which is
// an edition-scope discriminator inside ProductAddEditViewModel.
enum class ProductNature {
    GOODS, SERVICE;

    companion object {
        fun fromString(value: String?): ProductNature? = when (value) {
            "GOODS" -> GOODS
            "SERVICE" -> SERVICE
            else -> null
        }
    }
}
