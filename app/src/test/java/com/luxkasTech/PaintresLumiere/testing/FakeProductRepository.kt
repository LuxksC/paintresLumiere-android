package com.luxkasTech.PaintresLumiere.testing

import com.luxkasTech.PaintresLumiere.data.product.CategorySummary
import com.luxkasTech.PaintresLumiere.data.product.Product
import com.luxkasTech.PaintresLumiere.data.product.ProductCategory
import com.luxkasTech.PaintresLumiere.data.product.ProductPricing
import com.luxkasTech.PaintresLumiere.data.product.ProductRepository
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import com.luxkasTech.PaintresLumiere.data.product.ProductVariant
import java.math.BigDecimal

class FakeProductRepository : ProductRepository {
    var products: Result<List<Product>> = Result.success(emptyList())
    var popular: Result<List<Product>> = Result.success(emptyList())
    var variants: Result<List<ProductVariant>> = Result.success(emptyList())
    var productCalls = 0
    val variantRequests = mutableListOf<String>()
    var categories: Result<List<CategorySummary>> = Result.success(emptyList())

    override suspend fun getCategories(): List<CategorySummary> = categories.getOrThrow()

    override suspend fun getProducts(): List<Product> {
        productCalls++
        return products.getOrThrow()
    }

    var categoryProducts: Result<List<Product>> = Result.success(emptyList())
    val categoryRequests = mutableListOf<ProductCategory>()

    override suspend fun getProductsInCategory(category: ProductCategory): List<Product> {
        categoryRequests += category
        return categoryProducts.getOrThrow()
    }

    override suspend fun getPopularProducts(limit: Int?): List<Product> = popular.getOrThrow()

    override suspend fun getProductVariants(sku: String): List<ProductVariant> {
        variantRequests += sku
        return variants.getOrThrow()
    }
}

fun testProduct(id: String, name: String = "Product $id", status: ProductStatus = ProductStatus.ACTIVE) = Product(
    id = id,
    sku = "SKU-$id",
    name = name,
    slug = name.lowercase(),
    status = status,
    pricing = ProductPricing(BigDecimal("10"), BigDecimal.ZERO, BigDecimal("10")),
)
