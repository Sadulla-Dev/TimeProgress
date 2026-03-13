package com.example.yearprogress

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.yearprogress.components.LanguageDialog
import com.example.yearprogress.components.ProgressTracker
import com.example.yearprogress.ui.theme.YearProgressTheme
import com.example.yearprogress.utils.LanguageManager
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val sharedPrefs = newBase.getSharedPreferences("app_prefs", MODE_PRIVATE)
        val language = sharedPrefs.getString("language", "en") ?: "en"
        super.attachBaseContext(LanguageManager.changeLanguage(newBase, language))
    }

    private val viewModel: MainViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {

    val context = LocalContext.current

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    Box(modifier = Modifier.fillMaxSize()) {

        ProgressTracker(
            currentMode = themeMode,
            onChangeTheme = {viewModel.setThemeMode(it)},
            onChangeLanguage = { lang ->
                viewModel.changeLanguage(lang)
                (context as Activity).recreate()
            }
        )

        if (viewModel.isFirstLaunch) {
            LanguageDialog(
                onDismiss = { /* dismiss qilinmaydi */ },
                onLanguageSelected = { language ->
                    viewModel.changeLanguage(language)
                    (context as Activity).recreate()
                }
            )
        }
    }
}