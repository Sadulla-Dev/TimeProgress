package com.example.yearprogress

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.yearprogress.ui.theme.ThemeMode
import com.example.yearprogress.utils.PreferenceManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferenceManager = PreferenceManager(application.applicationContext)

    val themeMode: StateFlow<ThemeMode> = preferenceManager.themeModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.CUSTOM_DARK
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferenceManager.setThemeMode(mode) }
    }

    var language by mutableStateOf(preferenceManager.getLanguage())
        private set

    var isFirstLaunch by mutableStateOf(preferenceManager.isFirstLaunch())
        private set

    fun changeLanguage(lang: String) {
        preferenceManager.setLanguage(lang)
        preferenceManager.setFirstLaunchDone()
        language = lang
        isFirstLaunch = false
    }
}