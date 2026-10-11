package com.luxkasTech.PaintresLumiere.data.product

import com.luxkasTech.PaintresLumiere.core.network.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Serializable
data class ProductsResponse<T>(val products: List<T> = emptyList())

@Serializable
data class ProductPricing(
    @SerialName("selling_price") @Serializable(with = BigDecimalSerializer::class) val sellingPrice: BigDecimal,
    @SerialName("discount_rate") @Serializable(with = BigDecimalSerializer::class) val discountRate: BigDecimal = BigDecimal.ZERO,
    @SerialName("final_price") @Serializable(with = BigDecimalSerializer::class) val finalPrice: BigDecimal,
) {
    val hasDiscount: Boolean get() = discountRate.signum() > 0

    /** e.g. `-15%` for a 0.15 discount rate. */
    val discountPercentLabel: String
        get() = "-${discountRate.movePointRight(2).setScale(0, RoundingMode.DOWN).toPlainString()}%"
}

@Serializable
data class ProductDimensions(
    val width: Double? = null,
    val height: Double? = null,
    val length: Double? = null,
    val thickness: Double? = null,
    val unit: String? = null,
) {
    /**
     * Compact label used both for display and as the variant "size" key, e.g. `10 × 20 × 3mm`.
     * Matches iOS `ProductDimensions.compactLabel`.
     */
    val compactLabel: String
        get() {
            val axes = listOfNotNull(width, height, length, thickness).map(::formatNumber)
            if (axes.isEmpty()) return "—"
            val joined = axes.joinToString(" × ")
            return unit?.let { "$joined$it" } ?: joined
        }

    private fun formatNumber(value: Double): String =
        if (value % 1.0 == 0.0) value.toLong().toString() else numberFormat.format(value)

    private companion object {
        val numberFormat = DecimalFormat("0.##", DecimalFormatSymbols(Locale("pt", "BR")))
    }
}

@Serializable
data class ProductListSpecifications(val thickness: Double? = null)

@Serializable
data class ProductVariantSpecifications(
    val color: ProductColor = ProductColor.UNKNOWN,
    val dimensions: ProductDimensions = ProductDimensions(),
)

/**
 * Catalog representative of a SKU group (`GET /products`, `GET /products/popular`). The API
 * deduplicates by SKU; the individual variants come from `GET /products/sku/{sku}`.
 */
@Serializable
data class Product(
    val id: String,
    val sku: String,
    val name: String,
    val slug: String,
    val description: String? = null,
    val images: List<String> = emptyList(),
    @SerialName("stock_quantity") val stockQuantity: Int = 0,
    val status: ProductStatus = ProductStatus.UNKNOWN,
    val pricing: ProductPricing,
    val specifications: ProductListSpecifications = ProductListSpecifications(),
) {
    val primaryImageUrl: String? get() = images.firstOrNull()
}

/** One concrete, purchasable row: a SKU in a specific color and dimension. */
@Serializable
data class ProductVariant(
    val id: String,
    val name: String,
    val slug: String,
    val description: String? = null,
    val images: List<String> = emptyList(),
    @SerialName("stock_quantity") val stockQuantity: Int = 0,
    val category: ProductCategory = ProductCategory.UNKNOWN,
    val barcode: String? = null,
    val status: ProductStatus = ProductStatus.UNKNOWN,
    val pricing: ProductPricing,
    val specifications: ProductVariantSpecifications = ProductVariantSpecifications(),
) {
    val color: ProductColor get() = specifications.color
    val dimensions: ProductDimensions get() = specifications.dimensions
    val sizeLabel: String get() = dimensions.compactLabel
}
