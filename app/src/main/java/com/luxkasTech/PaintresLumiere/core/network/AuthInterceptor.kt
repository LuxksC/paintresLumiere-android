package com.luxkasTech.PaintresLumiere.core.network

import com.luxkasTech.PaintresLumiere.core.session.AccessTokenProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Adds `Authorization: Bearer <jwt>` whenever there is a session — like iOS `APIEndpoint.urlRequest()`. */
class AuthInterceptor @Inject constructor(
    private val tokenProvider: AccessTokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.accessToken()
        val request = if (token != null && chain.request().header(HEADER) == null) {
            chain.request().newBuilder().header(HEADER, "Bearer $token").build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }

    private companion object {
        const val HEADER = "Authorization"
    }
}
