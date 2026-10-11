package com.luxkasTech.PaintresLumiere.feature.admin.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.common.matchesSearch
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.product.Product
import com.luxkasTech.PaintresLumiere.data.product.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class AdminProductsUiState(
    val products: List<Product> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val error: UiText? = null,
) {
    val visibleProducts: List<Product>
        get() = products.filter { matchesSearch(query, it.name, it.sku) }
}

/**
 * Admin catalog list with local search by name or SKU.
 *
 * Source: `GET /products` — which only returns **active** SKU groups. The admin endpoint that also
 * returns inactive products (`GET /admin/products`, API repo) doesn't exist yet; switching to it is
 * a one-line change in the repository once it ships.
 */
@HiltViewModel
class AdminProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminProductsUiState())
    val state: StateFlow<AdminProductsUiState> = _state.asStateFlow()

    init {
        load()
    }

    private var loadedOnce = false

    /** Reload when coming back from the form or a deletion, so the list stays current. */
    fun onResume() {
        if (loadedOnce && !_state.value.isLoading) load()
    }

    fun onQueryChange(value: String) = _state.update { it.copy(query = value) }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val products = productRepository.getProducts().sortedBy { it.name.lowercase() }
                _state.update { it.copy(products = products, isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.toUiText()) }
            } finally {
                loadedOnce = true
            }
        }
    }
}
