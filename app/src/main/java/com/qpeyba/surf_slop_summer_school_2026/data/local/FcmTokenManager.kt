package com.qpeyba.surf_slop_summer_school_2026.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)

    suspend fun saveFcmToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    suspend fun getFcmToken(): String? {
        return prefs.getString(KEY_FCM_TOKEN, null)
    }

    suspend fun clear() {
        prefs.edit().remove(KEY_FCM_TOKEN).apply()
    }

    companion object {
        private const val KEY_FCM_TOKEN = "fcm_token"
    }
}
