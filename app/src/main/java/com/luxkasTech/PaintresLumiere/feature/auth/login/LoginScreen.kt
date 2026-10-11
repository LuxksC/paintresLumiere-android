package com.luxkasTech.PaintresLumiere.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLDivider
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLInputField
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLOrDivider
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PaintresLumiereTheme
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.feature.auth.components.BrandHeader
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    onSignUpRequested: () -> Unit,
    onForgotPasswordRequested: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LoginScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLogin = viewModel::login,
        onForgotPassword = onForgotPasswordRequested,
        onSignUp = onSignUpRequested,
        onGoogleSignIn = if (GoogleSignIn.isConfigured) {
            {
                scope.launch {
                    when (val result = GoogleSignIn.requestIdToken(context)) {
                        is GoogleSignIn.Result.Token -> viewModel.loginWithGoogle(result.idToken)
                        GoogleSignIn.Result.Cancelled -> Unit
                        GoogleSignIn.Result.Failed -> viewModel.onGoogleSignInFailed()
                    }
                }
            }
        } else {
            null
        },
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit,
    /** Null hides the Google button (client ID not configured in this build). */
    onGoogleSignIn: (() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val errorMessage = state.error?.asString()
    Box(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .drawBehind {
                // Subtle gold glow near the bottom, as on iOS.
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(PLColor.goldAntique.copy(alpha = 0.18f), PLColor.backgroundPrimary),
                        center = Offset(size.width * 0.5f, size.height * 0.85f),
                        radius = 320.dp.toPx(),
                    ),
                )
            },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PLSpacing.xl),
        ) {
            BrandHeader(Modifier.padding(top = 82.dp))

            Column(Modifier.padding(top = PLSpacing.xxl), verticalArrangement = Arrangement.spacedBy(PLSpacing.xs)) {
                Text(stringResource(R.string.auth_login_title), style = PLFont.h2, color = PLColor.textPrimary)
                Text(stringResource(R.string.auth_login_subtitle), style = PLFont.body, color = PLColor.textMuted)
            }
            PLDivider(Modifier.padding(top = PLSpacing.lg))

            Column(Modifier.padding(top = PLSpacing.md), verticalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
                PLInputField(
                    label = stringResource(R.string.auth_field_email),
                    value = state.email,
                    onValueChange = onEmailChange,
                    placeholder = stringResource(R.string.auth_field_email_placeholder),
                    errorMessage = if (errorMessage != null) " " else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                )
                PLInputField(
                    label = stringResource(R.string.auth_field_password),
                    value = state.password,
                    onValueChange = onPasswordChange,
                    placeholder = stringResource(R.string.auth_login_password_placeholder),
                    isSecure = true,
                    errorMessage = errorMessage,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        onLogin()
                    }),
                )
            }
            TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.auth_login_forgot_password), style = PLFont.link, color = PLColor.goldMid)
            }

            PLDivider(Modifier.padding(top = PLSpacing.md))
            PLButton(
                text = stringResource(R.string.auth_login_submit),
                onClick = {
                    focusManager.clearFocus()
                    onLogin()
                },
                type = if (state.isLoading) PLButtonType.Loading else PLButtonType.Primary,
                enabled = state.canSubmit,
                modifier = Modifier.padding(top = PLSpacing.lg),
            )

            if (onGoogleSignIn != null) {
                PLOrDivider(Modifier.padding(vertical = PLSpacing.md))
                PLButton(
                    text = stringResource(R.string.auth_login_google),
                    onClick = onGoogleSignIn,
                    type = PLButtonType.Ghost,
                    enabled = !state.isLoading,
                )
            }

            PLDivider(Modifier.padding(top = PLSpacing.lg))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = PLSpacing.sm),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.auth_login_no_account), style = PLFont.link, color = PLColor.textMuted)
                TextButton(onClick = onSignUp) {
                    Text(stringResource(R.string.auth_login_create_account), style = PLFont.button, color = PLColor.goldBright)
                }
            }
            Spacer(Modifier.height(PLSpacing.xxl))
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    PaintresLumiereTheme {
        LoginScreen(
            state = LoginUiState(email = "lucas@exemplo.com", error = UiText.Raw("Credenciais inválidas.")),
            onEmailChange = {},
            onPasswordChange = {},
            onLogin = {},
            onForgotPassword = {},
            onSignUp = {},
            onGoogleSignIn = {},
        )
    }
}
