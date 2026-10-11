package com.luxkasTech.PaintresLumiere.core.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextSearchTest {
    @Test
    fun `ignores case and accents`() {
        assertTrue(matchesSearch("arvore", "Árvore de Natal"))
        assertTrue(matchesSearch("ÁRV", "arvore"))
    }

    @Test
    fun `every term must match some field`() {
        assertTrue(matchesSearch("luna gold", "Luna", "GOLD-01"))
        assertFalse(matchesSearch("luna prata", "Luna", "GOLD-01"))
    }

    @Test
    fun `blank query matches everything`() {
        assertTrue(matchesSearch("  ", "anything"))
    }
}
