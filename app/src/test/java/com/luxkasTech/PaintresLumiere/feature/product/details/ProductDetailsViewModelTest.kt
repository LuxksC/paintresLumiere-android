package com.luxkasTech.PaintresLumiere.feature.product.details

import androidx.lifecycle.SavedStateHandle
import com.luxkasTech.PaintresLumiere.core.messages.ToastType
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.data.cart.CartError
import com.luxkasTech.PaintresLumiere.data.product.ProductColor
import com.luxkasTech.PaintresLumiere.data.product.ProductDimensions
import com.luxkasTech.PaintresLumiere.data.product.ProductPricing
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import com.luxkasTech.PaintresLumiere.data.product.ProductVariant
import com.luxkasTech.PaintresLumiere.data.product.ProductVariantSpecifications
import com.luxkasTech.PaintresLumiere.testing.FakeCartRepository
import com.luxkasTech.PaintresLumiere.testing.FakeMessagesService
import com.luxkasTech.PaintresLumiere.testing.FakeProductRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal

/**
 * The variant logic is the densest piece of the app — same scenarios as iOS card #102.
 *
 * Fixture: GOLD in 10 and 20, SILVER only in 10 (stock 0), BLACK only in 30.
 */
class ProductDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()
    private val messages = FakeMessagesService()
    private val cart = FakeCartRepository()

    private fun variant(
        id: String,
        color: ProductColor,
        width: Double,
        stock: Int,
        status: ProductStatus = ProductStatus.ACTIVE,
    ) = ProductVariant(
        id = id,
        name = "Luna $id",
        slug = "luna-$id",
        stockQuantity = stock,
        status = status,
        pricing = ProductPricing(BigDecimal("10"), BigDecimal.ZERO, BigDecimal("10")),
        specifications = ProductVariantSpecifications(color, ProductDimensions(width = width, unit = "cm")),
    )

    private val gold10 = variant("g10", ProductColor.GOLD, 10.0, 5)
    private val gold20 = variant("g20", ProductColor.GOLD, 20.0, 2)
    private val silver10 = variant("s10", ProductColor.SILVER, 10.0, 0, ProductStatus.OUT_OF_STOCK)
    private val black30 = variant("b30", ProductColor.BLACK, 30.0, 9)

    private fun viewModel(variants: List<ProductVariant> = listOf(gold10, gold20, silver10, black30)): ProductDetailsViewModel {
        repository.variants = Result.success(variants)
        return ProductDetailsViewModel(SavedStateHandle(mapOf("sku" to "LUNA")), repository, cart, messages)
    }

    @Test
    fun `requests the SKU from the route`() {
        viewModel()
        assertEquals(listOf("LUNA"), repository.variantRequests)
    }

    @Test
    fun `initial selection is the first variant`() {
        val state = viewModel().state.value
        assertEquals(DetailsLoadState.Loaded, state.loadState)
        assertEquals(ProductColor.GOLD, state.selectedColor)
        assertEquals("10cm", state.selectedSize)
        assertEquals(gold10, state.selectedVariant)
        assertEquals(1, state.quantity)
    }

    @Test
    fun `distinct colors and sizes keep API order`() {
        val state = viewModel().state.value
        assertEquals(listOf(ProductColor.GOLD, ProductColor.SILVER, ProductColor.BLACK), state.availableColors)
        assertEquals(listOf("10cm", "20cm", "30cm"), state.availableSizes)
        assertTrue(state.hasMultipleColors)
        assertTrue(state.hasMultipleSizes)
    }

    @Test
    fun `selected color only enables sizes that exist for it`() {
        val state = viewModel().state.value
        assertEquals(setOf("10cm", "20cm"), state.enabledSizes)
    }

    @Test
    fun `selected size only enables colors that exist for it`() {
        val state = viewModel().state.value
        assertEquals(setOf(ProductColor.GOLD, ProductColor.SILVER), state.enabledColors)
    }

    @Test
    fun `a disabled color cannot be selected`() {
        val vm = viewModel()
        vm.selectColor(ProductColor.BLACK) // no 10cm black
        assertEquals(ProductColor.GOLD, vm.state.value.selectedColor)
    }

    @Test
    fun `switching to an out of stock variant blocks purchase and zeroes quantity`() {
        val vm = viewModel()
        vm.setQuantity(3)
        vm.selectColor(ProductColor.SILVER)
        val state = vm.state.value
        assertEquals(silver10, state.selectedVariant)
        assertEquals(0, state.quantity)
        assertFalse(state.canPurchase)
    }

    @Test
    fun `quantity is clamped to the new variant's stock`() {
        val vm = viewModel()
        vm.setQuantity(5)
        assertEquals(5, vm.state.value.quantity)
        vm.selectSize("20cm") // gold20 has stock 2
        assertEquals(2, vm.state.value.quantity)
        assertEquals(2, vm.state.value.maxQuantity)
    }

    @Test
    fun `quantity never exceeds stock or drops below one`() {
        val vm = viewModel()
        vm.setQuantity(99)
        assertEquals(5, vm.state.value.quantity)
        vm.setQuantity(-3)
        assertEquals(1, vm.state.value.quantity)
    }

    @Test
    fun `an unknown status is never purchasable`() {
        val vm = viewModel(listOf(variant("u", ProductColor.GOLD, 10.0, 4, ProductStatus.UNKNOWN)))
        assertFalse(vm.state.value.canPurchase)
    }

    @Test
    fun `an unknown color is never offered and not picked initially`() {
        val unknown = variant("u", ProductColor.UNKNOWN, 10.0, 4)
        val vm = viewModel(listOf(unknown, gold10))
        val state = vm.state.value
        assertEquals(listOf(ProductColor.GOLD), state.availableColors)
        assertEquals(ProductColor.GOLD, state.selectedColor)
        assertFalse(state.hasMultipleColors)
    }

    @Test
    fun `no variants means no selection`() {
        val state = viewModel(emptyList()).state.value
        assertNull(state.selectedVariant)
        assertFalse(state.canPurchase)
    }

    @Test
    fun `a failed request moves to the failed state and toasts`() {
        repository.variants = Result.failure(ApiError.Network(IOException()))
        val vm = ProductDetailsViewModel(SavedStateHandle(mapOf("sku" to "LUNA")), repository, cart, messages)
        assertTrue(vm.state.value.loadState is DetailsLoadState.Failed)
        assertEquals(1, messages.shown.size)
    }

    @Test
    fun `add to cart adds the selected variant with the chosen quantity`() {
        val vm = viewModel()
        vm.setQuantity(3)
        vm.addToCart()
        assertEquals(listOf(gold10 to 3), cart.added)
        assertEquals(ToastType.Success, messages.shown.single().type)
    }

    @Test
    fun `buy adds and asks to open the cart`() = runTest {
        val vm = viewModel()
        vm.buyNow()
        assertEquals(ProductDetailsViewModel.Event.OpenCart, vm.events.first())
        assertEquals(1, cart.added.size)
        assertTrue(messages.shown.isEmpty())
    }

    @Test
    fun `nothing is added when the selection can't be bought`() {
        val vm = viewModel()
        vm.selectColor(ProductColor.SILVER)
        vm.addToCart()
        vm.buyNow()
        assertTrue(cart.added.isEmpty())
    }

    @Test
    fun `stock conflicts from the cart are reported`() {
        cart.failure = CartError.InsufficientStock(available = 1)
        val vm = viewModel()
        vm.addToCart()
        assertEquals(ToastType.Warning, messages.shown.single().type)
    }
}
