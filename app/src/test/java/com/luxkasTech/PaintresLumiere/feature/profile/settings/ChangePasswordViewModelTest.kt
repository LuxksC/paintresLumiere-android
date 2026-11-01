package com.luxkasTech.PaintresLumiere.feature.profile.settings

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

class ChangePasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val vm by lazy { ChangePasswordViewModel(auth, FakeMessagesService()) }

    private fun fill(current: String = "Antiga123", new: String = "NovaSenha1", confirm: String = new) {
        vm.onCurrentChange(current)
        vm.onNewChange(new)
        vm.onConfirmChange(confirm)
    }

    @Test
    fun `valid change is sent and finishes`() = runTest {
        fill()
        vm.save()
        assertEquals(listOf("Antiga123" to "NovaSenha1"), auth.passwordChanges)
        assertEquals(ChangePasswordViewModel.Event.Done, vm.events.first())
    }

    @Test
    fun `the new password must differ, be long enough and match`() {
        fill(new = "Antiga123")
        vm.save()
        assertEquals(UiText.Resource(R.string.security_error_same), vm.state.value.newError)
        fill(new = "curta")
        vm.save()
        assertEquals(UiText.Resource(R.string.auth_signup_error_password), vm.state.value.newError)
        fill(confirm = "outra-coisa")
        vm.save()
        assertEquals(UiText.Resource(R.string.auth_signup_error_confirm), vm.state.value.confirmError)
        assertTrue(auth.passwordChanges.isEmpty())
    }

    @Test
    fun `a wrong current password shows on that field`() {
        auth.changePasswordResult = Result.failure(ApiError.Http(400, "Senha atual incorreta"))
        fill()
        vm.save()
        assertEquals(UiText.Raw("Senha atual incorreta"), vm.state.value.currentError)
    }
}
