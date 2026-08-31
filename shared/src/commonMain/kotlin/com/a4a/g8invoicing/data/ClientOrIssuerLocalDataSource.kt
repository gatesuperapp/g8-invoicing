package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.data.util.DispatcherProvider
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.EmailState
import com.a4a.g8invoicing.ui.states.IssuerBankState
import g8invoicing.ClientOrIssuerEmail
import g8invoicing.DocumentClientOrIssuerEmail
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.PersonType
import g8invoicing.ClientOrIssuer
import g8invoicing.ClientOrIssuerAddress
import g8invoicing.DocumentClientOrIssuerAddress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ClientOrIssuerLocalDataSource(
    db: Database,
    private val currentCompanyRepository: CurrentCompanyRepository,
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
    private val issuerBankQueries = db.issuerBankQueries

    override suspend fun fetchClientOrIssuer(id: Long): ClientOrIssuerState? {
        return withContext(DispatcherProvider.IO) {
            try {
                clientOrIssuerQueries.get(id).executeAsOneOrNull()
                    ?.let {
                        it.transformIntoEditable(
                            addresses = fetchClientOrIssuerAddresses(it.id)?.toMutableList(),
                            emails = fetchClientOrIssuerEmails(it.id)?.toMutableList(),
                        ).copy(banks = fetchIssuerBanks(it.id))
                    }
            } catch (e: Exception) {
                null
            }
        }
    }

    // Read all bank accounts attached to an issuer, ordered stably by sort_order
    // then id (append-order tie-breaker).
    internal fun fetchIssuerBanks(issuerId: Long): List<IssuerBankState> {
        return try {
            issuerBankQueries.getForIssuer(issuerId).executeAsList().map { row ->
                IssuerBankState(
                    id = row.issuer_bank_id.toInt(),
                    label = row.label?.let { TextFieldValue(text = it) },
                    countryCode = row.country_code,
                    identifier = TextFieldValue(text = row.identifier ?: ""),
                    bic = TextFieldValue(text = row.bic ?: ""),
                    sortOrder = row.sort_order.toInt(),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Sync the whole bank list for an issuer: wipe + reinsert. Simple, safe on
    // small lists (users hold 1-3 accounts in practice), and avoids the delta
    // dance for insert/update/delete/reorder in one shot.
    internal fun saveIssuerBanks(issuerId: Long, banks: List<IssuerBankState>) {
        try {
            issuerBankQueries.deleteForIssuer(issuerId)
            banks.forEachIndexed { index, bank ->
                val label = bank.label?.text?.trim().orEmpty().ifEmpty { null }
                val identifier = bank.identifier.text.trim().ifEmpty { null }
                val bic = bank.bic.text.trim().ifEmpty { null }
                val country = bank.countryCode?.trim()?.ifEmpty { null }
                // Skip fully-empty rows: an unfilled "+ Ajouter un compte"
                // placeholder should not persist as an empty account.
                if (label == null && identifier == null && bic == null) return@forEachIndexed
                issuerBankQueries.save(
                    issuer_bank_id = null,
                    issuer_id = issuerId,
                    label = label,
                    country_code = country,
                    identifier = identifier,
                    bic = bic,
                    sort_order = index.toLong(),
                )
            }
        } catch (e: Exception) {
            // Log if needed
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchAll(type: PersonType): Flow<List<ClientOrIssuerState>> {
        // Clients are scoped to the entreprise courante; issuers ARE the
        // entreprises, so the issuer list stays unfiltered.
        val transform: (List<ClientOrIssuer>) -> List<ClientOrIssuerState> = { rows ->
            rows.map {
                it.transformIntoEditable(
                    addresses = fetchClientOrIssuerAddresses(it.id)?.toMutableList(),
                    emails = fetchClientOrIssuerEmails(it.id)?.toMutableList(),
                ).copy(banks = fetchIssuerBanks(it.id))
            }
        }
        return if (type == PersonType.CLIENT) {
            currentCompanyRepository.state.flatMapLatest { companyId ->
                val query = if (companyId != null) {
                    clientOrIssuerQueries.getAllClientsForCompany(companyId)
                } else {
                    clientOrIssuerQueries.getAll(type.name.lowercase())
                }
                query.asFlow().map { transform(it.executeAsList()) }
            }.flowOn(DispatcherProvider.IO)
        } else {
            clientOrIssuerQueries.getAll(type.name.lowercase())
                .asFlow()
                .map { transform(it.executeAsList()) }
                .flowOn(DispatcherProvider.IO)
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
        return createNewAndReturnId(clientOrIssuer) != null
    }

    override suspend fun createNewAndReturnId(clientOrIssuer: ClientOrIssuerState): Long? {
        // Wrapped in a single transaction so the clients-table Flow only re-emits once
        // all related rows (client, addresses, emails) are committed together. Otherwise
        // the list refreshes on the client insert alone and fetches an empty emails list.
        return withContext(DispatcherProvider.IO) {
            try {
                clientOrIssuerQueries.transactionWithResult<Long?> {
                    saveClientOrIssuerRow(clientOrIssuer)

                    val newEntityId = clientOrIssuerQueries.getLastInsertedRowId().executeAsOneOrNull()
                        ?: return@transactionWithResult null

                    if (!saveClientOrIssuerAddressRows(newEntityId, clientOrIssuer.addresses)) {
                        rollback(null)
                    }

                    saveClientOrIssuerEmailRows(newEntityId, clientOrIssuer.emails)
                    saveIssuerBanks(newEntityId, clientOrIssuer.banks)

                    newEntityId
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun saveClientOrIssuerRow(clientOrIssuer: ClientOrIssuerState) {
        val isClient = clientOrIssuer.type == ClientOrIssuerType.CLIENT ||
            clientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT
        clientOrIssuerQueries.save(
            id = null,
            type = if (isClient) ClientOrIssuerType.CLIENT.name.lowercase()
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
            if (clientOrIssuer.vatExempt) 1L else 0L,
            if (clientOrIssuer.intraEuSales) 1L else 0L,
            // Clients rattachés à l'entreprise courante ; issuers ne
            // s'auto-référencent pas.
            company_id = if (isClient) currentCompanyRepository.current else null,
            // Only meaningful on clients (Factur-X gate). Issuers store NULL —
            // the field doesn't apply on the emitting side.
            client_type = if (isClient) clientOrIssuer.clientType?.name else null,
            tax_withholding_enabled = if (clientOrIssuer.taxWithholdingEnabled) 1L else 0L,
        )
    }

    private fun saveClientOrIssuerAddressRows(
        clientOrIssuerId: Long,
        addresses: List<AddressState>?,
    ): Boolean {
        if (addresses.isNullOrEmpty()) return true
        for (address in addresses) {
            if (isAddressEmpty(address)) continue
            clientOrIssuerAddressQueries.save(
                id = null,
                address_title = address.addressTitle?.text?.trim(),
                address_line_1 = address.addressLine1?.text?.trim(),
                address_line_2 = address.addressLine2?.text?.trim(),
                zip_code = address.zipCode?.text?.trim(),
                city = address.city?.text?.trim(),
                country_code = address.countryCode?.trim(),
            )
            val newAddressId = clientOrIssuerAddressQueries.getLastInsertedRowId().executeAsOneOrNull()
                ?: return false
            linkClientOrIssuerToAddressQueries.save(
                id = null,
                client_or_issuer_id = clientOrIssuerId,
                address_id = newAddressId,
            )
        }
        return true
    }

    // An address is empty only if EVERY field, including country, is blank.
    // A country-only row is kept: the first-launch onboarding seeds one so
    // the VAT-exemption text can key off the issuer's country later, and a
    // user who picks a country in the form clearly means it.
    private fun isAddressEmpty(address: AddressState): Boolean {
        return address.addressTitle?.text.isNullOrBlank() &&
            address.addressLine1?.text.isNullOrBlank() &&
            address.addressLine2?.text.isNullOrBlank() &&
            address.zipCode?.text.isNullOrBlank() &&
            address.city?.text.isNullOrBlank() &&
            address.countryCode.isNullOrBlank()
    }

    private fun saveClientOrIssuerEmailRows(
        clientOrIssuerId: Long,
        emails: List<EmailState>?,
    ) {
        if (emails.isNullOrEmpty()) return
        for (email in emails) {
            if (email.email.text.isNotEmpty()) {
                clientOrIssuerEmailQueries.save(
                    id = null,
                    client_or_issuer_id = clientOrIssuerId,
                    email = email.email.text.trim(),
                )
            }
        }
    }

    private suspend fun saveInfoInClientOrIssuerTable(clientOrIssuer: ClientOrIssuerState) {
        return withContext(DispatcherProvider.IO) {
            try {
                val isClient = clientOrIssuer.type == ClientOrIssuerType.CLIENT ||
                    clientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT
                clientOrIssuerQueries.save(
                    id = null,
                    type = if (isClient) ClientOrIssuerType.CLIENT.name.lowercase()
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
                    if (clientOrIssuer.vatExempt) 1L else 0L,
                    if (clientOrIssuer.intraEuSales) 1L else 0L,
                    company_id = if (isClient) currentCompanyRepository.current else null,
                    client_type = if (isClient) clientOrIssuer.clientType?.name else null,
                    tax_withholding_enabled = if (clientOrIssuer.taxWithholdingEnabled) 1L else 0L,
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
                    if (isAddressEmpty(address)) continue
                    clientOrIssuerAddressQueries.save(
                        id = null,
                        address_title = address.addressTitle?.text?.trim(),
                        address_line_1 = address.addressLine1?.text?.trim(),
                        address_line_2 = address.addressLine2?.text?.trim(),
                        zip_code = address.zipCode?.text?.trim(),
                        city = address.city?.text?.trim(),
                        country_code = address.countryCode?.trim(),
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
                    if (isAddressEmpty(address)) return@forEach
                    documentClientOrIssuerAddressQueries.save(
                        id = null,
                        original_address_id = address.originalAddressId?.toLong(),
                        address_title = address.addressTitle?.text?.trim(),
                        address_line_1 = address.addressLine1?.text?.trim(),
                        address_line_2 = address.addressLine2?.text?.trim(),
                        zip_code = address.zipCode?.text?.trim(),
                        city = address.city?.text?.trim(),
                        country_code = address.countryCode?.trim(),
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
                        // Wrap in a transaction so the list flow only re-emits once the
                        // client, addresses and emails are all committed together.
                        clientOrIssuerQueries.transaction {
                            saveClientOrIssuerRow(client)
                            val newClientId = clientOrIssuerQueries.getLastInsertedRowId().executeAsOneOrNull()
                                ?: return@transaction
                            saveClientOrIssuerAddressRows(newClientId, client.addresses)
                            saveClientOrIssuerEmailRows(newClientId, client.emails)
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
                    val isClient = clientOrIssuer.type == ClientOrIssuerType.CLIENT ||
                        clientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT
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
                        vat_exempt = if (clientOrIssuer.vatExempt) 1L else 0L,
                        intra_eu_sales = if (clientOrIssuer.intraEuSales) 1L else 0L,
                        client_type = if (isClient) clientOrIssuer.clientType?.name else null,
                        tax_withholding_enabled = if (clientOrIssuer.taxWithholdingEnabled) 1L else 0L,
                    )
                    // Bank accounts live in their own table; simplest robust sync
                    // is delete-all-then-reinsert (small lists, rare edits).
                    saveIssuerBanks(it.toLong(), clientOrIssuer.banks)
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
                                country_code = address.countryCode?.trim(),
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
                // Re-derive the frozen payment_iban/payment_bic from the current
                // bank list. Users edit banks through the section (add a BIC to
                // an existing account, add a 2nd account) without touching the
                // top-level paymentIban/paymentBic fields — those would drift
                // to stale values on save without this sync. Matching rule:
                // keep the same bank if its IBAN is still present; otherwise
                // fall back to the first bank of the (edited) list.
                val syncedBank = documentClientOrIssuer.banks.firstOrNull { bank ->
                    val current = documentClientOrIssuer.paymentIban?.text?.trim().orEmpty()
                    current.isNotEmpty() && bank.identifier.text.trim() == current
                } ?: documentClientOrIssuer.banks.firstOrNull()
                val syncedIban = syncedBank?.identifier?.text?.trim()?.takeIf { it.isNotEmpty() }
                    ?: documentClientOrIssuer.paymentIban?.text?.trim()
                val syncedBic = syncedBank?.bic?.text?.trim()?.takeIf { it.isNotEmpty() }
                    ?: documentClientOrIssuer.paymentBic?.text?.trim()
                val syncedCountry = syncedBank?.countryCode?.trim()?.takeIf { it.isNotEmpty() }
                    ?: documentClientOrIssuer.paymentCountry?.trim()?.takeIf { it.isNotEmpty() }
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
                        vat_exempt = if (documentClientOrIssuer.vatExempt) 1L else 0L,
                        intra_eu_sales = if (documentClientOrIssuer.intraEuSales) 1L else 0L,
                        payment_iban = syncedIban,
                        payment_bic = syncedBic,
                        payment_country = syncedCountry,
                        client_type = if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT ||
                            documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT
                        ) documentClientOrIssuer.clientType?.name else null,
                        tax_withholding_enabled = if (documentClientOrIssuer.taxWithholdingEnabled) 1L else 0L,
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
                                country_code = address.countryCode?.trim(),
                            )
                        }
                    }
                    // For new addresses: create document address only (master will be created in syncToMaster block)
                    documentClientOrIssuer.id?.let { docClientId ->
                        addressesToCreate.forEach { address ->
                            if (isAddressEmpty(address)) return@forEach
                            // Create document address without master link (will be set during sync)
                            documentClientOrIssuerAddressQueries.save(
                                id = null,
                                original_address_id = null,
                                address_title = address.addressTitle?.text?.trim(),
                                address_line_1 = address.addressLine1?.text?.trim(),
                                address_line_2 = address.addressLine2?.text?.trim(),
                                zip_code = address.zipCode?.text?.trim(),
                                city = address.city?.text?.trim(),
                                country_code = address.countryCode?.trim(),
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

                // Banks are always master-owned resources (they live in the
                // IssuerBank table, keyed to the master issuer). When the user
                // edits banks from the doc-embedded issuer form (add/remove a
                // bank, add a BIC), those edits must persist to the master
                // regardless of the sync-to-master switch — which only controls
                // propagation of name/phone/company-id/address fields. Empty
                // list = state wasn't hydrated (older code paths, race
                // conditions) → keep master intact rather than wipe.
                val isIssuerEdit = documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_ISSUER ||
                    documentClientOrIssuer.type == ClientOrIssuerType.ISSUER
                if (isIssuerEdit && documentClientOrIssuer.banks.isNotEmpty()) {
                    documentClientOrIssuer.originalClientOrIssuerId?.toLong()?.let { masterId ->
                        saveIssuerBanks(masterId, documentClientOrIssuer.banks)
                    }
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
                        vat_exempt = if (documentClientOrIssuer.vatExempt) 1L else 0L,
                        intra_eu_sales = if (documentClientOrIssuer.intraEuSales) 1L else 0L,
                        client_type = if (masterType == ClientOrIssuerType.CLIENT.name.lowercase()) {
                            documentClientOrIssuer.clientType?.name
                        } else {
                            null
                        },
                        tax_withholding_enabled = if (documentClientOrIssuer.taxWithholdingEnabled) 1L else 0L,
                    )
                    // Banks handled unconditionally above — master-owned resource,
                    // not gated by syncToMaster.

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
                                country_code = docAddress.country_code,
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
                                country_code = docAddress.country_code,
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
                            // Legal / regime flags must be copied from the master —
                            // otherwise a new invoice always defaults to vatExempt=false /
                            // intraEuSales=false, ignoring the toggles the user just
                            // set on their entreprise from Mon Compte.
                            vatExempt = (issuer.vat_exempt ?: 0L) != 0L,
                            intraEuSales = (issuer.intra_eu_sales ?: 0L) != 0L,
                            taxWithholdingEnabled = issuer.tax_withholding_enabled != 0L,
                            banks = fetchIssuerBanks(issuer.id),
                            // Freeze the first bank (sort_order = 0) on the new doc.
                            // The payment-means picker on the invoice lets the user
                            // swap in a different bank later — this seeds the pick
                            // so the block renders correctly on a brand-new doc.
                            paymentIban = fetchIssuerBanks(issuer.id).firstOrNull()
                                ?.identifier?.text?.takeIf { it.isNotEmpty() }
                                ?.let { TextFieldValue(text = it) },
                            paymentBic = fetchIssuerBanks(issuer.id).firstOrNull()
                                ?.bic?.text?.takeIf { it.isNotEmpty() }
                                ?.let { TextFieldValue(text = it) },
                            paymentCountry = fetchIssuerBanks(issuer.id).firstOrNull()
                                ?.countryCode,
                        )
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun getCurrentIssuer(companyId: Long): ClientOrIssuerState? {
        return withContext(DispatcherProvider.IO) {
            try {
                clientOrIssuerQueries.get(companyId).executeAsOneOrNull()?.let { issuer ->
                    val banks = fetchIssuerBanks(issuer.id)
                    val firstBank = banks.firstOrNull()
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
                        // Copy the regime flags from the master row (see getLastIssuer).
                        vatExempt = (issuer.vat_exempt ?: 0L) != 0L,
                        intraEuSales = (issuer.intra_eu_sales ?: 0L) != 0L,
                        banks = banks,
                        // Freeze the first bank (sort_order = 0) — same seed as
                        // getLastIssuer; the payment-means picker can swap it later.
                        paymentIban = firstBank?.identifier?.text?.takeIf { it.isNotEmpty() }
                            ?.let { TextFieldValue(text = it) },
                        paymentBic = firstBank?.bic?.text?.takeIf { it.isNotEmpty() }
                            ?.let { TextFieldValue(text = it) },
                        paymentCountry = firstBank?.countryCode,
                    )
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun getIssuerBanks(issuerId: Long): List<IssuerBankState> {
        return withContext(DispatcherProvider.IO) { fetchIssuerBanks(issuerId) }
    }

    override suspend fun updateDocumentClientOrIssuerPaymentBank(
        documentClientOrIssuerId: Long,
        iban: String?,
        bic: String?,
        country: String?,
    ) {
        withContext(DispatcherProvider.IO) {
            try {
                documentClientOrIssuerQueries.updatePaymentBank(
                    id = documentClientOrIssuerId,
                    payment_iban = iban?.takeIf { it.isNotEmpty() },
                    payment_bic = bic?.takeIf { it.isNotEmpty() },
                    payment_country = country?.takeIf { it.isNotEmpty() },
                )
            } catch (_: Exception) {
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

    override suspend fun acknowledgeDocumentClientOrIssuerVersion(
        documentClientOrIssuerId: Long,
        masterId: Long,
    ): Int? {
        return withContext(DispatcherProvider.IO) {
            try {
                val version = clientOrIssuerQueries.get(masterId)
                    .executeAsOneOrNull()?.version ?: return@withContext null
                documentClientOrIssuerQueries.updateOriginalVersion(
                    id = documentClientOrIssuerId,
                    original_version = version,
                )
                version.toInt()
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun getLastCountryCode(): String? {
        return withContext(DispatcherProvider.IO) {
            try {
                // Single-column SELECT — SQLDelight returns String? directly (the column is
                // nullable in schema even though the WHERE filters out empty rows), so the
                // executeAsOneOrNull result is String??; unwrap and normalise.
                clientOrIssuerAddressQueries.getLastCountryCode()
                    .executeAsOneOrNull()?.trim()?.takeIf { it.isNotEmpty() }?.uppercase()
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun getRecentCountryCodes(limit: Int): List<String> {
        return withContext(DispatcherProvider.IO) {
            try {
                clientOrIssuerAddressQueries.getRecentCountryCodes(limit.toLong())
                    .executeAsList()
                    .mapNotNull { it?.trim()?.takeIf { s -> s.isNotEmpty() }?.uppercase() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun setCountryForClientsWithoutCountry(countryCode: String) {
        // Reuse the standard updateClientOrIssuer path — its ClientOrIssuer
        // UPDATE bumps updated_at on the parent row, which is what makes
        // fetchAll's listener (subscribed to ClientOrIssuer, not …Address)
        // re-emit. A raw bulk UPDATE on the address table alone would leave
        // the client-list state stale with country_code=null, and the
        // address-edit form would then overwrite the just-filled country
        // with the device-locale fallback.
        //
        // Two shapes to handle for pre-1.8 clients:
        //   1. client has address rows with null country_code → patch them.
        //   2. client has no address row at all (name/email only)      → create a
        //      minimal address row carrying just the country, so the country is
        //      persisted and the edit form can't overwrite it with the fallback.
        val normalized = countryCode.trim().uppercase()
        fetchAll(PersonType.CLIENT).first().forEach { client ->
            val addresses = client.addresses
            val patchedAddresses = when {
                addresses.isNullOrEmpty() -> listOf(AddressState(countryCode = normalized))
                addresses.any { it.countryCode.isNullOrBlank() } -> addresses.map { addr ->
                    if (addr.countryCode.isNullOrBlank()) addr.copy(countryCode = normalized)
                    else addr
                }
                else -> null
            }
            if (patchedAddresses != null) {
                updateClientOrIssuer(client.copy(addresses = patchedAddresses))
            }
        }
    }

    override suspend fun fetchLast3RecentClientOrIssuerIds(type: PersonType): List<Long> =
        withContext(DispatcherProvider.IO) {
            val typeStr = when (type) {
                PersonType.CLIENT -> ClientOrIssuerType.CLIENT.name.lowercase()
                PersonType.ISSUER -> ClientOrIssuerType.ISSUER.name.lowercase()
            }
            documentClientOrIssuerQueries
                .getLast3RecentOriginalIdsByType(typeStr)
                .executeAsList()
                .mapNotNull { it }
        }

    override suspend fun bulkAttachToCompany(ids: List<Long>, companyId: Long) {
        if (ids.isEmpty()) return
        withContext(DispatcherProvider.IO) {
            clientOrIssuerQueries.transaction {
                ids.forEach { id ->
                    clientOrIssuerQueries.updateCompanyId(companyId, id)
                }
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
        countryCode = clientOrIssuer.country_code,
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
        vatExempt = (clientOrIssuer.vat_exempt ?: 0L) != 0L,
        intraEuSales = (clientOrIssuer.intra_eu_sales ?: 0L) != 0L,
        clientType = com.a4a.g8invoicing.data.models.ClientType.fromDb(clientOrIssuer.client_type),
        taxWithholdingEnabled = clientOrIssuer.tax_withholding_enabled != 0L,
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
        countryCode = address.country_code,
    )
}
