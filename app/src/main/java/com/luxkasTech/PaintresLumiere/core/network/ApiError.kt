package com.luxkasTech.PaintresLumiere.core.network

import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText

/**
 * Every failure a repository can surface. Mirrors iOS `APIError`: 401 and 409 get their own cases,
 * everything else non-2xx is [Http]. A raw status code is never shown to the user — use
 * [toUiText].
 *
 * @property serverMessage the `error` field of the API's `{ "error": "..." }` body, when present.
 */
sealed class ApiError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    class Network(cause: Throwable) : ApiError("Network failure", cause)
    class Unauthorized(val serverMessage: String? = null) : ApiError("Unauthorized")
    class Conflict(val serverMessage: String?) : ApiError("Conflict: $serverMessage")
    class Http(val statusCode: Int, val serverMessage: String?) : ApiError("HTTP $statusCode: $serverMessage")
    class Decoding(cause: Throwable) : ApiError("Failed to decode response", cause)
    class Unknown(cause: Throwable) : ApiError("Unexpected error", cause)
}

fun Throwable.toUiText(): UiText = when (this) {
    is ApiError.Network -> UiText.Resource(R.string.error_network)
    is ApiError.Unauthorized -> UiText.Resource(R.string.error_unauthorized)
    is ApiError.Conflict -> serverMessage?.let(UiText::Raw) ?: UiText.Resource(R.string.error_conflict)
    is ApiError.Http -> serverMessage?.let(UiText::Raw) ?: UiText.Resource(R.string.error_request_failed)
    is ApiError.Decoding -> UiText.Resource(R.string.error_decoding)
    else -> UiText.Resource(R.string.error_unknown)
}
