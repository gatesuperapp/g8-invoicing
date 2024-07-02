package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.screens.ClientOrIssuerType
import com.a4a.g8invoicing.ui.screens.PersonType
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for ClientLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */

interface ClientOrIssuerLocalDataSourceInterface {
    fun fetchClientOrIssuer(id: Long): ClientOrIssuerState?
    fun fetchDocumentClientOrIssuer(id: Long): DocumentClientOrIssuerState?
    fun fetchAll(type: PersonType): Flow<List<ClientOrIssuerState>>
    suspend fun saveClientOrIssuer(clientOrIssuer: ClientOrIssuerState)
    suspend fun saveDocumentClientOrIssuer(documentClientOrIssuer: DocumentClientOrIssuerState): Int?
    suspend fun duplicateClients(clientsOrIssuers: List<ClientOrIssuerState>)
    suspend fun updateClientOrIssuer(clientOrIssuer: ClientOrIssuerState)
    suspend fun updateDocumentClientOrIssuer(documentClientOrIssuer: DocumentClientOrIssuerState)
    suspend fun deleteClientOrIssuer(id: Long)
    suspend fun deleteDocumentClientOrIssuer(id: Long)
    suspend fun getLastCreatedClientOrIssuerId(): Long?
    suspend fun getLastCreatedDocumentClientOrIssuerId(): Long?
}
