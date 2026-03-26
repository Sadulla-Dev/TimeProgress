package com.example.yearprogress.components.tabs


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yearprogress.R
import com.example.yearprogress.components.LanguageDialog
import com.example.yearprogress.utils.safeClickable
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.ui.theme.ThemeMode
import com.example.yearprogress.ui.theme.YearProgressTheme
import com.example.yearprogress.utils.PreferenceManager
import com.example.yearprogress.utils.UZ_LIFE_EXPECTANCY
import com.example.yearprogress.utils.WeekStartDay
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    currentMode: ThemeMode,
    onChangeTheme: (ThemeMode) -> Unit,
    onChangeLanguage: (String) -> Unit,
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context.applicationContext) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var weekStartDay by remember { mutableStateOf(preferenceManager.getWeekStartDay()) }

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── THEME section ─────────────────────────────────────────────────
        SettingsSectionLabel(label = stringResource(R.string.settings_appearance))
        Spacer(Modifier.height(10.dp))

        // Theme cards
        val themes = listOf(
            ThemeMode.CUSTOM_DARK to stringResource(R.string.theme_custom_dark),
            ThemeMode.SYSTEM_LIGHT to stringResource(R.string.theme_system_light),
        )

        themes.forEach { (mode, label) ->
            val isSelected = currentMode == mode
            val previewBg = when (mode) {
                ThemeMode.CUSTOM_DARK -> Color(0xFF0A0A0F)
                ThemeMode.SYSTEM_LIGHT -> Color(0xFFF8F8FC)
            }
            val previewAccent = when (mode) {
                ThemeMode.CUSTOM_DARK -> Color(0xFF818CF8)
                ThemeMode.SYSTEM_LIGHT -> Color(0xFF6366F1)
            }
            val previewCard = when (mode) {
                ThemeMode.CUSTOM_DARK -> Color(0xFF111118)
                ThemeMode.SYSTEM_LIGHT -> Color(0xFFFFFFFF)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) ProgressColors.colorWeek.copy(alpha = 0.07f) else ProgressColors.bgCard
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) ProgressColors.colorWeek.copy(alpha = 0.4f) else ProgressColors.cardBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .safeClickable { onChangeTheme(mode) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Mini theme preview
                Box(
                    modifier = Modifier
                        .size(52.dp, 38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(previewBg)
                        .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(8.dp))
                ) {
                    // Mini card
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp, 22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(previewCard)
                    )
                    // Mini progress bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = 8.dp, y = (-8).dp)
                            .size(20.dp, 3.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(previewAccent)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) ProgressColors.textPrimary else ProgressColors.textMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = when (mode) {
                            ThemeMode.CUSTOM_DARK -> stringResource(R.string.theme_custom_dark_desc)
                            ThemeMode.SYSTEM_LIGHT -> stringResource(R.string.theme_system_light_desc)
                        },
                        fontSize = 10.sp,
                        color = ProgressColors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Selected dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) ProgressColors.colorWeek else Color.Transparent
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else ProgressColors.cardBorder,
                            CircleShape
                        )
                )
            }

            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(20.dp))

        // ── LANGUAGE section ──────────────────────────────────────────────
        SettingsSectionLabel(label = stringResource(R.string.settings_language))
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ProgressColors.bgCard)
                .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(16.dp))
                .safeClickable { showLanguageDialog = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.change_language),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ProgressColors.textPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = stringResource(R.string.change_language_desc),
                    fontSize = 10.sp,
                    color = ProgressColors.textDim,
                    fontFamily = FontFamily.Monospace
                )
            }
            // Arrow
            Text(
                text = "›",
                fontSize = 22.sp,
                color = ProgressColors.textMuted,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(20.dp))

        SettingsSectionLabel(label = stringResource(R.string.settings_week))
        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ProgressColors.bgCard)
                .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.week_start_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ProgressColors.textPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = stringResource(R.string.week_start_desc),
                    fontSize = 10.sp,
                    color = ProgressColors.textDim,
                    fontFamily = FontFamily.Monospace
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        WeekStartDay.MONDAY to stringResource(R.string.week_start_monday),
                        WeekStartDay.SUNDAY to stringResource(R.string.week_start_sunday),
                    ).forEach { (option, label) ->
                        val selected = option == weekStartDay
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) ProgressColors.colorWeek.copy(0.12f) else ProgressColors.progress
                                )
                                .border(
                                    1.dp,
                                    if (selected) ProgressColors.colorWeek.copy(0.4f) else ProgressColors.cardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .safeClickable {
                                    weekStartDay = option
                                    preferenceManager.setWeekStartDay(option)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                color = if (selected) ProgressColors.colorWeek else ProgressColors.textMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── ABOUT section ─────────────────────────────────────────────────
        SettingsSectionLabel(label = stringResource(R.string.settings_about))
        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ProgressColors.bgCard)
                .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AboutRow(
                    label = stringResource(R.string.about_app_name),
                    value = stringResource(R.string.app_name),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ProgressColors.cardBorder)
                )
                AboutRow(
                    label = stringResource(R.string.about_data_source),
                    value = stringResource(R.string.about_data_source_value),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ProgressColors.cardBorder)
                )
                AboutRow(
                    label = stringResource(R.string.about_life_expectancy),
                    value = "$UZ_LIFE_EXPECTANCY ${stringResource(R.string.year)}",
                )
            }
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        LanguageDialog(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = onChangeLanguage
        )
    }
}

@Composable
fun SettingsSectionLabel(label: String) {
    Text(
        text = label,
        fontSize = 10.sp,
        color = ProgressColors.textDim,
        letterSpacing = 2.sp,
        fontFamily = FontFamily.Monospace
    )
}

@Composable
fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = ProgressColors.textMuted,
            fontFamily = FontFamily.Monospace
        )
        Text(
            value,
            fontSize = 12.sp,
            color = ProgressColors.textPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() = YearProgressTheme {
    SettingsScreen(
        currentMode = ThemeMode.CUSTOM_DARK,
        onChangeTheme = {},
        onChangeLanguage = {}
    )
}
