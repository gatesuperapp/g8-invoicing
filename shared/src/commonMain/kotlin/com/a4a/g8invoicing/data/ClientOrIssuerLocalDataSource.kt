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

    private suspend fun saveInfoInClientOrIssuerTable(clientOrIssuer: ClientOrIssuerState) {
        return withContext(DispatcherProvider.IO) {
            try {
                clientOrIssuerQueries.save(
                    id = null,
                    type = if (clientOrIssuer.type == ClientOrIssuerType.CLIENT ||
                        clientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT
                    ) ClientOrIssuerType.CLIENT.name.lowercase()
                    else ClientOrIssuerType.ISSUER.name.lowercase(),
                    clientOrIssuer.firstName?.text,
                    clientOrIssuer.name.text,
                    clientOrIssuer.phone?.text,
                    clientOrIssuer.emails?.firstOrNull()?.email?.text,
                    clientOrIssuer.notes?.text,
                    clientOrIssuer.companyId1Label?.text,
                    clientOrIssuer.companyId1Number?.text,
                    clientOrIssuer.companyId2Label?.text,
                    clientOrIssuer.companyId2Number?.text,
                    clientOrIssuer.companyId3Label?.text,
                    clientOrIssuer.companyId3Number?.text,
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
                        address_title = address.addressTitle?.text,
                        address_line_1 = address.addressLine1?.text,
                        address_line_2 = address.addressLine2?.text,
                        zip_code = address.zipCode?.text,
                        city = address.city?.text,
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
                        address_title = address.addressTitle?.text,
                        address_line_1 = address.addressLine1?.text,
                        address_line_2 = address.addressLine2?.text,
                        zip_code = address.zipCode?.text,
                        city = address.city?.text,
                    )

                    documentClientOrIssuerAddressQueries.getLastInsertedRowId().executeAsOneOrNull()
                        ?.let { newAddressId ->
                            linkClientOrIssuerToAddressQueries.save(
                                id = null,
                                client_or_issuer_id = clientOrIssuerId,
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
                    client.id?.let { clientId ->
                        if (!client.firstName?.text.isNullOrEmpty()) {
                            client.firstName = TextFieldValue("${client.firstName?.text} - Copie")
                        } else {
                            client.name = TextFieldValue("${client.name.text} - Copie")
                        }
                        saveInfoInClientOrIssuerTable(client)
                        saveInfoInClientOrIssuerAddressTables(clientId.toLong(), client.addresses)
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
                        first_name = clientOrIssuer.firstName?.text,
                        name = clientOrIssuer.name.text,
                        phone = clientOrIssuer.phone?.text,
                        email = clientOrIssuer.emails?.firstOrNull()?.email?.text,
                        notes = clientOrIssuer.notes?.text,
                        company_id1_label = clientOrIssuer.companyId1Label?.text,
                        company_id1_number = clientOrIssuer.companyId1Number?.text,
                        company_id2_label = clientOrIssuer.companyId2Label?.text,
                        company_id2_number = clientOrIssuer.companyId2Number?.text,
                        company_id3_label = clientOrIssuer.companyId3Label?.text,
                        company_id3_number = clientOrIssuer.companyId3Number?.text,
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
                                address_title = address.addressTitle?.text,
                                address_line_1 = address.addressLine1?.text,
                                address_line_2 = address.addressLine2?.text,
                                zip_code = address.zipCode?.text,
                                city = address.city?.text,
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
                        original_client_id = documentClientOrIssuer.originalClientId?.toLong(),
                        first_name = documentClientOrIssuer.firstName?.text,
                        name = documentClientOrIssuer.name.text,
                        phone = documentClientOrIssuer.phone?.text,
                        email = documentClientOrIssuer.emails?.firstOrNull()?.email?.text,
                        notes = documentClientOrIssuer.notes?.text,
                        company_id1_label = documentClientOrIssuer.companyId1Label?.text,
                        company_id1_number = documentClientOrIssuer.companyId1Number?.text,
                        company_id2_label = documentClientOrIssuer.companyId2Label?.text,
                        company_id2_number = documentClientOrIssuer.companyId2Number?.text,
                        company_id3_label = documentClientOrIssuer.companyId3Label?.text,
                        company_id3_number = documentClientOrIssuer.companyId3Number?.text,
                    )
                }
                // Addresses to delete
                val oldAddressesIds = documentClientOrIssuer.id?.toLong()?.let {
                    linkDocumentClientOrIssuerToAddressQueries.get(it).executeAsList()
                        .map { it.address_id }
                }
                val newAddressesIds =
                    documentClientOrIssuer.addresses?.mapNotNull { it.id?.toLong() }
                        ?: mutableListOf()

                val addressesToDelete = oldAddressesIds?.filterNot { it in newAddressesIds }
                addressesToDelete?.forEach {
                    linkDocumentClientOrIssuerToAddressQueries.delete(it)
                    documentClientOrIssuerAddressQueries.delete(it)
                }

                // Addresses to update and create
                documentClientOrIssuer.addresses?.let { addresses ->
                    val (addressesToUpdate, addressesToCreate) = addresses.partition { it.id != null }
                    addressesToUpdate.forEach { address ->
                        address.id?.let {
                            documentClientOrIssuerAddressQueries.update(
                                id = it.toLong(),
                                address_title = address.addressTitle?.text,
                                address_line_1 = address.addressLine1?.text,
                                address_line_2 = address.addressLine2?.text,
                                zip_code = address.zipCode?.text,
                                city = address.city?.text,
                            )
                        }
                    }
                    documentClientOrIssuer.id?.let {
                        saveInfoInDocumentClientOrIssuerAddressTables(
                            it.toLong(),
                            addressesToCreate
                        )
                    }
                }

                // Emails: delete all and recreate
                documentClientOrIssuer.id?.toLong()?.let { docClientId ->
                    documentClientOrIssuerEmailQueries.deleteByDocumentClientOrIssuerId(docClientId)
                    saveInfoInDocumentClientOrIssuerEmailTable(docClientId, documentClientOrIssuer.emails)
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
}

fun ClientOrIssuerAddress.transformIntoEditable(): AddressState {
    val clientOrIssuer = this

    return AddressState(
        id = clientOrIssuer.id.toInt(),
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
        addressTitle = address.address_title?.let { TextFieldValue(text = it) },
        addressLine1 = address.address_line_1?.let { TextFieldValue(text = it) },
        addressLine2 = address.address_line_2?.let { TextFieldValue(text = it) },
        zipCode = address.zip_code?.let { TextFieldValue(text = it) },
        city = address.city?.let { TextFieldValue(text = it) },
    )
}
