package com.example.kanjireader.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

private object ThemeKeys {
    val THEME_MODE = intPreferencesKey("theme_mode")
}

object ThemePreferences {

    enum class ThemeMode(val value: Int) {
        LIGHT(0),
        DARK(1),
        AUTO(2)
    }

    fun getThemeMode(context: Context): Flow<ThemeMode> {
        return context.dataStore.data.map { preferences ->
            val value = preferences[ThemeKeys.THEME_MODE] ?: ThemeMode.AUTO.value
            ThemeMode.values().firstOrNull { it.value == value } ?: ThemeMode.AUTO
        }
    }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[ThemeKeys.THEME_MODE] = mode.value
        }
    }
}