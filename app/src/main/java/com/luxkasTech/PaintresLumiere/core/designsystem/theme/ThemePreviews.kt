package com.luxkasTech.PaintresLumiere.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "Palette", backgroundColor = 0xFF0C0907, showBackground = true)
@Composable
private fun PalettePreview() {
    val rows = listOf(
        listOf(PLColor.backgroundPrimary, PLColor.backgroundSecondary, PLColor.backgroundElevated, PLColor.backgroundHighlight),
        listOf(PLColor.goldBright, PLColor.goldMid, PLColor.goldDeep, PLColor.goldAntique),
        listOf(PLColor.textPrimary, PLColor.textSecondary, PLColor.textMuted, PLColor.textDisabled),
        listOf(PLColor.error, PLColor.success, PLColor.warning, PLColor.borderSubtle),
    )
    Column(Modifier.padding(PLSpacing.md), verticalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
                row.forEach { Swatch(it) }
            }
        }
    }
}

@Composable
private fun Swatch(color: Color) {
    Box(
        Modifier
            .size(40.dp)
            .background(color, RoundedCornerShape(PLRadius.badge)),
    )
}

@Preview(name = "Typography", backgroundColor = 0xFF0C0907, showBackground = true)
@Composable
private fun TypographyPreview() {
    Column(Modifier.padding(PLSpacing.md)) {
        Text("Display", style = PLFont.display, color = PLColor.goldBright)
        Text("Heading 1", style = PLFont.h1, color = PLColor.textPrimary)
        Text("Heading 2", style = PLFont.h2, color = PLColor.textPrimary)
        Text("Body text", style = PLFont.body, color = PLColor.textSecondary)
        Text("Caption", style = PLFont.caption, color = PLColor.textMuted)
        Text("LABEL", style = PLFont.label, color = PLColor.textMuted)
        Text("Button", style = PLFont.button, color = PLColor.goldBright)
    }
}
