package com.a4a.g8invoicing.data

import android.content.ContentValues
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import com.a4a.g8invoicing.ui.viewmodels.PersonType
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import g8invoicing.ClientOrIssuer
import g8invoicing.ClientOrIssuerAddress
import g8invoicing.DocumentClientOrIssuer
import g8invoicing.DocumentClientOrIssuerAddress
import g8invoicing.LinkClientOrIssuerToAddressQueries
import g8invoicing.LinkDocumentClientOrIssuerToAddressQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

    override fun fetch(id: Long): ClientOrIssuerState? {
        try {
            return clientOrIssuerQueries.get(id).executeAsOneOrNull()
                ?.let {
                    it.transformIntoEditable(
                        fetchClientOrIssuerAddresses(it.id)?.toMutableList()
                    )
                }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override fun fetchDocumentClientOrIssuer(id: Long): ClientOrIssuerState? {
        try {
            return documentClientOrIssuerQueries.get(id).executeAsOneOrNull()
                ?.let {
                    it.transformIntoEditable(
                        fetchDocumentClientOrIssuerAddresses(it.id)
                    )
                }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override fun fetchAll(type: PersonType): Flow<List<ClientOrIssuerState>> {
        return clientOrIssuerQueries.getAll(type.name.lowercase())
            .asFlow()
            .map { query ->
                query.executeAsList()
                    .map {
                        it.transformIntoEditable(
                            fetchClientOrIssuerAddresses(it.id)?.toMutableList()
                        )
                    }
            }
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
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun fetchDocumentClientOrIssuerAddresses(id: Long): MutableList<AddressState>? {
        try {
            val listOfIds = linkDocumentClientOrIssuerToAddressQueries.get(id)
                .executeAsList()
            return if (listOfIds.isNotEmpty()) {
                listOfIds.map {
                    documentClientOrIssuerAddressQueries.get(it.id)
                        .executeAsOne()
                        .transformIntoEditable()
                }.toMutableList()
            } else null
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override suspend fun createNew(
        clientOrIssuer: ClientOrIssuerState,
    ): Long? {
        var id: Long? = null
        saveInfoInClientOrIssuerTable(clientOrIssuer)
        try {
            id = clientOrIssuerQueries.getLastInsertedRowId().executeAsOneOrNull()
            id?.let {
                saveInfoInAddressTables(it, clientOrIssuer.addresses)
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return id
    }

    private suspend fun saveInfoInClientOrIssuerTable(clientOrIssuer: ClientOrIssuerState) {
        return withContext(Dispatchers.IO) {
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
                    clientOrIssuer.email?.text,
                    clientOrIssuer.notes?.text,
                    clientOrIssuer.companyId1Label?.text,
                    clientOrIssuer.companyId1Number?.text,
                    clientOrIssuer.companyId2Label?.text,
                    clientOrIssuer.companyId2Number?.text,
                    clientOrIssuer.companyId3Label?.text,
                    clientOrIssuer.companyId3Number?.text,
                )
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    private suspend fun saveInfoInAddressTables(
        clientOrIssuerId: Long,
        addresses: List<AddressState>?,
    ) {
        return withContext(Dispatchers.IO) {
            try {
                addresses?.forEach { address ->
                    clientOrIssuerAddressQueries.save(
                        id = null,
                        address_title = address.addressTitle?.text,
                        address_line_1 = address.addressLine1?.text,
                        address_line_2 = address.addressLine2?.text,
                        zip_code = address.zipCode?.text,
                        city = address.city?.text,
                    )

                    clientOrIssuerAddressQueries.getLastInsertedRowId().executeAsOneOrNull()
                        ?.let { addressId ->
                            linkClientOrIssuerToAddress(
                                linkClientOrIssuerToAddressQueries,
                                clientOrIssuerId,
                                addressId
                            )
                        }
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun duplicateClients(clientsOrIssuers: List<ClientOrIssuerState>) {
        return withContext(Dispatchers.IO) {
            try {
                clientsOrIssuers.forEach { client ->
                    client.id?.let { clientId ->
                        //TODO: get the string outta here
                        if (!client.firstName?.text.isNullOrEmpty()) {
                            client.firstName = TextFieldValue("${client.firstName?.text} - Copie")
                        } else {
                            client.name = TextFieldValue("${client.name.text} - Copie")
                        }
                        saveInfoInClientOrIssuerTable(client)
                        saveInfoInAddressTables(clientId.toLong(), client.addresses)
                    }
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun updateClientOrIssuer(
        clientOrIssuer: ClientOrIssuerState,
    ) {
        return withContext(Dispatchers.IO) {
            try {
                clientOrIssuer.id?.let {
                    clientOrIssuerQueries.update(
                        id = it.toLong(),
                        type = clientOrIssuer.type?.name?.lowercase(),
                        first_name = clientOrIssuer.firstName?.text,
                        name = clientOrIssuer.name.text,
                        phone = clientOrIssuer.phone?.text,
                        email = clientOrIssuer.email?.text,
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
                        saveInfoInAddressTables(it.toLong(), addressesToCreate)
                    }
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun updateDocumentClientOrIssuer(
        documentClientOrIssuer: ClientOrIssuerState,
    ) {
        return withContext(Dispatchers.IO) {
            try {
                documentClientOrIssuer.id?.let {
                    documentClientOrIssuerQueries.update(
                        id = it.toLong(),
                        type = if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT ||
                            documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT
                        )
                            ClientOrIssuerType.CLIENT.name.lowercase()
                        else ClientOrIssuerType.ISSUER.name.lowercase(),
                        first_name = documentClientOrIssuer.firstName?.text,
                        name = documentClientOrIssuer.name.text,
                        phone = documentClientOrIssuer.phone?.text,
                        email = documentClientOrIssuer.email?.text,
                        notes = documentClientOrIssuer.notes?.text,
                        company_id1_label = documentClientOrIssuer.companyId1Label?.text,
                        company_id1_number = documentClientOrIssuer.companyId1Number?.text,
                        company_id2_label = documentClientOrIssuer.companyId2Label?.text,
                        company_id2_number = documentClientOrIssuer.companyId2Number?.text,
                        company_id3_label = documentClientOrIssuer.companyId3Label?.text,
                        company_id3_number = documentClientOrIssuer.companyId3Number?.text,
                    )
                }
                documentClientOrIssuer.addresses?.forEach { address ->
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
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun deleteClientOrIssuer(clientOrIssuer: ClientOrIssuerState) {
        return withContext(Dispatchers.IO) {
            try {
                clientOrIssuer.id?.let {
                    clientOrIssuerQueries.delete(it.toLong())
                    linkClientOrIssuerToAddressQueries.deleteWithClientId(it.toLong())
                }
                clientOrIssuer.addresses?.filter { it.id != null }?.forEach {
                    clientOrIssuerAddressQueries.delete(it.id!!.toLong())
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(documentClientOrIssuer: ClientOrIssuerState) {
        return withContext(Dispatchers.IO) {
            try {
                documentClientOrIssuer.id?.let {
                    documentClientOrIssuerQueries.delete(it.toLong())
                    linkDocumentClientOrIssuerToAddressQueries.deleteWithClientId(it.toLong())
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
        withContext(Dispatchers.IO) {
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
        email = clientOrIssuer.email?.let { TextFieldValue(text = it) },
        notes = clientOrIssuer.notes?.let { TextFieldValue(text = it) },
        companyId1Label = clientOrIssuer.company_id1_number?.let {
            clientOrIssuer.company_id1_label?.let {
                TextFieldValue(
                    text = it
                )
            }
        },
        companyId1Number = clientOrIssuer.company_id1_number?.let { TextFieldValue(text = it) },
        companyId2Label = clientOrIssuer.company_id2_number?.let {
            clientOrIssuer.company_id2_label?.let {
                TextFieldValue(
                    text = it
                )
            }
        },
        companyId2Number = clientOrIssuer.company_id2_number?.let { TextFieldValue(text = it) },
        companyId3Label = clientOrIssuer.company_id3_number?.let {
            clientOrIssuer.company_id3_label?.let {
                TextFieldValue(
                    text = it
                )
            }
        },
        companyId3Number = clientOrIssuer.company_id3_number?.let { TextFieldValue(text = it) },
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

fun DocumentClientOrIssuer.transformIntoEditable(
    addresses: List<AddressState>? = null,
): ClientOrIssuerState {
    val documentClientOrIssuer = this

    return ClientOrIssuerState(
        id = documentClientOrIssuer.id.toInt(),
        type = if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT.name.lowercase())
            ClientOrIssuerType.DOCUMENT_CLIENT
        else ClientOrIssuerType.DOCUMENT_ISSUER,
        firstName = documentClientOrIssuer.first_name?.let { TextFieldValue(text = it) },
        addresses = addresses,
        name = TextFieldValue(text = documentClientOrIssuer.name),
        phone = documentClientOrIssuer.phone?.let { TextFieldValue(text = it) },
        email = documentClientOrIssuer.email?.let { TextFieldValue(text = it) },
        notes = documentClientOrIssuer.notes?.let { TextFieldValue(text = it) },
        companyId1Label = documentClientOrIssuer.company_id1_number?.let {
            documentClientOrIssuer.company_id1_label?.let {
                TextFieldValue(
                    text = it
                )
            }
        },
        companyId1Number = documentClientOrIssuer.company_id1_number?.let { TextFieldValue(text = it) },
        companyId2Label = documentClientOrIssuer.company_id2_number?.let {
            documentClientOrIssuer.company_id2_label?.let {
                TextFieldValue(
                    text = it
                )
            }
        },
        companyId2Number = documentClientOrIssuer.company_id2_number?.let { TextFieldValue(text = it) },
        companyId3Label = documentClientOrIssuer.company_id3_number?.let {
            documentClientOrIssuer.company_id3_label?.let {
                TextFieldValue(
                    text = it
                )
            }
        },
        companyId3Number = documentClientOrIssuer.company_id3_number?.let { TextFieldValue(text = it) },
    )
}

fun linkClientOrIssuerToAddress(
    linkQueries: Any,
    clientOrIssuerId: Long,
    addressId: Long,
) {
    try {
        if (linkQueries is LinkClientOrIssuerToAddressQueries) {
            linkQueries.save(
                id = null,
                client_or_issuer_id = clientOrIssuerId,
                address_id = addressId
            )
        } else if (linkQueries is LinkDocumentClientOrIssuerToAddressQueries) {
            linkQueries.save(
                id = null,
                document_client_or_issuer_id = clientOrIssuerId,
                address_id = addressId
            )
        }
    } catch (e: Exception) {
        Log.e(ContentValues.TAG, "Error: ${e.message}")
    }
}
