package com.luxkasTech.PaintresLumiere.core.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordStrengthTest {

    @Test
    fun `empty password has no strength`() {
        assertEquals(PasswordStrength.None, PasswordStrength.evaluate(""))
    }

    @Test
    fun `anything shorter than 8 characters is weak, whatever it contains`() {
        assertEquals(PasswordStrength.Weak, PasswordStrength.evaluate("Ab1!xyz"))
    }

    @Test
    fun `8 lowercase letters is weak`() {
        assertEquals(PasswordStrength.Weak, PasswordStrength.evaluate("abcdefgh"))
    }

    @Test
    fun `one extra character class is moderate`() {
        assertEquals(PasswordStrength.Moderate, PasswordStrength.evaluate("abcdefg1"))
        assertEquals(PasswordStrength.Moderate, PasswordStrength.evaluate("Abcdefgh"))
        assertEquals(PasswordStrength.Moderate, PasswordStrength.evaluate("abcdefg!"))
    }

    @Test
    fun `two or more extra character classes is strong`() {
        assertEquals(PasswordStrength.Strong, PasswordStrength.evaluate("Abcdefg1"))
        assertEquals(PasswordStrength.Strong, PasswordStrength.evaluate("Abcdef1!"))
    }

    @Test
    fun `non ASCII letters count as symbols, like the iOS regex`() {
        assertEquals(PasswordStrength.Moderate, PasswordStrength.evaluate("açúcarzz"))
    }

    @Test
    fun `length counts user-perceived characters, like Swift's String count`() {
        // 4 emoji are 8 UTF-16 code units but only 4 characters on iOS.
        assertEquals(PasswordStrength.Weak, PasswordStrength.evaluate("😀😀😀😀"))
        assertEquals(PasswordStrength.Moderate, PasswordStrength.evaluate("😀😀😀😀abcd"))
    }
}
