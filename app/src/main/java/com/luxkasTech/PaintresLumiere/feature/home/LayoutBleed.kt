package com.luxkasTech.PaintresLumiere.feature.home

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp

/**
 * Widens a child by [horizontal] on each side, cancelling the parent's horizontal padding so a
 * carousel can scroll edge to edge while its first item still lines up with the content.
 */
internal fun Modifier.layoutBleed(horizontal: Dp): Modifier = layout { measurable, constraints ->
    val extra = horizontal.roundToPx() * 2
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = constraints.minWidth + extra,
            maxWidth = constraints.maxWidth + extra,
        ),
    )
    layout(placeable.width - extra, placeable.height) {
        placeable.place(-extra / 2, 0)
    }
}
