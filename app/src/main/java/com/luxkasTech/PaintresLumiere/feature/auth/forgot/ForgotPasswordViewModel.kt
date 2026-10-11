package com.luxkasTech.PaintresLumiere.feature.auth.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: UiText? = null,
    val isLoading: Boolean = false,
    /** Set once the reset link was requested; the screen then shows the confirmation. */
    val linkSent: Boolean = false,
) {
    val canSubmit: Boolean get() = email.isNotBlank() && !isLoading
}

/**
 * Password recovery via `POST /auth/forgot-password`. The API answers 200 whether or not the email
 * exists (no user enumeration), so the confirmation is deliberately neutral: "if this email is
 * registered…". The link in the email opens the reset screen through a deep link.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state: StateFlow<ForgotPasswordUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, emailError = null) }

    fun sendResetLink() {
        val email = _state.value.email.trim()
        if (!email.contains('@')) {
            _state.update { it.copy(emailError = UiText.Resource(R.string.auth_signup_error_email)) }
            return
        }
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                authRepository.requestPasswordReset(email)
                _state.update { it.copy(isLoading = false, linkSent = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, emailError = e.toUiText()) }
            }
        }
    }
}
