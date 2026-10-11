package com.luxkasTech.PaintresLumiere.core.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor

/** Capsule page indicator for image carousels; hidden when there is a single page. */
@Composable
fun PLPageIndicator(count: Int, activeIndex: Int, modifier: Modifier = Modifier) {
    if (count <= 1) return
    val description = stringResource(R.string.ds_page_indicator_a11y, activeIndex + 1, count)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(count) { index ->
            val active = index == activeIndex
            val width by animateDpAsState(if (active) 18.dp else 6.dp, tween(200), label = "dot")
            Box(
                Modifier
                    .width(width)
                    .height(6.dp)
                    .background(if (active) PLColor.goldBright else PLColor.textDisabled, CircleShape),
            )
        }
    }
}

@Preview(backgroundColor = 0xFF0C0907, showBackground = true)
@Composable
private fun PLPageIndicatorPreview() {
    PLPageIndicator(count = 4, activeIndex = 1)
}
