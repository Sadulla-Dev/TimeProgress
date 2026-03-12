package com.example.yearprogress

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.yearprogress.components.LanguageDialog
import com.example.yearprogress.components.ProgressTracker
import com.example.yearprogress.ui.theme.YearProgressTheme
import com.example.yearprogress.utils.LanguageManager

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val sharedPrefs = newBase.getSharedPreferences("app_prefs", MODE_PRIVATE)
        val language = sharedPrefs.getString("language", "en") ?: "en"
        super.attachBaseContext(LanguageManager.changeLanguage(newBase, language))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YearProgressTheme {
                MainScreen()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun MainScreen() {
        val showDialog = remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            ProgressTracker()

            Button(
                onClick = { showDialog.value = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text(text = stringResource(id = R.string.language_selection))
            }

            if (showDialog.value) {
                LanguageDialog(
                    onDismiss = { showDialog.value = false },
                    onLanguageSelected = { language ->
                        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                        sharedPrefs.edit().putString("language", language).apply()
                        recreate()
                    }
                )
            }
        }
    }
}
