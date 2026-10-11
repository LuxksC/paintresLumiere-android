package com.luxkasTech.PaintresLumiere.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import com.luxkasTech.PaintresLumiere.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class ProfileUiState(
    val isLoading: Boolean = false,
    val confirmingDelete: Boolean = false,
)

/**
 * Account actions. Logging out or deleting the account ends the session, and the root swaps to the
 * Auth flow — this ViewModel never navigates. Unlike iOS today, deleting asks for confirmation first.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val messages: MessagesService,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun logout() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch { authRepository.logout() }
    }

    fun requestDeleteAccount() = _state.update { it.copy(confirmingDelete = true) }

    fun dismissDeleteAccount() = _state.update { it.copy(confirmingDelete = false) }

    fun confirmDeleteAccount() {
        _state.update { it.copy(confirmingDelete = false, isLoading = true) }
        viewModelScope.launch {
            try {
                authRepository.deleteAccount()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                messages.showGenericError()
            }
        }
    }

    /** Edit profile, notifications, security and help screens are separate tickets. */
    fun onMenuItemClick(item: ProfileMenuItem) = messages.showComingSoon()
}

enum class ProfileMenuItem { EditProfile, Notifications, Security, Help }
