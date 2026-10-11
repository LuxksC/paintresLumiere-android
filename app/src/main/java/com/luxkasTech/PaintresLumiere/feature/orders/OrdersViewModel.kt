package com.luxkasTech.PaintresLumiere.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.order.OrderRepository
import com.luxkasTech.PaintresLumiere.data.order.OrderSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class OrdersUiState(
    val orders: List<OrderSummary> = emptyList(),
    val nextCursor: String? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: UiText? = null,
) {
    val canLoadMore: Boolean get() = nextCursor != null && !isLoadingMore
}

/** The user's orders, newest first, paginated with the `{ data, nextCursor }` envelope. */
@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OrdersUiState())
    val state: StateFlow<OrdersUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadFirstPage()
    }

    fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        loadFirstPage()
    }

    fun retry() {
        _state.update { it.copy(isLoading = true, error = null) }
        loadFirstPage()
    }

    fun loadMore() {
        val cursor = _state.value.nextCursor ?: return
        if (_state.value.isLoadingMore) return
        _state.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            try {
                val page = orderRepository.getOrders(cursor)
                _state.update { it.copy(orders = it.orders + page.data, nextCursor = page.nextCursor, isLoadingMore = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    private fun loadFirstPage() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val page = orderRepository.getOrders()
                _state.update {
                    it.copy(orders = page.data, nextCursor = page.nextCursor, isLoading = false, isRefreshing = false, error = null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, isRefreshing = false, error = e.toUiText()) }
            }
        }
    }
}
