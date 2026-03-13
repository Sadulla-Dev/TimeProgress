package com.example.yearprogress.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.yearprogress.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")
class PreferenceManager(private val context: Context) {

    private val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun getLanguage(): String = sharedPrefs.getString("language", "en") ?: "en"

    fun setLanguage(lang: String) {
        sharedPrefs.edit().putString("language", lang).apply()
    }

    fun isFirstLaunch(): Boolean = sharedPrefs.getBoolean("is_first_launch", true)

    fun setFirstLaunchDone() {
        sharedPrefs.edit().putBoolean("is_first_launch", false).apply()
    }

    // ── DataStore — faqat ThemeMode uchun ───────────────────────────────────
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data
        .map { prefs ->
            val modeName = prefs[Keys.THEME_MODE] ?: ThemeMode.CUSTOM_DARK.name
            try { ThemeMode.valueOf(modeName) } catch (e: Exception) { ThemeMode.CUSTOM_DARK }
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }
}