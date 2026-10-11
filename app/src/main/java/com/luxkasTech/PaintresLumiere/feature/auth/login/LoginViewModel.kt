package com.luxkasTech.PaintresLumiere.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null,
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotEmpty() && !isLoading
}

/**
 * Email/password sign-in. On success the repository starts the session and the root switches to
 * the Main flow by itself — this ViewModel never navigates.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }

    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    fun login() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                authRepository.login(current.email, current.password)
                // Keep the loading state: the screen is about to be replaced by the Main flow.
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.toUiText()) }
            }
        }
    }
}
