package com.luxkasTech.PaintresLumiere.data.cart

import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import com.luxkasTech.PaintresLumiere.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.luxkasTech.PaintresLumiere.core.network.BigDecimalSerializer
import com.luxkasTech.PaintresLumiere.core.network.apiCall
import com.luxkasTech.PaintresLumiere.data.product.ProductColor
import com.luxkasTech.PaintresLumiere.data.product.ProductDimensions
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import com.luxkasTech.PaintresLumiere.data.product.ProductVariant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

// Contract from backend cards PL-118 (GET /cart), PL-119 (POST /cart/items), PL-120 (PATCH) and
// PL-121 (DELETE). Totals are always computed by the server.

@Serializable
data class RemoteCartProduct(
    val name: String,
    val sku: String? = null,
    val slug: String? = null,
    val color: ProductColor = ProductColor.UNKNOWN,
    val dimensions: ProductDimensions = ProductDimensions(),
    val image: String? = null,
    @Serializable(with = BigDecimalSerializer::class) val sellingPrice: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) val discountRate: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = BigDecimalSerializer::class) val finalPrice: BigDecimal? = null,
    val stockQuantity: Int = 0,
    val status: ProductStatus = ProductStatus.UNKNOWN,
)

@Serializable
data class RemoteCartItem(
    val id: String,
    val productId: String,
    val quantity: Int,
    val product: RemoteCartProduct,
)

@Serializable
data class RemoteCart(
    val id: String? = null,
    val items: List<RemoteCartItem> = emptyList(),
    @Serializable(with = BigDecimalSerializer::class) val subtotal: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = BigDecimalSerializer::class) val discountTotal: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = BigDecimalSerializer::class) val total: BigDecimal = BigDecimal.ZERO,
)

@Serializable
data class AddCartItemRequest(val productId: String, val quantity: Int)

@Serializable
data class UpdateCartItemRequest(val quantity: Int)

interface CartApi {
    @GET("cart")
    suspend fun getCart(): RemoteCart

    @POST("cart/items")
    suspend fun addItem(@Body body: AddCartItemRequest): RemoteCart

    @PATCH("cart/items/{itemId}")
    suspend fun updateItem(@Path("itemId") itemId: String, @Body body: UpdateCartItemRequest): RemoteCart

    @DELETE("cart/items/{itemId}")
    suspend fun removeItem(@Path("itemId") itemId: String): RemoteCart

    @DELETE("cart")
    suspend fun clear(): RemoteCart
}

/** Maps the server cart to the app model; the server is the source of truth for prices. */
fun RemoteCart.toCart(): Cart = Cart(
    items.map { line ->
        val p = line.product
        CartItem(
            id = line.id,
            productId = line.productId,
            name = p.name,
            slug = p.slug ?: p.sku.orEmpty(),
            color = p.color,
            sizeLabel = p.dimensions.compactLabel,
            imageUrl = p.image,
            unitPrice = p.sellingPrice,
            finalUnitPrice = p.finalPrice ?: (p.sellingPrice - p.sellingPrice * p.discountRate).setScale(2, java.math.RoundingMode.HALF_UP),
            stockQuantity = p.stockQuantity,
            status = p.status,
            quantity = line.quantity,
        )
    },
)

/**
 * Cart backed by the API. Ready for when the cart endpoints ship (bind it in `CartModule`);
 * screens don't change because they only see [CartRepository].
 *
 * A 400 on add/update means the stock check failed server-side; it surfaces as
 * [CartError.InsufficientStock] when the remaining amount can be derived from the local view.
 */
@Singleton
class RemoteCartRepository @Inject constructor(
    private val api: CartApi,
    sessionManager: SessionManager,
    @ApplicationScope scope: CoroutineScope,
) : CartRepository {

    private val _cart = MutableStateFlow(Cart())
    override val cart: StateFlow<Cart> = _cart.asStateFlow()

    init {
        // The server keeps each user's cart; locally just forget it on sign-out.
        sessionManager.isAuthenticated.onEach { if (!it) _cart.value = Cart() }.launchIn(scope)
    }

    override suspend fun refresh(): Cart = publish(apiCall { api.getCart() })

    override suspend fun addItem(variant: ProductVariant, quantity: Int): Cart = stockAware(variant.stockQuantity, variant.id) {
        apiCall { api.addItem(AddCartItemRequest(variant.id, quantity)) }
    }

    override suspend fun updateQuantity(itemId: String, quantity: Int): Cart {
        if (quantity <= 0) return removeItem(itemId)
        val line = _cart.value.items.firstOrNull { it.id == itemId }
        return stockAware(line?.stockQuantity, line?.productId) {
            apiCall { api.updateItem(itemId, UpdateCartItemRequest(quantity)) }
        }
    }

    override suspend fun removeItem(itemId: String): Cart = publish(apiCall { api.removeItem(itemId) })

    override suspend fun clear(): Cart = publish(apiCall { api.clear() })

    private suspend fun stockAware(stock: Int?, productId: String?, call: suspend () -> RemoteCart): Cart = try {
        publish(call())
    } catch (e: ApiError.Http) {
        if (e.statusCode == 400 && stock != null) {
            val inCart = _cart.value.items.firstOrNull { it.productId == productId }?.quantity ?: 0
            throw CartError.InsufficientStock((stock - inCart).coerceAtLeast(0))
        }
        throw e
    }

    private fun publish(remote: RemoteCart): Cart = remote.toCart().also { _cart.value = it }
}
