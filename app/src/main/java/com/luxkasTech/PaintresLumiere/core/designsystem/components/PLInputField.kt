package com.luxkasTech.PaintresLumiere.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSize
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing

/**
 * Labelled text field — mirrors `PLInputField` on iOS, including its four visual states
 * (default, focused, filled, error).
 *
 * A non-null [errorMessage] puts the field in the error state. A blank message (`" "`) marks the
 * field as erroneous without printing text under it — used by Login to flag the email field while
 * the message itself sits under the password field.
 */
@Composable
fun PLInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isSecure: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var isVisible by rememberSaveable { mutableStateOf(false) }

    val state = when {
        errorMessage != null -> FieldState.Error
        isFocused -> FieldState.Focused
        value.isNotEmpty() -> FieldState.Filled
        else -> FieldState.Default
    }
    val shape = RoundedCornerShape(PLRadius.input)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label.uppercase(),
            style = PLFont.label.copy(letterSpacing = 1.sp),
            color = state.labelColor,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(PLSize.inputHeight)
                .background(PLColor.backgroundElevated, shape)
                .border(state.borderWidth, state.borderColor, shape)
                .padding(start = 14.dp, end = if (isSecure) 2.dp else 14.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = PLFont.body.copy(color = PLColor.textPrimary),
                cursorBrush = SolidColor(PLColor.goldMid),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                visualTransformation = when {
                    isSecure && !isVisible -> PasswordVisualTransformation()
                    else -> visualTransformation
                },
                interactionSource = interactionSource,
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(placeholder, style = PLFont.body, color = PLColor.textDisabled)
                        }
                        inner()
                    }
                },
            )
            if (isSecure) {
                IconButton(onClick = { isVisible = !isVisible }) {
                    Icon(
                        imageVector = if (isVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = stringResource(
                            if (isVisible) R.string.ds_input_hide_password else R.string.ds_input_show_password,
                        ),
                        tint = PLColor.textMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        if (!errorMessage.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PLSpacing.xs)) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = PLColor.error,
                    modifier = Modifier.size(14.dp),
                )
                Text(errorMessage, style = PLFont.caption, color = PLColor.error)
            }
        }
    }
}

private enum class FieldState { Default, Focused, Filled, Error }

private val FieldState.labelColor: Color
    get() = when (this) {
        FieldState.Error -> PLColor.error
        FieldState.Focused -> PLColor.goldBright
        else -> PLColor.textMuted
    }

private val FieldState.borderColor: Color
    get() = when (this) {
        FieldState.Error -> PLColor.error
        FieldState.Focused -> PLColor.goldMid
        FieldState.Filled -> PLColor.goldDeep
        FieldState.Default -> PLColor.borderSubtle
    }

private val FieldState.borderWidth
    get() = when (this) {
        FieldState.Error, FieldState.Focused -> 1.5.dp
        else -> 1.dp
    }

@Preview(backgroundColor = 0xFF0C0907, showBackground = true)
@Composable
private fun PLInputFieldPreview() {
    Column(Modifier.padding(PLSpacing.md), verticalArrangement = Arrangement.spacedBy(PLSpacing.md)) {
        PLInputField(label = "Email", value = "", onValueChange = {}, placeholder = "seu@email.com")
        PLInputField(label = "Email", value = "lucas@exemplo.com", onValueChange = {})
        PLInputField(label = "Senha", value = "segredo123", onValueChange = {}, isSecure = true)
        PLInputField(label = "Senha", value = "123", onValueChange = {}, isSecure = true, errorMessage = "Credenciais inválidas.")
    }
}
