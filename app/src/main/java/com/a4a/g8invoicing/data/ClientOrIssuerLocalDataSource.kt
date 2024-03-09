package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.ui.screens.PersonType
import com.a4a.g8invoicing.ui.states.CompanyDataState
import g8invoicing.ClientOrIssuer
import g8invoicing.CompanyIdentificatorQueries
import g8invoicing.DocumentClientOrIssuer
import g8invoicing.DocumentCompanyIdentificatorQueries
import g8invoicing.LinkClientOrIssuerToCompanyIdentificatorQueries
import g8invoicing.LinkDocClientOrIssuerToDocCompanyIdentificatorQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ClientOrIssuerLocalDataSource(
    db: Database,
) : ClientOrIssuerLocalDataSourceInterface {
    private val clientOrIssuerQueries = db.clientOrIssuerQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val companyIdentificatorQueries = db.companyIdentificatorQueries
    private val linkToCompanyQueries = db.linkClientOrIssuerToCompanyIdentificatorQueries
    private val documentCompanyIdentificatorQueries = db.documentCompanyIdentificatorQueries
    private val linkToDocumentCompanyQueries = db.linkDocClientOrIssuerToDocCompanyIdentificatorQueries


    override fun fetchClientOrIssuer(id: Long): ClientOrIssuerState? {
        return clientOrIssuerQueries.get(id).executeAsOneOrNull()
            ?.transformIntoEditable(linkToCompanyQueries, companyIdentificatorQueries)
    }

    override fun fetchDocumentClientOrIssuer(id: Long): ClientOrIssuerState? {
        return documentClientOrIssuerQueries.get(id).executeAsOneOrNull()
            ?.transformIntoEditable(linkToDocumentCompanyQueries, documentCompanyIdentificatorQueries)
    }

    override fun fetchAll(type: PersonType): Flow<List<ClientOrIssuerState>> {
        return clientOrIssuerQueries.getAll(type.name.lowercase())
            .asFlow()
            .map { query ->
                query.executeAsList()
                    .map {
                        it.transformIntoEditable(
                            linkToCompanyQueries,
                            companyIdentificatorQueries
                        )
                    }
            }
    }

    override suspend fun saveClientOrIssuer(clientOrIssuer: ClientOrIssuerState, type: String) {
        return withContext(Dispatchers.IO) {
            try {
                clientOrIssuer.companyData?.forEach { companyIdentification ->
                    saveCompanyData(companyIdentification, clientOrIssuer)
                }
                clientOrIssuerQueries.save(
                    client_or_issuer_id = null,
                    type = type,
                    clientOrIssuer.firstName?.text,
                    clientOrIssuer.name.text,
                    clientOrIssuer.address1?.text,
                    clientOrIssuer.address2?.text,
                    clientOrIssuer.zipCode?.text,
                    clientOrIssuer.city?.text,
                    clientOrIssuer.phone?.text,
                    clientOrIssuer.email?.text,
                    clientOrIssuer.notes?.text
                )
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun saveDocumentClientOrIssuer(clientOrIssuer: ClientOrIssuerState, type: String): Int? {
        var documentClientOrIssuerId: Int? = null
       withContext(Dispatchers.IO) {
            try {
                clientOrIssuer.companyData?.forEach { companyIdentification ->
                    saveDocumentCompanyData(companyIdentification, clientOrIssuer)
                }
                documentClientOrIssuerQueries.save(
                    document_client_or_issuer_id = null,
                    type = type,
                    clientOrIssuer.firstName?.text,
                    clientOrIssuer.name.text,
                    clientOrIssuer.address1?.text,
                    clientOrIssuer.address2?.text,
                    clientOrIssuer.zipCode?.text,
                    clientOrIssuer.city?.text,
                    clientOrIssuer.phone?.text,
                    clientOrIssuer.email?.text,
                    clientOrIssuer.notes?.text
                )
                documentClientOrIssuerId =
                    documentClientOrIssuerQueries.getLastInsertedRowId().executeAsOneOrNull()?.toInt()

            } catch (cause: Throwable) {
            }
        }
        return documentClientOrIssuerId
    }

    override suspend fun duplicateClientOrIssuer(client: ClientOrIssuerState) {
        return withContext(Dispatchers.IO) {
            try {
                clientOrIssuerQueries.save(
                    client_or_issuer_id = null,
                    type = "client",
                    client.firstName?.text,
                    client.name.text,
                    client.address1?.text,
                    client.address2?.text,
                    client.zipCode?.text,
                    client.city?.text,
                    client.phone?.text,
                    client.email?.text,
                    client.notes?.text
                )
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun updateClientOrIssuer(client: ClientOrIssuerState) {
        return withContext(Dispatchers.IO) {
            try {
                client.companyData?.forEach { companyData ->
                    companyData.id?.let {
                        companyIdentificatorQueries.update(
                            id = it,
                            label = companyData.label,
                            number = companyData.number.toString()
                        )
                    } ?: saveCompanyData(companyData, client)
                }

                client.id?.let {
                    clientOrIssuerQueries.update(
                        id = it.toLong(),
                        type = "client",
                        first_name = client.firstName?.text,
                        name = client.name.text,
                        address1 = client.address1?.text,
                        address2 = client.address2?.text,
                        zip_code = client.zipCode?.text,
                        city = client.city?.text,
                        phone = client.phone?.text,
                        email = client.email?.text,
                        notes = client.notes?.text
                    )
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun updateDocumentClientOrIssuer(documentClient: ClientOrIssuerState) {
        return withContext(Dispatchers.IO) {
            try {
                documentClient.companyData?.forEach { companyIdentificator ->
                    companyIdentificator.id?.let {
                        documentCompanyIdentificatorQueries.update(
                            id = it,
                            label = companyIdentificator.label,
                            number = companyIdentificator.number.toString()
                        )
                    } ?: saveDocumentCompanyData(companyIdentificator, documentClient)
                }

                documentClient.id?.let {
                    documentClientOrIssuerQueries.update(
                        id = it.toLong(),
                        type = "client",
                        first_name = documentClient.firstName?.text,
                        name = documentClient.name.text,
                        address1 = documentClient.address1?.text,
                        address2 = documentClient.address2?.text,
                        zip_code = documentClient.zipCode?.text,
                        city = documentClient.city?.text,
                        phone = documentClient.phone?.text,
                        email = documentClient.email?.text,
                        notes = documentClient.notes?.text
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

    override suspend fun getLastCreatedClientOrIssuerId(): Long? {
        var lastInserted: Long? = null
        withContext(Dispatchers.IO) {
            try {
                lastInserted = clientOrIssuerQueries.getLastInsertedRowId().executeAsOneOrNull()
            } catch (cause: Throwable) {
            }
        }
        return lastInserted
    }

    override suspend fun getLastCreatedDocumentClientOrIssuerId(): Long? {
        var lastInserted: Long? = null
        withContext(Dispatchers.IO) {
            try {
                lastInserted = documentClientOrIssuerQueries.getLastInsertedRowId().executeAsOneOrNull()
            } catch (cause: Throwable) {
            }
        }
        return lastInserted
    }

    private fun saveCompanyData(
        companyIdentification: CompanyDataState,
        client: ClientOrIssuerState,
    ) {
        // Save the values
        companyIdentificatorQueries.save(
            company_identificator_id = null,
            label = companyIdentification.label,
            number = companyIdentification.number.toString()
        )

        // Get the ID of the last inserted row
        val companyIdentificationId: Long? = companyIdentificatorQueries.lastInsertRowId().executeAsOneOrNull()

        // Save the company identifiants for the client
        client.id?.let { clientId ->
            companyIdentificationId?.let { companyId ->
                linkToCompanyQueries.save(
                    id = null,
                    client_or_issuer_id = clientId.toLong(),
                    company_identification_id = companyId,
                )
            }
        }
    }

    private fun saveDocumentCompanyData(
        companyIdentification: CompanyDataState,
        documentClient: ClientOrIssuerState,
    ) {
        // Save the values
        documentCompanyIdentificatorQueries.save(
            document_company_identificator_id = null,
            label = companyIdentification.label,
            number = companyIdentification.number.toString()
        )

        // Get the ID of the last inserted row
        val companyIdentificationId: Long? = documentCompanyIdentificatorQueries.lastInsertRowId().executeAsOneOrNull()

        // Save the company identifiants for the client
        documentClient.id?.let { clientId ->
            companyIdentificationId?.let { companyId ->
                linkToCompanyQueries.save(
                    id = null,
                    client_or_issuer_id = clientId.toLong(),
                    company_identification_id = companyId,
                )
            }
        }
    }
}

fun ClientOrIssuer.transformIntoEditable(
    linkToCompanyQueries: LinkClientOrIssuerToCompanyIdentificatorQueries,
    companyDataQueries: CompanyIdentificatorQueries,
): ClientOrIssuerState {
    val companyData: MutableList<CompanyDataState> = mutableListOf()
    val clientOrIssuer = this

    val identifiers = linkToCompanyQueries.get(clientOrIssuer.client_or_issuer_id)
            .executeAsList()

    identifiers.forEach {
        companyDataQueries.get(it).executeAsOneOrNull()?.let { data ->
            companyData += CompanyDataState(
                id = data.company_identificator_id,
                label = data.label,
                number = TextFieldValue(text = data.number)
            )
        }
    }

    return ClientOrIssuerState(
        id = clientOrIssuer.client_or_issuer_id.toInt(),
        firstName = TextFieldValue(text = clientOrIssuer.first_name ?: ""),
        name = TextFieldValue(text = clientOrIssuer.name),
        address1 = TextFieldValue(text = clientOrIssuer.address1 ?: ""),
        address2 = TextFieldValue(text = clientOrIssuer.address2 ?: ""),
        zipCode = TextFieldValue(text = clientOrIssuer.zip_code ?: ""),
        city = TextFieldValue(text = clientOrIssuer.city ?: ""),
        phone = TextFieldValue(text = clientOrIssuer.phone ?: ""),
        email = TextFieldValue(text = clientOrIssuer.email ?: ""),
        notes = TextFieldValue(text = clientOrIssuer.notes ?: ""),
        companyData = companyData.ifEmpty { null }
    )
}

fun DocumentClientOrIssuer.transformIntoEditable(
    linkToCompanyQueries: LinkDocClientOrIssuerToDocCompanyIdentificatorQueries,
    companyDataQueries: DocumentCompanyIdentificatorQueries,
): ClientOrIssuerState {
    val companyData: MutableList<CompanyDataState> = mutableListOf()
    val clientOrIssuer = this

    val identifiers = linkToCompanyQueries.get(clientOrIssuer.document_client_or_issuer_id)
        .executeAsList()

    identifiers.forEach {
        companyDataQueries.get(it).executeAsOneOrNull()?.let { data ->
            companyData += CompanyDataState(
                id = data.document_company_identificator_id,
                label = data.label,
                number = TextFieldValue(text = data.number)
            )
        }
    }

    return ClientOrIssuerState(
        id = clientOrIssuer.document_client_or_issuer_id.toInt(),
        firstName = TextFieldValue(text = clientOrIssuer.first_name ?: ""),
        name = TextFieldValue(text = clientOrIssuer.name),
        address1 = TextFieldValue(text = clientOrIssuer.address1 ?: ""),
        address2 = TextFieldValue(text = clientOrIssuer.address2 ?: ""),
        zipCode = TextFieldValue(text = clientOrIssuer.zip_code ?: ""),
        city = TextFieldValue(text = clientOrIssuer.city ?: ""),
        phone = TextFieldValue(text = clientOrIssuer.phone ?: ""),
        email = TextFieldValue(text = clientOrIssuer.email ?: ""),
        notes = TextFieldValue(text = clientOrIssuer.notes ?: ""),
        companyData = companyData.ifEmpty { null }
    )
}