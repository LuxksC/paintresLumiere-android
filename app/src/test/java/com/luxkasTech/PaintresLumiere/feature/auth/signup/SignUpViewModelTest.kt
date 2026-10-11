package com.luxkasTech.PaintresLumiere.feature.auth.signup

import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.testing.FakeAuthRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SignUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()
    private val viewModel = SignUpViewModel(repository)

    private fun fillValidForm() {
        viewModel.onNameChange("Lucas")
        viewModel.onEmailChange("lucas@example.com")
        viewModel.onPasswordChange("Senha123")
        viewModel.onConfirmPasswordChange("Senha123")
    }

    @Test
    fun `valid form can be submitted and sends optional fields`() {
        fillValidForm()
        viewModel.onCpfChange("12345678901")
        assertTrue(viewModel.state.value.canSubmit)
        viewModel.createAccount()
        val call = repository.signUpCalls.single()
        assertEquals("Lucas", call.name)
        assertEquals("12345678901", call.cpf)
        assertEquals("", call.cnpj)
    }

    @Test
    fun `mismatched passwords block submission before the API`() {
        fillValidForm()
        viewModel.onConfirmPasswordChange("Different1")
        assertFalse(viewModel.state.value.canSubmit)
        viewModel.createAccount()
        assertTrue(repository.signUpCalls.isEmpty())
        assertEquals(UiText.Resource(R.string.auth_signup_error_confirm), viewModel.state.value.confirmPasswordError)
    }

    @Test
    fun `validation flags every invalid field`() {
        viewModel.onNameChange("L")
        viewModel.onEmailChange("nope")
        viewModel.onPasswordChange("short")
        assertFalse(viewModel.validate())
        val s = viewModel.state.value
        assertNotNull(s.nameError)
        assertNotNull(s.emailError)
        assertNotNull(s.passwordError)
        assertNull(s.confirmPasswordError)
    }

    @Test
    fun `a 409 lands on the email field`() {
        repository.signUpResult = CompletableDeferred<Unit>().apply { completeExceptionally(ApiError.Conflict("Email already registered")) }
        fillValidForm()
        viewModel.createAccount()
        assertEquals(UiText.Raw("Email already registered"), viewModel.state.value.emailError)
        assertNull(viewModel.state.value.generalError)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `other failures land in the general error`() {
        repository.signUpResult = CompletableDeferred<Unit>().apply { completeExceptionally(ApiError.Network(java.io.IOException())) }
        fillValidForm()
        viewModel.createAccount()
        assertEquals(UiText.Resource(R.string.error_network), viewModel.state.value.generalError)
    }
}
