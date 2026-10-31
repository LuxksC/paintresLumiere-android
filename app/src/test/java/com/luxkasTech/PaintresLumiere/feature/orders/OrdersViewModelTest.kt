package com.luxkasTech.PaintresLumiere.feature.orders

import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.core.network.Page
import com.luxkasTech.PaintresLumiere.testing.FakeOrderRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import com.luxkasTech.PaintresLumiere.testing.testOrderSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class OrdersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeOrderRepository()

    @Test
    fun `loads the first page and then the next one`() {
        repository.pages = mapOf(
            null to Result.success(Page(listOf(testOrderSummary("1")), nextCursor = "c2")),
            "c2" to Result.success(Page(listOf(testOrderSummary("2")), nextCursor = null)),
        )
        val vm = OrdersViewModel(repository)
        assertEquals(listOf("1"), vm.state.value.orders.map { it.id })
        vm.loadMore()
        assertEquals(listOf("1", "2"), vm.state.value.orders.map { it.id })
        assertNull(vm.state.value.nextCursor)
        assertFalse(vm.state.value.canLoadMore)
        vm.loadMore()
        assertEquals(listOf(null, "c2"), repository.pageRequests)
    }

    @Test
    fun `error then retry`() {
        repository.pages = mapOf(null to Result.failure(ApiError.Network(IOException())))
        val vm = OrdersViewModel(repository)
        assertNotNull(vm.state.value.error)
        repository.pages = mapOf(null to Result.success(Page(listOf(testOrderSummary("1")))))
        vm.retry()
        assertEquals(1, vm.state.value.orders.size)
        assertNull(vm.state.value.error)
    }
}
