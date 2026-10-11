package com.luxkasTech.PaintresLumiere.core.network

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Debug-only HTTP logging. Full bodies are logged for most routes (handy for the catalog), but
 * routes whose bodies carry passwords, reset tokens or card tokens only log headers — those must
 * never reach logcat. `Authorization` is always redacted.
 */
class DebugLoggingInterceptor : Interceptor {

    private val full = logger(HttpLoggingInterceptor.Level.BODY)
    private val headersOnly = logger(HttpLoggingInterceptor.Level.HEADERS)

    override fun intercept(chain: Interceptor.Chain): Response {
        val target = if (isSensitivePath(chain.request().url.encodedPath)) headersOnly else full
        return target.intercept(chain)
    }

    private fun logger(level: HttpLoggingInterceptor.Level) = HttpLoggingInterceptor().apply {
        this.level = level
        redactHeader("Authorization")
    }

    companion object {
        private val SENSITIVE = listOf(
            Regex("^/login$"),
            Regex("^/signup$"),
            Regex("^/auth/.*"),
            Regex("^/profile/password$"),
            Regex("^/orders/[^/]+/payment$"),
        )

        fun isSensitivePath(path: String): Boolean = SENSITIVE.any { it.matches(path) }
    }
}
