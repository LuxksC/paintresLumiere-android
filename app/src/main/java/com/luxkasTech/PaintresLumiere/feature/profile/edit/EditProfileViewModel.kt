package com.luxkasTech.PaintresLumiere.feature.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import com.luxkasTech.PaintresLumiere.core.messages.ToastType
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.profile.CurrentUserStore
import com.luxkasTech.PaintresLumiere.data.profile.UpdateProfileRequest
import com.luxkasTech.PaintresLumiere.data.profile.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class EditProfileUiState(
    val name: String = "",
    val lastname: String = "",
    /** Digits only, like sign-up. */
    val phone: String = "",
    val cpf: String = "",
    val cnpj: String = "",
    val email: String = "",
    val nameError: UiText? = null,
    val documentError: UiText? = null,
    val isSaving: Boolean = false,
    val original: UserProfile? = null,
) {
    val hasChanges: Boolean
        get() = original != null && (
            name.trim() != original.name || lastname.trim() != original.lastname.orEmpty() ||
                phone != original.phone.orEmpty() || cpf != original.cpf.orEmpty() || cnpj != original.cnpj.orEmpty()
            )
}

/** Edits name, last name, phone, CPF and CNPJ (`PUT /profile`). Email is shown read-only. */
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val currentUser: CurrentUserStore,
    private val messages: MessagesService,
) : ViewModel() {

    sealed interface Event { data object Saved : Event }

    private val _state = MutableStateFlow(currentUser.profile.value?.let(::fromProfile) ?: EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()
    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    fun onNameChange(v: String) = _state.update { it.copy(name = v, nameError = null) }
    fun onLastnameChange(v: String) = _state.update { it.copy(lastname = v) }
    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v) }
    fun onCpfChange(v: String) = _state.update { it.copy(cpf = v, documentError = null) }
    fun onCnpjChange(v: String) = _state.update { it.copy(cnpj = v, documentError = null) }

    fun save() {
        val s = _state.value
        val original = s.original ?: return
        if (s.isSaving || !s.hasChanges) return
        if (s.name.trim().length < 2) {
            _state.update { it.copy(nameError = UiText.Resource(R.string.auth_signup_error_name)) }
            return
        }
        // Only changed fields are sent; clearing a field sends "" (backend contract PL-90).
        val request = UpdateProfileRequest(
            name = s.name.trim().takeIf { it != original.name },
            lastname = s.lastname.trim().takeIf { it != original.lastname.orEmpty() },
            phone = s.phone.takeIf { it != original.phone.orEmpty() },
            cpf = s.cpf.takeIf { it != original.cpf.orEmpty() },
            cnpj = s.cnpj.takeIf { it != original.cnpj.orEmpty() },
        )
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                currentUser.update(request)
                messages.show(UiText.Resource(R.string.edit_profile_saved), type = ToastType.Success)
                _events.send(Event.Saved)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiError.Conflict) {
                // CPF/CNPJ already used by another account in the tenant.
                _state.update { it.copy(isSaving = false, documentError = e.toUiText()) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false) }
                messages.show(UiText.Resource(R.string.admin_form_save_failed), e.toUiText(), ToastType.Error)
            }
        }
    }

    private fun fromProfile(p: UserProfile) = EditProfileUiState(
        name = p.name,
        lastname = p.lastname.orEmpty(),
        phone = p.phone.orEmpty().filter(Char::isDigit),
        cpf = p.cpf.orEmpty().filter(Char::isDigit),
        cnpj = p.cnpj.orEmpty().filter(Char::isDigit),
        email = p.email,
        original = p.copy(
            phone = p.phone?.filter(Char::isDigit),
            cpf = p.cpf?.filter(Char::isDigit),
            cnpj = p.cnpj?.filter(Char::isDigit),
        ),
    )
}
