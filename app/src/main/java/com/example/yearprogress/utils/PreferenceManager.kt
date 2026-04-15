package com.example.yearprogress.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
        const val GOALS_JSON = "goals_json"
        const val HABITS_JSON = "habits_json"
        const val PINNED_GOAL_ID = "pinned_goal_id"
    }

    private object ReminderKeys {
        const val DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
        const val DAILY_REMINDER_HOUR = "daily_reminder_hour"
        const val DAILY_REMINDER_MINUTE = "daily_reminder_minute"
        const val WEEKLY_REMINDER_ENABLED = "weekly_reminder_enabled"
        const val WEEKLY_REMINDER_HOUR = "weekly_reminder_hour"
        const val WEEKLY_REMINDER_MINUTE = "weekly_reminder_minute"
        const val GOAL_REMINDER_ENABLED = "goal_reminder_enabled"
        const val GOAL_REMINDER_HOUR = "goal_reminder_hour"
        const val GOAL_REMINDER_MINUTE = "goal_reminder_minute"
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
        val raw = sharedPrefs.getString(LifeKeys.WEEK_START_DAY, WeekStartDay.MONDAY.name)
        return runCatching { WeekStartDay.valueOf(raw ?: WeekStartDay.MONDAY.name) }
            .getOrDefault(WeekStartDay.MONDAY)
    }

    fun setWeekStartDay(day: WeekStartDay) {
        sharedPrefs.edit().putString(LifeKeys.WEEK_START_DAY, day.name).apply()
    }

    fun getGoals(): List<GoalCountdown> {
        return goalsFromJson(sharedPrefs.getString(LifeKeys.GOALS_JSON, null))
    }

    fun saveGoals(goals: List<GoalCountdown>) {
        sharedPrefs.edit().putString(LifeKeys.GOALS_JSON, goalsToJson(goals)).apply()
    }

    fun getPinnedGoalId(): String? = sharedPrefs.getString(LifeKeys.PINNED_GOAL_ID, null)

    fun setPinnedGoalId(id: String) {
        sharedPrefs.edit().putString(LifeKeys.PINNED_GOAL_ID, id).apply()
    }

    fun clearPinnedGoalId() {
        sharedPrefs.edit().remove(LifeKeys.PINNED_GOAL_ID).apply()
    }

    fun getHabits(): List<HabitTracker> {
        return habitsFromJson(sharedPrefs.getString(LifeKeys.HABITS_JSON, null))
    }

    fun saveHabits(habits: List<HabitTracker>) {
        sharedPrefs.edit().putString(LifeKeys.HABITS_JSON, habitsToJson(habits)).apply()
    }

    fun isDailyReminderEnabled(): Boolean {
        return sharedPrefs.getBoolean(ReminderKeys.DAILY_REMINDER_ENABLED, false)
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(ReminderKeys.DAILY_REMINDER_ENABLED, enabled).apply()
    }

    fun getDailyReminderHour(): Int = sharedPrefs.getInt(ReminderKeys.DAILY_REMINDER_HOUR, 20)

    fun getDailyReminderMinute(): Int = sharedPrefs.getInt(ReminderKeys.DAILY_REMINDER_MINUTE, 0)

    fun setDailyReminderTime(hour: Int, minute: Int) {
        sharedPrefs.edit()
            .putInt(ReminderKeys.DAILY_REMINDER_HOUR, hour)
            .putInt(ReminderKeys.DAILY_REMINDER_MINUTE, minute)
            .apply()
    }

    fun isWeeklyReminderEnabled(): Boolean {
        return sharedPrefs.getBoolean(ReminderKeys.WEEKLY_REMINDER_ENABLED, false)
    }

    fun setWeeklyReminderEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(ReminderKeys.WEEKLY_REMINDER_ENABLED, enabled).apply()
    }

    fun getWeeklyReminderHour(): Int = sharedPrefs.getInt(ReminderKeys.WEEKLY_REMINDER_HOUR, 20)

    fun getWeeklyReminderMinute(): Int = sharedPrefs.getInt(ReminderKeys.WEEKLY_REMINDER_MINUTE, 0)

    fun setWeeklyReminderTime(hour: Int, minute: Int) {
        sharedPrefs.edit()
            .putInt(ReminderKeys.WEEKLY_REMINDER_HOUR, hour)
            .putInt(ReminderKeys.WEEKLY_REMINDER_MINUTE, minute)
            .apply()
    }

    fun isGoalReminderEnabled(): Boolean {
        return sharedPrefs.getBoolean(ReminderKeys.GOAL_REMINDER_ENABLED, false)
    }

    fun setGoalReminderEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(ReminderKeys.GOAL_REMINDER_ENABLED, enabled).apply()
    }

    fun getGoalReminderHour(): Int = sharedPrefs.getInt(ReminderKeys.GOAL_REMINDER_HOUR, 9)

    fun getGoalReminderMinute(): Int = sharedPrefs.getInt(ReminderKeys.GOAL_REMINDER_MINUTE, 0)

    fun setGoalReminderTime(hour: Int, minute: Int) {
        sharedPrefs.edit()
            .putInt(ReminderKeys.GOAL_REMINDER_HOUR, hour)
            .putInt(ReminderKeys.GOAL_REMINDER_MINUTE, minute)
            .apply()
    }

    // ── DataStore — faqat ThemeMode uchun ───────────────────────────────────
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data
        .map { prefs ->
            val modeName = prefs[Keys.THEME_MODE] ?: ThemeMode.CUSTOM_DARK.name
            try {
                ThemeMode.valueOf(modeName)
            } catch (e: Exception) {
                ThemeMode.CUSTOM_DARK
            }
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }
}
