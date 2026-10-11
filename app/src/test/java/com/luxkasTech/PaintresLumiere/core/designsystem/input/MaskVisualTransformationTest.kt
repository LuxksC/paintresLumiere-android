package com.luxkasTech.PaintresLumiere.core.designsystem.input

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class MaskVisualTransformationTest {

    private val cpf = MaskVisualTransformation.Cpf

    @Test
    fun `formats progressively without trailing separators`() {
        assertEquals("", cpf.format(""))
        assertEquals("123", cpf.format("123"))
        assertEquals("123.4", cpf.format("1234"))
        assertEquals("123.456.789-01", cpf.format("12345678901"))
    }

    @Test
    fun `sanitize keeps digits up to the mask capacity`() {
        assertEquals("12345678901", cpf.sanitize("123.456.789-0199"))
        assertEquals("11987654321", MaskVisualTransformation.Phone.sanitize("(11) 98765-4321"))
    }

    @Test
    fun `offset mapping round trips`() {
        val transformed = cpf.filter(AnnotatedString("1234567"))
        assertEquals("123.456.7", transformed.text.text)
        val mapping = transformed.offsetMapping
        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(5, mapping.originalToTransformed(4)) // after "123.4"
        assertEquals(9, mapping.originalToTransformed(7))
        assertEquals(3, mapping.transformedToOriginal(4)) // on the dot → before digit 4
        assertEquals(7, mapping.transformedToOriginal(9))
    }

    @Test
    fun `cnpj and phone masks`() {
        assertEquals("12.345.678/0001-95", MaskVisualTransformation.Cnpj.format("12345678000195"))
        assertEquals("(11) 98765-4321", MaskVisualTransformation.Phone.format("11987654321"))
    }
}
