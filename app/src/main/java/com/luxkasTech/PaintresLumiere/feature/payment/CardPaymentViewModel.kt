package com.luxkasTech.PaintresLumiere.feature.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.payment.CardBrand
import com.luxkasTech.PaintresLumiere.data.payment.CardInput
import com.luxkasTech.PaintresLumiere.data.payment.CardTokenizationUnavailableException
import com.luxkasTech.PaintresLumiere.data.payment.CardTokenizer
import com.luxkasTech.PaintresLumiere.data.payment.CardValidation
import com.luxkasTech.PaintresLumiere.data.payment.PaymentMethod
import com.luxkasTech.PaintresLumiere.data.payment.PaymentRepository
import com.luxkasTech.PaintresLumiere.data.payment.PaymentStatus
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

enum class CardField { Number, Holder, Expiry, Cvv }

data class CardPaymentUiState(
    val installments: Int = 1,
    /** Digits only, in memory only — never saved to SavedStateHandle, disk or logs. */
    val number: String = "",
    val holder: String = "",
    val expiry: String = "",
    val cvv: String = "",
    val errors: Set<CardField> = emptySet(),
    val isSubmitting: Boolean = false,
    /** Card declined / unavailable: message plus the option to pick another method. */
    val failure: UiText? = null,
) {
    val brand: CardBrand get() = CardValidation.brandOf(number)
}

@HiltViewModel
class CardPaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tokenizer: CardTokenizer,
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    sealed interface Event {
        data class Paid(val orderId: String) : Event
    }

    private val orderId: String = checkNotNull(savedStateHandle[ORDER_ID_ARG])
    private val _state = MutableStateFlow(CardPaymentUiState(installments = savedStateHandle[INSTALLMENTS_ARG] ?: 1))
    val state: StateFlow<CardPaymentUiState> = _state.asStateFlow()
    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    fun onNumberChange(v: String) = edit { it.copy(number = v.filter(Char::isDigit).take(19)) }
    fun onHolderChange(v: String) = edit { it.copy(holder = v.take(60)) }
    fun onExpiryChange(v: String) = edit { it.copy(expiry = v.filter(Char::isDigit).take(4)) }
    fun onCvvChange(v: String) = edit { it.copy(cvv = v.filter(Char::isDigit).take(4)) }

    private fun edit(transform: (CardPaymentUiState) -> CardPaymentUiState) =
        _state.update { transform(it).copy(errors = emptySet(), failure = null) }

    fun pay() {
        val s = _state.value
        if (s.isSubmitting) return
        val errors = buildSet {
            if (!CardValidation.isValidNumber(s.number)) add(CardField.Number)
            if (s.holder.trim().length < 3) add(CardField.Holder)
            if (!CardValidation.isValidExpiry(s.expiry)) add(CardField.Expiry)
            if (!CardValidation.isValidCvv(s.cvv, s.brand)) add(CardField.Cvv)
        }
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        _state.update { it.copy(isSubmitting = true, failure = null) }
        viewModelScope.launch {
            try {
                val token = tokenizer.tokenize(CardInput(s.number, s.holder.trim(), s.expiry, s.cvv))
                val payment = paymentRepository.createPayment(orderId, PaymentMethod.CREDIT_CARD, s.installments, token)
                if (payment.status.isPaid) {
                    // Drop the card data from memory as soon as it is no longer needed.
                    _state.value = CardPaymentUiState(installments = s.installments)
                    _events.send(Event.Paid(orderId))
                } else {
                    val reason = if (payment.status == PaymentStatus.FAILED) R.string.card_declined else R.string.card_not_confirmed
                    _state.update { it.copy(isSubmitting = false, cvv = "", failure = UiText.Resource(reason)) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: CardTokenizationUnavailableException) {
                _state.update { it.copy(isSubmitting = false, failure = UiText.Resource(R.string.card_unavailable)) }
            } catch (e: Exception) {
                // The backend never forwards the acquirer's raw reason; show a generic message.
                _state.update { it.copy(isSubmitting = false, cvv = "", failure = e.toUiText()) }
            }
        }
    }

    companion object {
        const val ORDER_ID_ARG = "orderId"
        const val INSTALLMENTS_ARG = "installments"
    }
}
