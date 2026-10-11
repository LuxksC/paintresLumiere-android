package com.luxkasTech.PaintresLumiere.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius

/** −/value/+ stepper clamped to [range] — mirrors `PLQuantityStepper`. */
@Composable
fun PLQuantityStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 1..99,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(PLRadius.button)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(48.dp)
            .background(PLColor.backgroundElevated, shape)
            .border(1.dp, PLColor.borderSubtle, shape),
    ) {
        val canDecrement = enabled && value > range.first
        val canIncrement = enabled && value < range.last
        IconButton(onClick = { onValueChange(value - 1) }, enabled = canDecrement) {
            Icon(
                Icons.Filled.Remove,
                contentDescription = stringResource(R.string.ds_stepper_decrease),
                tint = if (canDecrement) PLColor.textPrimary else PLColor.textDisabled,
            )
        }
        Text(
            text = value.toString(),
            style = PLFont.button,
            color = PLColor.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .defaultMinSize(minWidth = 28.dp)
                .padding(horizontal = 2.dp),
        )
        IconButton(onClick = { onValueChange(value + 1) }, enabled = canIncrement) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(R.string.ds_stepper_increase),
                tint = if (canIncrement) PLColor.textPrimary else PLColor.textDisabled,
            )
        }
    }
}

@Preview(backgroundColor = 0xFF0C0907, showBackground = true)
@Composable
private fun PLQuantityStepperPreview() {
    PLQuantityStepper(value = 1, onValueChange = {}, range = 1..5)
}
