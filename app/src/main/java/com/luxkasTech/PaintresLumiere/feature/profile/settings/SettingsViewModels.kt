package com.luxkasTech.PaintresLumiere.feature.profile.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import com.luxkasTech.PaintresLumiere.core.messages.ToastType
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.auth.AuthRepository
import com.luxkasTech.PaintresLumiere.data.settings.NotificationPreferences
import com.luxkasTech.PaintresLumiere.data.settings.NotificationTopic
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

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val preferences: NotificationPreferences,
) : ViewModel() {
    val enabled: StateFlow<Set<NotificationTopic>> = preferences.enabled
    fun toggle(topic: NotificationTopic, on: Boolean) = preferences.set(topic, on)
}

data class ChangePasswordUiState(
    val current: String = "",
    val new: String = "",
    val confirm: String = "",
    val currentError: UiText? = null,
    val newError: UiText? = null,
    val confirmError: UiText? = null,
    val isSaving: Boolean = false,
)

/** Security → change password (current + new + confirmation). */
@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val messages: MessagesService,
) : ViewModel() {

    sealed interface Event { data object Done : Event }

    private val _state = MutableStateFlow(ChangePasswordUiState())
    val state: StateFlow<ChangePasswordUiState> = _state.asStateFlow()
    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    fun onCurrentChange(v: String) = _state.update { it.copy(current = v, currentError = null) }
    fun onNewChange(v: String) = _state.update { it.copy(new = v, newError = null) }
    fun onConfirmChange(v: String) = _state.update { it.copy(confirm = v, confirmError = null) }

    fun save() {
        val s = _state.value
        if (s.isSaving) return
        val currentError = if (s.current.isEmpty()) UiText.Resource(R.string.admin_form_error_required) else null
        val newError = when {
            s.new.length < 8 -> UiText.Resource(R.string.auth_signup_error_password)
            s.new == s.current -> UiText.Resource(R.string.security_error_same)
            else -> null
        }
        val confirmError = if (s.new != s.confirm) UiText.Resource(R.string.auth_signup_error_confirm) else null
        if (currentError != null || newError != null || confirmError != null) {
            _state.update { it.copy(currentError = currentError, newError = newError, confirmError = confirmError) }
            return
        }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                authRepository.changePassword(s.current, s.new)
                messages.show(UiText.Resource(R.string.security_password_changed), type = ToastType.Success)
                _events.send(Event.Done)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiError.Http) {
                // A wrong current password comes back as 400.
                _state.update { it.copy(isSaving = false, currentError = e.toUiText()) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false) }
                messages.show(UiText.Resource(R.string.admin_form_save_failed), e.toUiText(), ToastType.Error)
            }
        }
    }
}
