package com.luxkasTech.PaintresLumiere.testing

import com.luxkasTech.PaintresLumiere.core.session.TokenStorage
import com.luxkasTech.PaintresLumiere.data.cart.CartItem
import com.luxkasTech.PaintresLumiere.data.product.ProductColor
import com.luxkasTech.PaintresLumiere.data.product.ProductDimensions
import com.luxkasTech.PaintresLumiere.data.product.ProductPricing
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import com.luxkasTech.PaintresLumiere.data.product.ProductVariant
import com.luxkasTech.PaintresLumiere.data.product.ProductVariantSpecifications
import java.math.BigDecimal

fun testVariant(
    id: String,
    stock: Int = 5,
    status: ProductStatus = ProductStatus.ACTIVE,
    color: ProductColor = ProductColor.GOLD,
    width: Double = 10.0,
    price: String = "50.00",
    finalPrice: String = price,
) = ProductVariant(
    id = id,
    name = "Luna $id",
    slug = "luna-$id",
    images = listOf("https://cdn.example/$id.png"),
    stockQuantity = stock,
    status = status,
    pricing = ProductPricing(BigDecimal(price), BigDecimal.ZERO, BigDecimal(finalPrice)),
    specifications = ProductVariantSpecifications(color, ProductDimensions(width = width, unit = "cm")),
)

class MemoryTokenStorage(var value: String? = null) : TokenStorage {
    override fun read() = value
    override fun write(token: String) { value = token }
    override fun clear() { value = null }
}

fun testCartItem(
    id: String,
    quantity: Int = 1,
    stock: Int = 5,
    status: ProductStatus = ProductStatus.ACTIVE,
) = CartItem(
    id = id,
    productId = "variant-$id",
    name = "Luna $id",
    slug = "luna-$id",
    color = ProductColor.GOLD,
    sizeLabel = "10cm",
    unitPrice = BigDecimal("10.00"),
    finalUnitPrice = BigDecimal("10.00"),
    stockQuantity = stock,
    status = status,
    quantity = quantity,
)
