package com.luxkasTech.PaintresLumiere.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import com.luxkasTech.PaintresLumiere.data.profile.CurrentUserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class RootViewModel @Inject constructor(
    sessionManager: SessionManager,
    private val currentUser: CurrentUserStore,
) : ViewModel() {
    val isAuthenticated: StateFlow<Boolean> = sessionManager.isAuthenticated
    val isAdmin: StateFlow<Boolean> = currentUser.isAdmin

    init {
        // Load the profile (and so the role) whenever a session starts.
        sessionManager.isAuthenticated
            .filter { it }
            .onEach {
                try {
                    currentUser.refresh()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // No role means no admin surface; the Profile screen shows its own error/retry.
                }
            }
            .launchIn(viewModelScope)
    }
}
