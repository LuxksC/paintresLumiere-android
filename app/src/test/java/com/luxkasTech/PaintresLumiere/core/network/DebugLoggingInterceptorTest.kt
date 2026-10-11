package com.luxkasTech.PaintresLumiere.core.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugLoggingInterceptorTest {

    @Test
    fun `credential and payment routes are sensitive`() {
        listOf("/login", "/signup", "/auth/google", "/auth/reset-password", "/profile/password", "/orders/ord-1/payment")
            .forEach { assertTrue(it, DebugLoggingInterceptor.isSensitivePath(it)) }
    }

    @Test
    fun `catalog and order routes keep full logging`() {
        listOf("/products", "/products/sku/LUNA", "/orders", "/orders/ord-1", "/profile", "/cart/items")
            .forEach { assertFalse(it, DebugLoggingInterceptor.isSensitivePath(it)) }
    }
}
