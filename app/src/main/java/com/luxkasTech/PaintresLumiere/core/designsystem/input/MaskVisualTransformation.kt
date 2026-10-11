package com.luxkasTech.PaintresLumiere.core.designsystem.input

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Displays a digits-only value through a mask such as `###.###.###-##`, while the state keeps the
 * raw digits (which is what the API receives). Literal characters are only drawn once the digit
 * after them has been typed, so deleting never gets stuck on a separator.
 */
class MaskVisualTransformation(
    private val mask: String,
    private val slot: Char = '#',
) : VisualTransformation {

    val maxDigits: Int = mask.count { it == slot }

    fun format(digits: String): String {
        val out = StringBuilder()
        var index = 0
        for (c in mask) {
            if (index >= digits.length) break
            if (c == slot) {
                out.append(digits[index++])
            } else {
                out.append(c)
            }
        }
        return out.toString()
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(maxDigits)
        val formatted = format(digits)
        // For each formatted position, how many digits precede it; and the reverse.
        val transformedToOriginal = IntArray(formatted.length + 1)
        val originalToTransformed = IntArray(digits.length + 1)
        var count = 0
        for (i in formatted.indices) {
            transformedToOriginal[i] = count
            if (mask[i] == slot) {
                originalToTransformed[count] = i
                count++
            }
        }
        transformedToOriginal[formatted.length] = count
        originalToTransformed[digits.length] = formatted.length

        return TransformedText(
            AnnotatedString(formatted),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int) =
                    originalToTransformed[offset.coerceIn(0, digits.length)]

                override fun transformedToOriginal(offset: Int) =
                    transformedToOriginal[offset.coerceIn(0, formatted.length)]
            },
        )
    }

    companion object {
        val Cpf = MaskVisualTransformation("###.###.###-##")
        val Cnpj = MaskVisualTransformation("##.###.###/####-##")
        val Phone = MaskVisualTransformation("(##) #####-####")
    }
}

/** Keeps only digits, capped at the mask's capacity — use as the field's `onValueChange` filter. */
fun MaskVisualTransformation.sanitize(input: String): String = input.filter(Char::isDigit).take(maxDigits)
