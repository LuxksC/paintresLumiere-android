package com.luxkasTech.PaintresLumiere.feature.admin.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import com.luxkasTech.PaintresLumiere.core.messages.ToastType
import com.luxkasTech.PaintresLumiere.core.network.toUiText
import com.luxkasTech.PaintresLumiere.data.admin.AdminProductRepository
import com.luxkasTech.PaintresLumiere.data.admin.ProductUpsertRequest
import com.luxkasTech.PaintresLumiere.data.product.ProductCategory
import com.luxkasTech.PaintresLumiere.data.product.ProductColor
import com.luxkasTech.PaintresLumiere.data.product.ProductRepository
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import com.luxkasTech.PaintresLumiere.data.product.ProductVariant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

enum class FormField { Sku, Name, Category, Color, Price, Discount, Stock }

/** Every field is kept as typed text; parsing happens once, on save. */
data class ProductFormUiState(
    val mode: Mode = Mode.Create,
    val isLoading: Boolean = false,
    val loadError: UiText? = null,
    val isSaving: Boolean = false,
    val sku: String = "",
    val name: String = "",
    val description: String = "",
    val category: ProductCategory? = null,
    val color: ProductColor? = null,
    val price: String = "",
    val discountPercent: String = "",
    val stock: String = "0",
    val status: ProductStatus = ProductStatus.ACTIVE,
    val width: String = "",
    val height: String = "",
    val length: String = "",
    val thickness: String = "",
    val unit: String = "mm",
    val barcode: String = "",
    val errors: Map<FormField, UiText> = emptyMap(),
) {
    /** Create a new SKU, add a variant (color/size) to an existing SKU, or edit one variant. */
    enum class Mode { Create, AddVariant, Edit }
}

/**
 * Create / edit form for one product row (a variant). Route arguments:
 * - none → brand-new product;
 * - `sku` → new variant of that SKU (name, category and description are copied from it);
 * - `sku` + `variantId` → edit that variant.
 */
@HiltViewModel
class ProductFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val adminRepository: AdminProductRepository,
    private val messages: MessagesService,
) : ViewModel() {

    sealed interface Event {
        data object Saved : Event
    }

    private val sku: String? = savedStateHandle[SKU_ARG]
    private val variantId: String? = savedStateHandle[VARIANT_ARG]

    private val _state = MutableStateFlow(
        ProductFormUiState(
            mode = when {
                sku == null -> ProductFormUiState.Mode.Create
                variantId == null -> ProductFormUiState.Mode.AddVariant
                else -> ProductFormUiState.Mode.Edit
            },
            isLoading = sku != null,
            sku = sku.orEmpty(),
        ),
    )
    val state: StateFlow<ProductFormUiState> = _state.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    init {
        if (sku != null) load(sku)
    }

    fun update(transform: (ProductFormUiState) -> ProductFormUiState) = _state.update { transform(it).copy(errors = emptyMap()) }

    fun retry() {
        sku?.let(::load)
    }

    fun save() {
        val current = _state.value
        if (current.isSaving) return
        val request = validate(current) ?: return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                if (variantId != null) {
                    adminRepository.update(variantId, request)
                } else {
                    adminRepository.create(request)
                }
                messages.show(UiText.Resource(R.string.admin_form_saved), type = ToastType.Success)
                _events.send(Event.Saved)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false) }
                messages.show(UiText.Resource(R.string.admin_form_save_failed), e.toUiText(), ToastType.Error)
            }
        }
    }

    private fun load(sku: String) {
        _state.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            try {
                val variants = productRepository.getProductVariants(sku)
                val source = variantId?.let { id -> variants.firstOrNull { it.id == id } } ?: variants.firstOrNull()
                _state.update { prefill(it.copy(isLoading = false), source) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, loadError = e.toUiText()) }
            }
        }
    }

    private fun prefill(state: ProductFormUiState, v: ProductVariant?): ProductFormUiState {
        if (v == null) return state
        val shared = state.copy(
            name = v.name,
            description = v.description.orEmpty(),
            category = v.category.takeIf { it != ProductCategory.UNKNOWN },
        )
        if (state.mode != ProductFormUiState.Mode.Edit) return shared
        val d = v.dimensions
        return shared.copy(
            color = v.color.takeIf { it != ProductColor.UNKNOWN },
            price = v.pricing.sellingPrice.toPlainString(),
            discountPercent = v.pricing.discountRate.movePointRight(2).stripTrailingZeros().toPlainString().takeIf { v.pricing.hasDiscount }.orEmpty(),
            stock = v.stockQuantity.toString(),
            status = v.status.takeIf { it != ProductStatus.UNKNOWN } ?: ProductStatus.INACTIVE,
            width = d.width?.toPlain().orEmpty(),
            height = d.height?.toPlain().orEmpty(),
            length = d.length?.toPlain().orEmpty(),
            thickness = d.thickness?.toPlain().orEmpty(),
            unit = d.unit ?: "mm",
            barcode = v.barcode.orEmpty(),
        )
    }

    /** Returns the request, or null after publishing field errors. */
    private fun validate(s: ProductFormUiState): ProductUpsertRequest? {
        val errors = mutableMapOf<FormField, UiText>()
        val required = UiText.Resource(R.string.admin_form_error_required)
        if (s.sku.isBlank()) errors[FormField.Sku] = required
        if (s.name.isBlank()) errors[FormField.Name] = required
        if (s.category == null) errors[FormField.Category] = required
        if (s.color == null) errors[FormField.Color] = required
        val price = s.price.toDecimal()
        if (price == null || price.signum() <= 0) errors[FormField.Price] = UiText.Resource(R.string.admin_form_error_price)
        val discount = if (s.discountPercent.isBlank()) BigDecimal.ZERO else s.discountPercent.toDecimal()
        if (discount == null || discount < BigDecimal.ZERO || discount > BigDecimal(100)) {
            errors[FormField.Discount] = UiText.Resource(R.string.admin_form_error_discount)
        }
        val stock = s.stock.trim().toIntOrNull()
        if (stock == null || stock < 0) errors[FormField.Stock] = UiText.Resource(R.string.admin_form_error_stock)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return null
        }
        return ProductUpsertRequest(
            sku = s.sku.trim(),
            name = s.name.trim(),
            description = s.description.trim().ifBlank { null },
            category = s.category,
            sellingPrice = price,
            discountRate = discount?.divide(BigDecimal(100), 4, RoundingMode.HALF_UP),
            color = s.color,
            stockQuantity = stock,
            status = s.status,
            width = s.width.toDecimal()?.toDouble(),
            height = s.height.toDecimal()?.toDouble(),
            length = s.length.toDecimal()?.toDouble(),
            thickness = s.thickness.toDecimal()?.toDouble(),
            dimensionsUnit = s.unit.trim().ifBlank { null },
            barcode = s.barcode.trim().ifBlank { null },
        )
    }

    companion object {
        /** Must match the property names of `AdminProductFormDestination`. */
        const val SKU_ARG = "sku"
        const val VARIANT_ARG = "variantId"
    }
}

/** Accepts both "19,90" and "19.90". */
internal fun String.toDecimal(): BigDecimal? = trim().replace(',', '.').toBigDecimalOrNull()

private fun Double.toPlain(): String = BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()
