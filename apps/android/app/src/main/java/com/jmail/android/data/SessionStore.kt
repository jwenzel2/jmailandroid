package com.jmail.android.data

import android.content.Context
import android.util.Base64
import com.jmail.android.BuildConfig
import java.net.URI
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("jmail_session", Context.MODE_PRIVATE)
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    var serverUrl: String?
        get() = normalizeServerUrl(prefs.getString("server_url", BuildConfig.DEFAULT_SERVER_URL))
        set(value) =
            prefs.edit().apply {
                val normalized = normalizeServerUrl(value)
                if (normalized == null) remove("server_url") else putString("server_url", normalized)
            }.apply()

    var accessToken: String?
        get() = prefs.getString("access_token", null)?.let(::decrypt)
        set(value) = prefs.edit().apply {
            if (value == null) remove("access_token") else putString("access_token", encrypt(value))
        }.apply()

    var darkTheme: Boolean
        get() = prefs.getBoolean("dark_theme", false)
        set(value) = prefs.edit().putBoolean("dark_theme", value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun key(): SecretKey {
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance("AES", "AndroidKeyStore").apply {
            init(android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                    android.security.keystore.KeyProperties.PURPOSE_DECRYPT,
            ).setBlockModes("GCM").setEncryptionPaddings("NoPadding").build())
        }.generateKey()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return Base64.encodeToString(cipher.iv + cipher.doFinal(value.toByteArray()), Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val bytes = Base64.decode(value, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)))
    }

    companion object {
        private const val KEY_ALIAS = "jmail_mobile_session"

        fun normalizeServerUrl(value: String?): String? {
            val trimmed = value?.trim()?.trimEnd('/')?.takeIf { it.isNotEmpty() } ?: return null
            val withScheme =
                if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed
                else "https://$trimmed"
            val uri = URI.create(withScheme)
            val port = if (uri.port == -1) "" else ":${uri.port}"
            return "${uri.scheme}://${uri.host}$port"
        }
    }
}
