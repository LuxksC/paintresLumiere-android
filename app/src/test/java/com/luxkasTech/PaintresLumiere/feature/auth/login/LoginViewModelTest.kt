package com.luxkasTech.PaintresLumiere.feature.auth.login

import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.testing.FakeAuthRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()
    private val viewModel = LoginViewModel(repository)

    @Test
    fun `cannot submit until both fields are filled`() {
        assertFalse(viewModel.state.value.canSubmit)
        viewModel.onEmailChange("a@b.com")
        assertFalse(viewModel.state.value.canSubmit)
        viewModel.onPasswordChange("secret")
        assertTrue(viewModel.state.value.canSubmit)
    }

    @Test
    fun `login without required fields does not call the API`() {
        viewModel.login()
        assertTrue(repository.loginCalls.isEmpty())
    }

    @Test
    fun `login shows loading while the request is in flight`() {
        repository.loginResult = CompletableDeferred()
        viewModel.onEmailChange("a@b.com")
        viewModel.onPasswordChange("secret")
        viewModel.login()
        assertTrue(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.canSubmit)
        assertEquals(listOf("a@b.com" to "secret"), repository.loginCalls)
    }

    @Test
    fun `rejected credentials surface the mapped error and stop loading`() {
        repository.loginResult = CompletableDeferred<Unit>().apply { completeExceptionally(ApiError.Unauthorized()) }
        viewModel.onEmailChange("a@b.com")
        viewModel.onPasswordChange("wrong")
        viewModel.login()
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(UiText.Resource(R.string.error_unauthorized), viewModel.state.value.error)
    }

    @Test
    fun `editing a field clears the error`() {
        repository.loginResult = CompletableDeferred<Unit>().apply { completeExceptionally(ApiError.Unauthorized()) }
        viewModel.onEmailChange("a@b.com")
        viewModel.onPasswordChange("wrong")
        viewModel.login()
        viewModel.onPasswordChange("right")
        assertNull(viewModel.state.value.error)
    }
}
