package com.luxkasTech.PaintresLumiere.core.network.mock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockRouterTest {

    private val files = mapOf(
        "GET/products.json" to "list",
        "GET/products/sku/LUNA.json" to "luna",
        "GET/products/sku/_.json" to "any-sku",
        "GET/products/sku/_.meta.json" to """{"status":404,"delayMs":5}""",
        "PUT/products/_.json" to "updated",
        "GET/a/_/c.json" to "middle-wildcard",
        "GET/products@category=names.json" to "names-only",
    )
    private val router = MockRouter({ files[it] }, defaultDelayMs = 0)

    @Test
    fun `exact path is used with default status`() {
        assertEquals(MockResponse(200, "list", 0), router.resolve("GET", "/products"))
    }

    @Test
    fun `an exact file wins over the wildcard`() {
        assertEquals("luna", router.resolve("GET", "/products/sku/LUNA").body)
    }

    @Test
    fun `wildcard file and its meta apply to any other segment`() {
        assertEquals(MockResponse(404, "any-sku", 5), router.resolve("GET", "/products/sku/OTHER"))
    }

    @Test
    fun `wildcards can sit in the middle of the path`() {
        assertEquals("middle-wildcard", router.resolve("GET", "/a/123/c").body)
    }

    @Test
    fun `method is part of the route`() {
        assertEquals("updated", router.resolve("put", "/products/abc").body)
        assertEquals(404, router.resolve("DELETE", "/products/abc").status)
    }

    @Test
    fun `unknown routes answer 404 with an explicit message`() {
        val response = router.resolve("GET", "/nope")
        assertEquals(404, response.status)
        assertTrue(response.body.contains("No mock for GET /nope"))
    }

    @Test
    fun `a query-specific file wins, otherwise the plain path answers`() {
        assertEquals("names-only", router.resolve("GET", "/products", "category=names").body)
        assertEquals("list", router.resolve("GET", "/products", "category=letters").body)
    }
}
