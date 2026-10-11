package com.luxkasTech.PaintresLumiere.core.network.mock

import com.luxkasTech.PaintresLumiere.core.network.apiCall
import com.luxkasTech.PaintresLumiere.data.admin.AdminProductApi
import com.luxkasTech.PaintresLumiere.data.admin.ProductUpsertRequest
import com.luxkasTech.PaintresLumiere.data.auth.AuthApi
import com.luxkasTech.PaintresLumiere.data.auth.GoogleAuthRequest
import com.luxkasTech.PaintresLumiere.data.auth.LoginRequest
import com.luxkasTech.PaintresLumiere.data.auth.SignUpRequest
import com.luxkasTech.PaintresLumiere.data.order.CreateOrderRequest
import com.luxkasTech.PaintresLumiere.data.order.OrderApi
import com.luxkasTech.PaintresLumiere.data.order.OrderStatus
import com.luxkasTech.PaintresLumiere.data.payment.CreatePaymentRequest
import com.luxkasTech.PaintresLumiere.data.payment.PaymentApi
import com.luxkasTech.PaintresLumiere.data.payment.PaymentMethod
import com.luxkasTech.PaintresLumiere.data.payment.PaymentStatus
import com.luxkasTech.PaintresLumiere.data.product.ProductApi
import com.luxkasTech.PaintresLumiere.data.profile.ProfileApi
import com.luxkasTech.PaintresLumiere.data.profile.UserType
import com.luxkasTech.PaintresLumiere.core.network.ApiError
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File

/**
 * Runs every mock file through the **real** Retrofit + kotlinx.serialization stack, exactly as the
 * `mock` flavor does at runtime. A mock that drifts from the models fails here, in CI.
 */
class MockFixturesTest {

    private val mocksDir = File("src/main/assets/mocks")
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://mock.invalid/")
        .client(
            OkHttpClient.Builder()
                .addInterceptor(MockInterceptor(MockRouter({ path -> File(mocksDir, path).takeIf { it.exists() }?.readText() }, 0)))
                .build(),
        )
        .addConverterFactory(Json { ignoreUnknownKeys = true; explicitNulls = false }.asConverterFactory("application/json".toMediaType()))
        .build()

    private val products = retrofit.create(ProductApi::class.java)
    private val auth = retrofit.create(AuthApi::class.java)
    private val profile = retrofit.create(ProfileApi::class.java)
    private val admin = retrofit.create(AdminProductApi::class.java)
    private val orders = retrofit.create(OrderApi::class.java)
    private val payments = retrofit.create(PaymentApi::class.java)

    @Test
    fun `catalog mocks decode, and every listed SKU has variants`() = runTest {
        val catalog = apiCall { products.getProducts() }.products
        assertTrue(catalog.isNotEmpty())
        catalog.forEach { product ->
            val variants = apiCall { products.getProductBySku(product.sku) }.products
            assertTrue("SKU ${product.sku} has no variants mock", variants.isNotEmpty())
        }
        assertTrue(apiCall { products.getPopularProducts(5) }.products.isNotEmpty())
        val categories = apiCall { products.getCategories() }.categories
        assertTrue(categories.isNotEmpty())
        categories.forEach { summary ->
            val inCategory = apiCall { products.getProducts(category = summary.category.raw) }.products
            assertEquals("count for ${summary.category}", summary.productCount, inCategory.size)
        }
    }

    @Test
    fun `an unknown SKU behaves like the API 404`() = runTest {
        try {
            apiCall { products.getProductBySku("DOES-NOT-EXIST") }
            fail("Expected 404")
        } catch (e: ApiError.Http) {
            assertEquals(404, e.statusCode)
        }
    }

    @Test
    fun `auth and profile mocks decode`() = runTest {
        assertEquals("mock-access-token", apiCall { auth.login(LoginRequest("a@b.c", "x")) }.accessToken)
        assertTrue(apiCall { auth.signUp(SignUpRequest("A", "a@b.c", "12345678")) }.accessToken.isNotBlank())
        assertTrue(apiCall { auth.authenticateWithGoogle(GoogleAuthRequest("t")) }.accessToken.isNotBlank())
        apiCall { auth.logout() }
        apiCall { auth.deleteAccount() }
        assertEquals(UserType.ADMIN, apiCall { profile.getProfile() }.type)
        assertEquals("Lucas", apiCall { profile.updateProfile(com.luxkasTech.PaintresLumiere.data.profile.UpdateProfileRequest(phone = "")) }.name)
    }

    @Test
    fun `admin mocks decode`() = runTest {
        assertTrue(apiCall { admin.create(ProductUpsertRequest(name = "X")) }.id.isNotBlank())
        apiCall { admin.update("any-id", ProductUpsertRequest(name = "Y")) }
        apiCall { admin.delete("any-id") }
    }

    @Test
    fun `order mocks decode and every listed order has a detail`() = runTest {
        val page = apiCall { orders.getOrders() }
        assertEquals(page.total, page.data.size)
        page.data.forEach { summary ->
            val detail = apiCall { orders.getOrder(summary.id) }
            assertEquals(summary.status, detail.status)
            assertEquals(summary.totalAmount, detail.totalAmount)
            assertTrue(detail.statusHistory.isNotEmpty())
        }
        val created = apiCall { orders.createOrder(CreateOrderRequest()) }
        assertEquals(OrderStatus.RECEIVED, created.status)
        assertEquals(created.id, apiCall { orders.getOrder(created.id) }.id)
    }

    @Test
    fun `payment mocks answer per method`() = runTest {
        val pix = apiCall { payments.createPayment("ord-1002", CreatePaymentRequest(PaymentMethod.PIX)) }
        assertEquals(PaymentMethod.PIX, pix.method)
        assertTrue(!pix.pixQrCode.isNullOrBlank() && !pix.pixCopyPaste.isNullOrBlank())
        val boleto = apiCall { payments.createPayment("ord-1002", CreatePaymentRequest(PaymentMethod.BOLETO_INSTALLMENTS, 3)) }
        assertEquals(3, boleto.installmentCount)
        val card = apiCall { payments.createPayment("ord-1002", CreatePaymentRequest(PaymentMethod.CREDIT_CARD, 1, "tok")) }
        assertEquals(PaymentStatus.CONFIRMED, card.status)
        assertEquals(PaymentStatus.PENDING, apiCall { orders.getOrder("ord-1002") }.latestPayment?.status)
    }

    @Test
    fun `every mock file is covered by this test`() {
        // When you add a mock, exercise it above and list it here.
        val covered = setOf(
            "DELETE/products/_.json", "DELETE/users/me.json", "GET/products.json", "GET/products/popular.json", "GET/products/categories.json",
            "GET/products/sku/_.json", "GET/products/sku/_.meta.json", "GET/profile.json", "PUT/profile.json", "POST/auth/google.json",
            "POST/login.json", "POST/logout.json", "POST/products.json", "POST/products.meta.json",
            "POST/signup.json", "POST/signup.meta.json", "PUT/products/_.json",
            "POST/orders/_/payment~method=pix.json", "POST/orders/_/payment~method=pix.meta.json",
            "POST/orders/_/payment~method=boleto_parcelado.json", "POST/orders/_/payment~method=boleto_parcelado.meta.json",
            "POST/orders/_/payment~method=credit_card.json", "POST/orders/_/payment~method=credit_card.meta.json",
            "GET/orders.json", "GET/orders/_.json", "GET/orders/_.meta.json", "POST/orders.json", "POST/orders.meta.json",
        )
        val files = mocksDir.walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .map { it.relativeTo(mocksDir).invariantSeparatorsPath }
            .filterNot { it.startsWith("GET/products/sku/") && it != "GET/products/sku/_.json" && it != "GET/products/sku/_.meta.json" }
            .filterNot { it.startsWith("GET/products@category=") } // exercised through the categories loop
            .filterNot { it.startsWith("GET/orders/ord-") } // exercised through the orders loop
            .toSet()
        assertEquals("Register new mocks in MockFixturesTest", emptySet<String>(), files - covered)
    }
}
