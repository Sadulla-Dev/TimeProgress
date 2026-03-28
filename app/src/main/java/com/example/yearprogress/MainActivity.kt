package com.example.yearprogress

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.yearprogress.ui.theme.YearProgressTheme
import com.example.yearprogress.utils.LanguageManager

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val sharedPrefs = newBase.getSharedPreferences("app_prefs", MODE_PRIVATE)
        val language = sharedPrefs.getString("language", "en") ?: "en"
        super.attachBaseContext(LanguageManager.changeLanguage(newBase, language))
    }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            YearProgressTheme(themeMode = themeMode) {
                MainScreen()
            }
        }
    }
}