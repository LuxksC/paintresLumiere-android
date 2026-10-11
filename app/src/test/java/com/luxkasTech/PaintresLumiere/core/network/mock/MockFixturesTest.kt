package com.luxkasTech.PaintresLumiere.core.network.mock

import com.luxkasTech.PaintresLumiere.core.network.apiCall
import com.luxkasTech.PaintresLumiere.data.admin.AdminProductApi
import com.luxkasTech.PaintresLumiere.data.admin.ProductUpsertRequest
import com.luxkasTech.PaintresLumiere.data.auth.AuthApi
import com.luxkasTech.PaintresLumiere.data.auth.GoogleAuthRequest
import com.luxkasTech.PaintresLumiere.data.auth.LoginRequest
import com.luxkasTech.PaintresLumiere.data.auth.SignUpRequest
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
    }

    @Test
    fun `admin mocks decode`() = runTest {
        assertTrue(apiCall { admin.create(ProductUpsertRequest(name = "X")) }.id.isNotBlank())
        apiCall { admin.update("any-id", ProductUpsertRequest(name = "Y")) }
        apiCall { admin.delete("any-id") }
    }

    @Test
    fun `every mock file is covered by this test`() {
        // When you add a mock, exercise it above and list it here.
        val covered = setOf(
            "DELETE/products/_.json", "DELETE/users/me.json", "GET/products.json", "GET/products/popular.json", "GET/products/categories.json",
            "GET/products/sku/_.json", "GET/products/sku/_.meta.json", "GET/profile.json", "POST/auth/google.json",
            "POST/login.json", "POST/logout.json", "POST/products.json", "POST/products.meta.json",
            "POST/signup.json", "POST/signup.meta.json", "PUT/products/_.json",
        )
        val files = mocksDir.walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .map { it.relativeTo(mocksDir).invariantSeparatorsPath }
            .filterNot { it.startsWith("GET/products/sku/") && it != "GET/products/sku/_.json" && it != "GET/products/sku/_.meta.json" }
            .filterNot { it.startsWith("GET/products@category=") } // exercised through the categories loop
            .toSet()
        assertEquals("Register new mocks in MockFixturesTest", emptySet<String>(), files - covered)
    }
}
