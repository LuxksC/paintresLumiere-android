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

    override suspend fun delete(id: String) {
        failure?.let { throw it }
        deleted += id
    }
}
