package com.luxkasTech.PaintresLumiere.testing

import com.luxkasTech.PaintresLumiere.core.network.Page
import com.luxkasTech.PaintresLumiere.data.order.OrderDetail
import com.luxkasTech.PaintresLumiere.data.order.OrderRepository
import com.luxkasTech.PaintresLumiere.data.order.OrderStatus
import com.luxkasTech.PaintresLumiere.data.order.OrderSummary
import java.math.BigDecimal
import java.time.Instant

class FakeOrderRepository : OrderRepository {
    /** Pages by cursor; `null` is the first page. */
    var pages: Map<String?, Result<Page<OrderSummary>>> = mapOf(null to Result.success(Page()))
    var details: Map<String, Result<OrderDetail>> = emptyMap()
    var createResult: Result<OrderDetail> = Result.success(testOrderDetail("new"))
    val createdNotes = mutableListOf<String?>()
    val pageRequests = mutableListOf<String?>()

    override suspend fun getOrders(cursor: String?): Page<OrderSummary> {
        pageRequests += cursor
        return pages.getValue(cursor).getOrThrow()
    }

    override suspend fun getOrder(id: String): OrderDetail = details.getValue(id).getOrThrow()

    override suspend fun createOrder(notes: String?): OrderDetail {
        createdNotes += notes
        return createResult.getOrThrow()
    }
}

fun testOrderSummary(id: String, status: OrderStatus = OrderStatus.RECEIVED) =
    OrderSummary(id, "PL-$id", status, BigDecimal("100.00"), Instant.parse("2026-10-01T10:00:00Z"), 2)

fun testOrderDetail(id: String, status: OrderStatus = OrderStatus.RECEIVED, history: List<OrderStatus> = listOf(OrderStatus.RECEIVED)) =
    OrderDetail(
        id = id,
        number = "PL-$id",
        status = status,
        subtotalAmount = BigDecimal("100.00"),
        totalAmount = BigDecimal("100.00"),
        createdAt = Instant.parse("2026-10-01T10:00:00Z"),
        statusHistory = history.mapIndexed { i, s ->
            com.luxkasTech.PaintresLumiere.data.order.OrderStatusEntry(s, Instant.parse("2026-10-0${i + 1}T10:00:00Z"))
        },
    )
