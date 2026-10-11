package com.luxkasTech.PaintresLumiere.feature.auth.forgot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLDivider
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLInputField
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PaintresLumiereTheme
import com.luxkasTech.PaintresLumiere.feature.auth.components.AuthBackButton

@Composable
fun ForgotPasswordRoute(
    onFinished: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ForgotPasswordScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onSubmit = viewModel::sendResetLink,
        onBack = onFinished,
    )
}

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PLSpacing.xl),
    ) {
        AuthBackButton(onClick = onBack, modifier = Modifier.padding(top = PLSpacing.sm))
        Column(Modifier.padding(top = PLSpacing.sm), verticalArrangement = Arrangement.spacedBy(PLSpacing.xs)) {
            Text(
                stringResource(R.string.auth_forgot_title),
                style = PLFont.h1,
                color = PLColor.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            Text(stringResource(R.string.auth_forgot_subtitle), style = PLFont.body, color = PLColor.textMuted)
        }
        PLDivider(Modifier.padding(vertical = PLSpacing.lg))

        if (state.linkSent) {
            Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.md)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PLSpacing.sm),
                    modifier = Modifier
                        .background(PLColor.success.copy(alpha = 0.1f), RoundedCornerShape(PLRadius.card))
                        .padding(PLSpacing.md),
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PLColor.success, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.auth_forgot_sent), style = PLFont.body, color = PLColor.success)
                }
                Text(stringResource(R.string.auth_forgot_spam_hint), style = PLFont.caption, color = PLColor.textMuted)
                PLButton(stringResource(R.string.auth_forgot_back_to_login), onClick = onBack)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.lg)) {
                PLInputField(
                    label = stringResource(R.string.auth_field_email),
                    value = state.email,
                    onValueChange = onEmailChange,
                    placeholder = stringResource(R.string.auth_field_email_placeholder),
                    errorMessage = state.emailError?.asString(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSubmit() }),
                )
                PLButton(
                    text = stringResource(R.string.auth_forgot_submit),
                    onClick = onSubmit,
                    type = if (state.isLoading) PLButtonType.Loading else PLButtonType.Primary,
                    enabled = state.canSubmit,
                )
            }
        }
    }
}

@Preview
@Composable
private fun ForgotPasswordScreenPreview() {
    PaintresLumiereTheme {
        ForgotPasswordScreen(ForgotPasswordUiState(email = "lucas@exemplo.com"), {}, {}, {})
    }
}

@Preview
@Composable
private fun ForgotPasswordSentPreview() {
    PaintresLumiereTheme {
        ForgotPasswordScreen(ForgotPasswordUiState(linkSent = true), {}, {}, {})
    }
}
