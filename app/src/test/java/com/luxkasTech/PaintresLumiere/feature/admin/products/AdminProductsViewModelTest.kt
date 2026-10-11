package com.luxkasTech.PaintresLumiere.feature.admin.products

import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.testing.FakeProductRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import com.luxkasTech.PaintresLumiere.testing.testProduct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class AdminProductsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()

    @Test
    fun `filters by name or SKU`() {
        repository.products = Result.success(listOf(testProduct("1", "Luna"), testProduct("2", "Sol")))
        val vm = AdminProductsViewModel(repository)
        vm.onQueryChange("sku-2")
        assertEquals(listOf("Sol"), vm.state.value.visibleProducts.map { it.name })
        vm.onQueryChange("lu")
        assertEquals(listOf("Luna"), vm.state.value.visibleProducts.map { it.name })
    }

    @Test
    fun `load failure shows an error and retry recovers`() {
        repository.products = Result.failure(ApiError.Network(IOException()))
        val vm = AdminProductsViewModel(repository)
        assertNotNull(vm.state.value.error)
        repository.products = Result.success(listOf(testProduct("1")))
        vm.load()
        assertEquals(1, vm.state.value.products.size)
    }
}
