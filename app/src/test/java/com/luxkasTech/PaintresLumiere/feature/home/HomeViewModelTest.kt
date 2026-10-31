package com.luxkasTech.PaintresLumiere.feature.home

import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.testing.FakeMessagesService
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

    private fun viewModel() = HomeViewModel(repository, messages)

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
    fun `cart and notifications are coming soon`() {
        val viewModel = viewModel()
        viewModel.onCartClick()
        viewModel.onNotificationsClick()
        assertEquals(2, messages.shown.size)
    }
}
