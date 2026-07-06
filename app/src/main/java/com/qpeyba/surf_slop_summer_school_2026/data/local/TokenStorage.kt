package com.qpeyba.surf_slop_summer_school_2026.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "token_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun saveToken(token: String, expiresIn: Long) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + expiresIn * 1000)
            .apply()
    }

    suspend fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    suspend fun hasValidToken(): Boolean {
        val token = prefs.getString(KEY_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0)
        return token != null && System.currentTimeMillis() < expiresAt
    }

    suspend fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "access_token"
        private const val KEY_EXPIRES_AT = "expires_at"
    }
}
