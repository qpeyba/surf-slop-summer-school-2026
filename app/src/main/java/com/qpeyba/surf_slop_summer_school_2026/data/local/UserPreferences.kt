package com.qpeyba.surf_slop_summer_school_2026.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.dataStore

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_KEY] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_KEY] = enabled
        }
    }

    companion object {
        private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
    }
}
