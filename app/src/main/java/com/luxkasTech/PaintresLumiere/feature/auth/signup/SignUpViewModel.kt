package com.luxkasTech.PaintresLumiere.feature.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    /** Digits only; formatting is display-only. */
    val phone: String = "",
    val cpf: String = "",
    val cnpj: String = "",
    val isLoading: Boolean = false,
    val nameError: UiText? = null,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val confirmPasswordError: UiText? = null,
    val generalError: UiText? = null,
) {
    val passwordsMatch: Boolean get() = confirmPassword.isEmpty() || password == confirmPassword

    /** Same rule as iOS `SignUpViewModel.canSubmit`. */
    val canSubmit: Boolean
        get() = name.trim().length >= MIN_NAME && email.contains('@') && password.length >= MIN_PASSWORD &&
            passwordsMatch && !isLoading

    companion object {
        const val MIN_NAME = 2
        const val MIN_PASSWORD = 8
    }
}

/** Self-service sign-up. Success starts the session; the root then shows the Main flow. */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpUiState())
    val state: StateFlow<SignUpUiState> = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value, nameError = null, generalError = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, emailError = null, generalError = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, passwordError = null) }
    fun onConfirmPasswordChange(value: String) = _state.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    fun onPhoneChange(value: String) = _state.update { it.copy(phone = value) }
    fun onCpfChange(value: String) = _state.update { it.copy(cpf = value) }
    fun onCnpjChange(value: String) = _state.update { it.copy(cnpj = value) }

    fun createAccount() {
        if (!validate()) return
        val s = _state.value
        _state.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            try {
                authRepository.signUp(s.name, s.email, s.password, s.phone, s.cpf, s.cnpj)
            } catch (e: ApiError.Conflict) {
                // Email, CPF or CNPJ already registered — the server message says which.
                _state.update { it.copy(isLoading = false, emailError = e.toUiText()) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, generalError = e.toUiText()) }
            }
        }
    }

    /** Inline validation, run before any network call. Returns true when the form is valid. */
    fun validate(): Boolean {
        val s = _state.value
        val nameError = if (s.name.trim().length < SignUpUiState.MIN_NAME) UiText.Resource(R.string.auth_signup_error_name) else null
        val emailError = if (!s.email.contains('@')) UiText.Resource(R.string.auth_signup_error_email) else null
        val passwordError = if (s.password.length < SignUpUiState.MIN_PASSWORD) UiText.Resource(R.string.auth_signup_error_password) else null
        val confirmError = if (!s.passwordsMatch) UiText.Resource(R.string.auth_signup_error_confirm) else null
        _state.update {
            it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError, confirmPasswordError = confirmError)
        }
        return listOf(nameError, emailError, passwordError, confirmError).all { it == null }
    }
}
