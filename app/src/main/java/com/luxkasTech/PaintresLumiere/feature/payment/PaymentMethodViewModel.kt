package com.luxkasTech.PaintresLumiere.feature.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.order.OrderDetail
import com.luxkasTech.PaintresLumiere.data.order.OrderRepository
import com.luxkasTech.PaintresLumiere.data.payment.Payment
import com.luxkasTech.PaintresLumiere.data.payment.PaymentMethod
import com.luxkasTech.PaintresLumiere.data.payment.PaymentRepository
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

data class PaymentMethodUiState(
    val order: OrderDetail? = null,
    val isLoading: Boolean = true,
    val loadError: UiText? = null,
    val method: PaymentMethod = PaymentMethod.PIX,
    val installments: Int = 1,
    val isSubmitting: Boolean = false,
    val submitError: UiText? = null,
) {
    val installmentOptions: List<Int> get() = order?.let { Installments.options(it.totalAmount) } ?: listOf(1)
    val usesInstallments: Boolean get() = method == PaymentMethod.BOLETO_INSTALLMENTS || method == PaymentMethod.CREDIT_CARD
}

/**
 * Picks pix, carnê (boleto parcelado) or card for an order. Creating a charge is guarded against
 * double taps — a second tap could bill twice (the backend's idempotency key is a safety net, not
 * the UI's excuse).
 */
@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    sealed interface Event {
        data class ChargeCreated(val orderId: String, val payment: Payment) : Event
        data class CardRequested(val orderId: String, val installments: Int) : Event
    }

    private val orderId: String = checkNotNull(savedStateHandle[ORDER_ID_ARG])
    private val _state = MutableStateFlow(PaymentMethodUiState())
    val state: StateFlow<PaymentMethodUiState> = _state.asStateFlow()
    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    init {
        load()
    }

    fun retry() = load()

    fun selectMethod(method: PaymentMethod) = _state.update { it.copy(method = method, installments = 1, submitError = null) }

    fun selectInstallments(count: Int) = _state.update {
        it.copy(installments = count.coerceIn(1, it.installmentOptions.last()))
    }

    fun confirm() {
        val s = _state.value
        if (s.isSubmitting || s.order == null || !s.order.canPay) return
        if (s.method == PaymentMethod.CREDIT_CARD) {
            viewModelScope.launch { _events.send(Event.CardRequested(orderId, s.installments)) }
            return
        }
        _state.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            try {
                val installments = if (s.method == PaymentMethod.BOLETO_INSTALLMENTS) s.installments else null
                val payment = paymentRepository.createPayment(orderId, s.method, installments)
                _state.update { it.copy(isSubmitting = false) }
                _events.send(Event.ChargeCreated(orderId, payment))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // e.g. 400 "CPF ou CNPJ obrigatório" — the server message says what is missing.
                _state.update { it.copy(isSubmitting = false, submitError = e.toUiText()) }
            }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            try {
                val order = orderRepository.getOrder(orderId)
                _state.update { it.copy(order = order, isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, loadError = e.toUiText()) }
            }
        }
    }

    companion object {
        const val ORDER_ID_ARG = "orderId"
    }
}
