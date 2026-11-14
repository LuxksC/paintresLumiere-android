package com.luxkasTech.PaintresLumiere.data.product

import com.luxkasTech.PaintresLumiere.core.network.apiCall
import javax.inject.Inject
import javax.inject.Singleton

/** Catalog access — the Android counterpart of iOS `ProductServiceProtocol`. Throws `ApiError`. */
interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun getPopularProducts(limit: Int?): List<Product>
    suspend fun getProductVariants(sku: String): List<ProductVariant>

    /** Categories that have products; unknown categories and empty ones are dropped. */
    suspend fun getCategories(): List<CategorySummary>
}

@Singleton
class DefaultProductRepository @Inject constructor(
    private val api: ProductApi,
) : ProductRepository {
    override suspend fun getProducts() = apiCall { api.getProducts().products }
    override suspend fun getPopularProducts(limit: Int?) = apiCall { api.getPopularProducts(limit).products }
    override suspend fun getProductVariants(sku: String) = apiCall { api.getProductBySku(sku).products }
    override suspend fun getCategories() = apiCall {
        api.getCategories().categories.filter { it.category != ProductCategory.UNKNOWN && it.productCount > 0 }
    }
}
