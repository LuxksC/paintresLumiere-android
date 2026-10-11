package com.luxkasTech.PaintresLumiere.core.network

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET

class ApiCallTest {

    @Serializable
    private data class Payload(val value: String)

    private interface TestApi {
        @GET("thing")
        suspend fun thing(): Payload
    }

    private lateinit var server: MockWebServer
    private lateinit var api: TestApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TestApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private suspend fun failureFor(response: MockResponse): ApiError {
        server.enqueue(response)
        try {
            apiCall { api.thing() }
        } catch (e: ApiError) {
            return e
        }
        fail("Expected an ApiError")
        error("unreachable")
    }

    @Test
    fun `success returns the decoded body`() = runTest {
        server.enqueue(MockResponse().setBody("""{"value":"ok","extra":1}"""))
        assertEquals(Payload("ok"), apiCall { api.thing() })
    }

    @Test
    fun `401 maps to Unauthorized`() = runTest {
        assertTrue(failureFor(MockResponse().setResponseCode(401)) is ApiError.Unauthorized)
    }

    @Test
    fun `409 maps to Conflict carrying the server message`() = runTest {
        val error = failureFor(MockResponse().setResponseCode(409).setBody("""{"error":"Email already registered"}"""))
        assertEquals("Email already registered", (error as ApiError.Conflict).serverMessage)
    }

    @Test
    fun `other statuses map to Http with the server message when present`() = runTest {
        val error = failureFor(MockResponse().setResponseCode(404).setBody("""{"error":"Not found"}""")) as ApiError.Http
        assertEquals(404, error.statusCode)
        assertEquals("Not found", error.serverMessage)
    }

    @Test
    fun `a non JSON error body leaves the message empty`() = runTest {
        val error = failureFor(MockResponse().setResponseCode(500).setBody("<html>")) as ApiError.Http
        assertNull(error.serverMessage)
    }

    @Test
    fun `a malformed success body maps to Decoding`() = runTest {
        assertTrue(failureFor(MockResponse().setBody("""{"nope":1}""")) is ApiError.Decoding)
    }

    @Test
    fun `a dropped connection maps to Network`() = runTest {
        assertTrue(
            failureFor(MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AT_START)) is ApiError.Network,
        )
    }
}
