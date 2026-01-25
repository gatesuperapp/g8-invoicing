package com.a4a.g8invoicing

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.fakes.FakeProductDataSource
import com.a4a.g8invoicing.ui.states.ClientRef
import com.a4a.g8invoicing.ui.states.ProductPrice
import com.a4a.g8invoicing.ui.states.ProductState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProductDataSourceTest {

    private lateinit var dataSource: FakeProductDataSource

    @BeforeTest
    fun setup() {
        dataSource = FakeProductDataSource()
    }

    // ============= CREATION TESTS =============

    @Test
    fun createProduct_savesCorrectly() = runTest {
        val product = ProductState(
            name = TextFieldValue("Produit Test"),
            defaultPriceWithoutTax = BigDecimal.fromDouble(100.0),
            taxRate = BigDecimal.fromDouble(20.0)
        )

        dataSource.saveProduct(product)

        assertEquals(1, dataSource.getProductCount())
        val saved = dataSource.getProducts().first()
        assertEquals("Produit Test", saved.name.text)
        assertNotNull(saved.id)
    }

    @Test
    fun createProduct_withAllFields() = runTest {
        val product = ProductState(
            name = TextFieldValue("Produit Complet"),
            description = TextFieldValue("Description détaillée"),
            defaultPriceWithoutTax = BigDecimal.fromDouble(50.0),
            defaultPriceWithTax = BigDecimal.fromDouble(60.0),
            taxRate = BigDecimal.fromDouble(20.0),
            unit = TextFieldValue("pièce")
        )

        dataSource.saveProduct(product)

        val saved = dataSource.getProducts().first()
        assertEquals("Produit Complet", saved.name.text)
        assertEquals("Description détaillée", saved.description?.text)
        assertEquals("pièce", saved.unit?.text)
    }

    @Test
    fun createMultipleProducts_assignsUniqueIds() = runTest {
        val product1 = ProductState(name = TextFieldValue("Produit 1"))
        val product2 = ProductState(name = TextFieldValue("Produit 2"))
        val product3 = ProductState(name = TextFieldValue("Produit 3"))

        dataSource.saveProduct(product1)
        dataSource.saveProduct(product2)
        dataSource.saveProduct(product3)

        assertEquals(3, dataSource.getProductCount())
        val ids = dataSource.getProducts().map { it.id }
        assertEquals(ids.distinct().size, ids.size) // All IDs unique
    }

    // ============= FETCH TESTS =============

    @Test
    fun fetchProduct_existingProduct() = runTest {
        val product = ProductState(name = TextFieldValue("Mon Produit"))
        dataSource.saveProduct(product)

        val fetched = dataSource.fetchProduct(1L)

        assertNotNull(fetched)
        assertEquals("Mon Produit", fetched.name.text)
    }

    @Test
    fun fetchProduct_nonExistent() = runTest {
        val fetched = dataSource.fetchProduct(999L)
        assertNull(fetched)
    }

    @Test
    fun fetchAllProducts_emptyList() = runTest {
        val products = dataSource.fetchAllProducts().first()
        assertTrue(products.isEmpty())
    }

    @Test
    fun fetchAllProducts_multipleProducts() = runTest {
        dataSource.saveProduct(ProductState(name = TextFieldValue("Produit A")))
        dataSource.saveProduct(ProductState(name = TextFieldValue("Produit B")))

        val products = dataSource.fetchAllProducts().first()

        assertEquals(2, products.size)
    }

    // ============= UPDATE TESTS =============

    @Test
    fun updateProduct_changesName() = runTest {
        dataSource.saveProduct(ProductState(name = TextFieldValue("Ancien Nom")))
        val product = dataSource.getProducts().first()

        val updated = product.copy(name = TextFieldValue("Nouveau Nom"))
        dataSource.updateProduct(updated)

        val fetched = dataSource.fetchProduct(product.id!!.toLong())
        assertEquals("Nouveau Nom", fetched?.name?.text)
    }

    @Test
    fun updateProduct_changesPrice() = runTest {
        dataSource.saveProduct(
            ProductState(
                name = TextFieldValue("Produit"),
                defaultPriceWithoutTax = BigDecimal.fromDouble(100.0)
            )
        )
        val product = dataSource.getProducts().first()

        val updated = product.copy(defaultPriceWithoutTax = BigDecimal.fromDouble(150.0))
        dataSource.updateProduct(updated)

        val fetched = dataSource.fetchProduct(product.id!!.toLong())
        assertEquals(BigDecimal.fromDouble(150.0), fetched?.defaultPriceWithoutTax)
    }

    @Test
    fun updateProduct_nonExistent_noChange() = runTest {
        val nonExistent = ProductState(id = 999, name = TextFieldValue("Ghost"))

        dataSource.updateProduct(nonExistent)

        assertEquals(0, dataSource.getProductCount())
    }

    // ============= DELETE TESTS =============

    @Test
    fun deleteProduct_removesFromList() = runTest {
        dataSource.saveProduct(ProductState(name = TextFieldValue("À Supprimer")))
        assertEquals(1, dataSource.getProductCount())

        dataSource.deleteProduct(1L)

        assertEquals(0, dataSource.getProductCount())
    }

    @Test
    fun deleteProduct_nonExistent_noError() = runTest {
        dataSource.saveProduct(ProductState(name = TextFieldValue("Reste")))

        dataSource.deleteProduct(999L)

        assertEquals(1, dataSource.getProductCount())
    }

    @Test
    fun deleteProduct_leavesOthersIntact() = runTest {
        dataSource.saveProduct(ProductState(name = TextFieldValue("Produit 1")))
        dataSource.saveProduct(ProductState(name = TextFieldValue("Produit 2")))
        dataSource.saveProduct(ProductState(name = TextFieldValue("Produit 3")))

        dataSource.deleteProduct(2L)

        assertEquals(2, dataSource.getProductCount())
        val names = dataSource.getProducts().map { it.name.text }
        assertTrue("Produit 1" in names)
        assertTrue("Produit 3" in names)
    }

    // ============= DUPLICATE TESTS =============

    @Test
    fun duplicateProduct_createsNewWithSuffix() = runTest {
        dataSource.saveProduct(ProductState(name = TextFieldValue("Original")))
        val original = dataSource.getProducts().first()

        dataSource.duplicateProducts(listOf(original), " (copie)")

        assertEquals(2, dataSource.getProductCount())
        val names = dataSource.getProducts().map { it.name.text }
        assertTrue("Original" in names)
        assertTrue("Original (copie)" in names)
    }

    @Test
    fun duplicateMultipleProducts() = runTest {
        dataSource.saveProduct(ProductState(name = TextFieldValue("Produit A")))
        dataSource.saveProduct(ProductState(name = TextFieldValue("Produit B")))

        val toDuplicate = dataSource.getProducts()
        dataSource.duplicateProducts(toDuplicate, " (copie)")

        assertEquals(4, dataSource.getProductCount())
    }

    @Test
    fun duplicateProduct_preservesPrice() = runTest {
        dataSource.saveProduct(
            ProductState(
                name = TextFieldValue("Avec Prix"),
                defaultPriceWithoutTax = BigDecimal.fromDouble(99.99)
            )
        )

        val original = dataSource.getProducts().first()
        dataSource.duplicateProducts(listOf(original), " (copie)")

        val duplicate = dataSource.getProducts().find { it.name.text == "Avec Prix (copie)" }
        assertNotNull(duplicate)
        assertEquals(BigDecimal.fromDouble(99.99), duplicate.defaultPriceWithoutTax)
    }

    // ============= ADDITIONAL PRICES TESTS =============

    @Test
    fun addAdditionalPrice_toProduct() = runTest {
        dataSource.saveProduct(ProductState(name = TextFieldValue("Produit Multi-Prix")))
        val product = dataSource.getProducts().first()

        val additionalPrice = ProductPrice(
            priceWithoutTax = BigDecimal.fromDouble(80.0),
            priceWithTax = BigDecimal.fromDouble(96.0),
            clients = listOf(ClientRef(id = 1, name = "Client Spécial"))
        )
        dataSource.addAdditionalPriceToProduct(product.id!!, additionalPrice)

        val updated = dataSource.fetchProduct(product.id!!.toLong())
        assertNotNull(updated?.additionalPrices)
        assertEquals(1, updated?.additionalPrices?.size)
        assertEquals("Client Spécial", updated?.additionalPrices?.first()?.clients?.first()?.name)
    }

    @Test
    fun deleteAdditionalPrice_fromProduct() = runTest {
        dataSource.saveProduct(ProductState(name = TextFieldValue("Produit")))
        val product = dataSource.getProducts().first()

        val price = ProductPrice(priceWithoutTax = BigDecimal.fromDouble(50.0))
        dataSource.addAdditionalPriceToProduct(product.id!!, price)

        dataSource.deleteAdditionalPrice(product.id!!.toLong(), BigDecimal.fromDouble(50.0))

        val updated = dataSource.fetchProduct(product.id!!.toLong())
        assertTrue(updated?.additionalPrices?.isEmpty() ?: true)
    }

    @Test
    fun removeClientFromAdditionalPrice() = runTest {
        dataSource.saveProduct(ProductState(name = TextFieldValue("Produit")))
        val product = dataSource.getProducts().first()

        val price = ProductPrice(
            priceWithoutTax = BigDecimal.fromDouble(75.0),
            clients = listOf(
                ClientRef(id = 1, name = "Client 1"),
                ClientRef(id = 2, name = "Client 2")
            )
        )
        dataSource.addAdditionalPriceToProduct(product.id!!, price)

        dataSource.removeClientFromAdditionalPrice(product.id!!.toLong(), 1L)

        val updated = dataSource.fetchProduct(product.id!!.toLong())
        val clients = updated?.additionalPrices?.first()?.clients
        assertEquals(1, clients?.size)
        assertEquals("Client 2", clients?.first()?.name)
    }

    // ============= FLOW TESTS =============

    @Test
    fun flowUpdates_onSave() = runTest {
        val initialProducts = dataSource.fetchAllProducts().first()
        assertEquals(0, initialProducts.size)

        dataSource.saveProduct(ProductState(name = TextFieldValue("Nouveau")))

        val updatedProducts = dataSource.fetchAllProducts().first()
        assertEquals(1, updatedProducts.size)
    }

    @Test
    fun flowUpdates_onDelete() = runTest {
        dataSource.saveProduct(ProductState(name = TextFieldValue("Temporaire")))

        val beforeDelete = dataSource.fetchAllProducts().first()
        assertEquals(1, beforeDelete.size)

        dataSource.deleteProduct(1L)

        val afterDelete = dataSource.fetchAllProducts().first()
        assertEquals(0, afterDelete.size)
    }
}
