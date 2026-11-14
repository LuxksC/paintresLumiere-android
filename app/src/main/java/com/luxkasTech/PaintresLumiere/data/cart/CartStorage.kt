package com.luxkasTech.PaintresLumiere.data.cart

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** Persists the local cart so it survives relaunch (iOS uses UserDefaults for the same purpose). */
interface CartStorage {
    fun load(): Cart
    fun save(cart: Cart)
}

@Singleton
class SharedPreferencesCartStorage @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json,
) : CartStorage {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun load(): Cart {
        val raw = prefs.getString(KEY, null) ?: return Cart()
        return try {
            json.decodeFromString<Cart>(raw)
        } catch (e: SerializationException) {
            Log.w(TAG, "Discarding unreadable local cart", e)
            Cart()
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Discarding unreadable local cart", e)
            Cart()
        }
    }

    override fun save(cart: Cart) {
        prefs.edit().putString(KEY, json.encodeToString(Cart.serializer(), cart)).apply()
    }

    private companion object {
        const val TAG = "CartStorage"
        const val PREFS = "pl_cart"
        const val KEY = "cart"
    }
}
