package com.luxkasTech.PaintresLumiere.feature.admin.form

import androidx.lifecycle.SavedStateHandle
import com.luxkasTech.PaintresLumiere.core.messages.ToastType
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import com.luxkasTech.PaintresLumiere.data.product.ProductCategory
import com.luxkasTech.PaintresLumiere.data.product.ProductColor
import com.luxkasTech.PaintresLumiere.data.product.ProductPricing
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import com.luxkasTech.PaintresLumiere.testing.FakeAdminProductRepository
import com.luxkasTech.PaintresLumiere.testing.FakeMessagesService
import com.luxkasTech.PaintresLumiere.testing.FakeProductRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import com.luxkasTech.PaintresLumiere.testing.testVariant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

class ProductFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val products = FakeProductRepository()
    private val admin = FakeAdminProductRepository()
    private val messages = FakeMessagesService()

    private fun vm(vararg args: Pair<String, String>) =
        ProductFormViewModel(SavedStateHandle(mapOf(*args)), products, admin, messages)

    private fun ProductFormViewModel.fillValid() = update {
        it.copy(sku = "LUNA", name = "Luna", category = ProductCategory.NAMES, color = ProductColor.GOLD, price = "49,90", discountPercent = "10", stock = "7")
    }

    @Test
    fun `missing required fields are flagged and nothing is sent`() {
        val vm = vm()
        vm.save()
        val errors = vm.state.value.errors.keys
        assertTrue(errors.containsAll(listOf(FormField.Sku, FormField.Name, FormField.Category, FormField.Color, FormField.Price)))
        assertTrue(admin.created.isEmpty())
    }

    @Test
    fun `create sends parsed values and finishes`() = runTest {
        val vm = vm()
        vm.fillValid()
        vm.save()
        val request = admin.created.single()
        assertEquals(BigDecimal("49.90"), request.sellingPrice)
        assertEquals(BigDecimal("0.1000"), request.discountRate)
        assertEquals(7, request.stockQuantity)
        assertEquals(ProductStatus.ACTIVE, request.status)
        assertEquals(ProductFormViewModel.Event.Saved, vm.events.first())
    }

    @Test
    fun `discount above 100 is rejected`() {
        val vm = vm()
        vm.fillValid()
        vm.update { it.copy(discountPercent = "150") }
        vm.save()
        assertTrue(FormField.Discount in vm.state.value.errors)
    }

    @Test
    fun `edit prefills from the variant and updates it`() {
        products.variants = Result.success(
            listOf(testVariant("v1", stock = 3, color = ProductColor.SILVER).copy(pricing = ProductPricing(BigDecimal("50"), BigDecimal("0.15"), BigDecimal("42.5")))),
        )
        val vm = vm("sku" to "LUNA", "variantId" to "v1")
        val s = vm.state.value
        assertEquals(ProductFormUiState.Mode.Edit, s.mode)
        assertEquals("50", s.price)
        assertEquals("15", s.discountPercent)
        assertEquals(ProductColor.SILVER, s.color)
        vm.update { it.copy(category = ProductCategory.LETTERS) }
        vm.save()
        assertEquals("v1", admin.updated.single().first)
    }

    @Test
    fun `adding a variant copies the shared fields but not color or price`() {
        products.variants = Result.success(listOf(testVariant("v1", color = ProductColor.GOLD)))
        val s = vm("sku" to "LUNA").state.value
        assertEquals(ProductFormUiState.Mode.AddVariant, s.mode)
        assertEquals("Luna v1", s.name)
        assertEquals(null, s.color)
        assertEquals("", s.price)
    }

    @Test
    fun `a failed save keeps the form and shows an error toast`() {
        admin.failure = ApiError.Http(403, "Admin access required")
        val vm = vm()
        vm.fillValid()
        vm.save()
        assertEquals(false, vm.state.value.isSaving)
        assertEquals(ToastType.Error, messages.shown.single().type)
    }
}
