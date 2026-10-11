package com.luxkasTech.PaintresLumiere.data.cart

import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import com.luxkasTech.PaintresLumiere.testing.MemoryTokenStorage
import com.luxkasTech.PaintresLumiere.testing.testVariant
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.math.BigDecimal

class RemoteCartRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var api: CartApi
    private val session = SessionManager(MemoryTokenStorage("jwt"))

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(Json { ignoreUnknownKeys = true; explicitNulls = false }.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CartApi::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    private fun cartJson(quantity: Int, stock: Int = 5) = """
        {"items":[{"id":"ci-1","productId":"v1","quantity":$quantity,
          "product":{"name":"Luna","color":"gold","dimensions":{"width":10,"unit":"cm"},
                     "sellingPrice":50.00,"discountRate":0.10,"stockQuantity":$stock,"status":"active"}}],
         "subtotal":0,"discountTotal":0,"total":0}
    """

    @Test
    fun `add sends the variant id and publishes the server cart`() = runTest {
        val repo = RemoteCartRepository(api, session, backgroundScope)
        server.enqueue(MockResponse().setBody(cartJson(2)))
        val cart = repo.addItem(testVariant("v1"), 2)
        assertTrue(server.takeRequest().body.readUtf8().contains("\"productId\":\"v1\""))
        assertEquals(2, repo.cart.value.itemCount)
        assertEquals(BigDecimal("45.00"), cart.items.single().finalUnitPrice)
    }

    @Test
    fun `a 400 on add becomes InsufficientStock with what is left`() = runTest {
        val repo = RemoteCartRepository(api, session, backgroundScope)
        server.enqueue(MockResponse().setBody(cartJson(4)))
        repo.refresh()
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"error":"Only 5 available"}"""))
        try {
            repo.addItem(testVariant("v1", stock = 5), 3)
            fail("Expected InsufficientStock")
        } catch (e: CartError.InsufficientStock) {
            assertEquals(1, e.available)
        }
    }

    @Test
    fun `quantity zero removes the line`() = runTest {
        val repo = RemoteCartRepository(api, session, backgroundScope)
        server.enqueue(MockResponse().setBody("""{"items":[]}"""))
        repo.updateQuantity("ci-1", 0)
        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/cart/items/ci-1", request.path)
    }
}
