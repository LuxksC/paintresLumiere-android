package com.luxkasTech.PaintresLumiere.feature.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.data.order.OrderRepository
import com.luxkasTech.PaintresLumiere.data.payment.Payment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class PendingPaymentUiState(
    val orderId: String,
    val payment: Payment,
    val isPaid: Boolean = false,
    val isChecking: Boolean = false,
)

/**
 * Shared by the pix and boleto screens. Their confirmation is **asynchronous** (Asaas webhook →
 * backend), so while the screen is visible the order is polled with a growing interval
 * ([pollDelayMillis]), and polling stops when the screen leaves — never aggressive background
 * polling. Leaving doesn't lose the charge: it stays on the order detail.
 */
@HiltViewModel
class PendingPaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
    json: Json,
) : ViewModel() {

    private val _state = MutableStateFlow(
        PendingPaymentUiState(
            orderId = checkNotNull(savedStateHandle[ORDER_ID_ARG]),
            payment = json.decodeFromString(Payment.serializer(), checkNotNull(savedStateHandle[PAYMENT_ARG])),
        ),
    )
    val state: StateFlow<PendingPaymentUiState> = _state.asStateFlow()
    private var pollJob: Job? = null

    /** Call when the screen becomes visible. */
    fun startPolling() {
        if (pollJob?.isActive == true || _state.value.isPaid) return
        pollJob = viewModelScope.launch {
            var attempt = 0
            while (isActive && !_state.value.isPaid) {
                delay(pollDelayMillis(attempt++))
                check()
            }
        }
    }

    /** Call when the screen is no longer visible. */
    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    /** Pull-to-refresh / "Já paguei". */
    fun checkNow() {
        viewModelScope.launch { check() }
    }

    private suspend fun check() {
        _state.update { it.copy(isChecking = true) }
        try {
            val order = orderRepository.getOrder(_state.value.orderId)
            val current = order.payments.firstOrNull { it.id == _state.value.payment.id }
            _state.update {
                it.copy(payment = current ?: it.payment, isPaid = order.isPaid || current?.status?.isPaid == true, isChecking = false)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // A failed check is silent: the next tick (or a manual refresh) tries again.
            _state.update { it.copy(isChecking = false) }
        }
    }

    companion object {
        const val ORDER_ID_ARG = "orderId"
        const val PAYMENT_ARG = "payment"
    }
}

/** 5s, 10s, 20s, 40s, then every 60s. */
fun pollDelayMillis(attempt: Int): Long = (5_000L shl attempt.coerceAtMost(4)).coerceAtMost(60_000L)
