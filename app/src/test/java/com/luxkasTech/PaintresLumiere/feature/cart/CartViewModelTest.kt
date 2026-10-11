package com.luxkasTech.PaintresLumiere.feature.cart

import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.ToastType
import com.luxkasTech.PaintresLumiere.data.cart.Cart
import com.luxkasTech.PaintresLumiere.data.cart.CartError
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import com.luxkasTech.PaintresLumiere.testing.FakeCartRepository
import com.luxkasTech.PaintresLumiere.testing.FakeMessagesService
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import com.luxkasTech.PaintresLumiere.testing.testCartItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cart = FakeCartRepository(Cart(listOf(testCartItem("1", quantity = 2))))
    private val messages = FakeMessagesService()
    // Lazy: the ViewModel launches stateIn on creation, which needs the Main dispatcher rule active.
    private val viewModel by lazy { CartViewModel(cart, messages) }

    /** `stateIn(WhileSubscribed)` only follows the cart while someone collects it, like the screen does. */
    private fun TestScope.subscribe() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
    }

    @Test
    fun `state mirrors the shared cart`() = runTest {
        subscribe()
        assertEquals(2, viewModel.state.value.cart.itemCount)
        cart.set(Cart())
        assertTrue(viewModel.state.value.cart.isEmpty)
    }

    @Test
    fun `quantity changes and removals go to the repository`() {
        viewModel.changeQuantity("1", 3)
        viewModel.removeItem("1")
        assertEquals(listOf("1" to 3), cart.quantityUpdates)
        assertEquals(listOf("1"), cart.removed)
    }

    @Test
    fun `insufficient stock is reported with the available amount`() {
        cart.failure = CartError.InsufficientStock(available = 4)
        viewModel.changeQuantity("1", 9)
        val toast = messages.shown.single()
        assertEquals(ToastType.Warning, toast.type)
        assertEquals(UiText.Resource(R.string.cart_error_stock_message, 4), toast.subtitle)
    }

    @Test
    fun `clearing asks for confirmation first`() = runTest {
        subscribe()
        viewModel.requestClear()
        assertTrue(viewModel.state.value.confirmingClear)
        assertEquals(0, cart.clearCalls)
        viewModel.confirmClear()
        assertFalse(viewModel.state.value.confirmingClear)
        assertEquals(1, cart.clearCalls)
    }

    @Test
    fun `checkout is coming soon until orders exist`() {
        viewModel.checkout()
        assertEquals(ToastType.Warning, messages.shown.single().type)
    }

    @Test
    fun `unavailable or over-stock lines block checkout`() {
        assertTrue(Cart(listOf(testCartItem("1"))).canCheckout)
        assertFalse(Cart(listOf(testCartItem("1", status = ProductStatus.INACTIVE))).canCheckout)
        assertFalse(Cart(listOf(testCartItem("1", quantity = 6, stock = 5))).canCheckout)
        assertFalse(Cart().canCheckout)
    }
}
