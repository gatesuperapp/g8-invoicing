package com.a4a.g8invoicing.fakes

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.PersonType
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeClientOrIssuerDataSource : ClientOrIssuerLocalDataSourceInterface {

    private val clientsAndIssuers = mutableListOf<ClientOrIssuerState>()
    private val dataFlow = MutableStateFlow<List<ClientOrIssuerState>>(emptyList())
    private var nextId = 1
    private var lastCreatedId: Long? = null

    // For testing: access internal state
    fun getClients(): List<ClientOrIssuerState> = clientsAndIssuers.filter {
        it.type == ClientOrIssuerType.CLIENT
    }
    fun getIssuers(): List<ClientOrIssuerState> = clientsAndIssuers.filter {
        it.type == ClientOrIssuerType.ISSUER
    }
    fun getAll(): List<ClientOrIssuerState> = clientsAndIssuers.toList()
    fun getCount(): Int = clientsAndIssuers.size
    fun clear() {
        clientsAndIssuers.clear()
        dataFlow.value = emptyList()
        nextId = 1
        lastCreatedId = null
    }

    override suspend fun fetchClientOrIssuer(id: Long): ClientOrIssuerState? {
        return clientsAndIssuers.find { it.id == id.toInt() }
    }

    override fun fetchAll(type: PersonType): Flow<List<ClientOrIssuerState>> {
        return dataFlow.map { list ->
            list.filter {
                when (type) {
                    PersonType.CLIENT -> it.type == ClientOrIssuerType.CLIENT
                    PersonType.ISSUER -> it.type == ClientOrIssuerType.ISSUER
                }
            }
        }
    }

    override suspend fun createNew(clientOrIssuer: ClientOrIssuerState): Boolean {
        val newId = nextId++
        val newItem = clientOrIssuer.copy(id = newId)
        clientsAndIssuers.add(newItem)
        lastCreatedId = newId.toLong()
        dataFlow.value = clientsAndIssuers.toList()
        return true
    }

    override suspend fun duplicateClients(clientsOrIssuers: List<ClientOrIssuerState>) {
        clientsOrIssuers.forEach { item ->
            val duplicate = item.copy(
                id = nextId++,
                name = TextFieldValue("${item.name.text} (copie)")
            )
            clientsAndIssuers.add(duplicate)
        }
        dataFlow.value = clientsAndIssuers.toList()
    }

    override suspend fun updateClientOrIssuer(clientOrIssuer: ClientOrIssuerState) {
        val index = clientsAndIssuers.indexOfFirst { it.id == clientOrIssuer.id }
        if (index >= 0) {
            clientsAndIssuers[index] = clientOrIssuer
            dataFlow.value = clientsAndIssuers.toList()
        }
    }

    override suspend fun updateDocumentClientOrIssuer(documentClientOrIssuer: ClientOrIssuerState) {
        // Document clients/issuers handled separately
    }

    override suspend fun deleteClientOrIssuer(clientOrIssuer: ClientOrIssuerState) {
        clientsAndIssuers.removeAll { it.id == clientOrIssuer.id }
        dataFlow.value = clientsAndIssuers.toList()
    }

    override suspend fun deleteDocumentClientOrIssuer(documentClientOrIssuer: ClientOrIssuerState) {
        // Document clients/issuers handled separately
    }

    override suspend fun getLastCreatedClientId(): Long? {
        return lastCreatedId
    }

    // Helper to add address to a client
    fun addAddressToClient(clientId: Int, address: AddressState) {
        val client = clientsAndIssuers.find { it.id == clientId }
        client?.let {
            val currentAddresses = it.addresses?.toMutableList() ?: mutableListOf()
            currentAddresses.add(address)
            val index = clientsAndIssuers.indexOf(it)
            clientsAndIssuers[index] = it.copy(addresses = currentAddresses)
            dataFlow.value = clientsAndIssuers.toList()
        }
    }
}
