package com.luxkasTech.PaintresLumiere.core.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TelemetryTest {

    @Test
    fun `screen names come from type-safe routes without arguments`() {
        assertEquals("ProductDetails", screenNameOf("com.luxkasTech.PaintresLumiere.navigation.ProductDetailsDestination/{sku}"))
        assertEquals("Cart", screenNameOf("com.luxkasTech.PaintresLumiere.navigation.CartDestination"))
        assertEquals("AdminProductForm", screenNameOf("com.luxkasTech.PaintresLumiere.navigation.AdminProductFormDestination?sku={sku}&variantId={variantId}"))
        assertEquals("Unknown", screenNameOf(null))
    }

    @Test
    fun `crash handler records then delegates to the previous handler`() {
        val recorded = mutableListOf<Throwable>()
        val reporter = object : CrashReporter {
            override fun recordNonFatal(throwable: Throwable, context: Map<String, String>) = Unit
            override fun recordFatal(throwable: Throwable) { recorded += throwable }
            override fun setUserId(id: String?) = Unit
        }
        var delegated: Throwable? = null
        val handler = CrashHandler(reporter, Thread.UncaughtExceptionHandler { _, t -> delegated = t })
        val boom = IllegalStateException("boom")
        handler.uncaughtException(Thread.currentThread(), boom)
        assertSame(boom, recorded.single())
        assertSame(boom, delegated)
    }

    @Test
    fun `events carry only non-personal parameters`() {
        assertEquals(mapOf("product_id" to "v1", "quantity" to "2"), AnalyticsEvent.AddToCart("v1", 2).params)
    }
}
