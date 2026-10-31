package com.luxkasTech.PaintresLumiere.feature.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLDivider
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLInputField
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLPasswordStrengthBar
import com.luxkasTech.PaintresLumiere.core.designsystem.input.MaskVisualTransformation
import com.luxkasTech.PaintresLumiere.core.designsystem.input.sanitize
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PaintresLumiereTheme
import com.luxkasTech.PaintresLumiere.feature.auth.components.AuthBackButton

@Composable
fun SignUpRoute(
    onBack: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SignUpScreen(
        state = state,
        actions = SignUpActions(
            onNameChange = viewModel::onNameChange,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onPhoneChange = viewModel::onPhoneChange,
            onCpfChange = viewModel::onCpfChange,
            onCnpjChange = viewModel::onCnpjChange,
            onSubmit = viewModel::createAccount,
            onBack = onBack,
        ),
    )
}

data class SignUpActions(
    val onNameChange: (String) -> Unit = {},
    val onEmailChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onConfirmPasswordChange: (String) -> Unit = {},
    val onPhoneChange: (String) -> Unit = {},
    val onCpfChange: (String) -> Unit = {},
    val onCnpjChange: (String) -> Unit = {},
    val onSubmit: () -> Unit = {},
    val onBack: () -> Unit = {},
)

@Composable
fun SignUpScreen(state: SignUpUiState, actions: SignUpActions) {
    val focusManager = LocalFocusManager.current
    val next = KeyboardOptions(imeAction = ImeAction.Next)
    Column(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PLSpacing.xl),
    ) {
        AuthBackButton(onClick = actions.onBack, modifier = Modifier.padding(top = PLSpacing.sm))
        Column(Modifier.padding(top = PLSpacing.sm), verticalArrangement = Arrangement.spacedBy(PLSpacing.xs)) {
            Text(
                stringResource(R.string.auth_signup_title),
                style = PLFont.h1,
                color = PLColor.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            Text(stringResource(R.string.auth_signup_subtitle), style = PLFont.body, color = PLColor.textMuted)
        }
        PLDivider(Modifier.padding(vertical = PLSpacing.md))

        Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.md)) {
            Text(stringResource(R.string.auth_signup_required_hint), style = PLFont.caption, color = PLColor.textDisabled)
            PLInputField(
                label = stringResource(R.string.auth_signup_field_name),
                value = state.name,
                onValueChange = actions.onNameChange,
                placeholder = stringResource(R.string.auth_signup_field_name_placeholder),
                errorMessage = state.nameError?.asString(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            )
            PLInputField(
                label = stringResource(R.string.auth_signup_field_email),
                value = state.email,
                onValueChange = actions.onEmailChange,
                placeholder = stringResource(R.string.auth_field_email_placeholder),
                errorMessage = state.emailError?.asString(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            )
            Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.xs)) {
                PLInputField(
                    label = stringResource(R.string.auth_signup_field_password),
                    value = state.password,
                    onValueChange = actions.onPasswordChange,
                    placeholder = stringResource(R.string.auth_signup_field_password_placeholder),
                    isSecure = true,
                    errorMessage = state.passwordError?.asString(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                )
                PLPasswordStrengthBar(state.password)
            }
            PLInputField(
                label = stringResource(R.string.auth_signup_field_confirm),
                value = state.confirmPassword,
                onValueChange = actions.onConfirmPasswordChange,
                placeholder = stringResource(R.string.auth_signup_field_confirm_placeholder),
                isSecure = true,
                errorMessage = state.confirmPasswordError?.asString(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            )
        }
        PLDivider(Modifier.padding(vertical = PLSpacing.md))

        Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.md)) {
            Text(
                stringResource(R.string.auth_signup_optional_header),
                style = PLFont.label.copy(letterSpacing = 1.sp),
                color = PLColor.textDisabled,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(PLSpacing.md)) {
                PLInputField(
                    label = stringResource(R.string.auth_signup_field_cpf),
                    value = state.cpf,
                    onValueChange = { actions.onCpfChange(MaskVisualTransformation.Cpf.sanitize(it)) },
                    placeholder = "000.000.000-00",
                    visualTransformation = MaskVisualTransformation.Cpf,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f),
                )
                PLInputField(
                    label = stringResource(R.string.auth_signup_field_cnpj),
                    value = state.cnpj,
                    onValueChange = { actions.onCnpjChange(MaskVisualTransformation.Cnpj.sanitize(it)) },
                    placeholder = "00.000.000/0001-00",
                    visualTransformation = MaskVisualTransformation.Cnpj,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f),
                )
            }
            PLInputField(
                label = stringResource(R.string.auth_signup_field_phone),
                value = state.phone,
                onValueChange = { actions.onPhoneChange(MaskVisualTransformation.Phone.sanitize(it)) },
                placeholder = "(00) 00000-0000",
                visualTransformation = MaskVisualTransformation.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            )
        }

        state.generalError?.let { error ->
            Row(
                Modifier.padding(top = PLSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(PLSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = PLColor.error, modifier = Modifier.size(14.dp))
                Text(error.asString(), style = PLFont.caption, color = PLColor.error)
            }
        }

        PLDivider(Modifier.padding(vertical = PLSpacing.lg))
        PLButton(
            text = stringResource(R.string.auth_signup_submit),
            onClick = {
                focusManager.clearFocus()
                actions.onSubmit()
            },
            type = if (state.isLoading) PLButtonType.Loading else PLButtonType.Primary,
            enabled = state.canSubmit,
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = PLSpacing.sm),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.auth_signup_have_account), style = PLFont.link, color = PLColor.textMuted)
            TextButton(onClick = actions.onBack) {
                Text(stringResource(R.string.auth_signup_sign_in), style = PLFont.button, color = PLColor.goldBright)
            }
        }
        Spacer(Modifier.height(PLSpacing.xxl))
    }
}

@Preview
@Composable
private fun SignUpScreenPreview() {
    PaintresLumiereTheme {
        SignUpScreen(
            state = SignUpUiState(
                name = "Lucas",
                email = "lucas@",
                password = "Senha123",
                cpf = "12345678901",
                emailError = UiText.Raw("Email already registered"),
            ),
            actions = SignUpActions(),
        )
    }
}
