package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.ui.screens.PersonType
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import g8invoicing.DeliveryNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class DeliveryNoteLocalDataSource(
    db: Database,
) : DeliveryNoteLocalDataSourceInterface {
    private val deliveryNoteQueries = db.deliveryNoteQueries
    private val clientOrIssuerQueries = db.clientOrIssuerQueries
    private val clientOrIssuerCompanyDataQueries = db.clientOrIssuerCompanyDataQueries
    private val companyIdQueries = db.companyDataQueries
    private val deliveryNoteProductQueries = db.deliveryNoteProductQueries
    private val documentProductQueries = db.documentProductQueries

    override fun fetchDeliveryNote(id: Long): DeliveryNoteState? {
        return deliveryNoteQueries.getDeliveryNote(id).executeAsOneOrNull()
            ?.transformIntoEditableNote()
    }

    override fun fetchAllDeliveryNotes(): Flow<List<DeliveryNoteState>> {
        return deliveryNoteQueries.getAllDeliveryNotes()
            .asFlow()
            .map { query ->
                query.executeAsList().map { it.transformIntoEditableNote() }
            }
    }

    private fun DeliveryNote.transformIntoEditableNote(): DeliveryNoteState {
        var client: ClientOrIssuerEditable? = null
        var issuer: ClientOrIssuerEditable? = null
        var deliveryNote: DeliveryNoteState

        this.client_id?.let {
            client = clientOrIssuerQueries.getClientOrIssuer(it)
                .executeAsOneOrNull()
                ?.transformIntoEditable(clientOrIssuerCompanyDataQueries, companyIdQueries)
        }

        this.issuer_id?.let {
            issuer = clientOrIssuerQueries.getClientOrIssuer(it)
                .executeAsOneOrNull()
                ?.transformIntoEditable(clientOrIssuerCompanyDataQueries, companyIdQueries)
        }

        this.let {
            val items = buildDocumentProductList(it.delivery_note_id)

            val totalPriceWithoutTax = items.sumOf {
                (it.priceWithoutTax) * (it.quantity)
            }.setScale(2, RoundingMode.HALF_UP)

            /*val totalPriceWithoutTax = BigDecimal(0).setScale(2, RoundingMode.HALF_UP)

            items.forEach {
                totalPriceWithoutTax.add((it.priceWithoutTax) * (it.quantity))
            }*/

            // Calculate the total amount of each tax
            val groupedItems = items.groupBy {
                it.taxRate
            }
            val taxes = groupedItems.keys.distinct() // ex: taxes= [20, 10]

            val amounts: MutableList<BigDecimal> = mutableListOf()  // ex: amounts = [7.2, 2.4]
            groupedItems.values.forEach { documentProduct ->
                val listOfAmounts = documentProduct.map { item ->
                    item.priceWithoutTax * item.quantity * item.taxRate / BigDecimal(100)
                }
                val sumOfAmounts = listOfAmounts.sumOf { it }.setScale(2, RoundingMode.HALF_UP)
                amounts.add(
                    sumOfAmounts
                )
            }
            val amountsPerTaxRate: MutableList<Pair<BigDecimal, BigDecimal>> = mutableListOf()
            taxes.forEachIndexed { index, key ->
                amountsPerTaxRate.add(Pair(key, amounts[index]))
            } // ex: amountsPerTaxRate = [(20.0, 7.2), (10.0, 2.4)]

            deliveryNote = DeliveryNoteState(
                deliveryNoteId = it.delivery_note_id.toInt(),
                number = TextFieldValue(text = it.number),
                deliveryDate = it.delivery_date,
                orderNumber = TextFieldValue(text = it.order_number ?: ""),
                issuer = issuer,
                client = client,
                documentProducts = items,
                totalPriceWithoutTax = totalPriceWithoutTax,
                totalAmountsOfEachTax = amountsPerTaxRate,
                totalPriceWithTax = totalPriceWithoutTax + amounts.sumOf { it },
            )
        }

        return deliveryNote
    }

    private fun buildDocumentProductList(deliveryNoteId: Long): List<DocumentProductState> {
        val documentProducts: MutableList<DocumentProductState> = mutableListOf()

        val identifiers =
            deliveryNoteProductQueries.getDeliveryNoteProductIds(deliveryNoteId).executeAsList()
        identifiers.forEach {
            documentProductQueries.getDocumentProduct(it).executeAsOneOrNull()
                ?.let { documentProduct ->
                    documentProducts += documentProduct.transformIntoEditableDocumentProduct()
                }
        }
        return documentProducts
    }

    override suspend fun saveDeliveryNote(deliveryNote: DeliveryNoteState) {
        return withContext(Dispatchers.IO) {
            try {
                getExistingIssuerOrSaveNewIssuer(deliveryNote)

                deliveryNoteQueries.saveDeliveryNote(
                    delivery_note_id = null,
                    number = deliveryNote.number?.text ?: "",
                    delivery_date = deliveryNote.deliveryDate.toString(),
                    order_number = deliveryNote.orderNumber?.text,
                    issuer_id = deliveryNote.issuer?.id?.toLong()
                        ?: deliveryNote.issuer?.email?.let {
                            clientOrIssuerQueries.getClientOrIssuerIdByMail(it.text)
                                .executeAsOneOrNull()
                        },
                    client_id = deliveryNote.client?.id?.toLong(),
                    currency = deliveryNote.currency?.text
                )
            } catch (cause: Throwable) {
            }
        }
    }


    override suspend fun duplicateDeliveryNote(deliveryNote: DeliveryNoteState) {
        return withContext(Dispatchers.IO) {
            try {
                deliveryNoteQueries.saveDeliveryNote(
                    delivery_note_id = null,
                    number = deliveryNote.number?.text ?: " - ",
                    delivery_date = deliveryNote.deliveryDate,
                    order_number = deliveryNote.orderNumber?.text,
                    issuer_id = deliveryNote.issuer?.id?.toLong(),
                    client_id = deliveryNote.client?.id?.toLong(),
                    currency = deliveryNote.currency?.text
                )
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun updateDeliveryNote(deliveryNote: DeliveryNoteState) {
        return withContext(Dispatchers.IO) {
            try {
                getExistingIssuerOrSaveNewIssuer(deliveryNote)

                deliveryNoteQueries.updateDeliveryNote(
                    delivery_note_id = deliveryNote.deliveryNoteId?.toLong() ?: 0,
                    number = deliveryNote.number?.text ?: "",
                    delivery_date = deliveryNote.deliveryDate.toString(),
                    client_id = deliveryNote.client?.id?.toLong(),
                    issuer_id = deliveryNote.issuer?.id?.toLong()
                        ?: deliveryNote.issuer?.email?.let {
                            clientOrIssuerQueries.getClientOrIssuerIdByMail(it.text)
                                .executeAsOneOrNull()
                        },
                    order_number = deliveryNote.orderNumber?.text,
                    currency = deliveryNote.currency?.text
                )
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun deleteDeliveryNote(id: Long) {
        return withContext(Dispatchers.IO) {
            deliveryNoteQueries.deleteDeliveryNote(id)
        }
    }

    override suspend fun deleteDeliveryNoteProduct(documentProductId: Long) {
        return withContext(Dispatchers.IO) {
            deliveryNoteProductQueries.deleteDeliveryNoteProduct(documentProductId)
        }
    }

    override suspend fun addDeliveryNoteProduct(deliveryNoteId: Long, documentProductId: Long) {
        return withContext(Dispatchers.IO) {
            deliveryNoteProductQueries.saveDeliveryNoteProduct(
                id = null,
                delivery_note_id = deliveryNoteId,
                document_product_id = documentProductId)
        }
    }

    private fun getExistingIssuerOrSaveNewIssuer(deliveryNote: DeliveryNoteState) {
        deliveryNote.issuer?.id ?: deliveryNote.issuer?.let {
            clientOrIssuerQueries.saveClientOrIssuer(
                client_or_issuer_id = null,
                type = "issuer",
                first_name = it.firstName?.text,
                name = it.name.text,
                address1 = it.address1?.text,
                address2 = it.address2?.text,
                zip_code = it.zipCode?.text,
                city = it.city?.text,
                phone = it.phone?.text,
                email = it.email?.text,
                notes = it.notes?.text
            )
        }
    }
}


