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
    private val documentClientsAndIssuers = mutableListOf<ClientOrIssuerState>()
    private val dataFlow = MutableStateFlow<List<ClientOrIssuerState>>(emptyList())
    private var nextId = 1
    private var nextDocumentId = 1000 // Start document IDs at 1000 to distinguish them
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

    // Document clients/issuers (copies in documents)
    fun getDocumentIssuers(): List<ClientOrIssuerState> = documentClientsAndIssuers.filter {
        it.type == ClientOrIssuerType.DOCUMENT_ISSUER || it.type == ClientOrIssuerType.ISSUER
    }
    fun getDocumentClients(): List<ClientOrIssuerState> = documentClientsAndIssuers.filter {
        it.type == ClientOrIssuerType.DOCUMENT_CLIENT || it.type == ClientOrIssuerType.CLIENT
    }

    // Get the last issuer from master table (ordered by update time simulation)
    override suspend fun getLastIssuer(): ClientOrIssuerState? = clientsAndIssuers
        .filter { it.type == ClientOrIssuerType.ISSUER }
        .lastOrNull()
        ?.let { issuer ->
            // Return as DOCUMENT_ISSUER type with originalClientId set (like real implementation)
            ClientOrIssuerState(
                id = null,
                type = ClientOrIssuerType.DOCUMENT_ISSUER,
                originalClientOrIssuerId = issuer.id,
                firstName = issuer.firstName,
                name = issuer.name,
                phone = issuer.phone,
                emails = issuer.emails,
                addresses = issuer.addresses,
                notes = issuer.notes,
                companyId1Label = issuer.companyId1Label,
                companyId1Number = issuer.companyId1Number,
                companyId2Label = issuer.companyId2Label,
                companyId2Number = issuer.companyId2Number,
                companyId3Label = issuer.companyId3Label,
                companyId3Number = issuer.companyId3Number,
                logoPath = issuer.logoPath
            )
        }

    fun clear() {
        clientsAndIssuers.clear()
        documentClientsAndIssuers.clear()
        dataFlow.value = emptyList()
        nextId = 1
        nextDocumentId = 1000
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

    override suspend fun createNewAndReturnId(clientOrIssuer: ClientOrIssuerState): Long? {
        val newId = nextId++
        val newItem = clientOrIssuer.copy(id = newId)
        clientsAndIssuers.add(newItem)
        lastCreatedId = newId.toLong()
        dataFlow.value = clientsAndIssuers.toList()
        return newId.toLong()
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

    override suspend fun updateDocumentClientOrIssuer(documentClientOrIssuer: ClientOrIssuerState, syncToMaster: Boolean) {
        // Update in document clients/issuers list
        val docIndex = documentClientsAndIssuers.indexOfFirst { it.id == documentClientOrIssuer.id }
        if (docIndex >= 0) {
            documentClientsAndIssuers[docIndex] = documentClientOrIssuer
        }

        // Synchronize to master table if syncToMaster is true and it's an issuer with originalClientId
        if (syncToMaster) {
            documentClientOrIssuer.originalClientOrIssuerId?.let { masterId ->
                val masterIndex = clientsAndIssuers.indexOfFirst { it.id == masterId }
                if (masterIndex >= 0) {
                    // Update master table with document's data
                    clientsAndIssuers[masterIndex] = clientsAndIssuers[masterIndex].copy(
                        firstName = documentClientOrIssuer.firstName,
                        name = documentClientOrIssuer.name,
                        phone = documentClientOrIssuer.phone,
                        emails = documentClientOrIssuer.emails,
                        addresses = documentClientOrIssuer.addresses,
                        notes = documentClientOrIssuer.notes,
                        companyId1Label = documentClientOrIssuer.companyId1Label,
                        companyId1Number = documentClientOrIssuer.companyId1Number,
                        companyId2Label = documentClientOrIssuer.companyId2Label,
                        companyId2Number = documentClientOrIssuer.companyId2Number,
                        companyId3Label = documentClientOrIssuer.companyId3Label,
                        companyId3Number = documentClientOrIssuer.companyId3Number,
                        logoPath = documentClientOrIssuer.logoPath
                    )
                    dataFlow.value = clientsAndIssuers.toList()
                }
            }
        }
    }

    // Add a document client/issuer (for creating copies in documents)
    fun addDocumentClientOrIssuer(documentClientOrIssuer: ClientOrIssuerState): Int {
        val newId = nextDocumentId++
        val newItem = documentClientOrIssuer.copy(id = newId)
        documentClientsAndIssuers.add(newItem)
        return newId
    }

    // Get document client/issuer by ID
    fun getDocumentClientOrIssuer(id: Int): ClientOrIssuerState? {
        return documentClientsAndIssuers.find { it.id == id }
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

    override suspend fun getLastCreatedIssuerId(): Long? {
        return clientsAndIssuers
            .filter { it.type == ClientOrIssuerType.ISSUER }
            .maxByOrNull { it.id ?: 0 }
            ?.id?.toLong()
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

    override suspend fun getMasterVersion(masterId: Long): Int? {
        return clientsAndIssuers.find { it.id == masterId.toInt() }?.version
    }
}
