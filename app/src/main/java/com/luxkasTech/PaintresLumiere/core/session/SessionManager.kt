package com.luxkasTech.PaintresLumiere.core.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Supplies the bearer token to the network layer without exposing the rest of the session. */
fun interface AccessTokenProvider {
    fun accessToken(): String?
}

/**
 * Single source of truth for "is the user signed in". The root navigation observes
 * [isAuthenticated] to switch between the Auth and Main graphs, the same way the iOS
 * `AppCoordinator` checks `KeychainService.isAuthenticated`.
 *
 * The token is read from storage once and then served from memory, so the network interceptor
 * never decrypts on every request.
 */
@Singleton
class SessionManager @Inject constructor(
    private val storage: TokenStorage,
) : AccessTokenProvider {

    @Volatile
    private var token: String? = storage.read()

    private val _isAuthenticated = MutableStateFlow(token != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    override fun accessToken(): String? = token

    fun startSession(accessToken: String) {
        storage.write(accessToken)
        token = accessToken
        _isAuthenticated.value = true
    }

    fun endSession() {
        storage.clear()
        token = null
        _isAuthenticated.value = false
    }
}
