package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.data.models.PersonType
import kotlinx.coroutines.flow.Flow

/**
 * Interface for ClientLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */

interface ClientOrIssuerLocalDataSourceInterface {
    suspend fun fetchClientOrIssuer(id: Long): ClientOrIssuerState?
    fun fetchAll(type: PersonType): Flow<List<ClientOrIssuerState>>
    suspend fun createNew(clientOrIssuer: ClientOrIssuerState): Boolean
    suspend fun createNewAndReturnId(clientOrIssuer: ClientOrIssuerState): Long?
    suspend fun duplicateClients(clientsOrIssuers: List<ClientOrIssuerState>)
    suspend fun updateClientOrIssuer(clientOrIssuer: ClientOrIssuerState)
    suspend fun updateDocumentClientOrIssuer(documentClientOrIssuer: ClientOrIssuerState, syncToMaster: Boolean = true)
    suspend fun deleteClientOrIssuer(clientOrIssuer: ClientOrIssuerState)
    suspend fun deleteDocumentClientOrIssuer(documentClientOrIssuer: ClientOrIssuerState)
    suspend fun getLastCreatedClientId(): Long?
    suspend fun getLastCreatedIssuerId(): Long?
    suspend fun getLastIssuer(): ClientOrIssuerState?

    /**
     * Fetch the issuer matching [companyId] and wrap it as a
     * DOCUMENT_ISSUER-typed snapshot ready to be pinned on a brand-new
     * doc. Called from the 4 createNew() paths once
     * CurrentCompanyRepository has resolved the current company.
     */
    suspend fun getCurrentIssuer(companyId: Long): ClientOrIssuerState?
    suspend fun getMasterVersion(masterId: Long): Int?

    /**
     * Persists `original_version = <current master version>` on a document's
     * client/issuer snapshot. Semantically = "the user acknowledged that the
     * master card had drifted, and chose to keep the frozen data anyway".
     * Prevents the version-mismatch dialog from re-firing on every reopen
     * until the master gets edited *again* (which bumps master.version and
     * re-triggers the mismatch check).
     *
     * Returns the version that got written, or null if the master or doc
     * couldn't be found.
     */
    suspend fun acknowledgeDocumentClientOrIssuerVersion(
        documentClientOrIssuerId: Long,
        masterId: Long,
    ): Int?

    // Bank accounts of a master issuer, ordered by sort_order asc.
    suspend fun getIssuerBanks(issuerId: Long): List<com.a4a.g8invoicing.ui.states.IssuerBankState>

    // Freeze a specific bank on a doc's DocumentClientOrIssuer — invoked
    // when the user picks a different IBAN inside the payment-means modal.
    suspend fun updateDocumentClientOrIssuerPaymentBank(
        documentClientOrIssuerId: Long,
        iban: String?,
        bic: String?,
        country: String?,
    )

    /**
     * Country code (ISO 3166-1 alpha-2, uppercase) of the most recently created address
     * that has one saved. Used by the address form as the primary cascade default for a
     * newly-created address slot — someone who mostly bills the same country doesn't
     * re-pick it every time. null when no prior address carries a country (fresh install
     * or entirely legacy data), in which case the caller falls back to the device locale
     * and then to "FR" via [com.a4a.g8invoicing.data.models.CountryCodes.pickDefaultForNewAddress].
     */
    suspend fun getLastCountryCode(): String?

    /** Up to [limit] distinct country codes previously used on any address,
     *  ordered by most-recently-used. Powers the "Récents" section in the
     *  country picker. Empty on fresh install. */
    suspend fun getRecentCountryCodes(limit: Int): List<String>

    /** Bulk-fill country_code on every client (type='client') address that has
     *  no country yet. Used by the 1.8 onboarding wizard to remedy legacy
     *  clients whose addresses predate the country_code field. Issuer addresses
     *  are untouched. */
    suspend fun setCountryForClientsWithoutCountry(countryCode: String)

    /** Master ids of the 3 most recently used clients or issuers in documents,
     *  most recent first. Powers the "Recents" section in the picker sheets. */
    suspend fun fetchLast3RecentClientOrIssuerIds(type: PersonType): List<Long>

    /**
     * Move a batch of clients under [companyId] in a single DB transaction.
     * Used by the 1.9 migration wizard to attach the clients the user
     * selected for a given issuer, and to move orphans to their picked
     * issuer in the "à ranger" slide.
     */
    suspend fun bulkAttachToCompany(ids: List<Long>, companyId: Long)

    /**
     * Pre-delete guard for an entreprise. Returns the aggregate count of
     * clients + products + documents (invoice + credit note + delivery note
     * + quote) still attached to [companyId]. Zero → deletion is safe;
     * non-zero → the caller shows an alert instead of firing delete.
     */
    suspend fun countAttachedForCompany(companyId: Long): Long
}
