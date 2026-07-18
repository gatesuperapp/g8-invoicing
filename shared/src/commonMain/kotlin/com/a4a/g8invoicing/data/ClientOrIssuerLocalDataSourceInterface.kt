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
    suspend fun getMasterVersion(masterId: Long): Int?

    /**
     * Country code (ISO 3166-1 alpha-2, uppercase) of the most recently created address
     * that has one saved. Used by the address form as the primary cascade default for a
     * newly-created address slot — someone who mostly bills the same country doesn't
     * re-pick it every time. null when no prior address carries a country (fresh install
     * or entirely legacy data), in which case the caller falls back to the device locale
     * and then to "FR" via [com.a4a.g8invoicing.data.models.CountryCodes.pickDefaultForNewAddress].
     */
    suspend fun getLastCountryCode(): String?

    /** Bulk-fill country_code on every client (type='client') address that has
     *  no country yet. Used by the 1.8 onboarding wizard to remedy legacy
     *  clients whose addresses predate the country_code field. Issuer addresses
     *  are untouched. */
    suspend fun setCountryForClientsWithoutCountry(countryCode: String)
}
