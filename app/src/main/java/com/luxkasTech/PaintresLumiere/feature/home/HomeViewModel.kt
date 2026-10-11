package com.luxkasTech.PaintresLumiere.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.cart.CartRepository
import com.luxkasTech.PaintresLumiere.data.product.CategorySummary
import com.luxkasTech.PaintresLumiere.data.product.Product
import com.luxkasTech.PaintresLumiere.data.product.ProductCategory
import com.luxkasTech.PaintresLumiere.data.product.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class HomeUiState(
    val catalog: List<Product> = emptyList(),
    val popular: List<Product> = emptyList(),
    /**
     * Category tiles (`GET /products/categories`). Empty when the endpoint is unavailable — the
     * Home then falls back to the flat collection grid, so the live app keeps working until the
     * backend ships it.
     */
    val categories: List<CategorySummary> = emptyList(),
    /** True only for the very first load (nothing to show yet). */
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    /** Set when the catalog failed and there is nothing to show. */
    val error: UiText? = null,
    /** Units in the cart, for the badge on the cart icon. */
    val cartCount: Int = 0,
) {
    val showsFullScreenError: Boolean get() = error != null && catalog.isEmpty()
    val isEmpty: Boolean get() = !isLoading && error == null && catalog.isEmpty()
}

/**
 * Home catalog: "most popular" carousel + full collection, mirroring iOS `HomeViewModel`.
 *
 * Unlike iOS (where `async let` fails both sections together), the two requests are independent:
 * if only `/products/popular` fails the carousel is simply hidden and the collection still shows,
 * which is what the catalog spec's edge case asks for.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    cartRepository: CartRepository,
    private val messages: MessagesService,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val collator = Collator.getInstance(Locale("pt", "BR")).apply { strength = Collator.PRIMARY }

    init {
        load()
        cartRepository.cart
            .onEach { cart -> _state.update { it.copy(cartCount = cart.itemCount) } }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        load()
    }

    fun retry() {
        _state.update { it.copy(isLoading = it.catalog.isEmpty(), error = null) }
        load()
    }

    fun onNotificationsClick() = messages.showComingSoon()

    private fun load() {
        viewModelScope.launch {
            val popular = async { runCatching { productRepository.getPopularProducts(POPULAR_LIMIT) } }
            val catalog = async { runCatching { productRepository.getProducts() } }
            val categories = async { runCatching { productRepository.getCategories() } }
            val catalogResult = catalog.await()
            val popularResult = popular.await()
            val categoriesResult = categories.await()
            listOfNotNull(catalogResult.exceptionOrNull(), popularResult.exceptionOrNull(), categoriesResult.exceptionOrNull())
                .firstOrNull { it is CancellationException }
                ?.let { throw it }

            _state.update { current ->
                current.copy(
                    catalog = catalogResult.getOrNull()?.sortedWith(compareBy(collator) { it.name }) ?: current.catalog,
                    popular = popularResult.getOrNull() ?: current.popular,
                    categories = categoriesResult.getOrNull() ?: current.categories,
                    isLoading = false,
                    isRefreshing = false,
                    error = catalogResult.exceptionOrNull()?.toUiText(),
                )
            }
            if (catalogResult.isFailure) messages.showNetworkError()
        }
    }

    companion object {
        const val POPULAR_LIMIT = 5
    }
}
