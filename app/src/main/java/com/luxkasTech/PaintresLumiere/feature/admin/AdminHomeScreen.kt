package com.luxkasTech.PaintresLumiere.feature.admin

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLDivider
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PaintresLumiereTheme

enum class AdminSection(@StringRes val title: Int, @StringRes val subtitle: Int, val icon: ImageVector) {
    Products(R.string.admin_section_products, R.string.admin_section_products_subtitle, Icons.Outlined.Inventory2),
    Orders(R.string.admin_section_orders, R.string.admin_section_orders_subtitle, Icons.AutoMirrored.Outlined.ReceiptLong),
}

/** Entry point of the admin area. Each section opens its own screen as it lands. */
@Composable
fun AdminHomeRoute(
    onSectionSelected: (AdminSection) -> Unit,
) {
    AdminHomeScreen(onSectionSelected)
}

@Composable
fun AdminHomeScreen(onSectionSelected: (AdminSection) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PLSpacing.xl),
    ) {
        Column(Modifier.padding(top = PLSpacing.sm)) {
            Text(
                stringResource(R.string.admin_title),
                style = PLFont.h1,
                color = PLColor.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                stringResource(R.string.admin_subtitle),
                style = PLFont.label.copy(letterSpacing = 1.5.sp),
                color = PLColor.textMuted,
            )
        }
        PLDivider(Modifier.padding(vertical = PLSpacing.lg))
        Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.md)) {
            AdminSection.entries.forEach { section ->
                SectionCard(section) { onSectionSelected(section) }
            }
        }
    }
}

@Composable
private fun SectionCard(section: AdminSection, onClick: () -> Unit) {
    val shape = RoundedCornerShape(PLRadius.card)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PLColor.backgroundElevated)
            .border(1.dp, PLColor.borderSubtle, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(PLSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(PLSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(section.icon, contentDescription = null, tint = PLColor.goldMid, modifier = Modifier.size(28.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(section.title), style = PLFont.button, color = PLColor.textPrimary)
            Text(stringResource(section.subtitle), style = PLFont.caption, color = PLColor.textMuted)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = PLColor.textDisabled)
    }
}

@Preview
@Composable
private fun AdminHomePreview() {
    PaintresLumiereTheme { AdminHomeScreen {} }
}
