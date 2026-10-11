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

    @Test
    fun `encoding keeps the exact literal, scale included`() {
        val encoded = Json.encodeToString(Price.serializer(), Price(BigDecimal("19.90")))
        assertEquals("""{"amount":19.90}""", encoded)
        assertEquals(BigDecimal("19.90"), Json.decodeFromString(Price.serializer(), encoded).amount)
    }
}
