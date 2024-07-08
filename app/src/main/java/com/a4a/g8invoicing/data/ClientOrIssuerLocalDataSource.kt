package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.toLowerCase
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerType
import com.a4a.g8invoicing.ui.screens.PersonType
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import g8invoicing.ClientOrIssuer
import g8invoicing.DocumentClientOrIssuer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale

class ClientOrIssuerLocalDataSource(
    db: Database,
) : ClientOrIssuerLocalDataSourceInterface {
    private val clientOrIssuerQueries = db.clientOrIssuerQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries

    override fun fetchClientOrIssuer(id: Long): ClientOrIssuerState? {
        return clientOrIssuerQueries.get(id).executeAsOneOrNull()
            ?.transformIntoEditable()
    }

    override fun fetchDocumentClientOrIssuer(id: Long): DocumentClientOrIssuerState? {
        return documentClientOrIssuerQueries.get(id).executeAsOneOrNull()
            ?.transformIntoEditable()
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

    override suspend fun saveClientOrIssuer(clientOrIssuer: ClientOrIssuerState) {
        return withContext(Dispatchers.IO) {
            try {
                clientOrIssuerQueries.save(
                    client_or_issuer_id = null,
                    type = clientOrIssuer.type?.name?.lowercase(),
                    clientOrIssuer.firstName?.text,
                    clientOrIssuer.name.text,
                    clientOrIssuer.address1?.text,
                    clientOrIssuer.address2?.text,
                    clientOrIssuer.zipCode?.text,
                    clientOrIssuer.city?.text,
                    clientOrIssuer.phone?.text,
                    clientOrIssuer.email?.text,
                    clientOrIssuer.notes?.text,
                    clientOrIssuer.companyId1Label?.text,
                    clientOrIssuer.companyId1Number?.text,
                    clientOrIssuer.companyId2Label?.text,
                    clientOrIssuer.companyId2Number?.text,
                )
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun saveDocumentClientOrIssuer(
        documentClientOrIssuer: DocumentClientOrIssuerState,
    ): Int? {
        var documentClientOrIssuerId: Int? = null
        withContext(Dispatchers.IO) {
            try {
                documentClientOrIssuerQueries.save(
                    document_client_or_issuer_id = null,
                    type = if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT ||
                        documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT
                    )
                        ClientOrIssuerType.CLIENT.name.lowercase()
                    else ClientOrIssuerType.ISSUER.name.lowercase(),
                    documentClientOrIssuer.firstName?.text,
                    documentClientOrIssuer.name.text,
                    documentClientOrIssuer.address1?.text,
                    documentClientOrIssuer.address2?.text,
                    documentClientOrIssuer.zipCode?.text,
                    documentClientOrIssuer.city?.text,
                    documentClientOrIssuer.phone?.text,
                    documentClientOrIssuer.email?.text,
                    documentClientOrIssuer.notes?.text,
                    documentClientOrIssuer.companyId1Label?.text,
                    documentClientOrIssuer.companyId1Number?.text,
                    documentClientOrIssuer.companyId2Label?.text,
                    documentClientOrIssuer.companyId2Number?.text,
                )
                documentClientOrIssuerId =
                    documentClientOrIssuerQueries.getLastInsertedClientOrIssuerId()
                        .executeAsOneOrNull()
                        ?.toInt()

            } catch (cause: Throwable) {
            }
        }
        return documentClientOrIssuerId
    }

    override suspend fun duplicateClients(clientsOrIssuers: List<ClientOrIssuerState>) {
        return withContext(Dispatchers.IO) {
            try {
                clientsOrIssuers.forEach { selectedClientOrIssuer ->
                    selectedClientOrIssuer.id?.let {
                        var clientOrIssuer = fetchClientOrIssuer(it.toLong())
                        //TODO: get the string outta here
                        clientOrIssuer = if (!clientOrIssuer?.firstName?.text.isNullOrEmpty()) {
                            clientOrIssuer?.copy(firstName = TextFieldValue("${selectedClientOrIssuer.firstName?.text} - Copie"))
                        } else {
                            clientOrIssuer?.copy(name = TextFieldValue("${selectedClientOrIssuer.name.text} - Copie"))
                        }
                        clientOrIssuer?.let { client ->
                            clientOrIssuerQueries.save(
                                client_or_issuer_id = null,
                                type = ClientOrIssuerType.CLIENT.name.lowercase(),
                                client.firstName?.text,
                                client.name.text,
                                client.address1?.text,
                                client.address2?.text,
                                client.zipCode?.text,
                                client.city?.text,
                                client.phone?.text,
                                client.email?.text,
                                client.notes?.text,
                                client.companyId1Label?.text,
                                client.companyId1Number?.text,
                                client.companyId2Label?.text,
                                client.companyId2Number?.text,
                            )
                        }
                    }
                }
            } catch (cause: Throwable) {
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
                        address1 = clientOrIssuer.address1?.text,
                        address2 = clientOrIssuer.address2?.text,
                        zip_code = clientOrIssuer.zipCode?.text,
                        city = clientOrIssuer.city?.text,
                        phone = clientOrIssuer.phone?.text,
                        email = clientOrIssuer.email?.text,
                        notes = clientOrIssuer.notes?.text,
                        company_id1_label = clientOrIssuer.companyId1Label?.text,
                        company_id1_number = clientOrIssuer.companyId1Number?.text,
                        company_id2_label = clientOrIssuer.companyId2Label?.text,
                        company_id2_number = clientOrIssuer.companyId2Number?.text,
                    )
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun updateDocumentClientOrIssuer(
        documentClientOrIssuer: DocumentClientOrIssuerState,
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
                        address1 = documentClientOrIssuer.address1?.text,
                        address2 = documentClientOrIssuer.address2?.text,
                        zip_code = documentClientOrIssuer.zipCode?.text,
                        city = documentClientOrIssuer.city?.text,
                        phone = documentClientOrIssuer.phone?.text,
                        email = documentClientOrIssuer.email?.text,
                        notes = documentClientOrIssuer.notes?.text,
                        company_id1_label = documentClientOrIssuer.companyId1Label?.text,
                        company_id1_number = documentClientOrIssuer.companyId1Number?.text,
                        company_id2_label = documentClientOrIssuer.companyId2Label?.text,
                        company_id2_number = documentClientOrIssuer.companyId2Number?.text,
                    )
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun deleteClientOrIssuer(id: Long) {
        return withContext(Dispatchers.IO) {
            try {
                clientOrIssuerQueries.delete(id)
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(id: Long) {
        return withContext(Dispatchers.IO) {
            try {
                documentClientOrIssuerQueries.delete(id)
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

fun ClientOrIssuer.transformIntoEditable(
): ClientOrIssuerState {
    val clientOrIssuer = this

    return ClientOrIssuerState(
        id = clientOrIssuer.client_or_issuer_id.toInt(),
        type = if (clientOrIssuer.type == ClientOrIssuerType.CLIENT.name.lowercase())
            ClientOrIssuerType.CLIENT
        else ClientOrIssuerType.ISSUER,
        firstName = clientOrIssuer.first_name?.let { TextFieldValue(text = it) },
        name = TextFieldValue(text = clientOrIssuer.name),
        address1 = clientOrIssuer.address1?.let { TextFieldValue(text = it) },
        address2 = clientOrIssuer.address2?.let { TextFieldValue(text = it) },
        zipCode = clientOrIssuer.zip_code?.let { TextFieldValue(text = it) },
        city = clientOrIssuer.city?.let { TextFieldValue(text = it) },
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

fun DocumentClientOrIssuer.transformIntoEditable(
): DocumentClientOrIssuerState {
    val documentClientOrIssuer = this

    return DocumentClientOrIssuerState(
        id = documentClientOrIssuer.document_client_or_issuer_id.toInt(),
        type = if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT.name.lowercase())
            ClientOrIssuerType.DOCUMENT_CLIENT
        else ClientOrIssuerType.DOCUMENT_ISSUER,
        firstName = documentClientOrIssuer.first_name?.let { TextFieldValue(text = it) },
        name = TextFieldValue(text = documentClientOrIssuer.name),
        address1 = documentClientOrIssuer.address1?.let { TextFieldValue(text = it) },
        address2 = documentClientOrIssuer.address2?.let { TextFieldValue(text = it) },
        zipCode = documentClientOrIssuer.zip_code?.let { TextFieldValue(text = it) },
        city = documentClientOrIssuer.city?.let { TextFieldValue(text = it) },
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

fun ClientOrIssuer.transformIntoDocumentClientOrIssuer(
): DocumentClientOrIssuerState {
    val clientOrIssuer = this

    return DocumentClientOrIssuerState(
        id = clientOrIssuer.client_or_issuer_id.toInt(),
        firstName = clientOrIssuer.first_name?.let { TextFieldValue(text = it) },
        name = TextFieldValue(text = clientOrIssuer.name),
        address1 = clientOrIssuer.address1?.let { TextFieldValue(text = it) },
        address2 = clientOrIssuer.address2?.let { TextFieldValue(text = it) },
        zipCode = clientOrIssuer.zip_code?.let { TextFieldValue(text = it) },
        city = clientOrIssuer.city?.let { TextFieldValue(text = it) },
        phone = clientOrIssuer.phone?.let { TextFieldValue(text = it) },
        email = clientOrIssuer.email?.let { TextFieldValue(text = it) },
        notes = clientOrIssuer.notes?.let { TextFieldValue(text = it) },
        companyId1Label = TextFieldValue(text = clientOrIssuer.company_id1_label ?: "N° SIRET"),
        companyId1Number = clientOrIssuer.company_id1_number?.let { TextFieldValue(text = it) },
        companyId2Label = TextFieldValue(text = clientOrIssuer.company_id2_label ?: "N° TVA"),
        companyId2Number = clientOrIssuer.company_id2_number?.let { TextFieldValue(text = it) },
    )
}