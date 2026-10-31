package com.luxkasTech.PaintresLumiere.data.product

import com.luxkasTech.PaintresLumiere.core.network.Page
import com.luxkasTech.PaintresLumiere.core.network.apiCall
import javax.inject.Inject
import javax.inject.Singleton

/** Catalog access — the Android counterpart of iOS `ProductServiceProtocol`. Throws `ApiError`. */
const val PAGE_SIZE = 20

interface ProductRepository {
    /** The whole catalog (follows every page). */
    suspend fun getProducts(): List<Product>

    /** One page of the catalog; `nextCursor == null` means there is nothing after it. */
    suspend fun getCatalogPage(cursor: String?, limit: Int = PAGE_SIZE): Page<Product>

    /** Catalog filtered by category on the server (`GET /products?category=`). */
    suspend fun getProductsInCategory(category: ProductCategory): List<Product>
    suspend fun getPopularProducts(limit: Int?): List<Product>
    suspend fun getProductVariants(sku: String): List<ProductVariant>

    /** Categories that have products; unknown categories and empty ones are dropped. */
    suspend fun getCategories(): List<CategorySummary>
}

@Singleton
class DefaultProductRepository @Inject constructor(
    private val api: ProductApi,
) : ProductRepository {
    override suspend fun getProducts(): List<Product> {
        val all = mutableListOf<Product>()
        var cursor: String? = null
        do {
            val page = apiCall { api.getProducts(cursor = cursor) }
            all += page.items
            cursor = page.nextCursor
        } while (cursor != null)
        return all
    }

    override suspend fun getCatalogPage(cursor: String?, limit: Int): Page<Product> {
        val page = apiCall { api.getProducts(cursor = cursor, limit = limit) }
        return Page(page.items, page.nextCursor, page.total)
    }

    override suspend fun getProductsInCategory(category: ProductCategory) =
        apiCall { api.getProducts(category = category.raw).items }
    override suspend fun getPopularProducts(limit: Int?) = apiCall { api.getPopularProducts(limit).products }
    override suspend fun getProductVariants(sku: String) = apiCall { api.getProductBySku(sku).products }
    override suspend fun getCategories() = apiCall {
        api.getCategories().categories.filter { it.category != ProductCategory.UNKNOWN && it.productCount > 0 }
    }
}
