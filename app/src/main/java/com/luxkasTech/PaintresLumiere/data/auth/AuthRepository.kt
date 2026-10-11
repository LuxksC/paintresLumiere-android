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

    /** Optional fields are sent only when not blank. A 409 (email/CPF/CNPJ taken) is `ApiError.Conflict`. */
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        phone: String?,
        cpf: String?,
        cnpj: String?,
    )
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

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
        phone: String?,
        cpf: String?,
        cnpj: String?,
    ) {
        val request = SignUpRequest(
            name = name.trim(),
            email = email.trim(),
            password = password,
            phone = phone?.takeIf { it.isNotBlank() },
            cpf = cpf?.takeIf { it.isNotBlank() },
            cnpj = cnpj?.takeIf { it.isNotBlank() },
        )
        val response = apiCall { api.signUp(request) }
        session.startSession(response.accessToken)
    }
}
