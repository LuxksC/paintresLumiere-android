package com.luxkasTech.PaintresLumiere.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont

/** 1dp hairline in [PLColor.borderSubtle]. */
@Composable
fun PLDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = PLColor.borderSubtle)
}

/** Divider with a centered label, used between the primary login and social login buttons. */
@Composable
fun PLOrDivider(
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.common_or),
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PLDivider(Modifier.weight(1f))
        Text(label, style = PLFont.caption, color = PLColor.textMuted)
        PLDivider(Modifier.weight(1f))
    }
}
