package com.luxkasTech.PaintresLumiere.feature.profile

import com.luxkasTech.PaintresLumiere.core.messages.ToastType
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.testing.FakeAuthRepository
import com.luxkasTech.PaintresLumiere.testing.FakeMessagesService
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val messages = FakeMessagesService()
    private val viewModel = ProfileViewModel(auth, messages)

    @Test
    fun `logout calls the repository once`() {
        viewModel.logout()
        viewModel.logout()
        assertEquals(1, auth.logoutCalls)
    }

    @Test
    fun `delete asks for confirmation before calling the API`() {
        viewModel.requestDeleteAccount()
        assertTrue(viewModel.state.value.confirmingDelete)
        assertEquals(0, auth.deleteCalls)

        viewModel.confirmDeleteAccount()
        assertFalse(viewModel.state.value.confirmingDelete)
        assertEquals(1, auth.deleteCalls)
    }

    @Test
    fun `dismissing the dialog does not delete`() {
        viewModel.requestDeleteAccount()
        viewModel.dismissDeleteAccount()
        assertFalse(viewModel.state.value.confirmingDelete)
        assertEquals(0, auth.deleteCalls)
    }

    @Test
    fun `a failed delete stops loading and shows an error toast`() {
        auth.deleteResult = CompletableDeferred<Unit>().apply { completeExceptionally(ApiError.Http(500, null)) }
        viewModel.confirmDeleteAccount()
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(ToastType.Error, messages.shown.single().type)
    }
}
