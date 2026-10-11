package com.luxkasTech.PaintresLumiere.data.product

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    @GET("products")
    suspend fun getProducts(): ProductsResponse<Product>

    @GET("products/popular")
    suspend fun getPopularProducts(@Query("limit") limit: Int?): ProductsResponse<Product>

    @GET("products/sku/{sku}")
    suspend fun getProductBySku(@Path("sku") sku: String): ProductsResponse<ProductVariant>
}
