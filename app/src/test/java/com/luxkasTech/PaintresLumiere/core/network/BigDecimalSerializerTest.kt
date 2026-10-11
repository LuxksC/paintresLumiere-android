package com.luxkasTech.PaintresLumiere.core.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class BigDecimalSerializerTest {

    @Serializable
    private data class Price(@Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal)

    @Test
    fun `keeps the exact decimal literal`() {
        val price = Json.decodeFromString<Price>("""{"amount":19.9}""")
        assertEquals(BigDecimal("19.9"), price.amount)
    }

    @Test
    fun `accepts integers`() {
        assertEquals(BigDecimal("20"), Json.decodeFromString<Price>("""{"amount":20}""").amount)
    }
}
