package com.luxkasTech.PaintresLumiere.feature.auth.reset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLDivider
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLInputField
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLPasswordStrengthBar
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.feature.auth.components.AuthBackButton

@Composable
fun ResetPasswordRoute(onDone: () -> Unit, viewModel: ResetPasswordViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.events.collect { onDone() } }
    ResetPasswordScreen(state, viewModel::onPasswordChange, viewModel::onConfirmChange, viewModel::submit, onDone)
}

@Composable
fun ResetPasswordScreen(
    state: ResetPasswordUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    val password = KeyboardOptions(keyboardType = KeyboardType.Password)
    Column(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PLSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
    ) {
        AuthBackButton(onClick = onBack, modifier = Modifier.padding(top = PLSpacing.sm))
        Text(stringResource(R.string.reset_title), style = PLFont.h1, color = PLColor.textPrimary, modifier = Modifier.semantics { heading() })
        Text(stringResource(R.string.reset_subtitle), style = PLFont.body, color = PLColor.textMuted)
        PLDivider()
        PLInputField(
            stringResource(R.string.reset_new_password),
            state.password,
            onPasswordChange,
            isSecure = true,
            keyboardOptions = password,
            errorMessage = state.passwordError?.asString(),
        )
        PLPasswordStrengthBar(state.password)
        PLInputField(
            stringResource(R.string.auth_signup_field_confirm),
            state.confirm,
            onConfirmChange,
            isSecure = true,
            keyboardOptions = password,
            errorMessage = state.confirmError?.asString(),
        )
        state.error?.let { Text(it.asString(), style = PLFont.body, color = PLColor.error) }
        PLButton(
            stringResource(R.string.reset_submit),
            onClick = onSubmit,
            type = if (state.isLoading) PLButtonType.Loading else PLButtonType.Primary,
        )
    }
}
