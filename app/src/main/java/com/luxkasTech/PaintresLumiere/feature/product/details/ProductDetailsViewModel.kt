package com.luxkasTech.PaintresLumiere.feature.product.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.product.ProductColor
import com.luxkasTech.PaintresLumiere.data.product.ProductRepository
import com.luxkasTech.PaintresLumiere.data.product.ProductVariant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface DetailsLoadState {
    data object Loading : DetailsLoadState
    data object Loaded : DetailsLoadState
    data class Failed(val message: UiText) : DetailsLoadState
}

/**
 * Product details state. The backend returns **one row per variant** (same SKU, different color
 * and/or dimensions), so the selectors are derived from the variant list and a color/size pair
 * with no matching variant is never selectable. Same rules as iOS `ProductDetailsViewModel`.
 */
data class ProductDetailsUiState(
    val loadState: DetailsLoadState = DetailsLoadState.Loading,
    val variants: List<ProductVariant> = emptyList(),
    val selectedColor: ProductColor? = null,
    val selectedSize: String? = null,
    val quantity: Int = 1,
) {
    val selectedVariant: ProductVariant?
        get() = variants.firstOrNull { it.color == selectedColor && it.sizeLabel == selectedSize }

    /** Distinct colors in API order; an unknown color is never offered. */
    val availableColors: List<ProductColor>
        get() = variants.map { it.color }.distinct().filter { it != ProductColor.UNKNOWN }

    val availableSizes: List<String>
        get() = variants.map { it.sizeLabel }.distinct()

    /** Colors that have a variant for the selected size. */
    val enabledColors: Set<ProductColor>
        get() = selectedSize?.let { size -> variants.filter { it.sizeLabel == size }.map { it.color }.toSet() }
            ?: availableColors.toSet()

    /** Sizes that have a variant for the selected color. */
    val enabledSizes: Set<String>
        get() = selectedColor?.let { color -> variants.filter { it.color == color }.map { it.sizeLabel }.toSet() }
            ?: availableSizes.toSet()

    val maxQuantity: Int get() = selectedVariant?.stockQuantity ?: 0

    val canPurchase: Boolean get() = selectedVariant?.status?.isPurchasable == true && maxQuantity > 0

    val hasMultipleColors: Boolean get() = availableColors.size > 1
    val hasMultipleSizes: Boolean get() = availableSizes.size > 1
}

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val messages: MessagesService,
) : ViewModel() {

    val sku: String = checkNotNull(savedStateHandle[SKU_ARG]) { "ProductDetails opened without a SKU" }

    private val _state = MutableStateFlow(ProductDetailsUiState())
    val state: StateFlow<ProductDetailsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() {
        _state.update { it.copy(loadState = DetailsLoadState.Loading) }
        load()
    }

    fun selectColor(color: ProductColor) {
        if (color !in _state.value.enabledColors) return
        _state.update { it.copy(selectedColor = color).clampQuantity() }
    }

    fun selectSize(size: String) {
        if (size !in _state.value.enabledSizes) return
        _state.update { it.copy(selectedSize = size).clampQuantity() }
    }

    fun setQuantity(value: Int) {
        _state.update { it.copy(quantity = value.coerceIn(1, it.maxQuantity.coerceAtLeast(1))) }
    }

    /** Cart and checkout are Phase 3 — until then both actions say so instead of faking success. */
    fun buyNow() {
        if (_state.value.canPurchase) messages.showComingSoon()
    }

    fun addToCart() {
        if (_state.value.canPurchase) messages.showComingSoon()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val variants = productRepository.getProductVariants(sku)
                // Prefer a variant whose color can actually be shown in the selector.
                val initial = variants.firstOrNull { it.color != ProductColor.UNKNOWN } ?: variants.firstOrNull()
                _state.update {
                    it.copy(
                        loadState = DetailsLoadState.Loaded,
                        variants = variants,
                        selectedColor = initial?.color,
                        selectedSize = initial?.sizeLabel,
                        quantity = if ((initial?.stockQuantity ?: 0) > 0) 1 else 0,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(loadState = DetailsLoadState.Failed(e.toUiText())) }
                messages.showNetworkError()
            }
        }
    }

    private fun ProductDetailsUiState.clampQuantity(): ProductDetailsUiState {
        val stock = selectedVariant?.stockQuantity ?: 0
        return copy(quantity = if (stock <= 0) 0 else quantity.coerceIn(1, stock))
    }

    companion object {
        /** Must match the property name of `ProductDetailsDestination`. */
        const val SKU_ARG = "sku"
    }
}
