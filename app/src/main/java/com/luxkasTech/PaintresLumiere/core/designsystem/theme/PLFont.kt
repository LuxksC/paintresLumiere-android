package com.luxkasTech.PaintresLumiere.core.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.luxkasTech.PaintresLumiere.R

/** Inter, bundled under `res/font/` (same files as the iOS `DesignSystem/Fonts/`). */
val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

/**
 * Type scale. Sizes mirror `DesignSystem/PLTypography.swift`; `sp` keeps them scaling with the
 * user's font size setting, like Dynamic Type on iOS.
 */
object PLFont {
    val display = inter(FontWeight.Bold, 32)
    val h1 = inter(FontWeight.Bold, 26)
    val h2 = inter(FontWeight.SemiBold, 20)
    val body = inter(FontWeight.Normal, 14)
    val caption = inter(FontWeight.Medium, 12)
    val label = inter(FontWeight.SemiBold, 10)
    val button = inter(FontWeight.SemiBold, 15)
    val link = inter(FontWeight.Medium, 13)
    val navLink = inter(FontWeight.Medium, 14)
    val tabLabel = inter(FontWeight.SemiBold, 11)

    private fun inter(weight: FontWeight, size: Int) = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = weight,
        fontSize = size.sp,
    )
}
