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
    INVOICED,
    // Invoices only: user-set "verrouillée" flag that freezes the doc.
    // When set, DocumentAddEdit hides the text / style / line bottom bar
    // so no in-place edit is possible. Applied via the bulk tag picker
    // (Marquer > Verrouiller); toggle-off through the same picker.
    LOCKED,
}
