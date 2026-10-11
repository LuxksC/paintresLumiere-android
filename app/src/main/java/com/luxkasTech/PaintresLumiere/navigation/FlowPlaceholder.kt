package com.luxkasTech.PaintresLumiere.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor

/** Empty brand background used by destinations whose screen hasn't landed yet. */
@Composable
internal fun FlowPlaceholder() {
    Box(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary),
    )
}
