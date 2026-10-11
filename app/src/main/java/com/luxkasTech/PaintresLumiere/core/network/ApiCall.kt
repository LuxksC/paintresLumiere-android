package com.luxkasTech.PaintresLumiere.core.network

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

private val errorBodyJson = Json { ignoreUnknownKeys = true }

/**
 * Runs a Retrofit call and translates every failure into an [ApiError], so repositories expose a
 * single, documented error type. Coroutine cancellation is always rethrown untouched.
 */
suspend fun <T> apiCall(block: suspend () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: ApiError) {
    throw e
} catch (e: HttpException) {
    throw e.toApiError()
} catch (e: IOException) {
    throw ApiError.Network(e)
} catch (e: SerializationException) {
    throw ApiError.Decoding(e)
} catch (e: IllegalArgumentException) {
    // Retrofit wraps some converter failures this way.
    throw ApiError.Decoding(e)
} catch (e: Exception) {
    throw ApiError.Unknown(e)
}

internal fun HttpException.toApiError(): ApiError {
    val message = response()?.errorBody()?.string()?.let(::parseErrorMessage)
    return when (code()) {
        401 -> ApiError.Unauthorized(message)
        409 -> ApiError.Conflict(message)
        else -> ApiError.Http(code(), message)
    }
}

internal fun parseErrorMessage(body: String): String? = try {
    errorBodyJson.parseToJsonElement(body).jsonObject["error"]?.jsonPrimitive?.content
} catch (e: SerializationException) {
    null
} catch (e: IllegalArgumentException) {
    null
}
