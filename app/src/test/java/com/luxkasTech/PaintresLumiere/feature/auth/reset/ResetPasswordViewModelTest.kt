package com.luxkasTech.PaintresLumiere.feature.auth.reset

import androidx.lifecycle.SavedStateHandle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.testing.FakeAuthRepository
import com.luxkasTech.PaintresLumiere.testing.FakeMessagesService
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ResetPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private fun vm(token: String = "tok") = ResetPasswordViewModel(SavedStateHandle(mapOf("token" to token)), auth, FakeMessagesService())

    @Test
    fun `sends the token with the new password`() = runTest {
        val vm = vm()
        vm.onPasswordChange("NovaSenha1")
        vm.onConfirmChange("NovaSenha1")
        vm.submit()
        assertEquals(listOf("tok" to "NovaSenha1"), auth.passwordResets)
        assertEquals(ResetPasswordViewModel.Event.Done, vm.events.first())
    }

    @Test
    fun `short or mismatched passwords never reach the API`() {
        val vm = vm()
        vm.onPasswordChange("curta")
        vm.onConfirmChange("outra")
        vm.submit()
        assertTrue(auth.passwordResets.isEmpty())
        assertEquals(UiText.Resource(R.string.auth_signup_error_password), vm.state.value.passwordError)
        assertEquals(UiText.Resource(R.string.auth_signup_error_confirm), vm.state.value.confirmError)
    }

    @Test
    fun `a link without token is reported as invalid`() {
        val vm = vm(token = "")
        vm.onPasswordChange("NovaSenha1")
        vm.onConfirmChange("NovaSenha1")
        vm.submit()
        assertEquals(UiText.Resource(R.string.reset_invalid_link), vm.state.value.error)
    }

    @Test
    fun `an expired token shows the server message`() {
        auth.passwordResetResult = Result.failure(ApiError.Http(400, "Token inválido ou expirado"))
        val vm = vm()
        vm.onPasswordChange("NovaSenha1")
        vm.onConfirmChange("NovaSenha1")
        vm.submit()
        assertEquals(UiText.Raw("Token inválido ou expirado"), vm.state.value.error)
    }
}
