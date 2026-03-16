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
}
