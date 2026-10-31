package com.luxkasTech.PaintresLumiere.core.designsystem.components

import androidx.annotation.StringRes
import com.luxkasTech.PaintresLumiere.R
import java.text.BreakIterator

/**
 * Password strength heuristic. The rule is identical to iOS `PasswordStrength.evaluate(_:)` so
 * both apps judge the same password the same way:
 * - empty → [None]; fewer than 8 characters → [Weak]. Characters are user-perceived characters
 *   (grapheme clusters), like Swift's `String.count`, so an emoji counts once;
 * - 8+ characters, scored by how many of {ASCII uppercase, digit, non-alphanumeric} it contains:
 *   2–3 → [Strong], 1 → [Moderate], 0 → [Weak].
 */
enum class PasswordStrength(@StringRes val labelRes: Int?, val fraction: Float) {
    None(null, 0f),
    Weak(R.string.ds_password_strength_weak, 0.33f),
    Moderate(R.string.ds_password_strength_moderate, 0.55f),
    Strong(R.string.ds_password_strength_strong, 1f);

    companion object {
        fun evaluate(password: String): PasswordStrength {
            if (password.isEmpty()) return None
            if (password.characterCount() < 8) return Weak
            val hasUpper = password.any { it in 'A'..'Z' }
            val hasDigit = password.any { it in '0'..'9' }
            val hasSymbol = password.any { it !in 'A'..'Z' && it !in 'a'..'z' && it !in '0'..'9' }
            return when (listOf(hasUpper, hasDigit, hasSymbol).count { it }) {
                0 -> Weak
                1 -> Moderate
                else -> Strong
            }
        }
    }
}

private fun String.characterCount(): Int {
    val iterator = BreakIterator.getCharacterInstance().apply { setText(this@characterCount) }
    var count = 0
    while (iterator.next() != BreakIterator.DONE) count++
    return count
}
