package com.luxkasTech.PaintresLumiere.core.session

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores the token AES-256-GCM encrypted in a private SharedPreferences file, with the key held by
 * the Android Keystore (it never leaves secure hardware where available).
 *
 * `androidx.security:security-crypto` (EncryptedSharedPreferences) is deprecated, so this does the
 * same job directly with the platform APIs. The file is excluded from backups — see
 * `res/xml/backup_rules.xml`.
 *
 * If decryption ever fails (key invalidated, file restored from another device, corruption) the
 * stored value is dropped and the user is simply signed out — never a crash.
 */
@Singleton
class KeystoreTokenStorage @Inject constructor(
    @ApplicationContext context: Context,
) : TokenStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(): String? {
        val stored = prefs.getString(KEY_TOKEN, null) ?: return null
        return try {
            val bytes = Base64.decode(stored, Base64.NO_WRAP)
            require(bytes.size > IV_LENGTH) { "payload shorter than the IV" }
            val iv = bytes.copyOfRange(0, IV_LENGTH)
            val cipherText = bytes.copyOfRange(IV_LENGTH, bytes.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv))
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: GeneralSecurityException) {
            discard("Stored token could not be decrypted", e)
        } catch (e: ProviderException) {
            // The Keystore reports some hardware/key failures as this unchecked exception.
            discard("Keystore failed while decrypting the stored token", e)
        } catch (e: IllegalArgumentException) {
            discard("Stored token is malformed", e)
        }
    }

    /**
     * Like the iOS Keychain save, a failure here is not fatal: [SessionManager] keeps the token in
     * memory, so the user stays signed in until the app is closed and simply logs in again next time.
     */
    override fun write(token: String) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val payload = cipher.iv + cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            prefs.edit().putString(KEY_TOKEN, Base64.encodeToString(payload, Base64.NO_WRAP)).apply()
        } catch (e: GeneralSecurityException) {
            Log.w(TAG, "Token could not be encrypted; it will not survive an app restart", e)
            clear()
        } catch (e: ProviderException) {
            Log.w(TAG, "Keystore failed while encrypting the token; it will not survive an app restart", e)
            clear()
        }
    }

    private fun discard(reason: String, e: Exception): String? {
        Log.w(TAG, "$reason; clearing session", e)
        clear()
        return null
    }

    override fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val TAG = "KeystoreTokenStorage"
        const val PREFS_NAME = "pl_session"
        const val KEY_TOKEN = "access_token"
        const val KEY_ALIAS = "pl_session_key"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH = 12
        const val TAG_LENGTH_BITS = 128
    }
}
