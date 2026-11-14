package com.luxkasTech.PaintresLumiere.feature.payment

import androidx.lifecycle.SavedStateHandle
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.data.order.OrderStatus
import com.luxkasTech.PaintresLumiere.data.payment.PaymentMethod
import com.luxkasTech.PaintresLumiere.data.payment.PaymentStatus
import com.luxkasTech.PaintresLumiere.testing.FakeOrderRepository
import com.luxkasTech.PaintresLumiere.testing.FakePaymentRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import com.luxkasTech.PaintresLumiere.testing.testOrderDetail
import com.luxkasTech.PaintresLumiere.testing.testPayment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PaymentMethodViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val orders = FakeOrderRepository().apply {
        details = mapOf("o1" to Result.success(testOrderDetail("o1")))
    }
    private val payments = FakePaymentRepository()
    private fun vm() = PaymentMethodViewModel(SavedStateHandle(mapOf("orderId" to "o1")), orders, payments)

    @Test
    fun `pix creates a charge without installments`() = runTest {
        val vm = vm()
        vm.confirm()
        assertEquals(FakePaymentRepository.Call("o1", PaymentMethod.PIX, null, null), payments.calls.single())
        assertTrue(vm.events.first() is PaymentMethodViewModel.Event.ChargeCreated)
    }

    @Test
    fun `boleto sends the chosen installments`() {
        val vm = vm()
        vm.selectMethod(PaymentMethod.BOLETO_INSTALLMENTS)
        vm.selectInstallments(3) // total 100 → up to 3× of R$ 33,33
        vm.confirm()
        assertEquals(3, payments.calls.single().installments)
    }

    @Test
    fun `installments are clamped to what the total allows`() {
        val vm = vm()
        vm.selectMethod(PaymentMethod.BOLETO_INSTALLMENTS)
        vm.selectInstallments(12)
        assertEquals(3, vm.state.value.installments)
    }

    @Test
    fun `card goes to the card form instead of charging`() = runTest {
        val vm = vm()
        vm.selectMethod(PaymentMethod.CREDIT_CARD)
        vm.confirm()
        assertTrue(payments.calls.isEmpty())
        assertEquals(PaymentMethodViewModel.Event.CardRequested("o1", 1), vm.events.first())
    }

    @Test
    fun `a missing document error is shown as the server says it`() {
        payments.failure = ApiError.Http(400, "CPF ou CNPJ obrigatório para pagar.")
        val vm = vm()
        vm.confirm()
        assertEquals(UiText.Raw("CPF ou CNPJ obrigatório para pagar."), vm.state.value.submitError)
    }

    @Test
    fun `a paid order cannot be charged again`() {
        orders.details = mapOf("o1" to Result.success(testOrderDetail("o1").copy(payments = listOf(testPayment(PaymentMethod.PIX, PaymentStatus.CONFIRMED)))))
        val vm = vm()
        vm.confirm()
        assertTrue(payments.calls.isEmpty())
    }

    @Test
    fun `a cancelled order cannot be paid`() {
        orders.details = mapOf("o1" to Result.success(testOrderDetail("o1", OrderStatus.CANCELLED)))
        assertTrue(vm().state.value.order?.canPay == false)
    }
}
