package com.a4a.g8invoicing.data.models

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.RetentionState
import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * Country-aware default retention lines for an issuer that just had the
 * tax-withholding toggle turned on. Shared between:
 *  - datasources: called at `createNew` time (existingIssuer with toggle ON,
 *    no previous invoice to copy from)
 *  - viewmodels: called when the user attaches / edits an issuer on a doc
 *    that has no retentions yet
 *
 * Kept pure — labels are supplied by the caller so composables can resolve
 * them via `stringResource` and suspend callers via `getString`.
 *
 * MX gets two rows (ISR + IVA, per Mexican CFDI convention). Every other
 * country in [CountryCodes.RETENTION_COUNTRIES] gets a single row with the
 * country's standard rate ([CountryCodes.defaultRetentionRate]).
 */
fun defaultRetentionsForIssuer(
    issuer: ClientOrIssuerState,
    defaultLabel: String,
    mxIsrLabel: String,
    mxIvaLabel: String,
): List<RetentionState> {
    val country = issuer.addresses?.firstOrNull()?.countryCode?.uppercase()
    return if (country == "MX") {
        listOf(
            RetentionState(
                label = TextFieldValue(mxIsrLabel),
                rate = BigDecimal.parseString("10"),
                sortOrder = 0,
            ),
            RetentionState(
                label = TextFieldValue(mxIvaLabel),
                rate = BigDecimal.parseString("10.667"),
                sortOrder = 1,
            ),
        )
    } else {
        val rate = CountryCodes.defaultRetentionRate(country)
        listOf(
            RetentionState(
                label = TextFieldValue(defaultLabel),
                rate = BigDecimal.parseString(rate.toString()),
                sortOrder = 0,
            )
        )
    }
}
