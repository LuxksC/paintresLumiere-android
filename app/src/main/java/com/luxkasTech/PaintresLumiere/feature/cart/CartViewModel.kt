package com.luxkasTech.PaintresLumiere.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import com.luxkasTech.PaintresLumiere.core.messages.ToastType
import com.luxkasTech.PaintresLumiere.data.cart.Cart
import com.luxkasTech.PaintresLumiere.data.cart.CartError
import com.luxkasTech.PaintresLumiere.data.cart.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class CartUiState(
    val cart: Cart = Cart(),
    val confirmingClear: Boolean = false,
)

/** Cart screen. Reads the shared [CartRepository] flow, so the Home badge and this screen always agree. */
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val messages: MessagesService,
) : ViewModel() {

    private val confirmingClear = MutableStateFlow(false)

    val state: StateFlow<CartUiState> = combine(cartRepository.cart, confirmingClear) { cart, confirming ->
        CartUiState(cart, confirming)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState(cartRepository.cart.value))

    fun changeQuantity(itemId: String, quantity: Int) = launchCartAction {
        cartRepository.updateQuantity(itemId, quantity)
    }

    fun removeItem(itemId: String) = launchCartAction { cartRepository.removeItem(itemId) }

    fun requestClear() = confirmingClear.update { true }

    fun dismissClear() = confirmingClear.update { false }

    fun confirmClear() {
        confirmingClear.value = false
        launchCartAction { cartRepository.clear() }
    }

    /** Checkout needs `POST /orders` (#45). Until then it says so. */
    fun checkout() = messages.showComingSoon()

    private fun launchCartAction(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: CartError.InsufficientStock) {
                messages.show(
                    UiText.Resource(R.string.cart_error_stock_title),
                    UiText.Resource(R.string.cart_error_stock_message, e.available),
                    ToastType.Warning,
                )
            } catch (e: Exception) {
                messages.showGenericError()
            }
        }
    }
}
