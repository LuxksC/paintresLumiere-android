package com.luxkasTech.PaintresLumiere.feature.auth.forgot

import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.ToastType
import com.luxkasTech.PaintresLumiere.testing.FakeMessagesService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForgotPasswordViewModelTest {

    private val messages = FakeMessagesService()
    private val viewModel = ForgotPasswordViewModel(messages)

    @Test
    fun `invalid email shows an inline error and no toast`() {
        viewModel.onEmailChange("nope")
        viewModel.sendResetLink()
        assertEquals(UiText.Resource(R.string.auth_signup_error_email), viewModel.state.value.emailError)
        assertTrue(messages.shown.isEmpty())
    }

    @Test
    fun `valid email does not fake a success while the endpoint is missing`() {
        viewModel.onEmailChange("lucas@example.com")
        viewModel.sendResetLink()
        assertFalse(viewModel.state.value.linkSent)
        assertEquals(ToastType.Warning, messages.shown.single().type)
        assertEquals(UiText.Resource(R.string.toast_coming_soon_title), messages.shown.single().title)
    }
}
