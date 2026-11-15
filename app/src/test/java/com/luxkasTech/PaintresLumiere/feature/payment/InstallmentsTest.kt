package com.luxkasTech.PaintresLumiere.feature.payment

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class InstallmentsTest {
    @Test
    fun `no installment below the minimum`() {
        assertEquals(listOf(1, 2, 3), Installments.options(BigDecimal("95.00")))
        assertEquals(listOf(1), Installments.options(BigDecimal("29.90")))
    }

    @Test
    fun `capped at twelve`() {
        assertEquals(12, Installments.options(BigDecimal("10000")).last())
    }

    @Test
    fun `installment amount is rounded to cents`() {
        assertEquals(BigDecimal("33.33"), Installments.amount(BigDecimal("100.00"), 3))
    }
}
