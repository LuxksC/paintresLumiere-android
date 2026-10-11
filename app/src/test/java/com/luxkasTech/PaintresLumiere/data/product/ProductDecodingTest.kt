package com.luxkasTech.PaintresLumiere.data.product

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ProductDecodingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private fun variant(id: String, category: String, status: String, color: String) = """
        {
          "id": "$id", "name": "Luna", "slug": "luna-$id", "description": null,
          "images": ["https://cdn/x.png"], "stock_quantity": 4,
          "category": "$category", "barcode": null, "status": "$status",
          "pricing": {"selling_price": 50.0, "discount_rate": 0.15, "final_price": 42.5},
          "specifications": {"color": "$color", "dimensions": {"width": 10, "height": 20.5, "length": null, "thickness": 3, "unit": "mm"}}
        }
    """

    @Test
    fun `decodes a known variant`() {
        val response = json.decodeFromString<ProductsResponse<ProductVariant>>(
            """{"products":[${variant("1", "names", "out_of_stock", "silver")}]}""",
        )
        val v = response.products.single()
        assertEquals(ProductCategory.NAMES, v.category)
        assertEquals(ProductStatus.OUT_OF_STOCK, v.status)
        assertEquals(ProductColor.SILVER, v.color)
        assertEquals(BigDecimal("42.5"), v.pricing.finalPrice)
        assertEquals("10 × 20,5 × 3mm", v.sizeLabel)
    }

    @Test
    fun `unknown enum values degrade to UNKNOWN without failing the list`() {
        val response = json.decodeFromString<ProductsResponse<ProductVariant>>(
            """{"products":[${variant("1", "keychains", "archived", "neon")},${variant("2", "letters", "active", "gold")}]}""",
        )
        assertEquals(2, response.products.size)
        val unknown = response.products[0]
        assertEquals(ProductCategory.UNKNOWN, unknown.category)
        assertEquals(ProductStatus.UNKNOWN, unknown.status)
        assertEquals(ProductColor.UNKNOWN, unknown.color)
        val known = response.products[1]
        assertEquals(ProductCategory.LETTERS, known.category)
        assertEquals(ProductStatus.ACTIVE, known.status)
        assertEquals(ProductColor.GOLD, known.color)
    }

    @Test
    fun `an empty string never matches the UNKNOWN sentinel as a known value`() {
        val v = json.decodeFromString<ProductVariant>(variant("1", "", "", ""))
        assertEquals(ProductStatus.UNKNOWN, v.status)
    }

    @Test
    fun `only ACTIVE is purchasable`() {
        assertTrue(ProductStatus.ACTIVE.isPurchasable)
        assertFalse(ProductStatus.INACTIVE.isPurchasable)
        assertFalse(ProductStatus.OUT_OF_STOCK.isPurchasable)
        assertFalse(ProductStatus.UNKNOWN.isPurchasable)
    }

    @Test
    fun `UNKNOWN color is never selectable`() {
        assertFalse(ProductColor.UNKNOWN in ProductColor.selectable)
        assertEquals(ProductColor.entries.size - 1, ProductColor.selectable.size)
    }

    @Test
    fun `decodes a catalog list item`() {
        val product = json.decodeFromString<Product>(
            """{"id":"1","sku":"LUNA","name":"Luna","slug":"luna","images":[],"stock_quantity":0,
               "status":"active","pricing":{"selling_price":19.9,"discount_rate":0,"final_price":19.9},
               "specifications":{"thickness":3}}""",
        )
        assertEquals("LUNA", product.sku)
        assertFalse(product.pricing.hasDiscount)
        assertEquals(null, product.primaryImageUrl)
    }
}
