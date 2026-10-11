package com.luxkasTech.PaintresLumiere.feature.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.product.Product
import com.luxkasTech.PaintresLumiere.data.product.ProductCategory
import com.luxkasTech.PaintresLumiere.data.product.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class CategoryProductsUiState(
    val category: ProductCategory,
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
)

/** Products of one category (`GET /products?category=`), reached from a Home category tile. */
@HiltViewModel
class CategoryProductsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val category: ProductCategory = ProductCategory.entries
        .firstOrNull { it.raw == savedStateHandle.get<String>(CATEGORY_ARG) } ?: ProductCategory.UNKNOWN

    private val _state = MutableStateFlow(CategoryProductsUiState(category))
    val state: StateFlow<CategoryProductsUiState> = _state.asStateFlow()

    init {
        load()
    }

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
                val products = productRepository.getProductsInCategory(category).sortedBy { it.name.lowercase() }
                _state.update { it.copy(products = products, isLoading = false, isRefreshing = false, error = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, isRefreshing = false, error = e.toUiText()) }
            }
        }
    }

    companion object {
        /** Must match `CategoryProductsDestination.category` (the enum's raw API value). */
        const val CATEGORY_ARG = "category"
    }
}
