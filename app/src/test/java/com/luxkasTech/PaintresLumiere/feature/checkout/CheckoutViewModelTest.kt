package com.luxkasTech.PaintresLumiere.feature.checkout

import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.data.cart.Cart
import com.luxkasTech.PaintresLumiere.testing.FakeCartRepository
import com.luxkasTech.PaintresLumiere.testing.FakeOrderRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import com.luxkasTech.PaintresLumiere.testing.testCartItem
import com.luxkasTech.PaintresLumiere.testing.testOrderDetail
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CheckoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cart = FakeCartRepository(Cart(listOf(testCartItem("1", quantity = 2))))
    private val orders = FakeOrderRepository()
    private val vm by lazy { CheckoutViewModel(cart, orders) }

    @Test
    fun `placing an order sends only the notes, clears the cart and reports the order`() = runTest {
        vm.onNotesChange("  entregar no centro  ")
        vm.placeOrder()
        assertEquals(listOf<String?>("entregar no centro"), orders.createdNotes)
        assertEquals(1, cart.clearCalls)
        assertEquals(CheckoutViewModel.Event.OrderPlaced(testOrderDetail("new")), vm.events.first())
    }

    @Test
    fun `a double tap creates a single order`() {
        val pending = CompletableDeferred<Unit>()
        val slowOrders = object : com.luxkasTech.PaintresLumiere.data.order.OrderRepository by orders {
            override suspend fun createOrder(notes: String?) = pending.await().let { orders.createOrder(notes) }
        }
        val vm = CheckoutViewModel(cart, slowOrders)
        vm.placeOrder()
        vm.placeOrder()
        pending.complete(Unit)
        assertEquals(1, orders.createdNotes.size)
    }

    @Test
    fun `stock conflict keeps the cart and offers going back`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        orders.createResult = Result.failure(ApiError.Conflict("Luna sem estoque"))
        vm.placeOrder()
        val state = vm.state.value
        assertTrue(state.isStockConflict)
        assertEquals(UiText.Raw("Luna sem estoque"), state.error)
        assertFalse(state.isSubmitting)
        assertEquals(0, cart.clearCalls)
    }

    @Test
    fun `an empty or invalid cart cannot be submitted`() {
        cart.set(Cart())
        vm.placeOrder()
        assertTrue(orders.createdNotes.isEmpty())
        assertFalse(CheckoutUiState(cart = Cart()).canSubmit)
    }
}
