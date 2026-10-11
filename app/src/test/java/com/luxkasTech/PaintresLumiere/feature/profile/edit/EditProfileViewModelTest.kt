package com.luxkasTech.PaintresLumiere.feature.profile.edit

import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import com.luxkasTech.PaintresLumiere.data.profile.CurrentUserStore
import com.luxkasTech.PaintresLumiere.data.profile.UpdateProfileRequest
import com.luxkasTech.PaintresLumiere.data.profile.UserProfile
import com.luxkasTech.PaintresLumiere.testing.FakeMessagesService
import com.luxkasTech.PaintresLumiere.testing.FakeProfileRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import com.luxkasTech.PaintresLumiere.testing.MemoryTokenStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EditProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profiles = FakeProfileRepository(
        Result.success(UserProfile(id = "1", name = "Ana", lastname = "Lima", phone = "(11) 98765-4321", email = "ana@example.com")),
    )
    private val store = CurrentUserStore(profiles, SessionManager(MemoryTokenStorage("jwt")), CoroutineScope(UnconfinedTestDispatcher()))
    private val messages = FakeMessagesService()

    private suspend fun vm(): EditProfileViewModel {
        store.refresh()
        return EditProfileViewModel(store, messages)
    }

    @Test
    fun `prefills from the current profile with digits only`() = runTest {
        val s = vm().state.value
        assertEquals("Ana", s.name)
        assertEquals("11987654321", s.phone)
        assertFalse(s.hasChanges)
    }

    @Test
    fun `sends only what changed, and an empty string to clear`() = runTest {
        val vm = vm()
        vm.onCpfChange("12345678901")
        vm.onLastnameChange("")
        vm.save()
        assertEquals(UpdateProfileRequest(lastname = "", cpf = "12345678901"), profiles.updates.single())
        assertEquals(EditProfileViewModel.Event.Saved, vm.events.first())
    }

    @Test
    fun `a taken document lands on the document fields`() = runTest {
        profiles.updateResult = Result.failure(ApiError.Conflict("CPF já cadastrado"))
        val vm = vm()
        vm.onCpfChange("12345678901")
        vm.save()
        assertEquals(UiText.Raw("CPF já cadastrado"), vm.state.value.documentError)
        assertFalse(vm.state.value.isSaving)
    }

    @Test
    fun `a too short name is rejected locally`() = runTest {
        val vm = vm()
        vm.onNameChange("A")
        vm.save()
        assertTrue(profiles.updates.isEmpty())
    }
}
