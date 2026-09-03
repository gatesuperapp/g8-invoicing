package com.a4a.g8invoicing.ui.screens.shared

import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.util.normalizeForSearch

// Filters a list of documents against a free-text query. Matches on document
// number, client last name and client first name. Uses [normalizeForSearch]
// so diacritics + case are ignored (search "durand" hits "Dürand"). An empty
// query returns the input list unchanged.
fun <T : DocumentState> List<T>.filterByQuery(query: String): List<T> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return this
    val needle = trimmed.normalizeForSearch()
    return filter { doc ->
        doc.documentNumber.text.normalizeForSearch().contains(needle) ||
            doc.documentClient?.name?.text?.normalizeForSearch()?.contains(needle) == true ||
            doc.documentClient?.firstName?.text?.normalizeForSearch()?.contains(needle) == true
    }
}

// Client/issuer variant. Matches on last name, first name and any email.
fun List<ClientOrIssuerState>.filterClientsByQuery(query: String): List<ClientOrIssuerState> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return this
    val needle = trimmed.normalizeForSearch()
    return filter { c ->
        c.name.text.normalizeForSearch().contains(needle) ||
            c.firstName?.text?.normalizeForSearch()?.contains(needle) == true ||
            c.emails?.any { it.email.text.normalizeForSearch().contains(needle) } == true
    }
}

// Product variant. Matches on name and description.
fun List<ProductState>.filterProductsByQuery(query: String): List<ProductState> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return this
    val needle = trimmed.normalizeForSearch()
    return filter { p ->
        p.name.text.normalizeForSearch().contains(needle) ||
            p.description?.text?.normalizeForSearch()?.contains(needle) == true
    }
}
