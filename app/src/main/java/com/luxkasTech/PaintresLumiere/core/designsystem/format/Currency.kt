package com.luxkasTech.PaintresLumiere.core.designsystem.format

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private val brlLocale = Locale("pt", "BR")

/**
 * Formats a price as Brazilian reais (`R$ 1.234,56`) regardless of the device locale — prices are
 * always BRL, matching iOS `Decimal.brlFormatted`.
 */
fun BigDecimal.brlFormatted(): String = NumberFormat.getCurrencyInstance(brlLocale).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}.format(this)
