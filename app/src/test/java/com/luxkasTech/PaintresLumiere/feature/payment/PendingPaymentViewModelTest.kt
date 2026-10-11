package com.luxkasTech.PaintresLumiere.feature.payment

import androidx.lifecycle.SavedStateHandle
import com.luxkasTech.PaintresLumiere.data.payment.Payment
import com.luxkasTech.PaintresLumiere.data.payment.PaymentMethod
import com.luxkasTech.PaintresLumiere.data.payment.PaymentStatus
import com.luxkasTech.PaintresLumiere.testing.FakeOrderRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import com.luxkasTech.PaintresLumiere.testing.testOrderDetail
import com.luxkasTech.PaintresLumiere.testing.testPayment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PendingPaymentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    private val pending = testPayment(PaymentMethod.PIX)
    private val orders = FakeOrderRepository().apply {
        details = mapOf("o1" to Result.success(testOrderDetail("o1").copy(payments = listOf(pending))))
    }

    private fun vm() = PendingPaymentViewModel(
        SavedStateHandle(mapOf("orderId" to "o1", "payment" to json.encodeToString(Payment.serializer(), pending))),
        orders,
        json,
    )

    @Test
    fun `backoff grows and caps at a minute`() {
        assertEquals(listOf(5_000L, 10_000L, 20_000L, 40_000L, 60_000L, 60_000L), (0..5).map(::pollDelayMillis))
    }

    @Test
    fun `decodes the charge from the route`() {
        assertEquals("000201mock", vm().state.value.payment.pixCopyPaste)
    }

    @Test
    fun `polling detects the confirmation and stops`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = vm()
        vm.startPolling()
        advanceTimeBy(5_001)
        assertFalse(vm.state.value.isPaid)

        orders.details = mapOf("o1" to Result.success(testOrderDetail("o1").copy(payments = listOf(pending.copy(status = PaymentStatus.CONFIRMED)))))
        advanceTimeBy(10_001)
        assertTrue(vm.state.value.isPaid)
        assertEquals(PaymentStatus.CONFIRMED, vm.state.value.payment.status)
        vm.stopPolling()
    }

    @Test
    fun `stopping halts the checks`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = vm()
        vm.startPolling()
        vm.stopPolling()
        orders.details = mapOf("o1" to Result.success(testOrderDetail("o1").copy(payments = listOf(pending.copy(status = PaymentStatus.CONFIRMED)))))
        advanceTimeBy(120_000)
        assertFalse(vm.state.value.isPaid)
    }
}
