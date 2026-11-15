package com.luxkasTech.PaintresLumiere.testing

import com.luxkasTech.PaintresLumiere.data.payment.Payment
import com.luxkasTech.PaintresLumiere.data.payment.PaymentMethod
import com.luxkasTech.PaintresLumiere.data.payment.PaymentRepository
import com.luxkasTech.PaintresLumiere.data.payment.PaymentStatus
import java.math.BigDecimal

class FakePaymentRepository : PaymentRepository {
    data class Call(val orderId: String, val method: PaymentMethod, val installments: Int?, val cardToken: String?)

    val calls = mutableListOf<Call>()
    var failure: Exception? = null

    override suspend fun createPayment(orderId: String, method: PaymentMethod, installmentCount: Int?, cardToken: String?): Payment {
        failure?.let { throw it }
        calls += Call(orderId, method, installmentCount, cardToken)
        return testPayment(method)
    }
}

fun testPayment(method: PaymentMethod, status: PaymentStatus = PaymentStatus.PENDING) =
    Payment(id = "pay-${method.raw}", method = method, status = status, amount = BigDecimal("100.00"), pixCopyPaste = "000201mock")
