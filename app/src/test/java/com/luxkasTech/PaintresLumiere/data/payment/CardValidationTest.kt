package com.luxkasTech.PaintresLumiere.data.payment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class CardValidationTest {

    @Test
    fun `luhn accepts test cards and rejects typos`() {
        assertTrue(CardValidation.isValidNumber("4242424242424242"))
        assertTrue(CardValidation.isValidNumber("378282246310005"))
        assertTrue(CardValidation.isValidNumber("5555555555554444"))
        assertFalse(CardValidation.isValidNumber("4242424242424241"))
        assertFalse(CardValidation.isValidNumber("4242"))
    }

    @Test
    fun `detects brands`() {
        assertEquals(CardBrand.VISA, CardValidation.brandOf("4242424242424242"))
        assertEquals(CardBrand.MASTERCARD, CardValidation.brandOf("5555555555554444"))
        assertEquals(CardBrand.MASTERCARD, CardValidation.brandOf("2223003122003222"))
        assertEquals(CardBrand.AMEX, CardValidation.brandOf("378282246310005"))
        assertEquals(CardBrand.ELO, CardValidation.brandOf("6362970000457013"))
        assertEquals(CardBrand.HIPERCARD, CardValidation.brandOf("6062825624254001"))
    }

    @Test
    fun `expiry must be a real month not in the past`() {
        val now = YearMonth.of(2026, 10)
        assertTrue(CardValidation.isValidExpiry("1026", now))
        assertTrue(CardValidation.isValidExpiry("0130", now))
        assertFalse(CardValidation.isValidExpiry("0926", now))
        assertFalse(CardValidation.isValidExpiry("1327", now))
    }

    @Test
    fun `cvv length follows the brand`() {
        assertTrue(CardValidation.isValidCvv("123", CardBrand.VISA))
        assertFalse(CardValidation.isValidCvv("123", CardBrand.AMEX))
        assertTrue(CardValidation.isValidCvv("1234", CardBrand.AMEX))
    }
}
