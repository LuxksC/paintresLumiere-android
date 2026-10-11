package com.luxkasTech.PaintresLumiere.feature.orders.detail

import com.luxkasTech.PaintresLumiere.data.order.OrderStatus
import com.luxkasTech.PaintresLumiere.data.payment.PaymentMethod
import com.luxkasTech.PaintresLumiere.data.payment.PaymentStatus
import com.luxkasTech.PaintresLumiere.testing.testOrderDetail
import com.luxkasTech.PaintresLumiere.testing.testPayment
import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentActionTest {

    private fun order(vararg payments: com.luxkasTech.PaintresLumiere.data.payment.Payment, status: OrderStatus = OrderStatus.RECEIVED) =
        testOrderDetail("o1", status).copy(payments = payments.toList())

    @Test
    fun `no charge yet means choosing a method`() {
        assertEquals(PaymentAction.ChooseMethod, paymentActionFor(order()))
    }

    @Test
    fun `a pending pix or carne is resumed, never recreated`() {
        val pix = testPayment(PaymentMethod.PIX)
        assertEquals(PaymentAction.Resume(pix), paymentActionFor(order(pix)))
        val boleto = testPayment(PaymentMethod.BOLETO_INSTALLMENTS)
        assertEquals(PaymentAction.Resume(boleto), paymentActionFor(order(boleto)))
    }

    @Test
    fun `overdue boleto or declined card offers another method`() {
        assertEquals(PaymentAction.ChooseMethod, paymentActionFor(order(testPayment(PaymentMethod.BOLETO_INSTALLMENTS, PaymentStatus.OVERDUE))))
        assertEquals(PaymentAction.ChooseMethod, paymentActionFor(order(testPayment(PaymentMethod.CREDIT_CARD, PaymentStatus.FAILED))))
    }

    @Test
    fun `paid, cancelled or delivered orders offer nothing`() {
        assertEquals(PaymentAction.None, paymentActionFor(order(testPayment(PaymentMethod.PIX, PaymentStatus.CONFIRMED))))
        assertEquals(PaymentAction.None, paymentActionFor(order(status = OrderStatus.CANCELLED)))
        assertEquals(
            PaymentAction.None,
            paymentActionFor(order(testPayment(PaymentMethod.PIX, PaymentStatus.FAILED), testPayment(PaymentMethod.PIX, PaymentStatus.RECEIVED))),
        )
    }
}
