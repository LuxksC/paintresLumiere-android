package com.luxkasTech.PaintresLumiere.data.cart

import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import com.luxkasTech.PaintresLumiere.data.product.ProductColor
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import com.luxkasTech.PaintresLumiere.testing.MemoryTokenStorage
import com.luxkasTech.PaintresLumiere.testing.testCartItem
import com.luxkasTech.PaintresLumiere.testing.testVariant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CartPersistenceAndConcurrencyTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Test
    fun `a cart survives the JSON round trip used by storage, prices included`() {
        val cart = Cart(
            listOf(
                testCartItem("1", quantity = 3).copy(unitPrice = BigDecimal("19.90"), finalUnitPrice = BigDecimal("17.91")),
                testCartItem("2").copy(color = ProductColor.UNKNOWN, status = ProductStatus.INACTIVE),
            ),
        )
        val restored = json.decodeFromString(Cart.serializer(), json.encodeToString(Cart.serializer(), cart))
        assertEquals(cart, restored)
        assertEquals(BigDecimal("53.73"), restored.items.first().lineTotal)
    }

    @Test
    fun `concurrent adds of the same variant are serialized and summed`() = runBlocking {
        val storage = object : CartStorage {
            var saved = Cart()
            override fun load() = saved
            override fun save(cart: Cart) { saved = cart }
        }
        val repo = LocalCartRepository(storage, SessionManager(MemoryTokenStorage("jwt")), CoroutineScope(Dispatchers.Unconfined))
        val variant = testVariant("v1", stock = 100)
        (1..20).map { async(Dispatchers.Default) { repo.addItem(variant, 1) } }.awaitAll()
        assertEquals(1, repo.cart.value.items.size)
        assertEquals(20, repo.cart.value.items.single().quantity)
        assertEquals(20, storage.saved.itemCount)
    }
}
