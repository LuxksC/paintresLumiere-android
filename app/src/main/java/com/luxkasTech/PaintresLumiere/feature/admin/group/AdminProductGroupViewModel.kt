package com.luxkasTech.PaintresLumiere.feature.admin.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import com.luxkasTech.PaintresLumiere.core.messages.ToastType
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.admin.AdminProductRepository
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

data class AdminProductGroupUiState(
    val sku: String,
    val variants: List<ProductVariant> = emptyList(),
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val pendingDelete: ProductVariant? = null,
)

/** All variants (rows) of one SKU, with edit and delete. */
@HiltViewModel
class AdminProductGroupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val adminRepository: AdminProductRepository,
    private val messages: MessagesService,
) : ViewModel() {

    private val sku: String = checkNotNull(savedStateHandle[SKU_ARG])
    private val _state = MutableStateFlow(AdminProductGroupUiState(sku))
    val state: StateFlow<AdminProductGroupUiState> = _state.asStateFlow()
    private var loadedOnce = false

    init {
        load()
    }

    /** Reload when coming back from the form, so edits show up. */
    fun onResume() {
        if (loadedOnce && !_state.value.isLoading) load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val variants = productRepository.getProductVariants(sku)
                _state.update { it.copy(variants = variants, isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.toUiText()) }
            } finally {
                loadedOnce = true
            }
        }
    }

    fun requestDelete(variant: ProductVariant) = _state.update { it.copy(pendingDelete = variant) }

    fun dismissDelete() = _state.update { it.copy(pendingDelete = null) }

    fun confirmDelete() {
        val variant = _state.value.pendingDelete ?: return
        _state.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            try {
                adminRepository.delete(variant.id)
                messages.show(UiText.Resource(R.string.admin_group_deleted), UiText.Raw(variant.name), ToastType.Success)
                load()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                messages.show(UiText.Resource(R.string.admin_group_delete_failed), e.toUiText(), ToastType.Error)
            }
        }
    }

    companion object {
        const val SKU_ARG = "sku"
    }
}
