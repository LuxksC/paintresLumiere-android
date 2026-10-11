package com.luxkasTech.PaintresLumiere.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing

/** Live strength feedback under the sign-up password field — mirrors `PLPasswordStrengthBar`. */
@Composable
fun PLPasswordStrengthBar(password: String, modifier: Modifier = Modifier) {
    val strength = PasswordStrength.evaluate(password)
    val fraction by animateFloatAsState(strength.fraction, animationSpec = tween(300), label = "strength")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(PLSpacing.xs)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(PLColor.backgroundHighlight, CircleShape),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .background(strength.color, CircleShape),
            )
        }
        strength.labelRes?.let {
            Text(stringResource(it), style = PLFont.caption, color = strength.color)
        }
    }
}

private val PasswordStrength.color: Color
    get() = when (this) {
        PasswordStrength.None -> PLColor.backgroundHighlight
        PasswordStrength.Weak -> PLColor.error
        PasswordStrength.Moderate -> PLColor.goldMid
        PasswordStrength.Strong -> PLColor.success
    }

@Preview(backgroundColor = 0xFF0C0907, showBackground = true)
@Composable
private fun PLPasswordStrengthBarPreview() {
    Column(Modifier.padding(PLSpacing.md), verticalArrangement = Arrangement.spacedBy(PLSpacing.md)) {
        PLPasswordStrengthBar("abc")
        PLPasswordStrengthBar("abcdefgh1")
        PLPasswordStrengthBar("Abcdefgh1!")
    }
}
