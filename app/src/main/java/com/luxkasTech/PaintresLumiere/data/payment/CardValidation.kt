package com.luxkasTech.PaintresLumiere.data.payment

import java.time.YearMonth

enum class CardBrand(val label: String, val cvvLength: Int) {
    VISA("Visa", 3),
    MASTERCARD("Mastercard", 3),
    AMEX("American Express", 4),
    ELO("Elo", 3),
    HIPERCARD("Hipercard", 3),
    UNKNOWN("", 3),
}

/** Local card checks before tokenization. Pure functions — nothing here stores card data. */
object CardValidation {

    /** Luhn checksum on 13–19 digits. */
    fun isValidNumber(digits: String): Boolean {
        if (digits.length !in 13..19 || !digits.all(Char::isDigit)) return false
        val sum = digits.reversed().mapIndexed { i, c ->
            val d = c - '0'
            if (i % 2 == 1) (d * 2).let { if (it > 9) it - 9 else it } else d
        }.sum()
        return sum % 10 == 0
    }

    fun brandOf(digits: String): CardBrand = when {
        ELO_PREFIXES.any(digits::startsWith) -> CardBrand.ELO
        digits.startsWith("606282") || digits.startsWith("3841") -> CardBrand.HIPERCARD
        digits.startsWith("34") || digits.startsWith("37") -> CardBrand.AMEX
        digits.startsWith("4") -> CardBrand.VISA
        digits.take(2).toIntOrNull() in 51..55 || digits.take(4).toIntOrNull() in 2221..2720 -> CardBrand.MASTERCARD
        else -> CardBrand.UNKNOWN
    }

    /** `MMYY` → valid if the month is real and not in the past. */
    fun isValidExpiry(mmyy: String, now: YearMonth = YearMonth.now()): Boolean {
        if (mmyy.length != 4 || !mmyy.all(Char::isDigit)) return false
        val month = mmyy.take(2).toInt()
        if (month !in 1..12) return false
        val expiry = YearMonth.of(2000 + mmyy.takeLast(2).toInt(), month)
        return !expiry.isBefore(now)
    }

    fun isValidCvv(cvv: String, brand: CardBrand): Boolean = cvv.length == brand.cvvLength && cvv.all(Char::isDigit)

    private val ELO_PREFIXES = listOf("4011", "4312", "4389", "4514", "4576", "5041", "5066", "5067", "509", "6277", "6362", "6363", "650", "6516", "6550")
}

data class CardInput(val number: String, val holderName: String, val expiryMmYy: String, val cvv: String)

/**
 * Turns card data into a provider token **on the device**, so card number, CVV and expiry never
 * reach our API (backend card PL-147 — keeps the project out of PCI-DSS scope). Bound per flavor:
 * `mock` returns a fake token; `live` is a placeholder until the Asaas tokenization is integrated.
 */
interface CardTokenizer {
    suspend fun tokenize(card: CardInput): String
}

class CardTokenizationUnavailableException : Exception("Card tokenization is not configured in this build")
