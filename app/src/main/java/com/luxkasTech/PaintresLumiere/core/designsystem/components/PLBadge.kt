package com.luxkasTech.PaintresLumiere.core.designsystem.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing

enum class PLBadgeStatus(@StringRes val labelRes: Int) {
    Ready(R.string.ds_badge_ready),
    Draft(R.string.ds_badge_draft),
    Processing(R.string.ds_badge_processing),
    Error(R.string.ds_badge_error),
}

/** Small status pill — mirrors `PLBadge` on iOS. */
@Composable
fun PLBadge(status: PLBadgeStatus, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(status.labelRes),
        style = PLFont.label.copy(letterSpacing = 0.5.sp),
        color = status.foreground,
        modifier = modifier
            .background(status.background, RoundedCornerShape(PLRadius.badge))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

private val PLBadgeStatus.background: Color
    get() = when (this) {
        PLBadgeStatus.Ready -> PLColor.success.copy(alpha = 0.15f)
        PLBadgeStatus.Draft -> PLColor.backgroundHighlight
        PLBadgeStatus.Processing -> PLColor.goldAntique.copy(alpha = 0.2f)
        PLBadgeStatus.Error -> PLColor.errorBackground
    }

private val PLBadgeStatus.foreground: Color
    get() = when (this) {
        PLBadgeStatus.Ready -> PLColor.success
        PLBadgeStatus.Draft -> PLColor.textMuted
        PLBadgeStatus.Processing -> PLColor.goldMid
        PLBadgeStatus.Error -> PLColor.error
    }

@Preview(backgroundColor = 0xFF0C0907, showBackground = true)
@Composable
private fun PLBadgePreview() {
    Row(Modifier.padding(PLSpacing.md), horizontalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
        PLBadgeStatus.entries.forEach { PLBadge(it) }
    }
}
