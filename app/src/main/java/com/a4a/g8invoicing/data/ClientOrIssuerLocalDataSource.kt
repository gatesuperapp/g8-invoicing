package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.ui.screens.PersonType
import g8invoicing.ClientOrIssuer
import g8invoicing.ClientOrIssuerCompanyDataQueries
import g8invoicing.CompanyDataQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ClientOrIssuerLocalDataSource(
    db: Database,
) : ClientOrIssuerLocalDataSourceInterface {
    private val clientOrIssuerQueries = db.clientOrIssuerQueries
    private val companyDataQueries = db.companyDataQueries
    private val clientOrIssuerCompanyDataQueries = db.clientOrIssuerCompanyDataQueries

    override fun fetchClientOrIssuer(id: Long): ClientOrIssuerEditable? {
        return clientOrIssuerQueries.getClientOrIssuer(id).executeAsOneOrNull()
            ?.transformIntoEditable(clientOrIssuerCompanyDataQueries, companyDataQueries)
    }

    override fun fetchAll(type: PersonType): Flow<List<ClientOrIssuerEditable>> {
        return clientOrIssuerQueries.getAll(type.name.lowercase())
            .asFlow()
            .map { query ->
                query.executeAsList()
                    .map {
                        it.transformIntoEditable(
                            clientOrIssuerCompanyDataQueries,
                            companyDataQueries
                        )
                    }
            }
    }

    override suspend fun saveClientOrIssuer(clientOrIssuer: ClientOrIssuerEditable, type: String) {
        return withContext(Dispatchers.IO) {
            try {
                clientOrIssuer.companyData?.forEach { companyIdentification ->
                    saveCompanyId(companyIdentification, clientOrIssuer)
                }
                clientOrIssuerQueries.saveClientOrIssuer(
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

    override suspend fun duplicateClientOrIssuer(client: ClientOrIssuerEditable) {
        return withContext(Dispatchers.IO) {
            try {
                clientOrIssuerQueries.saveClientOrIssuer(
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

    override suspend fun updateClientOrIssuer(client: ClientOrIssuerEditable) {
        return withContext(Dispatchers.IO) {
            try {
                client.companyData?.forEach { companyData ->
                    companyData.id?.let {
                        companyDataQueries.updateCompanyData(
                            company_identification_id = it,
                            label = companyData.label,
                            number = companyData.number
                        )
                    } ?: saveCompanyId(companyData, client)
                }

                client.id?.let {
                    clientOrIssuerQueries.updateClientOrIssuer(
                        client_or_issuer_id = it.toLong(),
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

    /*
        override fun checkIfEmpty(): Int {
            return clientQueries.checkIfEmpty().executeAsOne().toInt()
        }
    */

    override suspend fun deleteClientOrIssuer(id: Long) {
        return withContext(Dispatchers.IO) {
            try {
                clientOrIssuerQueries.deleteClientOrIssuer(id.toLong())
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun getLastCreatedId(): Long? {
        var lastinsert: Long? = null
        withContext(Dispatchers.IO) {
            try {
                lastinsert = clientOrIssuerQueries.getLastInsertedRowId().executeAsOneOrNull()
            } catch (cause: Throwable) {
            }
        }
        println("lastinsert = " + lastinsert)
        return lastinsert
    }

    private fun saveCompanyId(
        companyIdentification: CompanyDataEditable,
        client: ClientOrIssuerEditable,
    ) {
        // Save the values
        companyDataQueries.saveCompanyData(
            company_identification_id = null,
            label = companyIdentification.label,
            number = companyIdentification.number
        )

        // Get the ID of the last inserted row
        val companyIdentificationId: Long? = companyDataQueries.lastInsertRowId().executeAsOneOrNull()

        // Save the company identifiants for the client
        client.id?.let { clientId ->
            companyIdentificationId?.let { companyId ->
                clientOrIssuerCompanyDataQueries.saveClientOrIssuerCompanyData(
                    id = null,
                    client_or_issuer_id = clientId.toLong(),
                    company_identification_id = companyId,
                )
            }
        }
    }
}

fun ClientOrIssuer.transformIntoEditable(
    clientOrIssuerCompanyDataQueries: ClientOrIssuerCompanyDataQueries,
    companyDataQueries: CompanyDataQueries,
): ClientOrIssuerEditable {
    val companyData: MutableList<CompanyDataEditable> = mutableListOf()
    val clientOrIssuer = this

    val identifiers =
        clientOrIssuerCompanyDataQueries.getClientOrIssuerCompanyData(clientOrIssuer.client_or_issuer_id)
            .executeAsList()

    identifiers.forEach {
        companyDataQueries.getCompanyData(it).executeAsOneOrNull()?.let { data ->
            companyData += CompanyDataEditable(
                id = data.company_identification_id,
                label = data.label,
                number = data.number
            )
        }
    }

    return ClientOrIssuerEditable(
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