package com.luxkasTech.PaintresLumiere.feature.category

import androidx.lifecycle.SavedStateHandle
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.data.product.ProductCategory
import com.luxkasTech.PaintresLumiere.testing.FakeProductRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import com.luxkasTech.PaintresLumiere.testing.testProduct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class CategoryProductsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()

    private fun vm(category: String = "names") =
        CategoryProductsViewModel(SavedStateHandle(mapOf("category" to category)), repository)

    @Test
    fun `requests the category from the route and sorts by name`() {
        repository.categoryProducts = Result.success(listOf(testProduct("1", "Zeca"), testProduct("2", "Ana")))
        val state = vm().state.value
        assertEquals(listOf(ProductCategory.NAMES), repository.categoryRequests)
        assertEquals(listOf("Ana", "Zeca"), state.products.map { it.name })
    }

    @Test
    fun `failure then retry`() {
        repository.categoryProducts = Result.failure(ApiError.Network(IOException()))
        val vm = vm()
        assertNotNull(vm.state.value.error)
        repository.categoryProducts = Result.success(listOf(testProduct("1")))
        vm.retry()
        assertEquals(1, vm.state.value.products.size)
    }
}
