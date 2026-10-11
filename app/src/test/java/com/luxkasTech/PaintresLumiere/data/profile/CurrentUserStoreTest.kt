package com.luxkasTech.PaintresLumiere.data.profile

import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import com.luxkasTech.PaintresLumiere.testing.FakeProfileRepository
import com.luxkasTech.PaintresLumiere.testing.MemoryTokenStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CurrentUserStoreTest {

    private val session = SessionManager(MemoryTokenStorage("jwt"))
    private val profiles = FakeProfileRepository()

    private fun TestScope.store() = CurrentUserStore(profiles, session, CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

    private fun profile(type: UserType) = UserProfile(id = "1", type = type, name = "Ana", email = "ana@example.com")

    @Test
    fun `admin type grants admin`() = runTest {
        profiles.result = Result.success(profile(UserType.ADMIN))
        val store = store()
        store.refresh()
        assertTrue(store.isAdmin.value)
    }

    @Test
    fun `client and unknown types never grant admin`() = runTest {
        val store = store()
        profiles.result = Result.success(profile(UserType.CLIENT))
        store.refresh()
        assertFalse(store.isAdmin.value)
        profiles.result = Result.success(profile(UserType.UNKNOWN))
        store.refresh()
        assertFalse(store.isAdmin.value)
    }

    @Test
    fun `no profile means no admin`() = runTest {
        profiles.result = Result.failure(ApiError.Network(IOException()))
        val store = store()
        runCatching { store.refresh() }
        assertFalse(store.isAdmin.value)
    }

    @Test
    fun `signing out forgets the profile and the role`() = runTest {
        profiles.result = Result.success(profile(UserType.ADMIN))
        val store = store()
        store.refresh()
        session.endSession()
        assertNull(store.profile.value)
        assertFalse(store.isAdmin.value)
    }

    @Test
    fun `unknown type strings decode to UNKNOWN`() {
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val decoded = json.decodeFromString<UserProfile>("""{"id":"1","type":"super_admin","name":"A","email":"a@b.c"}""")
        assertEquals(UserType.UNKNOWN, decoded.type)
    }
}
