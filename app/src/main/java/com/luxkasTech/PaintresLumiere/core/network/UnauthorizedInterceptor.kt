package com.luxkasTech.PaintresLumiere.core.network

import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * A 401 on an *authenticated* request means the stored JWT is no longer accepted (expired, user
 * deleted or deactivated). The session is ended, which sends the user back to Login.
 *
 * A 401 on an unauthenticated request (wrong password on `POST /login`) is just a normal error
 * and leaves the session alone. So does a late 401 for a token that is no longer the current one
 * (the user signed in again while the request was in flight): it must not end the new session.
 */
class UnauthorizedInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val sentAuthorization = request.header("Authorization")
        if (response.code == 401 && sentAuthorization != null &&
            sentAuthorization == sessionManager.accessToken()?.let { "Bearer $it" }
        ) {
            sessionManager.endSession()
        }
        return response
    }
}
