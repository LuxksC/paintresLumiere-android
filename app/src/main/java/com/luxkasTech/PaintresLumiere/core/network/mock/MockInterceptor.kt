package com.luxkasTech.PaintresLumiere.core.network.mock

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Answers every request from [MockRouter] instead of the network (the `mock` build flavor).
 *
 * It sits at the end of the OkHttp chain, so everything above it — auth header, 401 handling,
 * Retrofit, kotlinx.serialization decoding and `ApiError` mapping — runs exactly as with the real
 * API. A mock that doesn't match the models fails the same way a bad API response would.
 */
class MockInterceptor(private val router: MockRouter) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val mock = router.resolve(request.method, request.url.encodedPath)
        if (mock.delayMs > 0) Thread.sleep(mock.delayMs)
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(mock.status)
            .message("Mock ${mock.status}")
            .body(mock.body.toResponseBody(JSON))
            .build()
    }

    private companion object {
        val JSON = "application/json".toMediaType()
    }
}
