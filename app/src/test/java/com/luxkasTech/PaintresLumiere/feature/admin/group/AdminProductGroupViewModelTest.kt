package com.luxkasTech.PaintresLumiere.feature.admin.group

import androidx.lifecycle.SavedStateHandle
import com.luxkasTech.PaintresLumiere.testing.FakeAdminProductRepository
import com.luxkasTech.PaintresLumiere.testing.FakeMessagesService
import com.luxkasTech.PaintresLumiere.testing.FakeProductRepository
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import com.luxkasTech.PaintresLumiere.testing.testVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AdminProductGroupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val products = FakeProductRepository().apply {
        variants = Result.success(listOf(testVariant("v1"), testVariant("v2")))
    }
    private val admin = FakeAdminProductRepository()
    private val vm by lazy { AdminProductGroupViewModel(SavedStateHandle(mapOf("sku" to "LUNA")), products, admin, FakeMessagesService()) }

    @Test
    fun `lists the SKU variants`() {
        assertEquals(listOf("v1", "v2"), vm.state.value.variants.map { it.id })
    }

    @Test
    fun `delete needs confirmation and then reloads`() {
        vm.requestDelete(vm.state.value.variants.first())
        assertTrue(admin.deleted.isEmpty())
        vm.confirmDelete()
        assertEquals(listOf("v1"), admin.deleted)
        assertNull(vm.state.value.pendingDelete)
        assertEquals(2, products.variantRequests.size)
    }

    @Test
    fun `stock is adjusted optimistically and reverted on failure`() {
        val v1 = vm.state.value.variants.first()
        admin.failure = com.luxkasTech.PaintresLumiere.core.network.ApiError.Http(500, null)
        vm.adjustStock(v1, 1)
        assertEquals(v1.stockQuantity, vm.state.value.variants.first().stockQuantity)
        admin.failure = null
        vm.adjustStock(v1, 1)
        assertEquals(listOf("v1" to 1), admin.stockAdjustments)
    }

    @Test
    fun `stock never goes below zero`() {
        val empty = vm.state.value.variants.first().copy(stockQuantity = 0)
        vm.adjustStock(empty, -1)
        assertTrue(admin.stockAdjustments.isEmpty())
    }

    @Test
    fun `images are checked for type and size before uploading`() {
        vm.uploadImage("v1", ByteArray(10), "image/gif")
        vm.uploadImage("v1", ByteArray(4 * 1024 * 1024 + 1), "image/png")
        assertTrue(admin.uploads.isEmpty())
        vm.uploadImage("v1", ByteArray(10), "image/png")
        assertEquals(listOf("v1" to "image/png"), admin.uploads)
        assertNull(vm.state.value.uploadingVariantId)
    }
}
