package com.luxkasTech.PaintresLumiere.feature.orders.detail

import androidx.lifecycle.SavedStateHandle
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.data.order.OrderStatus
import com.luxkasTech.PaintresLumiere.testing.FakeOrderRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import com.luxkasTech.PaintresLumiere.testing.testOrderDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class OrderDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeOrderRepository()
    private fun vm() = OrderDetailViewModel(SavedStateHandle(mapOf("orderId" to "42")), repository)

    @Test
    fun `loads the order and derives its timeline`() {
        repository.details = mapOf("42" to Result.success(testOrderDetail("42", OrderStatus.RECEIVED)))
        val state = vm().state.value
        assertEquals("42", state.order?.id)
        assertEquals(StepState.Current, state.timeline.first().state)
    }

    @Test
    fun `refresh picks up a status change`() {
        repository.details = mapOf("42" to Result.success(testOrderDetail("42")))
        val vm = vm()
        repository.details = mapOf(
            "42" to Result.success(testOrderDetail("42", OrderStatus.IN_PRODUCTION, listOf(OrderStatus.RECEIVED, OrderStatus.IN_PRODUCTION))),
        )
        vm.refresh()
        assertEquals(OrderStatus.IN_PRODUCTION, vm.state.value.order?.status)
    }

    @Test
    fun `not found shows the error`() {
        repository.details = mapOf("42" to Result.failure(ApiError.Http(404, "Order not found")))
        assertNotNull(vm().state.value.error)
    }
}
