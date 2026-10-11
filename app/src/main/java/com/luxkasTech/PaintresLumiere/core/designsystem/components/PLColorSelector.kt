package com.luxkasTech.PaintresLumiere.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing

/** One swatch offered by [PLColorSelector]. */
data class PLColorOption<T>(
    val value: T,
    val label: String,
    val swatch: Color,
    val needsOutline: Boolean = false,
)

/**
 * Horizontal row of color swatches — mirrors `PLColorSelector`. Options not in [enabled] are dimmed
 * and not selectable (no variant exists for that combination).
 */
@Composable
fun <T> PLColorSelector(
    options: List<PLColorOption<T>>,
    enabled: Set<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(PLSpacing.sm),
        contentPadding = PaddingValues(vertical = 2.dp),
    ) {
        items(options, key = { it.label }) { option ->
            val isSelected = option.value == selected
            val isEnabled = option.value in enabled
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .alpha(if (isEnabled) 1f else 0.3f)
                    .selectable(
                        selected = isSelected,
                        enabled = isEnabled,
                        role = Role.RadioButton,
                        onClick = { onSelect(option.value) },
                    )
                    .semantics { contentDescription = option.label },
            ) {
                if (isSelected) {
                    Box(
                        Modifier
                            .size(42.dp)
                            .border(2.dp, PLColor.goldBright, CircleShape),
                    )
                }
                Box(
                    Modifier
                        .size(32.dp)
                        .background(option.swatch, CircleShape)
                        .then(if (option.needsOutline) Modifier.border(1.dp, PLColor.borderSubtle, CircleShape) else Modifier),
                )
            }
        }
    }
}

@Preview(backgroundColor = 0xFF0C0907, showBackground = true)
@Composable
private fun PLColorSelectorPreview() {
    val options = listOf(
        PLColorOption("gold", "Dourado", Color(0xFFD4A53A)),
        PLColorOption("silver", "Prata", Color(0xFFC0C5CB), needsOutline = true),
        PLColorOption("black", "Preto", Color(0xFF1A1A1A)),
    )
    PLColorSelector(options, enabled = setOf("gold", "silver"), selected = "gold", onSelect = {})
}
