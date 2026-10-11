package com.luxkasTech.PaintresLumiere.data.order

import com.luxkasTech.PaintresLumiere.core.network.Page
import com.luxkasTech.PaintresLumiere.core.network.apiCall
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orders API, per backend cards PL-122/124/125. None of these routes is live yet: the payloads in
 * `assets/mocks/{GET,POST}/orders` are this app's **contract proposal** and the `mock` flavor serves them.
 */
interface OrderApi {
    @GET("orders")
    suspend fun getOrders(@Query("cursor") cursor: String? = null): Page<OrderSummary>

    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") id: String): OrderDetail

    /** Converts the user's server-side cart into an order. Never sends prices. */
    @POST("orders")
    suspend fun createOrder(@Body body: CreateOrderRequest): OrderDetail
}

interface OrderRepository {
    suspend fun getOrders(cursor: String? = null): Page<OrderSummary>
    suspend fun getOrder(id: String): OrderDetail
    suspend fun createOrder(notes: String?): OrderDetail
}

@Singleton
class DefaultOrderRepository @Inject constructor(
    private val api: OrderApi,
) : OrderRepository {
    override suspend fun getOrders(cursor: String?) = apiCall { api.getOrders(cursor) }
    override suspend fun getOrder(id: String) = apiCall { api.getOrder(id) }
    override suspend fun createOrder(notes: String?) = apiCall { api.createOrder(CreateOrderRequest(notes?.takeIf { it.isNotBlank() })) }
}
