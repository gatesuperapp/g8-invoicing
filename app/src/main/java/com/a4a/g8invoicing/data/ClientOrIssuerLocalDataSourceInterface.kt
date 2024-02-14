package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.screens.PersonType
import kotlinx.coroutines.flow.Flow

/**
 * Interface for ClientLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */

interface ClientOrIssuerLocalDataSourceInterface {
    fun fetchClientOrIssuer(id: Long): ClientOrIssuerEditable?
    fun fetchAll(type: PersonType): Flow<List<ClientOrIssuerEditable>>
    suspend fun saveClientOrIssuer(clientOrIssuer: ClientOrIssuerEditable, type: String)
    suspend fun duplicateClientOrIssuer(client: ClientOrIssuerEditable)
    suspend fun updateClientOrIssuer(client: ClientOrIssuerEditable)
   // fun checkIfEmpty(): Int
    suspend fun deleteClientOrIssuer(id: Long)
    suspend fun getLastCreatedId(): Long?
}
