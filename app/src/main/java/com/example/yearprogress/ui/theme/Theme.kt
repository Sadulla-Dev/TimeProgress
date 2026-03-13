package com.example.yearprogress.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


val ProgressColors
    @Composable
    get() = AppTheme.colors

enum class ThemeMode {
    CUSTOM_DARK,   // Hozirgi qora dizayn
    SYSTEM_LIGHT,  // System light colors
}

// ── AppColors data class — barcha composable larga pass qilinadi ─────────────
data class AppColors(
    val bgDark: Color,
    val bgCard: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val textDim: Color,
    val colorYear: Color,
    val colorMonth: Color,
    val colorWeek: Color,
    val colorDay: Color,
    val colorLife: Color,
    val progress: Color,
    val fieldBackground: Color,
)

val CustomDarkColors = AppColors(
    bgDark = BG_DARK,
    bgCard = BG_CARD,
    cardBorder = CARD_BORDER,
    textPrimary = TEXT_PRIMARY,
    textMuted = TEXT_MUTED,
    textDim = TEXT_DIM,
    colorYear = COLOR_YEAR,
    colorMonth = COLOR_MONTH,
    colorWeek = COLOR_WEEK,
    colorDay = COLOR_DAY,
    colorLife = COLOR_LIFE,
    progress = Progress,
    fieldBackground = FIELD_DARK
)

val SystemLightColors = AppColors(
    bgDark = LIGHT_BG_DARK,
    bgCard = LIGHT_BG_CARD,
    cardBorder = LIGHT_CARD_BORDER,
    textPrimary = LIGHT_TEXT_PRIMARY,
    textMuted = LIGHT_TEXT_MUTED,
    textDim = LIGHT_TEXT_DIM,
    colorYear = LIGHT_COLOR_YEAR,
    colorMonth = LIGHT_COLOR_MONTH,
    colorWeek = LIGHT_COLOR_WEEK,
    colorDay = LIGHT_COLOR_DAY,
    colorLife = LIGHT_COLOR_LIFE,
    progress = LIGHT_CARD_BORDER,
    fieldBackground = FIELD_LIGHT
)

// ── CompositionLocal ─────────────────────────────────────────────────────────
val LocalAppColors = compositionLocalOf { CustomDarkColors }

// ── Convenience accessor ─────────────────────────────────────────────────────
object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}

// ── Main theme ───────────────────────────────────────────────────────────────
@Composable
fun YearProgressTheme(
    themeMode: ThemeMode = ThemeMode.CUSTOM_DARK,
    content: @Composable () -> Unit
) {
    val appColors = when (themeMode) {
        ThemeMode.CUSTOM_DARK -> CustomDarkColors
        ThemeMode.SYSTEM_LIGHT -> SystemLightColors
    }

    // Material theme — system mode larda dynamic color
    val materialColorScheme = when (themeMode) {
        ThemeMode.CUSTOM_DARK -> darkColorScheme(
            primary = COLOR_YEAR,
            background = BG_DARK,
            surface = BG_CARD,
        )

        ThemeMode.SYSTEM_LIGHT -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicLightColorScheme(LocalContext.current)
            } else {
                lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}