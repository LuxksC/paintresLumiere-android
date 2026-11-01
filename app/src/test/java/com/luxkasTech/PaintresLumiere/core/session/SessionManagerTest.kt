package com.luxkasTech.PaintresLumiere.core.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionManagerTest {

    private class FakeTokenStorage(var value: String? = null) : TokenStorage {
        override fun read() = value
        override fun write(token: String) { value = token }
        override fun clear() { value = null }
    }

    @Test
    fun `starts authenticated when a token is already stored`() {
        val session = SessionManager(FakeTokenStorage("jwt"))
        assertTrue(session.isAuthenticated.value)
        assertEquals("jwt", session.accessToken())
    }

    @Test
    fun `starts signed out when storage is empty`() {
        val session = SessionManager(FakeTokenStorage())
        assertFalse(session.isAuthenticated.value)
        assertNull(session.accessToken())
    }

    @Test
    fun `startSession persists the token and flips the state`() {
        val storage = FakeTokenStorage()
        val session = SessionManager(storage)
        session.startSession("new-jwt")
        assertEquals("new-jwt", storage.value)
        assertEquals("new-jwt", session.accessToken())
        assertTrue(session.isAuthenticated.value)
    }

    @Test
    fun `endSession clears storage and memory`() {
        val storage = FakeTokenStorage("jwt")
        val session = SessionManager(storage)
        session.endSession()
        assertNull(storage.value)
        assertNull(session.accessToken())
        assertFalse(session.isAuthenticated.value)
    }
}
