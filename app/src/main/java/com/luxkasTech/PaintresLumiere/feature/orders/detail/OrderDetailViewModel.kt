package com.luxkasTech.PaintresLumiere.feature.orders.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.order.OrderDetail
import com.luxkasTech.PaintresLumiere.data.order.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class OrderDetailUiState(
    val order: OrderDetail? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
) {
    val timeline: List<TimelineStep> get() = order?.let { buildTimeline(it.status, it.statusHistory) }.orEmpty()
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle[ORDER_ID_ARG])
    private val _state = MutableStateFlow(OrderDetailUiState())
    val state: StateFlow<OrderDetailUiState> = _state.asStateFlow()

    private var loadedOnce = false

    init {
        load()
    }

    fun onResume() {
        if (loadedOnce && !_state.value.isLoading) load()
    }

    /** The status changes on the admin side, so pull-to-refresh matters here. */
    fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        load()
    }

    fun retry() {
        _state.update { it.copy(isLoading = true, error = null) }
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val order = orderRepository.getOrder(orderId)
                _state.update { it.copy(order = order, isLoading = false, isRefreshing = false, error = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, isRefreshing = false, error = e.toUiText()) }
            } finally {
                loadedOnce = true
            }
        }
    }

    companion object {
        const val ORDER_ID_ARG = "orderId"
    }
}
