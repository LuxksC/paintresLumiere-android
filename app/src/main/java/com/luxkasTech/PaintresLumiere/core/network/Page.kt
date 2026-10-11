package com.luxkasTech.PaintresLumiere.core.network

import kotlinx.serialization.Serializable

/**
 * Paginated envelope agreed in backend card PL-22: `{ data, nextCursor, total }`. New list
 * endpoints (orders first) use it from day one.
 */
@Serializable
data class Page<T>(
    val data: List<T> = emptyList(),
    val nextCursor: String? = null,
    val total: Int? = null,
)
