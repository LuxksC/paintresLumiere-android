package com.luxkasTech.PaintresLumiere.data.cart

import com.luxkasTech.PaintresLumiere.data.product.ProductVariant
import kotlinx.coroutines.flow.StateFlow

/**
 * The cart — the full surface the API will expose (`GET /cart`, `POST /cart/items`,
 * `PATCH/DELETE /cart/items/{id}`, `DELETE /cart`). It is also the single source of truth for every
 * screen that shows the cart (Home badge, product details, cart screen) through [cart].
 *
 * Today it is [LocalCartRepository]. When the cart endpoints ship, a remote implementation replaces
 * the binding and no screen changes.
 */
interface CartRepository {
    val cart: StateFlow<Cart>

    suspend fun refresh(): Cart

    /** Adds [quantity] of [variant]; adding a variant already in the cart sums the quantities. */
    suspend fun addItem(variant: ProductVariant, quantity: Int): Cart

    suspend fun updateQuantity(itemId: String, quantity: Int): Cart

    suspend fun removeItem(itemId: String): Cart

    suspend fun clear(): Cart
}
