package com.luxkasTech.PaintresLumiere.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.auth.AuthRepository
import com.luxkasTech.PaintresLumiere.data.profile.CurrentUserStore
import com.luxkasTech.PaintresLumiere.data.profile.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isProfileLoading: Boolean = true,
    /** Never show stale or placeholder identity as if it were real — an error is shown instead. */
    val profileError: UiText? = null,
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
    private val currentUser: CurrentUserStore,
    private val messages: MessagesService,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
        // Keep showing the latest data after an edit (the store is updated by Edit Profile).
        currentUser.profile
            .onEach { profile -> if (profile != null) _state.update { it.copy(profile = profile) } }
            .launchIn(viewModelScope)
    }

    fun loadProfile() {
        _state.update { it.copy(isProfileLoading = true, profileError = null) }
        viewModelScope.launch {
            try {
                val profile = currentUser.refresh()
                _state.update { it.copy(profile = profile, isProfileLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(profile = null, isProfileLoading = false, profileError = e.toUiText()) }
            }
        }
    }

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
}

enum class ProfileMenuItem { EditProfile, Notifications, Security, Help }
