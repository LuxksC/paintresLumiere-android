package com.luxkasTech.PaintresLumiere.data.payment

import com.luxkasTech.PaintresLumiere.core.network.apiCall
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton

/** Backend cards PL-145/146/147 — not live yet; the `mock` flavor serves the proposed payloads. */
interface PaymentApi {
    @POST("orders/{orderId}/payment")
    suspend fun createPayment(@Path("orderId") orderId: String, @Body body: CreatePaymentRequest): Payment
}

interface PaymentRepository {
    suspend fun createPayment(orderId: String, method: PaymentMethod, installmentCount: Int? = null, cardToken: String? = null): Payment
}

@Singleton
class DefaultPaymentRepository @Inject constructor(
    private val api: PaymentApi,
) : PaymentRepository {
    override suspend fun createPayment(orderId: String, method: PaymentMethod, installmentCount: Int?, cardToken: String?) =
        apiCall { api.createPayment(orderId, CreatePaymentRequest(method, installmentCount, cardToken)) }
}
