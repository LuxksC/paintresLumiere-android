package com.luxkasTech.PaintresLumiere.data.product

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    /**
     * [category] and [search] follow backend card PL-110 (not live yet — the API ignores unknown
     * params, the `mock` flavor honours them).
     */
    @GET("products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int? = null,
    ): CatalogPage

    @GET("products/popular")
    suspend fun getPopularProducts(@Query("limit") limit: Int?): ProductsResponse<Product>

    /** Contract from backend card PL-106; not live yet (the `mock` flavor serves it). */
    @GET("products/categories")
    suspend fun getCategories(): CategoriesResponse

    @GET("products/sku/{sku}")
    suspend fun getProductBySku(@Path("sku") sku: String): ProductsResponse<ProductVariant>
}
