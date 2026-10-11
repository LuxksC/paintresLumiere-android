package com.luxkasTech.PaintresLumiere.core.session

/** Persists the API access token (JWT). The Android counterpart of the iOS `KeychainService`. */
interface TokenStorage {
    fun read(): String?
    fun write(token: String)
    fun clear()
}
