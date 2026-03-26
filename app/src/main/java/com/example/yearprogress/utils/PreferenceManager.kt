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
import java.time.LocalDate

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

    private object BirthDateKeys {
        const val BIRTH_DATE_ISO = "birth_date_iso"
    }

    private object LifeKeys {
        const val LIFE_EXPECTANCY_PRESET = "life_expectancy_preset"
        const val CUSTOM_LIFE_EXPECTANCY = "custom_life_expectancy"
        const val WEEK_START_DAY = "week_start_day"
    }

    fun getBirthDate(): LocalDate? {
        val raw = sharedPrefs.getString(BirthDateKeys.BIRTH_DATE_ISO, null) ?: return null
        return runCatching { LocalDate.parse(raw) }.getOrNull()
    }

    fun setBirthDate(date: LocalDate) {
        sharedPrefs.edit().putString(BirthDateKeys.BIRTH_DATE_ISO, date.toString()).apply()
    }

    fun clearBirthDate() {
        sharedPrefs.edit().remove(BirthDateKeys.BIRTH_DATE_ISO).apply()
    }

    fun getLifeExpectancyPresetId(): String {
        return sharedPrefs.getString(LifeKeys.LIFE_EXPECTANCY_PRESET, "uzbekistan") ?: "uzbekistan"
    }

    fun setLifeExpectancyPresetId(id: String) {
        sharedPrefs.edit().putString(LifeKeys.LIFE_EXPECTANCY_PRESET, id).apply()
    }

    fun getCustomLifeExpectancy(): Double {
        return sharedPrefs.getString(
            LifeKeys.CUSTOM_LIFE_EXPECTANCY,
            DEFAULT_CUSTOM_LIFE_EXPECTANCY.toString()
        )?.toDoubleOrNull() ?: DEFAULT_CUSTOM_LIFE_EXPECTANCY
    }

    fun setCustomLifeExpectancy(years: Double) {
        sharedPrefs.edit()
            .putString(LifeKeys.CUSTOM_LIFE_EXPECTANCY, years.toString())
            .apply()
    }

    fun getWeekStartDay(): WeekStartDay {
        val raw = sharedPrefs.getString(LifeKeys.WEEK_START_DAY, WeekStartDay.SUNDAY.name)
        return runCatching { WeekStartDay.valueOf(raw ?: WeekStartDay.SUNDAY.name) }
            .getOrDefault(WeekStartDay.SUNDAY)
    }

    fun setWeekStartDay(day: WeekStartDay) {
        sharedPrefs.edit().putString(LifeKeys.WEEK_START_DAY, day.name).apply()
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
