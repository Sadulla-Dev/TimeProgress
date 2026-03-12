package com.example.yearprogress

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel

class MainViewModel(context: Application) : AndroidViewModel(context) {

    private val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var isFirstLaunch by mutableStateOf(sharedPrefs.getBoolean("is_first_launch", true))
        private set

    var language by mutableStateOf(sharedPrefs.getString("language", "en") ?: "en")
        private set

    // Nomini o'zgartiramiz: oldingi clash bo'lmaydi
    fun changeLanguage(lang: String) {
        sharedPrefs.edit { putString("language", lang) }
        sharedPrefs.edit { putBoolean("is_first_launch", false) }
        language = lang
        isFirstLaunch = false
    }
}