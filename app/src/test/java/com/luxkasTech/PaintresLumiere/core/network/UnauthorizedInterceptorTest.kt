package com.luxkasTech.PaintresLumiere.core.network

import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import com.luxkasTech.PaintresLumiere.core.session.TokenStorage
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnauthorizedInterceptorTest {

    private class MemoryStorage(var value: String?) : TokenStorage {
        override fun read() = value
        override fun write(token: String) { value = token }
        override fun clear() { value = null }
    }

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun clientFor(session: SessionManager) = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(session))
        .addInterceptor(UnauthorizedInterceptor(session))
        .build()

    private fun get(client: OkHttpClient) =
        client.newCall(Request.Builder().url(server.url("/profile")).build()).execute().close()

    @Test
    fun `adds the bearer token when signed in`() {
        val session = SessionManager(MemoryStorage("jwt"))
        server.enqueue(MockResponse())
        get(clientFor(session))
        assertEquals("Bearer jwt", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `401 on an authenticated request ends the session`() {
        val session = SessionManager(MemoryStorage("jwt"))
        server.enqueue(MockResponse().setResponseCode(401))
        get(clientFor(session))
        assertFalse(session.isAuthenticated.value)
    }

    @Test
    fun `401 without a session is a plain error`() {
        val storage = MemoryStorage(null)
        val session = SessionManager(storage)
        server.enqueue(MockResponse().setResponseCode(401))
        get(clientFor(session))
        assertEquals(null, server.takeRequest().getHeader("Authorization"))
        assertFalse(session.isAuthenticated.value)
    }

    @Test
    fun `other errors keep the session`() {
        val session = SessionManager(MemoryStorage("jwt"))
        server.enqueue(MockResponse().setResponseCode(500))
        get(clientFor(session))
        assertTrue(session.isAuthenticated.value)
    }
}
