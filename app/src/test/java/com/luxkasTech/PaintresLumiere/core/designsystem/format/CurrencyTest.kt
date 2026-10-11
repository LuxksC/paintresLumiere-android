package com.luxkasTech.PaintresLumiere.core.designsystem.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CurrencyTest {

    // NumberFormat separates the symbol with a no-break space; normalize it for readable asserts.
    private fun BigDecimal.formatted() = brlFormatted().replace(' ', ' ')

    @Test
    fun `formats with BRL symbol, dot thousands and comma decimals`() {
        assertEquals("R$ 1.234,56", BigDecimal("1234.56").formatted())
    }

    @Test
    fun `always shows two decimal places`() {
        assertEquals("R$ 19,90", BigDecimal("19.9").formatted())
        assertEquals("R$ 20,00", BigDecimal("20").formatted())
    }
}
