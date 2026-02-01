package com.a4a.g8invoicing

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.PersonType
import com.a4a.g8invoicing.fakes.FakeClientOrIssuerDataSource
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.EmailState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClientOrIssuerDataSourceTest {

    private lateinit var dataSource: FakeClientOrIssuerDataSource

    @BeforeTest
    fun setup() {
        dataSource = FakeClientOrIssuerDataSource()
    }

    // ============= CLIENT CREATION TESTS =============

    @Test
    fun createClient_savesCorrectly() = runTest {
        val client = ClientOrIssuerState(
            name = TextFieldValue("Client Test"),
            type = ClientOrIssuerType.CLIENT
        )

        val result = dataSource.createNew(client)

        assertTrue(result)
        assertEquals(1, dataSource.getClients().size)
        assertEquals("Client Test", dataSource.getClients().first().name.text)
    }

    @Test
    fun createClient_withAddress() = runTest {
        val address = AddressState(
            addressLine1 = TextFieldValue("123 Rue Test"),
            city = TextFieldValue("Paris"),
            zipCode = TextFieldValue("75001")
        )
        val client = ClientOrIssuerState(
            name = TextFieldValue("Client Adresse"),
            type = ClientOrIssuerType.CLIENT,
            addresses = listOf(address)
        )

        dataSource.createNew(client)

        val saved = dataSource.getClients().first()
        assertNotNull(saved.addresses)
        assertEquals(1, saved.addresses?.size)
        assertEquals("123 Rue Test", saved.addresses?.first()?.addressLine1?.text)
    }

    @Test
    fun createClient_withMultipleAddresses() = runTest {
        val address1 = AddressState(
            addressTitle = TextFieldValue("Siège"),
            addressLine1 = TextFieldValue("1 Rue Principale")
        )
        val address2 = AddressState(
            addressTitle = TextFieldValue("Entrepôt"),
            addressLine1 = TextFieldValue("Zone Industrielle")
        )
        val client = ClientOrIssuerState(
            name = TextFieldValue("Multi-Adresses"),
            type = ClientOrIssuerType.CLIENT,
            addresses = listOf(address1, address2)
        )

        dataSource.createNew(client)

        val saved = dataSource.getClients().first()
        assertEquals(2, saved.addresses?.size)
    }

    @Test
    fun createClient_withAllFields() = runTest {
        val client = ClientOrIssuerState(
            name = TextFieldValue("Entreprise Complète"),
            firstName = TextFieldValue("Jean"),
            type = ClientOrIssuerType.CLIENT,
            emails = listOf(EmailState(email = TextFieldValue("contact@entreprise.fr"))),
            phone = TextFieldValue("0123456789"),
            notes = TextFieldValue("Client important"),
            companyId1Number = TextFieldValue("12345678901234"),
            companyId2Number = TextFieldValue("FR12345678901")
        )

        dataSource.createNew(client)

        val saved = dataSource.getClients().first()
        assertEquals("Jean", saved.firstName?.text)
        assertEquals("contact@entreprise.fr", saved.emails?.firstOrNull()?.email?.text)
        assertEquals("0123456789", saved.phone?.text)
    }

    // ============= ISSUER CREATION TESTS =============

    @Test
    fun createIssuer_savesCorrectly() = runTest {
        val issuer = ClientOrIssuerState(
            name = TextFieldValue("Mon Entreprise"),
            type = ClientOrIssuerType.ISSUER
        )

        dataSource.createNew(issuer)

        assertEquals(1, dataSource.getIssuers().size)
        assertEquals(0, dataSource.getClients().size)
        assertEquals("Mon Entreprise", dataSource.getIssuers().first().name.text)
    }

    @Test
    fun createIssuer_withCompanyIds() = runTest {
        val issuer = ClientOrIssuerState(
            name = TextFieldValue("Ma Société"),
            type = ClientOrIssuerType.ISSUER,
            companyId1Label = TextFieldValue("SIRET"),
            companyId1Number = TextFieldValue("12345678901234"),
            companyId2Label = TextFieldValue("TVA"),
            companyId2Number = TextFieldValue("FR12345678901"),
            companyId3Label = TextFieldValue("RCS"),
            companyId3Number = TextFieldValue("Paris B 123 456 789")
        )

        dataSource.createNew(issuer)

        val saved = dataSource.getIssuers().first()
        assertEquals("12345678901234", saved.companyId1Number?.text)
        assertEquals("FR12345678901", saved.companyId2Number?.text)
    }

    // ============= FETCH TESTS =============

    @Test
    fun fetchClient_existingClient() = runTest {
        val client = ClientOrIssuerState(
            name = TextFieldValue("Client Existant"),
            type = ClientOrIssuerType.CLIENT
        )
        dataSource.createNew(client)

        val fetched = dataSource.fetchClientOrIssuer(1L)

        assertNotNull(fetched)
        assertEquals("Client Existant", fetched.name.text)
    }

    @Test
    fun fetchClient_nonExistent() = runTest {
        val fetched = dataSource.fetchClientOrIssuer(999L)
        assertNull(fetched)
    }

    @Test
    fun fetchAllClients_filtersCorrectly() = runTest {
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Client 1"),
            type = ClientOrIssuerType.CLIENT
        ))
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Emetteur 1"),
            type = ClientOrIssuerType.ISSUER
        ))
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Client 2"),
            type = ClientOrIssuerType.CLIENT
        ))

        val clients = dataSource.fetchAll(PersonType.CLIENT).first()

        assertEquals(2, clients.size)
        assertTrue(clients.all { it.type == ClientOrIssuerType.CLIENT })
    }

    @Test
    fun fetchAllIssuers_filtersCorrectly() = runTest {
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Client 1"),
            type = ClientOrIssuerType.CLIENT
        ))
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Emetteur 1"),
            type = ClientOrIssuerType.ISSUER
        ))

        val issuers = dataSource.fetchAll(PersonType.ISSUER).first()

        assertEquals(1, issuers.size)
        assertEquals("Emetteur 1", issuers.first().name.text)
    }

    // ============= UPDATE TESTS =============

    @Test
    fun updateClient_changesName() = runTest {
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Ancien Nom"),
            type = ClientOrIssuerType.CLIENT
        ))
        val client = dataSource.getClients().first()

        val updated = client.copy(name = TextFieldValue("Nouveau Nom"))
        dataSource.updateClientOrIssuer(updated)

        val fetched = dataSource.fetchClientOrIssuer(client.id!!.toLong())
        assertEquals("Nouveau Nom", fetched?.name?.text)
    }

    @Test
    fun updateClient_changesEmail() = runTest {
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Client"),
            type = ClientOrIssuerType.CLIENT,
            emails = listOf(EmailState(email = TextFieldValue("ancien@email.fr")))
        ))
        val client = dataSource.getClients().first()

        val updated = client.copy(emails = listOf(EmailState(email = TextFieldValue("nouveau@email.fr"))))
        dataSource.updateClientOrIssuer(updated)

        val fetched = dataSource.fetchClientOrIssuer(client.id!!.toLong())
        assertEquals("nouveau@email.fr", fetched?.emails?.firstOrNull()?.email?.text)
    }

    @Test
    fun updateClient_addsAddress() = runTest {
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Client Sans Adresse"),
            type = ClientOrIssuerType.CLIENT
        ))
        val client = dataSource.getClients().first()

        val newAddress = AddressState(
            addressLine1 = TextFieldValue("Nouvelle Adresse")
        )
        dataSource.addAddressToClient(client.id!!, newAddress)

        val fetched = dataSource.fetchClientOrIssuer(client.id!!.toLong())
        assertEquals(1, fetched?.addresses?.size)
    }

    @Test
    fun updateClient_removesAddress() = runTest {
        val address = AddressState(addressLine1 = TextFieldValue("A supprimer"))
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Client"),
            type = ClientOrIssuerType.CLIENT,
            addresses = listOf(address)
        ))
        val client = dataSource.getClients().first()

        val updated = client.copy(addresses = emptyList())
        dataSource.updateClientOrIssuer(updated)

        val fetched = dataSource.fetchClientOrIssuer(client.id!!.toLong())
        assertTrue(fetched?.addresses?.isEmpty() ?: true)
    }

    // ============= DELETE TESTS =============

    @Test
    fun deleteClient_removesFromList() = runTest {
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("À Supprimer"),
            type = ClientOrIssuerType.CLIENT
        ))
        val client = dataSource.getClients().first()

        dataSource.deleteClientOrIssuer(client)

        assertEquals(0, dataSource.getClients().size)
    }

    @Test
    fun deleteClient_leavesOthersIntact() = runTest {
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Client 1"),
            type = ClientOrIssuerType.CLIENT
        ))
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Client 2"),
            type = ClientOrIssuerType.CLIENT
        ))
        val toDelete = dataSource.getClients().first()

        dataSource.deleteClientOrIssuer(toDelete)

        assertEquals(1, dataSource.getClients().size)
        assertEquals("Client 2", dataSource.getClients().first().name.text)
    }

    // ============= DUPLICATE TESTS =============

    @Test
    fun duplicateClient_createsNewWithSuffix() = runTest {
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Original"),
            type = ClientOrIssuerType.CLIENT
        ))

        val toDuplicate = dataSource.getClients()
        dataSource.duplicateClients(toDuplicate)

        assertEquals(2, dataSource.getClients().size)
        val names = dataSource.getClients().map { it.name.text }
        assertTrue("Original" in names)
        assertTrue("Original (copie)" in names)
    }

    @Test
    fun duplicateMultipleClients() = runTest {
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Client A"),
            type = ClientOrIssuerType.CLIENT
        ))
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Client B"),
            type = ClientOrIssuerType.CLIENT
        ))

        val toDuplicate = dataSource.getClients()
        dataSource.duplicateClients(toDuplicate)

        assertEquals(4, dataSource.getClients().size)
    }

    @Test
    fun duplicateClient_preservesAddresses() = runTest {
        val address = AddressState(
            addressLine1 = TextFieldValue("Adresse Originale"),
            city = TextFieldValue("Lyon")
        )
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Avec Adresse"),
            type = ClientOrIssuerType.CLIENT,
            addresses = listOf(address)
        ))

        val toDuplicate = dataSource.getClients()
        dataSource.duplicateClients(toDuplicate)

        val duplicate = dataSource.getClients().find { it.name.text == "Avec Adresse (copie)" }
        assertNotNull(duplicate)
        assertEquals("Adresse Originale", duplicate.addresses?.first()?.addressLine1?.text)
    }

    // ============= LAST CREATED ID TESTS =============

    @Test
    fun getLastCreatedClientId_returnsCorrectId() = runTest {
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Premier"),
            type = ClientOrIssuerType.CLIENT
        ))
        dataSource.createNew(ClientOrIssuerState(
            name = TextFieldValue("Deuxième"),
            type = ClientOrIssuerType.CLIENT
        ))

        val lastId = dataSource.getLastCreatedClientId()

        assertEquals(2L, lastId)
    }

    @Test
    fun getLastCreatedClientId_nullWhenEmpty() = runTest {
        val lastId = dataSource.getLastCreatedClientId()
        assertNull(lastId)
    }
}
