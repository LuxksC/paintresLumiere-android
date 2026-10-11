package com.luxkasTech.PaintresLumiere.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing

/** Square category tile for the Home grid: image (or icon), label and product count. */
@Composable
fun PLCategoryCard(
    label: String,
    countLabel: String,
    imageUrl: String?,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(PLRadius.card)
    Column(
        modifier = modifier
            .clip(shape)
            .background(PLColor.backgroundElevated)
            .border(1.dp, PLColor.borderSubtle, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = "$label, $countLabel" }
            .padding(PLSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(PLSpacing.sm),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.4f)
                .clip(RoundedCornerShape(PLRadius.card - 2.dp))
                .background(PLColor.backgroundHighlight),
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl.isNullOrBlank()) {
                Icon(icon, contentDescription = null, tint = PLColor.goldMid, modifier = Modifier.size(40.dp))
            } else {
                PLRemoteImage(url = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        }
        Column(Modifier.padding(horizontal = PLSpacing.xs)) {
            Text(label, style = PLFont.button, color = PLColor.textPrimary)
            Text(countLabel, style = PLFont.caption, color = PLColor.textMuted)
        }
    }
}

@Preview(backgroundColor = 0xFF0C0907, showBackground = true)
@Composable
private fun PLCategoryCardPreview() {
    PLCategoryCard("Nomes", "3 peças", null, Icons.Outlined.Category, {}, Modifier.width(170.dp).padding(PLSpacing.md))
}
