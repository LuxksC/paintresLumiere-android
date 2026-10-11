package com.luxkasTech.PaintresLumiere.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.cart.Cart
import com.luxkasTech.PaintresLumiere.data.cart.CartRepository
import com.luxkasTech.PaintresLumiere.data.order.OrderDetail
import com.luxkasTech.PaintresLumiere.data.order.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class CheckoutUiState(
    val cart: Cart = Cart(),
    val notes: String = "",
    val isSubmitting: Boolean = false,
    val error: UiText? = null,
    /** True when the failure was a stock conflict: the user should go back and fix the cart. */
    val isStockConflict: Boolean = false,
) {
    val canSubmit: Boolean get() = cart.canCheckout && !isSubmitting
}

/**
 * Checkout. Sends only the notes — never prices or totals; the server recomputes everything from
 * the cart (backend card PL-122). A double tap must not create two orders, so submission is
 * ignored while one is in flight.
 */
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    sealed interface Event {
        data class OrderPlaced(val order: OrderDetail) : Event
    }

    private val form = MutableStateFlow(CheckoutUiState())
    val state: StateFlow<CheckoutUiState> = combine(cartRepository.cart, form) { cart, f -> f.copy(cart = cart) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckoutUiState(cart = cartRepository.cart.value))

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    fun onNotesChange(value: String) = form.update { it.copy(notes = value.take(MAX_NOTES)) }

    fun placeOrder() {
        if (form.value.isSubmitting || !cartRepository.cart.value.canCheckout) return
        form.update { it.copy(isSubmitting = true, error = null, isStockConflict = false) }
        viewModelScope.launch {
            try {
                val order = orderRepository.createOrder(form.value.notes.trim())
                // The server emptied its cart in the same transaction; mirror it locally.
                cartRepository.clear()
                _events.send(Event.OrderPlaced(order))
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiError.Conflict) {
                form.update {
                    it.copy(isSubmitting = false, isStockConflict = true, error = e.serverMessage?.let(UiText::Raw) ?: UiText.Resource(R.string.checkout_error_stock))
                }
            } catch (e: Exception) {
                form.update { it.copy(isSubmitting = false, error = e.toUiText()) }
            }
        }
    }

    companion object {
        const val MAX_NOTES = 500
    }
}
