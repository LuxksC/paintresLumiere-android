package com.luxkasTech.PaintresLumiere.feature.auth.forgot

import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.testing.FakeAuthRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ForgotPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val viewModel by lazy { ForgotPasswordViewModel(auth) }

    @Test
    fun `invalid email shows an inline error and calls nothing`() {
        viewModel.onEmailChange("nope")
        viewModel.sendResetLink()
        assertEquals(UiText.Resource(R.string.auth_signup_error_email), viewModel.state.value.emailError)
        assertTrue(auth.resetRequests.isEmpty())
    }

    @Test
    fun `valid email requests the link and shows the neutral confirmation`() {
        viewModel.onEmailChange(" lucas@example.com ")
        viewModel.sendResetLink()
        assertEquals(listOf("lucas@example.com"), auth.resetRequests)
        assertTrue(viewModel.state.value.linkSent)
    }

    @Test
    fun `network failure keeps the form`() {
        auth.resetRequestResult = Result.failure(ApiError.Network(IOException()))
        viewModel.onEmailChange("lucas@example.com")
        viewModel.sendResetLink()
        assertFalse(viewModel.state.value.linkSent)
        assertEquals(UiText.Resource(R.string.error_network), viewModel.state.value.emailError)
    }
}
