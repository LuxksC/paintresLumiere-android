package com.luxkasTech.PaintresLumiere.data.profile

import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import com.luxkasTech.PaintresLumiere.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The signed-in user's profile, shared app-wide (Profile screen, admin gating). Cleared as soon as
 * the session ends so a second account on the same device never sees the previous user's data or
 * role.
 *
 * The role comes from `GET /profile` → `type`. Anything but a confirmed `admin` — including an
 * unknown value or a profile that failed to load — means **no admin access**.
 */
@Singleton
class CurrentUserStore @Inject constructor(
    private val profileRepository: ProfileRepository,
    sessionManager: SessionManager,
    @ApplicationScope scope: CoroutineScope,
) {
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    val isAdmin: StateFlow<Boolean> = _profile
        .map { it?.type == UserType.ADMIN }
        .stateIn(scope, SharingStarted.Eagerly, false)

    init {
        sessionManager.isAuthenticated
            .onEach { signedIn -> if (!signedIn) _profile.value = null }
            .launchIn(scope)
    }

    /** Fetches the profile and publishes it. Throws `ApiError` (and keeps the previous value). */
    suspend fun refresh(): UserProfile = profileRepository.getProfile().also { _profile.value = it }
}
