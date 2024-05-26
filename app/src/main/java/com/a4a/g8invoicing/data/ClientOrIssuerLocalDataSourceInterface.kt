package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.screens.PersonType
import kotlinx.coroutines.flow.Flow

/**
 * Interface for ClientLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */

interface ClientOrIssuerLocalDataSourceInterface {
    fun fetchClientOrIssuer(id: Long): ClientOrIssuerState?
    fun fetchDocumentClientOrIssuer(id: Long): ClientOrIssuerState?
    fun fetchAll(type: PersonType): Flow<List<ClientOrIssuerState>>
    suspend fun saveClientOrIssuer(clientOrIssuer: ClientOrIssuerState, type: String)
    suspend fun saveDocumentClientOrIssuer(clientOrIssuer: ClientOrIssuerState, type: String): Int?
    suspend fun duplicateClientsOrIssuers(clientsOrIssuers: List<ClientOrIssuerState>)
    suspend fun updateClientOrIssuer(clientOrIssuer: ClientOrIssuerState)
    suspend fun updateDocumentClientOrIssuer(clientOrIssuer: ClientOrIssuerState)
    suspend fun deleteClientOrIssuer(id: Long)
    suspend fun deleteDocumentClientOrIssuer(id: Long)
    suspend fun getLastCreatedClientOrIssuerId(): Long?
    suspend fun getLastCreatedDocumentClientOrIssuerId(): Long?
}
