package com.luxkasTech.PaintresLumiere.data.cart

import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import com.luxkasTech.PaintresLumiere.testing.MemoryTokenStorage
import com.luxkasTech.PaintresLumiere.testing.testVariant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class LocalCartRepositoryTest {

    private class MemoryCartStorage(var saved: Cart = Cart()) : CartStorage {
        override fun load() = saved
        override fun save(cart: Cart) { saved = cart }
    }

    private val storage = MemoryCartStorage()
    private val session = SessionManager(MemoryTokenStorage("jwt"))

    private fun TestScope.repository() =
        LocalCartRepository(storage, session, kotlinx.coroutines.CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

    @Test
    fun `adding a variant creates a line pointing at the variant id`() = runTest {
        val cart = repository().addItem(testVariant("v1"), 2)
        val line = cart.items.single()
        assertEquals("v1", line.productId)
        assertEquals(2, line.quantity)
        assertEquals(storage.saved, cart)
    }

    @Test
    fun `adding the same variant again sums the quantity`() = runTest {
        val repo = repository()
        repo.addItem(testVariant("v1"), 2)
        val cart = repo.addItem(testVariant("v1"), 1)
        assertEquals(1, cart.items.size)
        assertEquals(3, cart.items.single().quantity)
    }

    @Test
    fun `cannot exceed the variant stock and reports what is still available`() = runTest {
        val repo = repository()
        repo.addItem(testVariant("v1", stock = 5), 4)
        try {
            repo.addItem(testVariant("v1", stock = 5), 2)
            fail("Expected InsufficientStock")
        } catch (e: CartError.InsufficientStock) {
            assertEquals(1, e.available)
        }
        assertEquals(4, repo.cart.value.items.single().quantity)
    }

    @Test(expected = CartError.Unavailable::class)
    fun `a non purchasable variant cannot be added`() = runTest {
        repository().addItem(testVariant("v1", status = ProductStatus.OUT_OF_STOCK), 1)
    }

    @Test
    fun `update quantity respects stock and zero removes the line`() = runTest {
        val repo = repository()
        val id = repo.addItem(testVariant("v1", stock = 3), 1).items.single().id
        assertEquals(3, repo.updateQuantity(id, 3).items.single().quantity)
        try {
            repo.updateQuantity(id, 4)
            fail("Expected InsufficientStock")
        } catch (e: CartError.InsufficientStock) {
            assertEquals(3, e.available)
        }
        assertTrue(repo.updateQuantity(id, 0).isEmpty)
    }

    @Test
    fun `totals use discounted prices and expose the discount`() = runTest {
        val repo = repository()
        repo.addItem(testVariant("a", price = "50.00", finalPrice = "40.00"), 2)
        val cart = repo.addItem(testVariant("b", price = "10.00"), 1)
        assertEquals(BigDecimal("110.00"), cart.subtotal)
        assertEquals(BigDecimal("90.00"), cart.total)
        assertEquals(BigDecimal("20.00"), cart.discountTotal)
        assertEquals(3, cart.itemCount)
        assertTrue(cart.canCheckout)
    }

    @Test
    fun `remove and clear`() = runTest {
        val repo = repository()
        val first = repo.addItem(testVariant("a"), 1).items.single().id
        repo.addItem(testVariant("b"), 1)
        assertEquals(listOf("b"), repo.removeItem(first).items.map { it.productId })
        assertTrue(repo.clear().isEmpty)
    }

    @Test
    fun `the cart survives a relaunch through storage`() = runTest {
        repository().addItem(testVariant("v1"), 2)
        assertEquals(2, repository().cart.value.items.single().quantity)
    }

    @Test
    fun `signing out clears the cart`() = runTest {
        val repo = repository()
        repo.addItem(testVariant("v1"), 1)
        session.endSession()
        assertTrue(repo.cart.value.isEmpty)
        assertFalse(storage.saved.items.isNotEmpty())
    }
}
