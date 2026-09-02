package com.a4a.g8invoicing.ui.navigation

enum class DocumentTag {
    UNDEFINED,
    DRAFT,
    SENT,
    PAID,
    LATE,
    REMINDED,
    CANCELLED,
    // Delivery notes / quotes only: set automatically when the source
    // document is converted to an invoice via the bottom-bar Convert action.
    INVOICED
}
