package com.luxkasTech.PaintresLumiere.data.product

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class ProductPricingTest {

    private fun pricing(rate: String) = ProductPricing(BigDecimal("100"), BigDecimal(rate), BigDecimal("100"))

    @Test
    fun `discount label is a truncated whole percentage`() {
        assertEquals("-15%", pricing("0.15").discountPercentLabel)
        assertEquals("-12%", pricing("0.125").discountPercentLabel)
    }

    @Test
    fun `dimensions without values render a dash`() {
        assertEquals("—", ProductDimensions().compactLabel)
        assertEquals("10 × 10", ProductDimensions(width = 10.0, height = 10.0).compactLabel)
    }
}
