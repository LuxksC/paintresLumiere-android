package com.luxkasTech.PaintresLumiere.data.order

import androidx.annotation.StringRes
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.network.BigDecimalSerializer
import com.luxkasTech.PaintresLumiere.core.network.InstantSerializer
import com.luxkasTech.PaintresLumiere.core.network.UnknownTolerantEnumSerializer
import com.luxkasTech.PaintresLumiere.data.payment.Payment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.Instant

/**
 * Order lifecycle from backend card PL-117: recebido → em_producao → pronto → enviado → entregue,
 * plus cancelado. Tolerant to unknown values like every API enum.
 */
@Serializable(with = OrderStatus.Serializer::class)
enum class OrderStatus(val raw: String, @StringRes val labelRes: Int, val step: Int) {
    RECEIVED("recebido", R.string.order_status_received, 0),
    IN_PRODUCTION("em_producao", R.string.order_status_in_production, 1),
    READY("pronto", R.string.order_status_ready, 2),
    SHIPPED("enviado", R.string.order_status_shipped, 3),
    DELIVERED("entregue", R.string.order_status_delivered, 4),
    CANCELLED("cancelado", R.string.order_status_cancelled, -1),
    UNKNOWN("", R.string.order_status_unknown, -1);

    object Serializer : UnknownTolerantEnumSerializer<OrderStatus>("OrderStatus", entries, UNKNOWN, { it.raw })

    companion object {
        /** The happy-path steps shown on the tracking timeline, in order. */
        val timeline: List<OrderStatus> = listOf(RECEIVED, IN_PRODUCTION, READY, SHIPPED, DELIVERED)
    }
}

/** Row of `GET /orders` (lean, for the list). */
@Serializable
data class OrderSummary(
    val id: String,
    val number: String? = null,
    val status: OrderStatus = OrderStatus.UNKNOWN,
    @SerialName("total_amount") @Serializable(with = BigDecimalSerializer::class) val totalAmount: BigDecimal,
    @SerialName("created_at") @Serializable(with = InstantSerializer::class) val createdAt: Instant,
    @SerialName("item_count") val itemCount: Int = 0,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
) {
    val displayNumber: String get() = number ?: id.takeLast(6).uppercase()
}

/**
 * Line of an order. Everything here is the **snapshot frozen at checkout** — never re-read from
 * the catalog, which may have changed or removed the product since.
 */
@Serializable
data class OrderItem(
    val id: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("product_name") val productName: String,
    @SerialName("product_sku") val productSku: String? = null,
    @SerialName("product_color") val productColor: String? = null,
    @SerialName("product_dimensions") val productDimensions: String? = null,
    @SerialName("unit_price") @Serializable(with = BigDecimalSerializer::class) val unitPrice: BigDecimal,
    @SerialName("discount_rate") @Serializable(with = BigDecimalSerializer::class) val discountRate: BigDecimal = BigDecimal.ZERO,
    val quantity: Int,
    @SerialName("line_total") @Serializable(with = BigDecimalSerializer::class) val lineTotal: BigDecimal,
)

@Serializable
data class OrderStatusEntry(
    val status: OrderStatus = OrderStatus.UNKNOWN,
    @SerialName("created_at") @Serializable(with = InstantSerializer::class) val createdAt: Instant,
    val note: String? = null,
)

/** `GET /orders/{id}` and the response of `POST /orders`. */
@Serializable
data class OrderDetail(
    val id: String,
    val number: String? = null,
    val status: OrderStatus = OrderStatus.UNKNOWN,
    @SerialName("subtotal_amount") @Serializable(with = BigDecimalSerializer::class) val subtotalAmount: BigDecimal,
    @SerialName("discount_amount") @Serializable(with = BigDecimalSerializer::class) val discountAmount: BigDecimal = BigDecimal.ZERO,
    @SerialName("shipping_amount") @Serializable(with = BigDecimalSerializer::class) val shippingAmount: BigDecimal = BigDecimal.ZERO,
    @SerialName("total_amount") @Serializable(with = BigDecimalSerializer::class) val totalAmount: BigDecimal,
    val notes: String? = null,
    @SerialName("created_at") @Serializable(with = InstantSerializer::class) val createdAt: Instant,
    val items: List<OrderItem> = emptyList(),
    @SerialName("status_history") val statusHistory: List<OrderStatusEntry> = emptyList(),
    /** Every charge attempt, oldest first (backend card PL-125: "include payment data"). */
    val payments: List<Payment> = emptyList(),
) {
    val displayNumber: String get() = number ?: id.takeLast(6).uppercase()
    val latestPayment: Payment? get() = payments.lastOrNull()
    val isPaid: Boolean get() = payments.any { it.status.isPaid }
    /** Payable when nothing has been paid and the order wasn't cancelled or delivered. */
    val canPay: Boolean get() = !isPaid && status != OrderStatus.CANCELLED && status != OrderStatus.DELIVERED
}

@Serializable
data class CreateOrderRequest(val notes: String? = null)
