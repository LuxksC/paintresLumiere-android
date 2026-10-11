package com.luxkasTech.PaintresLumiere.feature.profile.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLInputField
import com.luxkasTech.PaintresLumiere.core.designsystem.input.MaskVisualTransformation
import com.luxkasTech.PaintresLumiere.core.designsystem.input.sanitize
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing

@Composable
fun EditProfileRoute(onFinished: () -> Unit, viewModel: EditProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.events.collect { onFinished() } }
    EditProfileScreen(
        state = state,
        onBack = onFinished,
        onNameChange = viewModel::onNameChange,
        onLastnameChange = viewModel::onLastnameChange,
        onPhoneChange = viewModel::onPhoneChange,
        onCpfChange = viewModel::onCpfChange,
        onCnpjChange = viewModel::onCnpjChange,
        onSave = viewModel::save,
    )
}

@Composable
fun EditProfileScreen(
    state: EditProfileUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onLastnameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCpfChange: (String) -> Unit,
    onCnpjChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val words = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
    val numbers = KeyboardOptions(keyboardType = KeyboardType.Number)
    Column(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .statusBarsPadding()
            .imePadding(),
    ) {
        Row(Modifier.padding(horizontal = PLSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), tint = PLColor.textPrimary)
            }
            Text(stringResource(R.string.profile_menu_edit), style = PLFont.h2, color = PLColor.textPrimary, modifier = Modifier.semantics { heading() })
        }
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(PLSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
        ) {
            PLInputField(stringResource(R.string.auth_signup_field_name), state.name, onNameChange, keyboardOptions = words, errorMessage = state.nameError?.asString())
            PLInputField(stringResource(R.string.edit_profile_lastname), state.lastname, onLastnameChange, keyboardOptions = words)
            PLInputField(
                label = stringResource(R.string.auth_signup_field_phone),
                value = state.phone,
                onValueChange = { onPhoneChange(MaskVisualTransformation.Phone.sanitize(it)) },
                visualTransformation = MaskVisualTransformation.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            PLInputField(
                label = stringResource(R.string.auth_signup_field_cpf),
                value = state.cpf,
                onValueChange = { onCpfChange(MaskVisualTransformation.Cpf.sanitize(it)) },
                visualTransformation = MaskVisualTransformation.Cpf,
                keyboardOptions = numbers,
                errorMessage = state.documentError?.let { " " },
            )
            PLInputField(
                label = stringResource(R.string.auth_signup_field_cnpj),
                value = state.cnpj,
                onValueChange = { onCnpjChange(MaskVisualTransformation.Cnpj.sanitize(it)) },
                visualTransformation = MaskVisualTransformation.Cnpj,
                keyboardOptions = numbers,
                errorMessage = state.documentError?.asString(),
            )
            Text(stringResource(R.string.edit_profile_document_hint), style = PLFont.caption, color = PLColor.textDisabled)
            Text(stringResource(R.string.edit_profile_email, state.email), style = PLFont.caption, color = PLColor.textMuted)
            PLButton(
                stringResource(R.string.admin_form_save),
                onClick = onSave,
                type = if (state.isSaving) PLButtonType.Loading else PLButtonType.Primary,
                enabled = state.hasChanges,
            )
        }
    }
}
