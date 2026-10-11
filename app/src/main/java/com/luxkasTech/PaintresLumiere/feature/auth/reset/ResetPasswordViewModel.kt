package com.luxkasTech.PaintresLumiere.feature.auth.reset

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import com.luxkasTech.PaintresLumiere.core.messages.ToastType
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class ResetPasswordUiState(
    val password: String = "",
    val confirm: String = "",
    val passwordError: UiText? = null,
    val confirmError: UiText? = null,
    val error: UiText? = null,
    val isLoading: Boolean = false,
)

/** "Set a new password", opened from the email link (`token` query parameter). */
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val messages: MessagesService,
) : ViewModel() {

    sealed interface Event { data object Done : Event }

    private val token: String = savedStateHandle.get<String>(TOKEN_ARG).orEmpty()
    private val _state = MutableStateFlow(ResetPasswordUiState())
    val state: StateFlow<ResetPasswordUiState> = _state.asStateFlow()
    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, passwordError = null, error = null) }
    fun onConfirmChange(v: String) = _state.update { it.copy(confirm = v, confirmError = null, error = null) }

    fun submit() {
        val s = _state.value
        if (s.isLoading) return
        // Same rule as sign-up (the backend card asks to keep both in sync).
        val passwordError = if (s.password.length < 8) UiText.Resource(R.string.auth_signup_error_password) else null
        val confirmError = if (s.password != s.confirm) UiText.Resource(R.string.auth_signup_error_confirm) else null
        if (passwordError != null || confirmError != null || token.isBlank()) {
            _state.update {
                it.copy(
                    passwordError = passwordError,
                    confirmError = confirmError,
                    error = if (token.isBlank()) UiText.Resource(R.string.reset_invalid_link) else null,
                )
            }
            return
        }
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                authRepository.resetPassword(token, s.password)
                messages.show(UiText.Resource(R.string.reset_done), type = ToastType.Success)
                _events.send(Event.Done)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Expired / used / unknown token → generic 400 from the API.
                _state.update { it.copy(isLoading = false, error = e.toUiText()) }
            }
        }
    }

    companion object {
        const val TOKEN_ARG = "token"
    }
}
