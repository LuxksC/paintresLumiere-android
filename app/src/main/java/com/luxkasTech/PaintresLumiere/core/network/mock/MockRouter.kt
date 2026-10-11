package com.luxkasTech.PaintresLumiere.core.network.mock

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Reads a mock file by its path under `assets/mocks/`; null when it doesn't exist. */
fun interface MockAssetSource {
    fun read(path: String): String?
}

data class MockResponse(val status: Int, val body: String, val delayMs: Long)

/**
 * Optional sidecar `<file>.meta.json` next to a mock, to simulate errors or latency:
 * `{ "status": 409, "delayMs": 1500 }`.
 */
@Serializable
private data class MockMeta(val status: Int? = null, val delayMs: Long? = null)

/**
 * Maps a request to a mock file. Mocks are grouped by HTTP method and endpoint path:
 *
 * ```
 * assets/mocks/GET/products.json               → GET /products
 * assets/mocks/GET/products/sku/LUNA.json      → GET /products/sku/LUNA   (exact match wins)
 * assets/mocks/GET/products/sku/_.json         → GET /products/sku/{any}  (`_` = any segment)
 * assets/mocks/POST/login.json + login.meta.json
 * ```
 *
 * Candidates are tried from the most specific (no wildcard) to the least, so a specific file can
 * override the generic one. No match → 404 with an explicit message, like an unknown route.
 */
class MockRouter(
    private val source: MockAssetSource,
    private val defaultDelayMs: Long = DEFAULT_DELAY_MS,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun resolve(method: String, path: String): MockResponse {
        val segments = path.trim('/').split('/').filter { it.isNotEmpty() }
        for (candidate in candidates(method.uppercase(), segments)) {
            val body = source.read("$candidate.json") ?: continue
            val meta = source.read("$candidate.meta.json")?.let(::parseMeta) ?: MockMeta()
            return MockResponse(meta.status ?: 200, body, meta.delayMs ?: defaultDelayMs)
        }
        return MockResponse(404, """{"error":"No mock for $method /${segments.joinToString("/")}"}""", 0)
    }

    private fun parseMeta(raw: String): MockMeta? = try {
        json.decodeFromString<MockMeta>(raw)
    } catch (e: SerializationException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }

    /** Every combination of segments replaced by `_`, fewest wildcards first. */
    private fun candidates(method: String, segments: List<String>): List<String> {
        val n = segments.size
        return (0 until (1 shl n))
            .sortedBy { Integer.bitCount(it) }
            .map { mask ->
                val parts = segments.mapIndexed { i, s -> if (mask and (1 shl i) != 0) WILDCARD else s }
                (listOf(method) + parts).joinToString("/")
            }
    }

    companion object {
        const val DEFAULT_DELAY_MS = 400L
        const val WILDCARD = "_"
    }
}
