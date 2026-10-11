package com.luxkasTech.PaintresLumiere.data.auth

import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import com.luxkasTech.PaintresLumiere.testing.MemoryTokenStorage
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** The auth repository against a fake HTTP server: request shapes and session side effects. */
class DefaultAuthRepositoryTest {

    private lateinit var server: MockWebServer
    private val storage = MemoryTokenStorage()
    private val session = SessionManager(storage)
    private lateinit var repository: DefaultAuthRepository
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApi::class.java)
        repository = DefaultAuthRepository(api, session)
    }

    @After
    fun tearDown() = server.shutdown()

    private fun token(value: String = "jwt-1") = MockResponse().setBody("""{"accessToken":"$value"}""")

    private fun lastBody() = json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject

    @Test
    fun `login trims the email and starts the session`() = runTest {
        server.enqueue(token())
        repository.login("  ana@example.com ", "secret")
        val body = lastBody()
        assertEquals("\"ana@example.com\"", body["email"].toString())
        assertTrue(session.isAuthenticated.value)
        assertEquals("jwt-1", storage.value)
    }

    @Test
    fun `failed login never stores a token`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        try {
            repository.login("ana@example.com", "wrong")
            fail("Expected Unauthorized")
        } catch (e: ApiError.Unauthorized) {
            assertFalse(session.isAuthenticated.value)
            assertNull(storage.value)
        }
    }

    @Test
    fun `sign up omits blank optional fields`() = runTest {
        server.enqueue(token().setResponseCode(201))
        repository.signUp("Ana", "ana@example.com", "Senha123", phone = "", cpf = "12345678901", cnpj = null)
        val body = lastBody()
        assertFalse("phone" in body)
        assertFalse("cnpj" in body)
        assertEquals("\"12345678901\"", body["cpf"].toString())
        assertTrue(session.isAuthenticated.value)
    }

    @Test
    fun `google exchange sends the id token`() = runTest {
        server.enqueue(token("jwt-google"))
        repository.authenticateWithGoogle("google-token")
        assertEquals("\"google-token\"", lastBody()["idToken"].toString())
        assertEquals("jwt-google", storage.value)
    }

    @Test
    fun `logout ends the session even when the API call fails`() = runTest {
        session.startSession("jwt")
        server.enqueue(MockResponse().setResponseCode(500))
        repository.logout()
        assertFalse(session.isAuthenticated.value)
    }

    @Test
    fun `account deletion keeps the session when the API refuses it`() = runTest {
        session.startSession("jwt")
        server.enqueue(MockResponse().setResponseCode(403).setBody("""{"error":"Not allowed"}"""))
        try {
            repository.deleteAccount()
            fail("Expected Http error")
        } catch (e: ApiError.Http) {
            assertTrue(session.isAuthenticated.value)
        }
    }

    @Test
    fun `account deletion ends the session on success`() = runTest {
        session.startSession("jwt")
        server.enqueue(MockResponse().setBody("""{"message":"User deleted"}"""))
        repository.deleteAccount()
        assertEquals("DELETE", server.takeRequest().method)
        assertFalse(session.isAuthenticated.value)
    }

    @Test
    fun `password reset request trims the email`() = runTest {
        server.enqueue(MockResponse().setBody("""{"message":"ok"}"""))
        repository.requestPasswordReset(" ana@example.com ")
        val request = server.takeRequest()
        assertEquals("/auth/forgot-password", request.path)
        assertTrue(request.body.readUtf8().contains("\"ana@example.com\""))
    }
}
