package com.luxkasTech.PaintresLumiere.data.auth

import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import com.luxkasTech.PaintresLumiere.core.session.TokenStorage
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class DefaultAuthRepositoryTest {

    private class MemoryStorage(var value: String?) : TokenStorage {
        override fun read() = value
        override fun write(token: String) { value = token }
        override fun clear() { value = null }
    }

    private class FakeAuthApi : AuthApi {
        var logoutError: Exception? = null
        var deleteError: Exception? = null
        override suspend fun login(body: LoginRequest) = error("unused")
        override suspend fun signUp(body: SignUpRequest) = error("unused")
        override suspend fun logout() { logoutError?.let { throw it } }
        override suspend fun deleteAccount() { deleteError?.let { throw it } }
    }

    private val api = FakeAuthApi()
    private val session = SessionManager(MemoryStorage("jwt"))
    private val repository = DefaultAuthRepository(api, session)

    private fun httpError(code: Int) = HttpException(Response.error<Unit>(code, "".toResponseBody()))

    @Test
    fun `logout ends the session even when the API is unreachable`() = runTest {
        api.logoutError = IOException()
        repository.logout()
        assertFalse(session.isAuthenticated.value)
    }

    @Test
    fun `deleting an account that is already gone ends the session`() = runTest {
        api.deleteError = httpError(404)
        repository.deleteAccount()
        assertFalse(session.isAuthenticated.value)
    }

    @Test
    fun `a failed delete keeps the session and rethrows`() = runTest {
        api.deleteError = httpError(500)
        val result = runCatching { repository.deleteAccount() }
        assertTrue(result.exceptionOrNull() is ApiError.Http)
        assertTrue(session.isAuthenticated.value)
    }
}
