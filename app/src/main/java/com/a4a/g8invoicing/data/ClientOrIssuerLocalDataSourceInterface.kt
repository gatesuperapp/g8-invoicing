package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.viewmodels.PersonType
import kotlinx.coroutines.flow.Flow

/**
 * Interface for ClientLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */

interface ClientOrIssuerLocalDataSourceInterface {
    fun fetch(id: Long): ClientOrIssuerState?
    fun fetchDocumentClientOrIssuer(id: Long): ClientOrIssuerState?
    fun fetchAll(type: PersonType): Flow<List<ClientOrIssuerState>>
    suspend fun saveClientOrIssuer( clientOrIssuer: ClientOrIssuerState)
    suspend fun duplicateClients(clientsOrIssuers: List<ClientOrIssuerState>)
    suspend fun updateClientOrIssuer(clientOrIssuer: ClientOrIssuerState)
    suspend fun updateDocumentClientOrIssuer(documentClientOrIssuer: ClientOrIssuerState)
    suspend fun deleteClientOrIssuer(clientOrIssuer: ClientOrIssuerState)
    suspend fun deleteDocumentClientOrIssuer(documentClientOrIssuer: ClientOrIssuerState)
    suspend fun getLastCreatedClientId(): Long?
}
