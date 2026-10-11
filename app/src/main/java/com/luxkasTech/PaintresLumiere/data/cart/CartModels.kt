package com.luxkasTech.PaintresLumiere.data.cart

import com.luxkasTech.PaintresLumiere.core.network.BigDecimalSerializer
import com.luxkasTech.PaintresLumiere.data.product.ProductColor
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * One cart line. [productId] is the **variant row** id (a SKU in one color and dimension), never
 * the SKU — the variant is what carries its own stock. Prices are a snapshot for display only;
 * the API will always recompute them (the order freezes the price at checkout).
 */
@Serializable
data class CartItem(
    val id: String,
    val productId: String,
    val name: String,
    val slug: String,
    val color: ProductColor,
    val sizeLabel: String,
    val imageUrl: String? = null,
    @Serializable(with = BigDecimalSerializer::class) val unitPrice: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) val finalUnitPrice: BigDecimal,
    val stockQuantity: Int,
    val status: ProductStatus,
    val quantity: Int,
) {
    val hasDiscount: Boolean get() = finalUnitPrice < unitPrice
    val lineSubtotal: BigDecimal get() = unitPrice * quantity.toBigDecimal()
    val lineTotal: BigDecimal get() = finalUnitPrice * quantity.toBigDecimal()

    /** The line can't be checked out as is. */
    val isUnavailable: Boolean get() = !status.isPurchasable
    val exceedsStock: Boolean get() = quantity > stockQuantity
}

@Serializable
data class Cart(val items: List<CartItem> = emptyList()) {
    val itemCount: Int get() = items.sumOf { it.quantity }
    val subtotal: BigDecimal get() = items.fold(BigDecimal.ZERO) { acc, it -> acc + it.lineSubtotal }
    val total: BigDecimal get() = items.fold(BigDecimal.ZERO) { acc, it -> acc + it.lineTotal }
    val discountTotal: BigDecimal get() = subtotal - total
    val isEmpty: Boolean get() = items.isEmpty()
    val canCheckout: Boolean get() = items.isNotEmpty() && items.none { it.isUnavailable || it.exceedsStock }
}

/** Business-rule failures, mirroring the 4xx the cart endpoints will return. */
sealed class CartError(message: String) : Exception(message) {
    /** The requested total quantity exceeds the variant's stock; [available] is what can be added. */
    class InsufficientStock(val available: Int) : CartError("Only $available available")
    class Unavailable : CartError("Product is not available")
    class ItemNotFound : CartError("Cart item not found")
}
