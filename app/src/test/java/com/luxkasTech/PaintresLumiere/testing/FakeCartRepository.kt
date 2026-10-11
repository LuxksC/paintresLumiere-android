package com.luxkasTech.PaintresLumiere.testing

import com.luxkasTech.PaintresLumiere.data.cart.Cart
import com.luxkasTech.PaintresLumiere.data.cart.CartRepository
import com.luxkasTech.PaintresLumiere.data.product.ProductVariant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory double: records calls; set [failure] to make the next mutation throw. */
class FakeCartRepository(initial: Cart = Cart()) : CartRepository {
    private val state = MutableStateFlow(initial)
    override val cart: StateFlow<Cart> = state

    var failure: Exception? = null
    val added = mutableListOf<Pair<ProductVariant, Int>>()
    val quantityUpdates = mutableListOf<Pair<String, Int>>()
    val removed = mutableListOf<String>()
    var clearCalls = 0

    fun set(cart: Cart) {
        state.value = cart
    }

    private fun fail() {
        failure?.let {
            failure = null
            throw it
        }
    }

    override suspend fun refresh() = state.value

    override suspend fun addItem(variant: ProductVariant, quantity: Int): Cart {
        fail()
        added += variant to quantity
        return state.value
    }

    override suspend fun updateQuantity(itemId: String, quantity: Int): Cart {
        fail()
        quantityUpdates += itemId to quantity
        return state.value
    }

    override suspend fun removeItem(itemId: String): Cart {
        fail()
        removed += itemId
        return state.value
    }

    override suspend fun clear(): Cart {
        fail()
        clearCalls++
        state.value = Cart()
        return state.value
    }
}
