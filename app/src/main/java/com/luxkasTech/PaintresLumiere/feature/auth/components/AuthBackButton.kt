package com.luxkasTech.PaintresLumiere.feature.auth.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont

/** "← Voltar" text button used at the top of the secondary auth screens. */
@Composable
fun AuthBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier, contentPadding = PaddingValues(vertical = 8.dp)) {
        Text("← " + stringResource(R.string.common_back), style = PLFont.navLink, color = PLColor.goldMid)
    }
}
