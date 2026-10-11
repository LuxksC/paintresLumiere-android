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
 * and leaves the session alone.
 */
class UnauthorizedInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 401 && request.header("Authorization") != null) {
            sessionManager.endSession()
        }
        return response
    }
}
