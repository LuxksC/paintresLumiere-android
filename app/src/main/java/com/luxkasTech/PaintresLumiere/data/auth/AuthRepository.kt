package com.luxkasTech.PaintresLumiere.data.auth

import com.luxkasTech.PaintresLumiere.core.network.apiCall
import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authentication — the Android counterpart of iOS `AuthServiceProtocol`. A successful sign-in
 * starts the session, which is what moves the app from the Auth flow to the Main flow. Throws
 * `ApiError`.
 */
interface AuthRepository {
    suspend fun login(email: String, password: String)
}

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val api: AuthApi,
    private val session: SessionManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String) {
        val response = apiCall { api.login(LoginRequest(email.trim(), password)) }
        session.startSession(response.accessToken)
    }
}
