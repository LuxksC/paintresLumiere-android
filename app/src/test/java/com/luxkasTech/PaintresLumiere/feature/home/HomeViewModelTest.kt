package com.luxkasTech.PaintresLumiere.feature.home

import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.data.cart.Cart
import com.luxkasTech.PaintresLumiere.data.product.CategorySummary
import com.luxkasTech.PaintresLumiere.data.product.ProductCategory
import com.luxkasTech.PaintresLumiere.testing.FakeCartRepository
import com.luxkasTech.PaintresLumiere.testing.FakeMessagesService
import com.luxkasTech.PaintresLumiere.testing.testCartItem
import com.luxkasTech.PaintresLumiere.testing.FakeProductRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import com.luxkasTech.PaintresLumiere.testing.testProduct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()
    private val messages = FakeMessagesService()

    private val cart = FakeCartRepository()

    private fun viewModel() = HomeViewModel(repository, cart, messages)

    @Test
    fun `loads catalog sorted by name, ignoring case and accents`() {
        repository.products = Result.success(
            listOf(testProduct("1", "Zebra"), testProduct("2", "árvore"), testProduct("3", "Bola")),
        )
        val state = viewModel().state.value
        assertFalse(state.isLoading)
        assertEquals(listOf("árvore", "Bola", "Zebra"), state.catalog.map { it.name })
    }

    @Test
    fun `popular failure hides the carousel but keeps the catalog`() {
        repository.products = Result.success(listOf(testProduct("1")))
        repository.popular = Result.failure(ApiError.Network(IOException()))
        val state = viewModel().state.value
        assertEquals(1, state.catalog.size)
        assertTrue(state.popular.isEmpty())
        assertFalse(state.showsFullScreenError)
        assertTrue(messages.shown.isEmpty())
    }

    @Test
    fun `catalog failure shows the full screen error and a network toast`() {
        repository.products = Result.failure(ApiError.Network(IOException()))
        val state = viewModel().state.value
        assertTrue(state.showsFullScreenError)
        assertEquals(UiText.Resource(R.string.error_network), state.error)
        assertEquals(1, messages.shown.size)
    }

    @Test
    fun `retry reloads and clears the error`() {
        repository.products = Result.failure(ApiError.Network(IOException()))
        val viewModel = viewModel()
        repository.products = Result.success(listOf(testProduct("1")))
        viewModel.retry()
        val state = viewModel.state.value
        assertFalse(state.showsFullScreenError)
        assertEquals(1, state.catalog.size)
        assertEquals(2, repository.productCalls)
    }

    @Test
    fun `empty catalog is an explicit empty state`() {
        assertTrue(viewModel().state.value.isEmpty)
    }

    @Test
    fun `a failed refresh keeps the products on screen`() {
        repository.products = Result.success(listOf(testProduct("1")))
        val viewModel = viewModel()
        repository.products = Result.failure(ApiError.Network(IOException()))
        viewModel.refresh()
        val state = viewModel.state.value
        assertEquals(1, state.catalog.size)
        assertFalse(state.isRefreshing)
        assertFalse(state.showsFullScreenError)
    }

    @Test
    fun `notifications are coming soon`() {
        viewModel().onNotificationsClick()
        assertEquals(1, messages.shown.size)
    }

    @Test
    fun `cart badge follows the cart`() {
        val viewModel = viewModel()
        assertEquals(0, viewModel.state.value.cartCount)
        cart.set(Cart(listOf(testCartItem("1", quantity = 2), testCartItem("2", quantity = 1))))
        assertEquals(3, viewModel.state.value.cartCount)
    }

    @Test
    fun `categories are loaded when the endpoint answers`() {
        repository.categories = Result.success(listOf(CategorySummary(ProductCategory.NAMES, "Nomes", 3)))
        assertEquals(listOf(ProductCategory.NAMES), viewModel().state.value.categories.map { it.category })
    }

    @Test
    fun `missing categories endpoint falls back silently to the collection`() {
        repository.products = Result.success(listOf(testProduct("1")))
        repository.categories = Result.failure(ApiError.Http(404, null))
        val state = viewModel().state.value
        assertTrue(state.categories.isEmpty())
        assertEquals(1, state.catalog.size)
        assertFalse(state.showsFullScreenError)
        assertTrue(messages.shown.isEmpty())
    }

    @Test
    fun `search filters the loaded catalog by name or SKU ignoring accents`() {
        repository.products = Result.success(listOf(testProduct("1", "Coração"), testProduct("2", "Sol")))
        val viewModel = viewModel()
        viewModel.onQueryChange("coracao")
        assertTrue(viewModel.state.value.isSearching)
        assertEquals(listOf("Coração"), viewModel.state.value.searchResults.map { it.name })
        viewModel.onQueryChange("sku-2")
        assertEquals(listOf("Sol"), viewModel.state.value.searchResults.map { it.name })
        viewModel.onQueryChange(" ")
        assertFalse(viewModel.state.value.isSearching)
    }

    @Test
    fun `a paginated catalog keeps server order and loads the next page`() {
        repository.catalogPages = mapOf(
            null to Result.success(com.luxkasTech.PaintresLumiere.core.network.Page(listOf(testProduct("1", "Zeca"), testProduct("2", "Ana")), nextCursor = "p2")),
            "p2" to Result.success(com.luxkasTech.PaintresLumiere.core.network.Page(listOf(testProduct("3", "Bia")))),
        )
        val viewModel = viewModel()
        assertEquals(listOf("Zeca", "Ana"), viewModel.state.value.catalog.map { it.name })
        viewModel.loadMore()
        assertEquals(listOf("Zeca", "Ana", "Bia"), viewModel.state.value.catalog.map { it.name })
        assertEquals(null, viewModel.state.value.nextCursor)
    }

    @Test
    fun `a failed page keeps the cursor for a retry`() {
        repository.catalogPages = mapOf(
            null to Result.success(com.luxkasTech.PaintresLumiere.core.network.Page(listOf(testProduct("1")), nextCursor = "p2")),
            "p2" to Result.failure(ApiError.Network(IOException())),
        )
        val viewModel = viewModel()
        viewModel.loadMore()
        assertEquals("p2", viewModel.state.value.nextCursor)
        assertFalse(viewModel.state.value.isLoadingMore)
    }
}
