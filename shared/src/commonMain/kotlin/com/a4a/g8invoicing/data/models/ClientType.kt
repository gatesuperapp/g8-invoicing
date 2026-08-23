package com.a4a.g8invoicing.data.models

/**
 * B2B vs B2C discriminator on a client card. Drives the Factur-X export
 * gate: PROFESSIONAL → electronic invoice (Factur-X), INDIVIDUAL → plain
 * PDF. Null means the user hasn't answered yet — the export flow surfaces
 * a modal to force the choice before generating the file.
 *
 * Stored as `.name` in DB (`ClientOrIssuer.client_type`,
 * `DocumentClientOrIssuer.client_type`). Frozen on the doc-side at save
 * time so re-exporting an old doc keeps its original nature even after
 * the master client card is edited.
 */
enum class ClientType {
    PROFESSIONAL,
    INDIVIDUAL,
    ;

    companion object {
        fun fromDb(value: String?): ClientType? =
            value?.let { runCatching { valueOf(it) }.getOrNull() }
    }
}

/**
 * Non-null payload wrapper for the CLIENT_TYPE screen element. The form's
 * `onValueChange(ScreenElement, Any)` contract can't carry a raw null, so
 * we wrap the (nullable) choice — clearing the selection fires
 * `ClientTypeChoice(null)`.
 */
data class ClientTypeChoice(val value: ClientType?)
