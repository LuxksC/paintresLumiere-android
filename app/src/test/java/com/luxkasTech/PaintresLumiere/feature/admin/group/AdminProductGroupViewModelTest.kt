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
    private val vm = AdminProductGroupViewModel(SavedStateHandle(mapOf("sku" to "LUNA")), products, admin, FakeMessagesService())

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
}
