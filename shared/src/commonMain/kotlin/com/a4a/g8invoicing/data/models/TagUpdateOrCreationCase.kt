package com.a4a.g8invoicing.data.models

enum class TagUpdateOrCreationCase {
    TAG_CREATION, // new invoice, delivery note conversion, duplication
    UPDATED_BY_USER,
    AUTOMATICALLY_CANCELLED, //after creating credit note or corrected invoice
    DUE_DATE_EXPIRED
}
