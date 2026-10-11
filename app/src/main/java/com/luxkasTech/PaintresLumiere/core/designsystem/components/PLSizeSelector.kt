package com.luxkasTech.PaintresLumiere.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing

/** Horizontal row of size pills — mirrors `PLSizeSelector`. */
@Composable
fun PLSizeSelector(
    sizes: List<String>,
    enabled: Set<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
        items(sizes, key = { it }) { size ->
            val isSelected = size == selected
            val isEnabled = size in enabled
            val shape = RoundedCornerShape(PLRadius.button)
            Text(
                text = size,
                style = PLFont.button,
                color = if (isSelected) PLColor.backgroundPrimary else PLColor.textPrimary,
                modifier = Modifier
                    .alpha(if (isEnabled) 1f else 0.3f)
                    .clip(shape)
                    .background(if (isSelected) PLColor.goldMid else Color.Transparent)
                    .border(1.5.dp, if (isSelected) Color.Transparent else PLColor.borderSubtle, shape)
                    .selectable(
                        selected = isSelected,
                        enabled = isEnabled,
                        role = Role.RadioButton,
                        onClick = { onSelect(size) },
                    )
                    .defaultMinSize(minHeight = 40.dp)
                    .wrapContentHeight()
                    .padding(horizontal = PLSpacing.md),
            )
        }
    }
}

@Preview(backgroundColor = 0xFF0C0907, showBackground = true)
@Composable
private fun PLSizeSelectorPreview() {
    PLSizeSelector(
        sizes = listOf("10 × 10cm", "20 × 20cm", "30 × 30cm"),
        enabled = setOf("10 × 10cm", "20 × 20cm"),
        selected = "10 × 10cm",
        onSelect = {},
    )
}
