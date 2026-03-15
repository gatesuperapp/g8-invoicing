package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.data.util.DispatcherProvider
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.EmailState
import g8invoicing.ClientOrIssuerEmail
import g8invoicing.DocumentClientOrIssuerEmail
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.PersonType
import g8invoicing.ClientOrIssuer
import g8invoicing.ClientOrIssuerAddress
import g8invoicing.DocumentClientOrIssuerAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ClientOrIssuerLocalDataSource(
    db: Database,
) : ClientOrIssuerLocalDataSourceInterface {
    private val clientOrIssuerQueries = db.clientOrIssuerQueries
    private val clientOrIssuerAddressQueries = db.clientOrIssuerAddressQueries
    private val linkClientOrIssuerToAddressQueries = db.linkClientOrIssuerToAddressQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentClientOrIssuerAddressQueries = db.documentClientOrIssuerAddressQueries
    private val linkDocumentClientOrIssuerToAddressQueries =
        db.linkDocumentClientOrIssuerToAddressQueries
    private val clientOrIssuerEmailQueries = db.clientOrIssuerEmailQueries
    private val documentClientOrIssuerEmailQueries = db.documentClientOrIssuerEmailQueries

    override suspend fun fetchClientOrIssuer(id: Long): ClientOrIssuerState? {
        return withContext(DispatcherProvider.IO) {
            try {
                clientOrIssuerQueries.get(id).executeAsOneOrNull()
                    ?.let {
                        it.transformIntoEditable(
                            addresses = fetchClientOrIssuerAddresses(it.id)?.toMutableList(),
                            emails = fetchClientOrIssuerEmails(it.id)?.toMutableList()
                        )
                    }
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun fetchAll(type: PersonType): Flow<List<ClientOrIssuerState>> {
        return clientOrIssuerQueries.getAll(type.name.lowercase())
            .asFlow()
            .map { query ->
                query.executeAsList()
                    .map {
                        it.transformIntoEditable(
                            addresses = fetchClientOrIssuerAddresses(it.id)?.toMutableList(),
                            emails = fetchClientOrIssuerEmails(it.id)?.toMutableList()
                        )
                    }
            }
            .flowOn(DispatcherProvider.IO)
    }

    fun fetchClientOrIssuerAddresses(clientOrIssuerId: Long): List<AddressState>? {
        try {
            val listOfIds =
                linkClientOrIssuerToAddressQueries.getWithClientOrIssuerId(clientOrIssuerId)
                    .executeAsList()
            return if (listOfIds.isNotEmpty()) {
                listOfIds.map { addressId ->
                    clientOrIssuerAddressQueries.get(addressId)
                        .executeAsOne()
                        .transformIntoEditable()
                }
            } else
                null
        } catch (e: Exception) {
            // Log error if needed
        }
        return null
    }

    fun fetchClientOrIssuerEmails(clientOrIssuerId: Long): List<EmailState>? {
        try {
            val emails = clientOrIssuerEmailQueries.getByClientOrIssuerId(clientOrIssuerId)
                .executeAsList()
            return if (emails.isNotEmpty()) {
                emails.map { it.transformIntoEditable() }
            } else null
        } catch (e: Exception) {
            // Log error if needed
        }
        return null
    }

    fun fetchDocumentClientOrIssuerEmails(documentClientOrIssuerId: Long): List<EmailState>? {
        try {
            val emails = documentClientOrIssuerEmailQueries.getByDocumentClientOrIssuerId(documentClientOrIssuerId)
                .executeAsList()
            return if (emails.isNotEmpty()) {
                emails.map { it.transformIntoEditable() }
            } else null
        } catch (e: Exception) {
            // Log error if needed
        }
        return null
    }


    override suspend fun createNew(clientOrIssuer: ClientOrIssuerState): Boolean {
        return withContext(DispatcherProvider.IO) {
            try {
                // 1: Save main info
                saveInfoInClientOrIssuerTable(clientOrIssuer)

                // 2: Get the entity ID
                val newEntityId = clientOrIssuerQueries.getLastInsertedRowId().executeAsOneOrNull()
                    ?: run {
                        return@withContext false
                    }

                // 3: Save linked addresses
                val addressesSavedSuccessfully = saveInfoInClientOrIssuerAddressTables(newEntityId, clientOrIssuer.addresses)
                if (!addressesSavedSuccessfully) {
                    return@withContext false
                }

                // 4: Save linked emails
                saveInfoInClientOrIssuerEmailTable(newEntityId, clientOrIssuer.emails)

                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun createNewAndReturnId(clientOrIssuer: ClientOrIssuerState): Long? {
        return withContext(DispatcherProvider.IO) {
            try {
                // 1: Save main info
                saveInfoInClientOrIssuerTable(clientOrIssuer)

                // 2: Get the entity ID
                val newEntityId = clientOrIssuerQueries.getLastInsertedRowId().executeAsOneOrNull()
                    ?: return@withContext null

                // 3: Save linked addresses
                val addressesSavedSuccessfully = saveInfoInClientOrIssuerAddressTables(newEntityId, clientOrIssuer.addresses)
                if (!addressesSavedSuccessfully) {
                    return@withContext null
                }

                // 4: Save linked emails
                saveInfoInClientOrIssuerEmailTable(newEntityId, clientOrIssuer.emails)

                newEntityId
            } catch (e: Exception) {
                null
            }
        }
    }

    private suspend fun saveInfoInClientOrIssuerTable(clientOrIssuer: ClientOrIssuerState) {
        return withContext(DispatcherProvider.IO) {
            try {
                clientOrIssuerQueries.save(
                    id = null,
                    type = if (clientOrIssuer.type == ClientOrIssuerType.CLIENT ||
                        clientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT
                    ) ClientOrIssuerType.CLIENT.name.lowercase()
                    else ClientOrIssuerType.ISSUER.name.lowercase(),
                    clientOrIssuer.firstName?.text?.trim(),
                    clientOrIssuer.name.text.trim(),
                    clientOrIssuer.phone?.text?.trim(),
                    clientOrIssuer.emails?.firstOrNull()?.email?.text?.trim(),
                    clientOrIssuer.notes?.text?.trim(),
                    clientOrIssuer.companyId1Label?.text?.trim(),
                    clientOrIssuer.companyId1Number?.text?.trim(),
                    clientOrIssuer.companyId2Label?.text?.trim(),
                    clientOrIssuer.companyId2Number?.text?.trim(),
                    clientOrIssuer.companyId3Label?.text?.trim(),
                    clientOrIssuer.companyId3Number?.text?.trim(),
                    clientOrIssuer.logoPath,
                )
            } catch (e: Exception) {
                // Log error if needed
            }
        }
    }

    private suspend fun saveInfoInClientOrIssuerAddressTables(
        clientOrIssuerId: Long,
        addresses: List<AddressState>?,
    ): Boolean {
        if (addresses.isNullOrEmpty()) {
            return true
        }

        return withContext(DispatcherProvider.IO) {
            try {
                for (address in addresses) {
                    clientOrIssuerAddressQueries.save(
                        id = null,
                        address_title = address.addressTitle?.text?.trim(),
                        address_line_1 = address.addressLine1?.text?.trim(),
                        address_line_2 = address.addressLine2?.text?.trim(),
                        zip_code = address.zipCode?.text?.trim(),
                        city = address.city?.text?.trim(),
                    )

                    val newAddressId = clientOrIssuerAddressQueries.getLastInsertedRowId().executeAsOneOrNull()
                    if (newAddressId == null) {
                        return@withContext false
                    }

                    linkClientOrIssuerToAddressQueries.save(
                        id = null,
                        client_or_issuer_id = clientOrIssuerId,
                        address_id = newAddressId
                    )
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private suspend fun saveInfoInClientOrIssuerEmailTable(
        clientOrIssuerId: Long,
        emails: List<EmailState>?,
    ) {
        if (emails.isNullOrEmpty()) return

        withContext(DispatcherProvider.IO) {
            try {
                for (email in emails) {
                    if (email.email.text.isNotEmpty()) {
                        clientOrIssuerEmailQueries.save(
                            id = null,
                            client_or_issuer_id = clientOrIssuerId,
                            email = email.email.text.trim()
                        )
                    }
                }
            } catch (e: Exception) {
                // Log error if needed
            }
        }
    }

    private suspend fun saveInfoInDocumentClientOrIssuerEmailTable(
        documentClientOrIssuerId: Long,
        emails: List<EmailState>?,
    ) {
        if (emails.isNullOrEmpty()) return

        withContext(DispatcherProvider.IO) {
            try {
                for (email in emails) {
                    if (email.email.text.isNotEmpty()) {
                        documentClientOrIssuerEmailQueries.save(
                            id = null,
                            document_client_or_issuer_id = documentClientOrIssuerId,
                            email = email.email.text.trim()
                        )
                    }
                }
            } catch (e: Exception) {
                // Log error if needed
            }
        }
    }

    private suspend fun saveInfoInDocumentClientOrIssuerAddressTables(
        clientOrIssuerId: Long,
        addresses: List<AddressState>?,
    ) {
        return withContext(DispatcherProvider.IO) {
            try {
                addresses?.forEach { address ->
                    documentClientOrIssuerAddressQueries.save(
                        id = null,
                        original_address_id = address.originalAddressId?.toLong(),
                        address_title = address.addressTitle?.text?.trim(),
                        address_line_1 = address.addressLine1?.text?.trim(),
                        address_line_2 = address.addressLine2?.text?.trim(),
                        zip_code = address.zipCode?.text?.trim(),
                        city = address.city?.text?.trim(),
                    )

                    documentClientOrIssuerAddressQueries.getLastInsertedRowId().executeAsOneOrNull()
                        ?.let { newAddressId ->
                            linkDocumentClientOrIssuerToAddressQueries.save(
                                id = null,
                                document_client_or_issuer_id = clientOrIssuerId,
                                address_id = newAddressId
                            )
                        }
                }
            } catch (e: Exception) {
                // Log error if needed
            }
        }
    }


    override suspend fun duplicateClients(clientsOrIssuers: List<ClientOrIssuerState>) {
        return withContext(DispatcherProvider.IO) {
            try {
                clientsOrIssuers.forEach { client ->
                    client.id?.let {
                        // Modify name for duplicate
                        if (!client.firstName?.text.isNullOrEmpty()) {
                            client.firstName = TextFieldValue("${client.firstName?.text} - Copie")
                        } else {
                            client.name = TextFieldValue("${client.name.text} - Copie")
                        }
                        // Save new client
                        saveInfoInClientOrIssuerTable(client)
                        // Get the ID of the newly created client
                        val newClientId = clientOrIssuerQueries.getLastInsertedRowId().executeAsOneOrNull()
                        // Save addresses with the NEW client ID
                        newClientId?.let { newId ->
                            saveInfoInClientOrIssuerAddressTables(newId, client.addresses)
                        }
                    }
                }
            } catch (e: Exception) {
                // Log error if needed
            }
        }
    }

    override suspend fun updateClientOrIssuer(
        clientOrIssuer: ClientOrIssuerState,
    ) {
        return withContext(DispatcherProvider.IO) {
            try {
                clientOrIssuer.id?.let {
                    clientOrIssuerQueries.update(
                        id = it.toLong(),
                        type = clientOrIssuer.type?.name?.lowercase(),
                        first_name = clientOrIssuer.firstName?.text?.trim(),
                        name = clientOrIssuer.name.text.trim(),
                        phone = clientOrIssuer.phone?.text?.trim(),
                        email = clientOrIssuer.emails?.firstOrNull()?.email?.text?.trim(),
                        notes = clientOrIssuer.notes?.text?.trim(),
                        company_id1_label = clientOrIssuer.companyId1Label?.text?.trim(),
                        company_id1_number = clientOrIssuer.companyId1Number?.text?.trim(),
                        company_id2_label = clientOrIssuer.companyId2Label?.text?.trim(),
                        company_id2_number = clientOrIssuer.companyId2Number?.text?.trim(),
                        company_id3_label = clientOrIssuer.companyId3Label?.text?.trim(),
                        company_id3_number = clientOrIssuer.companyId3Number?.text?.trim(),
                        logo_path = clientOrIssuer.logoPath,
                    )
                }

                // Addresses to delete
                val oldAddressesIds = clientOrIssuer.id?.toLong()?.let {
                    linkClientOrIssuerToAddressQueries.get(it).executeAsList().map { it.address_id }
                }
                val newAddressesIds =
                    clientOrIssuer.addresses?.mapNotNull { it.id?.toLong() } ?: mutableListOf()

                val addressesToDelete = oldAddressesIds?.filterNot { it in newAddressesIds }
                addressesToDelete?.forEach {
                    linkClientOrIssuerToAddressQueries.delete(it)
                    clientOrIssuerAddressQueries.delete(it)
                }

                // Addresses to update and create
                clientOrIssuer.addresses?.let { addresses ->
                    val (addressesToUpdate, addressesToCreate) = addresses.partition { it.id != null }
                    addressesToUpdate.forEach { address ->
                        address.id?.let {
                            clientOrIssuerAddressQueries.update(
                                id = it.toLong(),
                                address_title = address.addressTitle?.text?.trim(),
                                address_line_1 = address.addressLine1?.text?.trim(),
                                address_line_2 = address.addressLine2?.text?.trim(),
                                zip_code = address.zipCode?.text?.trim(),
                                city = address.city?.text?.trim(),
                            )
                        }
                    }
                    clientOrIssuer.id?.let {
                        saveInfoInClientOrIssuerAddressTables(it.toLong(), addressesToCreate)
                    }
                }

                // Emails: delete all and recreate (simpler than tracking changes)
                clientOrIssuer.id?.toLong()?.let { clientId ->
                    clientOrIssuerEmailQueries.deleteByClientOrIssuerId(clientId)
                    saveInfoInClientOrIssuerEmailTable(clientId, clientOrIssuer.emails)
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun updateDocumentClientOrIssuer(
        documentClientOrIssuer: ClientOrIssuerState,
        syncToMaster: Boolean,
    ) {
        return withContext(DispatcherProvider.IO) {
            try {
                documentClientOrIssuer.id?.let {
                    documentClientOrIssuerQueries.update(
                        id = it.toLong(),
                        type = if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT ||
                            documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT
                        )
                            ClientOrIssuerType.CLIENT.name.lowercase()
                        else ClientOrIssuerType.ISSUER.name.lowercase(),
                        original_client_id = documentClientOrIssuer.originalClientOrIssuerId?.toLong(),
                        original_version = documentClientOrIssuer.originalVersion?.toLong(),
                        first_name = documentClientOrIssuer.firstName?.text?.trim(),
                        name = documentClientOrIssuer.name.text.trim(),
                        phone = documentClientOrIssuer.phone?.text?.trim(),
                        email = documentClientOrIssuer.emails?.firstOrNull()?.email?.text?.trim(),
                        notes = documentClientOrIssuer.notes?.text?.trim(),
                        company_id1_label = documentClientOrIssuer.companyId1Label?.text?.trim(),
                        company_id1_number = documentClientOrIssuer.companyId1Number?.text?.trim(),
                        company_id2_label = documentClientOrIssuer.companyId2Label?.text?.trim(),
                        company_id2_number = documentClientOrIssuer.companyId2Number?.text?.trim(),
                        company_id3_label = documentClientOrIssuer.companyId3Label?.text?.trim(),
                        company_id3_number = documentClientOrIssuer.companyId3Number?.text?.trim(),
                        logo_path = documentClientOrIssuer.logoPath,
                    )
                }
                // Addresses to delete
                val oldLinks = documentClientOrIssuer.id?.toLong()?.let {
                    linkDocumentClientOrIssuerToAddressQueries.get(it).executeAsList()
                }
                val newAddressesIds =
                    documentClientOrIssuer.addresses?.mapNotNull { it.id?.toLong() }
                        ?: mutableListOf()

                val linksToDelete = oldLinks?.filterNot { it.address_id in newAddressesIds }
                linksToDelete?.forEach { link ->
                    linkDocumentClientOrIssuerToAddressQueries.delete(link.id)
                    documentClientOrIssuerAddressQueries.delete(link.address_id)
                }

                // Addresses to update and create
                documentClientOrIssuer.addresses?.let { addresses ->
                    val (addressesToUpdate, addressesToCreate) = addresses.partition { it.id != null }
                    addressesToUpdate.forEach { address ->
                        address.id?.let {
                            documentClientOrIssuerAddressQueries.update(
                                id = it.toLong(),
                                address_title = address.addressTitle?.text?.trim(),
                                address_line_1 = address.addressLine1?.text?.trim(),
                                address_line_2 = address.addressLine2?.text?.trim(),
                                zip_code = address.zipCode?.text?.trim(),
                                city = address.city?.text?.trim(),
                            )
                        }
                    }
                    // For new addresses: create document address only (master will be created in syncToMaster block)
                    documentClientOrIssuer.id?.let { docClientId ->
                        addressesToCreate.forEach { address ->
                            // Create document address without master link (will be set during sync)
                            documentClientOrIssuerAddressQueries.save(
                                id = null,
                                original_address_id = null,
                                address_title = address.addressTitle?.text?.trim(),
                                address_line_1 = address.addressLine1?.text?.trim(),
                                address_line_2 = address.addressLine2?.text?.trim(),
                                zip_code = address.zipCode?.text?.trim(),
                                city = address.city?.text?.trim(),
                            )
                            // Link document address to document client/issuer
                            documentClientOrIssuerAddressQueries.getLastInsertedRowId()
                                .executeAsOneOrNull()?.let { newDocAddressId ->
                                    linkDocumentClientOrIssuerToAddressQueries.save(
                                        id = null,
                                        document_client_or_issuer_id = docClientId.toLong(),
                                        address_id = newDocAddressId
                                    )
                                }
                        }
                    }
                }

                // Emails: delete all and recreate
                documentClientOrIssuer.id?.toLong()?.let { docClientId ->
                    documentClientOrIssuerEmailQueries.deleteByDocumentClientOrIssuerId(docClientId)
                    saveInfoInDocumentClientOrIssuerEmailTable(docClientId, documentClientOrIssuer.emails)
                }

                // Sync to master table if syncToMaster is true and there's an originalClientOrIssuerId
                if (syncToMaster) {
                documentClientOrIssuer.originalClientOrIssuerId?.toLong()?.let { masterId ->
                    // Determine master type based on document type
                    val masterType = if (documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_ISSUER ||
                        documentClientOrIssuer.type == ClientOrIssuerType.ISSUER) {
                        ClientOrIssuerType.ISSUER.name.lowercase()
                    } else {
                        ClientOrIssuerType.CLIENT.name.lowercase()
                    }

                    // Mettre à jour la table maître ClientOrIssuer
                    clientOrIssuerQueries.update(
                        id = masterId,
                        type = masterType,
                        first_name = documentClientOrIssuer.firstName?.text?.trim(),
                        name = documentClientOrIssuer.name.text.trim(),
                        phone = documentClientOrIssuer.phone?.text?.trim(),
                        email = documentClientOrIssuer.emails?.firstOrNull()?.email?.text?.trim(),
                        notes = documentClientOrIssuer.notes?.text?.trim(),
                        company_id1_label = documentClientOrIssuer.companyId1Label?.text?.trim(),
                        company_id1_number = documentClientOrIssuer.companyId1Number?.text?.trim(),
                        company_id2_label = documentClientOrIssuer.companyId2Label?.text?.trim(),
                        company_id2_number = documentClientOrIssuer.companyId2Number?.text?.trim(),
                        company_id3_label = documentClientOrIssuer.companyId3Label?.text?.trim(),
                        company_id3_number = documentClientOrIssuer.companyId3Number?.text?.trim(),
                        logo_path = documentClientOrIssuer.logoPath,
                    )

                    // Emails: supprimer et recréer dans table maître
                    clientOrIssuerEmailQueries.deleteByClientOrIssuerId(masterId)
                    saveInfoInClientOrIssuerEmailTable(masterId, documentClientOrIssuer.emails)

                    // Fetch document addresses from DB to get actual IDs (UI state may have null IDs)
                    val docClientId = documentClientOrIssuer.id?.toLong()
                    val docAddressLinks = docClientId?.let {
                        linkDocumentClientOrIssuerToAddressQueries.get(it).executeAsList()
                    } ?: emptyList()
                    val docAddressesFromDb = docAddressLinks.mapNotNull { link ->
                        documentClientOrIssuerAddressQueries.get(link.address_id).executeAsOneOrNull()
                    }

                    // Addresses: delete master addresses that were removed from document
                    val masterLinks = linkClientOrIssuerToAddressQueries.get(masterId).executeAsList()
                    val currentMasterAddressIds = docAddressesFromDb
                        .mapNotNull { it.original_address_id }
                    val masterLinksToDelete = masterLinks.filterNot { it.address_id in currentMasterAddressIds }
                    masterLinksToDelete.forEach { link ->
                        linkClientOrIssuerToAddressQueries.delete(link.id)
                        clientOrIssuerAddressQueries.delete(link.address_id)
                    }

                    // Addresses: update or create master addresses using DB data
                    docAddressesFromDb.forEach { docAddress ->
                        val masterAddressId = docAddress.original_address_id
                        if (masterAddressId != null) {
                            // Update existing master address
                            clientOrIssuerAddressQueries.update(
                                id = masterAddressId,
                                address_title = docAddress.address_title,
                                address_line_1 = docAddress.address_line_1,
                                address_line_2 = docAddress.address_line_2,
                                zip_code = docAddress.zip_code,
                                city = docAddress.city,
                            )
                        } else {
                            // Document address has no master address - create it now
                            clientOrIssuerAddressQueries.save(
                                id = null,
                                address_title = docAddress.address_title,
                                address_line_1 = docAddress.address_line_1,
                                address_line_2 = docAddress.address_line_2,
                                zip_code = docAddress.zip_code,
                                city = docAddress.city,
                            )
                            val newMasterAddressId = clientOrIssuerAddressQueries.getLastInsertedRowId()
                                .executeAsOneOrNull()
                            newMasterAddressId?.let { newId ->
                                // Link master address to master client/issuer
                                linkClientOrIssuerToAddressQueries.save(
                                    id = null,
                                    client_or_issuer_id = masterId,
                                    address_id = newId
                                )
                                // Update document address with reference to master
                                documentClientOrIssuerAddressQueries.updateOriginalAddressId(
                                    id = docAddress.id,
                                    original_address_id = newId
                                )
                            }
                        }
                    }

                    // After syncing to master, update original_version in document to match new master version
                    documentClientOrIssuer.id?.let { docId ->
                        val newMasterVersion = clientOrIssuerQueries.get(masterId).executeAsOneOrNull()?.version
                        newMasterVersion?.let { version ->
                            documentClientOrIssuerQueries.updateOriginalVersion(
                                id = docId.toLong(),
                                original_version = version
                            )
                        }
                    }
                }
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun deleteClientOrIssuer(clientOrIssuer: ClientOrIssuerState) {
        return withContext(DispatcherProvider.IO) {
            try {
                clientOrIssuer.id?.let {
                    clientOrIssuerQueries.delete(it.toLong())
                    linkClientOrIssuerToAddressQueries.deleteWithClientId(it.toLong())
                    clientOrIssuerEmailQueries.deleteByClientOrIssuerId(it.toLong())
                }
                clientOrIssuer.addresses?.filter { it.id != null }?.forEach {
                    clientOrIssuerAddressQueries.delete(it.id!!.toLong())
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(documentClientOrIssuer: ClientOrIssuerState) {
        return withContext(DispatcherProvider.IO) {
            try {
                documentClientOrIssuer.id?.let {
                    documentClientOrIssuerQueries.delete(it.toLong())
                    linkDocumentClientOrIssuerToAddressQueries.deleteWithClientId(it.toLong())
                    documentClientOrIssuerEmailQueries.deleteByDocumentClientOrIssuerId(it.toLong())
                }
                documentClientOrIssuer.addresses?.filter { it.id != null }?.forEach {
                    documentClientOrIssuerAddressQueries.delete(it.id!!.toLong())
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun getLastCreatedClientId(): Long? {
        var lastInserted: Long? = null
        withContext(DispatcherProvider.IO) {
            try {
                lastInserted = clientOrIssuerQueries.getLastInsertedClientId().executeAsOneOrNull()
            } catch (cause: Throwable) {
            }
        }
        return lastInserted
    }

    override suspend fun getLastCreatedIssuerId(): Long? {
        return withContext(DispatcherProvider.IO) {
            try {
                clientOrIssuerQueries.getLastInsertedIssuerId().executeAsOneOrNull()
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun getLastIssuer(): ClientOrIssuerState? {
        return withContext(DispatcherProvider.IO) {
            try {
                // 1. Chercher le dernier émetteur utilisé dans un document
                val lastDocumentIssuer = documentClientOrIssuerQueries.getLastInsertedIssuer()
                    .executeAsOneOrNull()

                // 2. Si trouvé, utiliser son original_client_id pour récupérer
                //    les données à jour depuis la table maître
                val masterIssuerId = lastDocumentIssuer?.original_client_id
                    ?: clientOrIssuerQueries.getLastInsertedIssuerId().executeAsOneOrNull()

                // 3. Récupérer l'émetteur maître avec ses données actuelles
                masterIssuerId?.let { id ->
                    clientOrIssuerQueries.get(id).executeAsOneOrNull()?.let { issuer ->
                        ClientOrIssuerState(
                            id = null, // Nouveau document, pas encore d'ID
                            type = ClientOrIssuerType.DOCUMENT_ISSUER,
                            originalClientOrIssuerId = issuer.id.toInt(),
                            originalVersion = issuer.version?.toInt() ?: 1,
                            firstName = issuer.first_name?.let { TextFieldValue(text = it) },
                            name = TextFieldValue(text = issuer.name),
                            phone = issuer.phone?.let { TextFieldValue(text = it) },
                            emails = fetchClientOrIssuerEmails(issuer.id),
                            addresses = fetchClientOrIssuerAddresses(issuer.id),
                            notes = issuer.notes?.let { TextFieldValue(text = it) },
                            companyId1Label = issuer.company_id1_label?.let { TextFieldValue(text = it) },
                            companyId1Number = issuer.company_id1_number?.let { TextFieldValue(text = it) },
                            companyId2Label = issuer.company_id2_label?.let { TextFieldValue(text = it) },
                            companyId2Number = issuer.company_id2_number?.let { TextFieldValue(text = it) },
                            companyId3Label = issuer.company_id3_label?.let { TextFieldValue(text = it) },
                            companyId3Number = issuer.company_id3_number?.let { TextFieldValue(text = it) },
                            logoPath = issuer.logo_path,
                        )
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun getMasterVersion(masterId: Long): Int? {
        return withContext(DispatcherProvider.IO) {
            try {
                clientOrIssuerQueries.get(masterId).executeAsOneOrNull()?.version?.toInt()
            } catch (e: Exception) {
                null
            }
        }
    }
}

fun ClientOrIssuerAddress.transformIntoEditable(): AddressState {
    val clientOrIssuer = this

    return AddressState(
        id = clientOrIssuer.id.toInt(),
        // Set originalAddressId to point to this master address ID
        // so when this address is copied to a document, we can sync changes back
        originalAddressId = clientOrIssuer.id.toInt(),
        addressTitle = clientOrIssuer.address_title?.let { TextFieldValue(text = it) },
        addressLine1 = clientOrIssuer.address_line_1?.let { TextFieldValue(text = it) },
        addressLine2 = clientOrIssuer.address_line_2?.let { TextFieldValue(text = it) },
        zipCode = clientOrIssuer.zip_code?.let { TextFieldValue(text = it) },
        city = clientOrIssuer.city?.let { TextFieldValue(text = it) },
    )
}

fun ClientOrIssuer.transformIntoEditable(
    addresses: List<AddressState>? = null,
    emails: List<EmailState>? = null,
): ClientOrIssuerState {
    val clientOrIssuer = this

    return ClientOrIssuerState(
        id = clientOrIssuer.id.toInt(),
        type = if (clientOrIssuer.type == ClientOrIssuerType.CLIENT.name.lowercase())
            ClientOrIssuerType.CLIENT
        else ClientOrIssuerType.ISSUER,
        version = clientOrIssuer.version?.toInt(),
        firstName = clientOrIssuer.first_name?.let { TextFieldValue(text = it) },
        name = TextFieldValue(text = clientOrIssuer.name),
        addresses = addresses,
        phone = clientOrIssuer.phone?.let { TextFieldValue(text = it) },
        emails = emails,
        notes = clientOrIssuer.notes?.let { TextFieldValue(text = it) },
        companyId1Label = clientOrIssuer.company_id1_label?.let {
            TextFieldValue(
                text = it
            )
        },
        companyId1Number = clientOrIssuer.company_id1_number?.let { TextFieldValue(text = it) },
        companyId2Label = clientOrIssuer.company_id2_label?.let {
            TextFieldValue(
                text = it
            )
        },
        companyId2Number = clientOrIssuer.company_id2_number?.let { TextFieldValue(text = it) },
        companyId3Label = clientOrIssuer.company_id3_label?.let {
            TextFieldValue(
                text = it
            )
        },
        companyId3Number = clientOrIssuer.company_id3_number?.let { TextFieldValue(text = it) },
        logoPath = clientOrIssuer.logo_path,
    )
}

fun ClientOrIssuerEmail.transformIntoEditable(): EmailState {
    return EmailState(
        id = this.id.toInt(),
        email = TextFieldValue(text = this.email)
    )
}

fun DocumentClientOrIssuerEmail.transformIntoEditable(): EmailState {
    return EmailState(
        id = this.id.toInt(),
        email = TextFieldValue(text = this.email)
    )
}

fun DocumentClientOrIssuerAddress.transformIntoEditable(): AddressState {
    val address = this

    return AddressState(
        id = address.id.toInt(),
        originalAddressId = address.original_address_id?.toInt(),
        addressTitle = address.address_title?.let { TextFieldValue(text = it) },
        addressLine1 = address.address_line_1?.let { TextFieldValue(text = it) },
        addressLine2 = address.address_line_2?.let { TextFieldValue(text = it) },
        zipCode = address.zip_code?.let { TextFieldValue(text = it) },
        city = address.city?.let { TextFieldValue(text = it) },
    )
}
