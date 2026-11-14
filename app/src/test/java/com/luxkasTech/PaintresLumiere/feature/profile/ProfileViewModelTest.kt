package com.luxkasTech.PaintresLumiere.feature.profile

import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.ToastType
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.testing.FakeAuthRepository
import com.luxkasTech.PaintresLumiere.data.profile.UserProfile
import com.luxkasTech.PaintresLumiere.testing.FakeMessagesService
import com.luxkasTech.PaintresLumiere.testing.FakeProfileRepository
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
    private val profiles = FakeProfileRepository()
    private val viewModel by lazy { ProfileViewModel(auth, profiles, messages) }

    @Test
    fun `loads the real profile on start`() {
        val state = viewModel.state.value
        assertEquals("Lucas", state.profile?.name)
        assertFalse(state.isProfileLoading)
    }

    @Test
    fun `a failed profile load shows an error instead of identity data`() {
        profiles.result = Result.failure(ApiError.Network(java.io.IOException()))
        val state = viewModel.state.value
        assertEquals(null, state.profile)
        assertEquals(UiText.Resource(R.string.error_network), state.profileError)
    }

    @Test
    fun `retry reloads the profile`() {
        profiles.result = Result.failure(ApiError.Network(java.io.IOException()))
        viewModel.state.value
        profiles.result = Result.success(UserProfile(id = "1", name = "Ana", email = "ana@example.com"))
        viewModel.loadProfile()
        assertEquals("Ana", viewModel.state.value.profile?.name)
        assertEquals(2, profiles.calls)
    }

    @Test
    fun `display name joins name and last name`() {
        assertEquals("Ana Lima", UserProfile(id = "1", name = "Ana", lastname = "Lima", email = "a@b.c").displayName)
        assertEquals("Ana", UserProfile(id = "1", name = "Ana", lastname = " ", email = "a@b.c").displayName)
    }

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
