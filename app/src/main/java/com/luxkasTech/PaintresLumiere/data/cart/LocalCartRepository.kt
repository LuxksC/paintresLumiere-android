package com.luxkasTech.PaintresLumiere.data.cart

import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import com.luxkasTech.PaintresLumiere.data.product.ProductVariant
import com.luxkasTech.PaintresLumiere.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Device-local cart that applies the same rules the backend cards define, so the UI behaves the
 * same before and after the API exists:
 * - a variant already in the cart has its quantity summed, never a second line;
 * - the resulting quantity can't exceed the variant's stock ([CartError.InsufficientStock]);
 * - a variant that isn't purchasable can't be added ([CartError.Unavailable]);
 * - stock is **not** reserved — it is only decremented at checkout.
 *
 * The cart belongs to the signed-in user: it is cleared when the session ends.
 */
@Singleton
class LocalCartRepository @Inject constructor(
    private val storage: CartStorage,
    sessionManager: SessionManager,
    @ApplicationScope scope: CoroutineScope,
) : CartRepository {

    private val mutex = Mutex()
    private val _cart = MutableStateFlow(storage.load())
    override val cart: StateFlow<Cart> = _cart.asStateFlow()

    init {
        sessionManager.isAuthenticated
            .drop(1)
            .filter { signedIn -> !signedIn }
            .onEach { clear() }
            .launchIn(scope)
    }

    override suspend fun refresh(): Cart = _cart.value

    override suspend fun addItem(variant: ProductVariant, quantity: Int): Cart = mutate { cart ->
        require(quantity > 0) { "quantity must be positive" }
        if (!variant.status.isPurchasable) throw CartError.Unavailable()
        val existing = cart.items.firstOrNull { it.productId == variant.id }
        val newQuantity = (existing?.quantity ?: 0) + quantity
        if (newQuantity > variant.stockQuantity) {
            throw CartError.InsufficientStock((variant.stockQuantity - (existing?.quantity ?: 0)).coerceAtLeast(0))
        }
        val line = CartItem(
            id = existing?.id ?: UUID.randomUUID().toString(),
            productId = variant.id,
            name = variant.name,
            slug = variant.slug,
            color = variant.color,
            sizeLabel = variant.sizeLabel,
            imageUrl = variant.images.firstOrNull(),
            unitPrice = variant.pricing.sellingPrice,
            finalUnitPrice = variant.pricing.finalPrice,
            stockQuantity = variant.stockQuantity,
            status = variant.status,
            quantity = newQuantity,
        )
        cart.copy(items = if (existing == null) cart.items + line else cart.items.map { if (it.id == existing.id) line else it })
    }

    override suspend fun updateQuantity(itemId: String, quantity: Int): Cart = mutate { cart ->
        val item = cart.items.firstOrNull { it.id == itemId } ?: throw CartError.ItemNotFound()
        if (quantity <= 0) return@mutate cart.copy(items = cart.items - item)
        if (quantity > item.stockQuantity) throw CartError.InsufficientStock(item.stockQuantity)
        cart.copy(items = cart.items.map { if (it.id == itemId) it.copy(quantity = quantity) else it })
    }

    override suspend fun removeItem(itemId: String): Cart = mutate { cart ->
        cart.copy(items = cart.items.filterNot { it.id == itemId })
    }

    override suspend fun clear(): Cart = mutate { Cart() }

    private suspend fun mutate(block: (Cart) -> Cart): Cart = mutex.withLock {
        val updated = block(_cart.value)
        storage.save(updated)
        _cart.value = updated
        updated
    }
}
