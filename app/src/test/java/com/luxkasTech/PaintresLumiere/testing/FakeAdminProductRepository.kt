package com.luxkasTech.PaintresLumiere.testing

import com.luxkasTech.PaintresLumiere.data.admin.AdminProductRepository
import com.luxkasTech.PaintresLumiere.data.admin.CreatedProduct
import com.luxkasTech.PaintresLumiere.data.admin.ProductUpsertRequest

class FakeAdminProductRepository : AdminProductRepository {
    val created = mutableListOf<ProductUpsertRequest>()
    val updated = mutableListOf<Pair<String, ProductUpsertRequest>>()
    val deleted = mutableListOf<String>()
    var failure: Exception? = null

    override suspend fun create(request: ProductUpsertRequest): CreatedProduct {
        failure?.let { throw it }
        created += request
        return CreatedProduct("new-id")
    }

    override suspend fun update(id: String, request: ProductUpsertRequest) {
        failure?.let { throw it }
        updated += id to request
    }

    val stockAdjustments = mutableListOf<Pair<String, Int>>()
    val uploads = mutableListOf<Pair<String, String>>()

    override suspend fun adjustStock(id: String, delta: Int): com.luxkasTech.PaintresLumiere.data.product.ProductVariant {
        failure?.let { throw it }
        stockAdjustments += id to delta
        return testVariant(id, stock = 5 + delta)
    }

    override suspend fun uploadImage(productId: String, bytes: ByteArray, mimeType: String, fileName: String): com.luxkasTech.PaintresLumiere.data.admin.UploadedImage {
        failure?.let { throw it }
        uploads += productId to mimeType
        return com.luxkasTech.PaintresLumiere.data.admin.UploadedImage("https://cdn/x.png")
    }

    override suspend fun delete(id: String) {
        failure?.let { throw it }
        deleted += id
    }
}
