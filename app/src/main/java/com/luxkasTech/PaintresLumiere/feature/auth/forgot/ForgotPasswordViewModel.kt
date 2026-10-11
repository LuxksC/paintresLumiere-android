package com.luxkasTech.PaintresLumiere.feature.auth.forgot

import androidx.lifecycle.ViewModel
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

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
 * Password recovery.
 *
 * The API has no reset endpoints yet (`POST /auth/forgot-password` and
 * `POST /auth/reset-password`, tracked in the API repo). Until they exist this screen does **not**
 * pretend: it validates the email and shows the "coming soon" toast, instead of a success message
 * for an email that will never arrive. iOS currently calls `logout()` here as a placeholder — a
 * known bug that is deliberately not ported. Wiring the real call only needs to replace the body of
 * [sendResetLink] and set [ForgotPasswordUiState.linkSent].
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val messages: MessagesService,
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
        messages.showComingSoon()
    }
}
