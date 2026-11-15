package com.luxkasTech.PaintresLumiere.feature.payment

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Installment options for boleto (carnê) and card. Provisional business rule until backend card
 * PL-146 fixes it: up to [MAX_INSTALLMENTS], and no installment below [MIN_INSTALLMENT].
 * The server always recomputes; this only drives the selector.
 */
object Installments {
    const val MAX_INSTALLMENTS = 12
    val MIN_INSTALLMENT = BigDecimal("30.00")

    fun options(total: BigDecimal): List<Int> {
        if (total.signum() <= 0) return listOf(1)
        val byValue = total.divide(MIN_INSTALLMENT, 0, RoundingMode.DOWN).toInt()
        return (1..byValue.coerceIn(1, MAX_INSTALLMENTS)).toList()
    }

    fun amount(total: BigDecimal, count: Int): BigDecimal = total.divide(BigDecimal(count), 2, RoundingMode.HALF_UP)
}
