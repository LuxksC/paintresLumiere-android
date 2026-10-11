package com.luxkasTech.PaintresLumiere.data.admin

import com.luxkasTech.PaintresLumiere.core.network.BigDecimalSerializer
import com.luxkasTech.PaintresLumiere.core.network.apiCall
import com.luxkasTech.PaintresLumiere.data.product.ProductCategory
import com.luxkasTech.PaintresLumiere.data.product.ProductColor
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.luxkasTech.PaintresLumiere.data.product.ProductVariant
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Body for `POST /products` and `PUT /products/{id}`. Null fields are omitted (the shared Json has
 * `explicitNulls = false`), so an update only touches what is set. `images` is never sent: on PUT
 * it would *replace* the stored array.
 */
@Serializable
data class ProductUpsertRequest(
    val sku: String? = null,
    val name: String? = null,
    val description: String? = null,
    val category: ProductCategory? = null,
    @SerialName("selling_price") @Serializable(with = BigDecimalSerializer::class) val sellingPrice: BigDecimal? = null,
    @SerialName("discount_rate") @Serializable(with = BigDecimalSerializer::class) val discountRate: BigDecimal? = null,
    val color: ProductColor? = null,
    @SerialName("stock_quantity") val stockQuantity: Int? = null,
    val status: ProductStatus? = null,
    val width: Double? = null,
    val height: Double? = null,
    val length: Double? = null,
    val thickness: Double? = null,
    @SerialName("dimensions_unit") val dimensionsUnit: String? = null,
    val barcode: String? = null,
)

@Serializable
data class CreatedProduct(val id: String, val slug: String? = null)

/** `PATCH /products/{id}/stock` (backend card PL-200): exactly one of the two fields. */
@Serializable
data class StockUpdateRequest(val stockQuantity: Int? = null, val delta: Int? = null)

@Serializable
data class UploadedImage(val image: String)

/** What `POST /products/{id}/images` accepts (API docs): ≤ 4 MB, png/jpeg/heic/webp. */
object ProductImageRules {
    const val MAX_BYTES = 4 * 1024 * 1024
    val MIME_TYPES = setOf("image/png", "image/jpeg", "image/jpg", "image/heic", "image/webp")
}

interface AdminProductApi {
    @POST("products")
    suspend fun create(@Body body: ProductUpsertRequest): CreatedProduct

    @PUT("products/{id}")
    suspend fun update(@Path("id") id: String, @Body body: ProductUpsertRequest)

    @DELETE("products/{id}")
    suspend fun delete(@Path("id") id: String)

    /** Not live yet (PL-200); the `mock` flavor serves it. Returns the updated variant. */
    @PATCH("products/{id}/stock")
    suspend fun updateStock(@Path("id") id: String, @Body body: StockUpdateRequest): ProductVariant

    /** Live: the API uploads to S3 and appends the URL to `images` asynchronously. */
    @Multipart
    @POST("products/{productId}/images")
    suspend fun uploadImage(@Path("productId") productId: String, @Part file: MultipartBody.Part): UploadedImage
}

/** Admin-only catalog writes. The API answers 403 for non-admins. Throws `ApiError`. */
interface AdminProductRepository {
    suspend fun create(request: ProductUpsertRequest): CreatedProduct
    suspend fun update(id: String, request: ProductUpsertRequest)
    suspend fun delete(id: String)
    suspend fun adjustStock(id: String, delta: Int): ProductVariant
    suspend fun uploadImage(productId: String, bytes: ByteArray, mimeType: String, fileName: String): UploadedImage
}

@Singleton
class DefaultAdminProductRepository @Inject constructor(
    private val api: AdminProductApi,
) : AdminProductRepository {
    override suspend fun create(request: ProductUpsertRequest) = apiCall { api.create(request) }
    override suspend fun update(id: String, request: ProductUpsertRequest) = apiCall { api.update(id, request) }
    override suspend fun delete(id: String) = apiCall { api.delete(id) }
    override suspend fun adjustStock(id: String, delta: Int) = apiCall { api.updateStock(id, StockUpdateRequest(delta = delta)) }

    override suspend fun uploadImage(productId: String, bytes: ByteArray, mimeType: String, fileName: String): UploadedImage {
        val part = MultipartBody.Part.createFormData("file", fileName, bytes.toRequestBody(mimeType.toMediaType()))
        return apiCall { api.uploadImage(productId, part) }
    }
}
