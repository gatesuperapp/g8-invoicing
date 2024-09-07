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
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
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

    override fun fetchClientOrIssuer(id: Long): ClientOrIssuerState? {
        try {
            return clientOrIssuerQueries.get(id).executeAsOneOrNull()
                ?.let {
                    it.transformIntoEditable(
                        fetchClientOrIssuerAddresses(it.id)
                    )
                }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override fun fetchDocumentClientOrIssuer(id: Long): DocumentClientOrIssuerState? {
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
                        it.transformIntoEditable()
                    }
            }
    }

    private fun fetchClientOrIssuerAddresses(id: Long): List<AddressState>? {
        try {
            val listOfIds = linkClientOrIssuerToAddressQueries.get(id)
                .executeAsList()
            return if (listOfIds.isNotEmpty()) {
                listOfIds.map {
                    clientOrIssuerAddressQueries.get(it.id)
                        .executeAsOne()
                        .transformIntoEditable()
                }
            } else null
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun fetchDocumentClientOrIssuerAddresses(id: Long): List<AddressState>? {
        try {
            val listOfIds = linkDocumentClientOrIssuerToAddressQueries.get(id)
                .executeAsList()
            return if (listOfIds.isNotEmpty()) {
                listOfIds.map {
                    documentClientOrIssuerAddressQueries.get(it.id)
                        .executeAsOne()
                        .transformIntoEditable()
                }
            } else null
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override suspend fun saveClientOrIssuer(
        clientOrIssuer: ClientOrIssuerState
    ) {
        saveInfoInClientOrIssuerTable(clientOrIssuer)
        saveInfoInOtherClientOrIssuerTables(clientOrIssuer.id?.toLong(), clientOrIssuer.addresses)
    }

    private suspend fun saveInfoInClientOrIssuerTable(clientOrIssuer: ClientOrIssuerState) {
        return withContext(Dispatchers.IO) {
            try {
                clientOrIssuerQueries.save(
                    id = null,
                    type = clientOrIssuer.type?.name?.lowercase(),
                    clientOrIssuer.firstName?.text,
                    clientOrIssuer.name.text,
                    clientOrIssuer.phone?.text,
                    clientOrIssuer.email?.text,
                    clientOrIssuer.notes?.text,
                    clientOrIssuer.companyId1Label?.text,
                    clientOrIssuer.companyId1Number?.text,
                    clientOrIssuer.companyId2Label?.text,
                    clientOrIssuer.companyId2Number?.text,
                )
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    private suspend fun saveInfoInOtherClientOrIssuerTables(
        clientOrIssuerId: Long?,
        addresses: List<AddressState>?,
    ) {
        return withContext(Dispatchers.IO) {
            try {
                addresses?.forEach { address ->
                    clientOrIssuerAddressQueries.save(
                        id = null,
                        address_line_1 = address.addressLine1?.text,
                        address_line_2 = address.addressLine2?.text,
                        zip_code = address.zipCode?.text,
                        city = address.city?.text,
                    )

                    clientOrIssuerId?.let { clientOrIssuerId ->
                        clientOrIssuerQueries.getLastInsertedRowId().executeAsOneOrNull()?.toInt()
                            ?.let { addressId ->
                                linkClientOrIssuerToAddress(
                                    linkClientOrIssuerToAddressQueries,
                                    clientOrIssuerId,
                                    addressId.toLong()
                                )
                            }
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
                    client.id?.let {clientId ->
                        //TODO: get the string outta here
                        if (!client.firstName?.text.isNullOrEmpty()) {
                            client.firstName = TextFieldValue("${client.firstName?.text} - Copie")
                        } else {
                            client.name = TextFieldValue("${client.name.text} - Copie")
                        }
                        saveInfoInClientOrIssuerTable(client)
                        saveInfoInOtherClientOrIssuerTables(clientId.toLong(), client.addresses)
                    }
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun updateClientOrIssuer(
        clientOrIssuer: ClientOrIssuerState
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
                    )
                }
                clientOrIssuer.addresses?.forEach { address ->
                    address.id?.let {
                        clientOrIssuerAddressQueries.update(
                            id = it.toLong(),
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

    override suspend fun updateDocumentClientOrIssuer(
        documentClientOrIssuer: DocumentClientOrIssuerState
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
                    )
                }
                documentClientOrIssuer.addresses?.forEach { address ->
                    address.id?.let {
                        documentClientOrIssuerAddressQueries.update(
                            id = it.toLong(),
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
                }
                clientOrIssuer.addresses?.filter { it.id != null }?.forEach {
                    clientOrIssuerAddressQueries.delete(it.id!!.toLong())
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(documentClientOrIssuer: DocumentClientOrIssuerState) {
        return withContext(Dispatchers.IO) {
            try {
                documentClientOrIssuer.id?.let {
                    documentClientOrIssuerQueries.delete(it.toLong())
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
        companyId2Label = clientOrIssuer.company_id1_number?.let {
            clientOrIssuer.company_id2_label?.let {
                TextFieldValue(
                    text = it
                )
            }
        },
        companyId2Number = clientOrIssuer.company_id2_number?.let { TextFieldValue(text = it) },
    )
}

fun DocumentClientOrIssuerAddress.transformIntoEditable(): AddressState {
    val address = this

    return AddressState(
        id = address.id.toInt(),
        addressLine1 = address.address_line_1?.let { TextFieldValue(text = it) },
        addressLine2 = address.address_line_2?.let { TextFieldValue(text = it) },
        zipCode = address.zip_code?.let { TextFieldValue(text = it) },
        city = address.city?.let { TextFieldValue(text = it) },
    )
}

fun DocumentClientOrIssuer.transformIntoEditable(
    addresses: List<AddressState>? = null,
): DocumentClientOrIssuerState {
    val documentClientOrIssuer = this

    return DocumentClientOrIssuerState(
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
        companyId2Label = documentClientOrIssuer.company_id1_number?.let {
            documentClientOrIssuer.company_id2_label?.let {
                TextFieldValue(
                    text = it
                )
            }
        },
        companyId2Number = documentClientOrIssuer.company_id2_number?.let { TextFieldValue(text = it) },
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
