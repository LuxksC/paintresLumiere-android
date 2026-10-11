package com.luxkasTech.PaintresLumiere.feature.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing

/** "Paintres Lumière / LASER CUT STUDIO" wordmark shown on the auth screens. */
@Composable
fun BrandHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(PLSpacing.xs)) {
        Box(
            Modifier
                .width(80.dp)
                .height(1.dp)
                .background(PLColor.goldAntique.copy(alpha = 0.6f)),
        )
        Text(
            text = stringResource(R.string.app_name),
            style = PLFont.display,
            color = PLColor.goldBright,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.auth_brand_tagline),
            style = PLFont.label.copy(letterSpacing = 2.5.sp),
            color = PLColor.textMuted,
        )
    }
}
