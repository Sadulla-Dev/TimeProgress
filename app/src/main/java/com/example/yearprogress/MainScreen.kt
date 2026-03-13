@file:OptIn(ExperimentalLayoutApi::class)

package com.example.yearprogress

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yearprogress.components.LanguageDialog
import com.example.yearprogress.components.MainScreenTab


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {

    val context = LocalContext.current

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    Box(modifier = Modifier.fillMaxSize()) {

        MainScreenTab(
            currentMode = themeMode,
            onChangeTheme = { viewModel.setThemeMode(it) },
            onChangeLanguage = { lang ->
                viewModel.changeLanguage(lang)
                (context as Activity).recreate()
            }
        )

        if (viewModel.isFirstLaunch) {
            LanguageDialog(
                onDismiss = {},
                onLanguageSelected = { language ->
                    viewModel.changeLanguage(language)
                    (context as Activity).recreate()
                }
            )
        }
    }
}