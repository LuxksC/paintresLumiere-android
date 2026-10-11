package com.luxkasTech.PaintresLumiere.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSize
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing

enum class PLButtonType { Primary, Secondary, Ghost, Destructive, Loading }

/**
 * Full-width brand button — mirrors `PLButton` on iOS. [PLButtonType.Loading] shows a spinner and
 * is never clickable.
 */
@Composable
fun PLButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: PLButtonType = PLButtonType.Primary,
    enabled: Boolean = true,
) {
    val isLoading = type == PLButtonType.Loading
    val shape = RoundedCornerShape(PLRadius.button)
    Surface(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        color = type.backgroundColor,
        border = type.borderWidth?.let { BorderStroke(it, type.borderColor) },
        modifier = modifier
            .fillMaxWidth()
            .height(PLSize.buttonHeight)
            .alpha(if (enabled || isLoading) 1f else 0.4f)
            .semantics { role = Role.Button },
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = PLColor.backgroundPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .padding(end = PLSpacing.sm)
                        .size(18.dp),
                )
            }
            Text(
                text = if (isLoading) stringResource(R.string.common_loading) else text,
                style = PLFont.button,
                color = type.foregroundColor,
            )
        }
    }
}

private val PLButtonType.backgroundColor: Color
    get() = when (this) {
        PLButtonType.Primary, PLButtonType.Loading -> PLColor.goldMid
        PLButtonType.Secondary, PLButtonType.Ghost -> Color.Transparent
        PLButtonType.Destructive -> PLColor.errorBackground
    }

private val PLButtonType.foregroundColor: Color
    get() = when (this) {
        PLButtonType.Primary, PLButtonType.Loading -> PLColor.backgroundPrimary
        PLButtonType.Secondary -> PLColor.goldBright
        PLButtonType.Ghost, PLButtonType.Destructive -> PLColor.textPrimary
    }

private val PLButtonType.borderColor: Color
    get() = when (this) {
        PLButtonType.Secondary -> PLColor.goldMid
        PLButtonType.Ghost -> PLColor.borderSubtle
        else -> Color.Transparent
    }

private val PLButtonType.borderWidth
    get() = when (this) {
        PLButtonType.Secondary, PLButtonType.Ghost -> 1.5.dp
        else -> null
    }

@Preview(backgroundColor = 0xFF0C0907, showBackground = true)
@Composable
private fun PLButtonPreview() {
    Column(Modifier.padding(PLSpacing.md), verticalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
        PLButton("Entrar", onClick = {})
        PLButton("Secundário", onClick = {}, type = PLButtonType.Secondary)
        PLButton("Continuar com Google", onClick = {}, type = PLButtonType.Ghost)
        PLButton("Excluir conta", onClick = {}, type = PLButtonType.Destructive)
        PLButton("Entrar", onClick = {}, type = PLButtonType.Loading)
        PLButton("Desabilitado", onClick = {}, enabled = false)
    }
}
