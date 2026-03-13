@file:OptIn(ExperimentalLayoutApi::class)

package com.example.yearprogress.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yearprogress.R
import com.example.yearprogress.ui.theme.AppColors
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.ui.theme.ThemeMode
import com.example.yearprogress.utils.UZ_LIFE_EXPECTANCY
import com.example.yearprogress.utils.ageComponents
import com.example.yearprogress.utils.dayProgress
import com.example.yearprogress.utils.getDaySuffix
import com.example.yearprogress.utils.lifeProgress
import com.example.yearprogress.utils.monthProgress
import com.example.yearprogress.utils.weekProgress
import com.example.yearprogress.utils.yearProgress
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

private enum class MainTab { TIME, LIFE, SETTINGS }

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProgressTracker(
    currentMode: ThemeMode,
    onChangeTheme: (ThemeMode) -> Unit,
    onChangeLanguage: (String) -> Unit
) {
    val colors = ProgressColors

    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000); now = LocalDateTime.now()
        }
    }

    var selectedTab by remember { mutableStateOf(MainTab.TIME) }
    var birthDate by remember { mutableStateOf<LocalDate?>(null) }

    val months =
        listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
    val dayNames = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    val cards = listOf(
        Triple(
            stringResource(R.string.year), now.year.toString(),
            yearProgress(now) to ChronoUnit.SECONDS.between(
                LocalDateTime.of(now.year, 1, 1, 0, 0),
                LocalDateTime.of(now.year, 12, 31, 23, 59, 59)
            ) to colors.colorYear
        ),
        Triple(
            stringResource(R.string.month), months[now.monthValue - 1],
            monthProgress(now) to ChronoUnit.SECONDS.between(
                LocalDateTime.of(now.year, now.month, 1, 0, 0),
                LocalDateTime.of(now.year, now.month, 1, 0, 0).plusMonths(1).minusSeconds(1)
            ) to colors.colorMonth
        ),
        Triple(
            stringResource(R.string.week), dayNames[now.dayOfWeek.value % 7],
            weekProgress(now) to 604800L to colors.colorWeek
        ),
        Triple(
            stringResource(R.string.day), "${now.dayOfMonth}${getDaySuffix(now.dayOfMonth)}",
            dayProgress(now) to 86400L to colors.colorDay
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDark)
    ) {
        // Background glow
        Box(
            modifier = Modifier
                .size(500.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-200).dp)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF6366F1).copy(0.06f), Color.Transparent)),
                    CircleShape
                )
        )

        // ── Main content ──────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {

            // Scrollable area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(52.dp))

                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiveChip(colors)
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.textPrimary
                            )
                        ) {
                            append(stringResource(R.string.time) + "\n")
                        }
                        withStyle(
                            SpanStyle(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.textPrimary.copy(0.25f)
                            )
                        ) {
                            append(stringResource(R.string.is_passing))
                        }
                    }
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    stringResource(R.string.every_second_minute_hour_is_not_coming_back),
                    fontSize = 13.sp,
                    color = colors.textMuted
                )

                Spacer(Modifier.height(24.dp))

                // Tab content
                when (selectedTab) {
                    MainTab.TIME -> {
                        cards.forEach { (title, label, rest) ->
                            val (progressPair, color) = rest
                            val (progress, totalSec) = progressPair
                            TimeCard(
                                title = title,
                                label = label,
                                progress = progress,
                                totalSeconds = totalSec,
                                accentColor = color,
                                colors = colors
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    MainTab.LIFE -> {
                        if (birthDate != null) {
                            LifeSection(
                                birthDate = birthDate!!,
                                onReset = { birthDate = null },
                                colors = colors
                            )
                        } else {
                            BirthDateInput(onSubmit = { birthDate = it }, colors = colors)
                        }
                    }

                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            currentMode = currentMode,
                            onChangeTheme = onChangeTheme,
                            onChangeLanguage = onChangeLanguage,
                            colors = colors
                        )
                    }
                }

                if (selectedTab == MainTab.LIFE) {
                    Spacer(Modifier.height(24.dp))
                    // Footer
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            stringResource(
                                R.string.uzbekistan_average_life_expectancy,
                                UZ_LIFE_EXPECTANCY
                            ),
                            fontSize = 10.sp,
                            color = colors.textDim,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.based_on_world_bank_data),
                            fontSize = 9.sp,
                            color = colors.textDim.copy(0.6f),
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Bottom tab bar ────────────────────────────────────────────
            BottomTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                colors = colors
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOTTOM TAB BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BottomTabBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    colors: AppColors
) {
    // Tab accent ranglari
    val tabAccent = mapOf(
        MainTab.TIME to colors.colorYear,
        MainTab.LIFE to colors.colorLife,
        MainTab.SETTINGS to colors.colorWeek,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .border(
                width = 1.dp,
                color = colors.cardBorder,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val accent = tabAccent[tab] ?: colors.colorYear

                val label = when (tab) {
                    MainTab.TIME -> stringResource(R.string.tab_time)
                    MainTab.LIFE -> stringResource(R.string.tab_life)
                    MainTab.SETTINGS -> stringResource(R.string.tab_settings)
                }

                val icon = when (tab) {
                    MainTab.TIME -> "◷"
                    MainTab.LIFE -> "◈"
                    MainTab.SETTINGS -> "◎"
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) accent.copy(alpha = 0.10f) else Color.Transparent
                        )
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Icon dot — active holda accent, aks holda dim
//                    Box(
//                        modifier = Modifier
//                            .size(if (isSelected) 6.dp else 5.dp)
//                            .clip(CircleShape)
//                            .background(
//                                if (isSelected) accent else colors.textDim
//                            )
//                    )

                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) accent else colors.textMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    // Active indicator line
                    Box(
                        modifier = Modifier
                            .width(if (isSelected) 20.dp else 0.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (isSelected) accent else Color.Transparent)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SETTINGS SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsScreen(
    currentMode: ThemeMode,
    onChangeTheme: (ThemeMode) -> Unit,
    onChangeLanguage: (String) -> Unit,
    colors: AppColors
) {
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── THEME section ─────────────────────────────────────────────────
        SettingsSectionLabel(label = stringResource(R.string.settings_appearance), colors = colors)
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
            val previewText = when (mode) {
                ThemeMode.CUSTOM_DARK -> Color(0xFFFFFFFF)
                ThemeMode.SYSTEM_LIGHT -> Color(0xFF0A0A0F)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) colors.colorWeek.copy(alpha = 0.07f) else colors.bgCard
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) colors.colorWeek.copy(alpha = 0.4f) else colors.cardBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onChangeTheme(mode) }
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
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
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
                        color = if (isSelected) colors.textPrimary else colors.textMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = when (mode) {
                            ThemeMode.CUSTOM_DARK -> stringResource(R.string.theme_custom_dark_desc)
                            ThemeMode.SYSTEM_LIGHT -> stringResource(R.string.theme_system_light_desc)
                        },
                        fontSize = 10.sp,
                        color = colors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Selected dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) colors.colorWeek else Color.Transparent
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else colors.cardBorder,
                            CircleShape
                        )
                )
            }

            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(20.dp))

        // ── LANGUAGE section ──────────────────────────────────────────────
        SettingsSectionLabel(label = stringResource(R.string.settings_language), colors = colors)
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.bgCard)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                .clickable { showLanguageDialog = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.change_language),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = stringResource(R.string.change_language_desc),
                    fontSize = 10.sp,
                    color = colors.textDim,
                    fontFamily = FontFamily.Monospace
                )
            }
            // Arrow
            Text(
                text = "›",
                fontSize = 22.sp,
                color = colors.textMuted,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── ABOUT section ─────────────────────────────────────────────────
        SettingsSectionLabel(label = stringResource(R.string.settings_about), colors = colors)
        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.bgCard)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AboutRow(
                    label = stringResource(R.string.about_app_name),
                    value = stringResource(R.string.app_name),
                    colors = colors
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.cardBorder)
                )
                AboutRow(
                    label = stringResource(R.string.about_data_source),
                    value = stringResource(R.string.about_data_source_value),
                    colors = colors
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.cardBorder)
                )
                AboutRow(
                    label = stringResource(R.string.about_life_expectancy),
                    value = "$UZ_LIFE_EXPECTANCY ${stringResource(R.string.year)}",
                    colors = colors
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
private fun SettingsSectionLabel(label: String, colors: AppColors) {
    Text(
        text = label,
        fontSize = 10.sp,
        color = colors.textDim,
        letterSpacing = 2.sp,
        fontFamily = FontFamily.Monospace
    )
}

@Composable
private fun AboutRow(label: String, value: String, colors: AppColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
        Text(
            value,
            fontSize = 12.sp,
            color = colors.textPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ANIMATED PROGRESS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun animatedProgressFloat(target: Float): Float {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(target) {
        animated.animateTo(target, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }
    return animated.value
}

// ─────────────────────────────────────────────────────────────────────────────
// PULSING DOT
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PulsingDot(colors: AppColors) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(colors.colorLife.copy(alpha = alpha))
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// LIVE CHIP
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LiveChip(colors: AppColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(99.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        PulsingDot(colors)
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.live),
            fontSize = 10.sp,
            color = colors.textMuted,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TIME CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TimeCard(
    title: String,
    label: String,
    progress: Double,
    totalSeconds: Long,
    accentColor: Color,
    colors: AppColors
) {
    val animProg = animatedProgressFloat(progress.toFloat())
    val elapsed = (progress * totalSeconds).toLong()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        title,
                        fontSize = 10.sp,
                        color = colors.textDim,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${String.format(Locale.US, "%,d", elapsed)}s",
                        fontSize = 11.sp,
                        color = colors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "/ ${String.format(Locale.US, "%,d", totalSeconds)}s",
                        fontSize = 10.sp,
                        color = colors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(colors.progress)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animProg)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(99.dp))
                        .background(accentColor)
                )
            }

            Spacer(Modifier.height(10.dp))

            val pct = progress * 100
            val intPart = pct.toLong().toString()
            val decPart = String.format(Locale.US, "%.6f", pct - pct.toLong()).substring(1)

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.textPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        ) { append(intPart) }
                        withStyle(
                            SpanStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        ) { append("$decPart%") }
                    }
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        stringResource(R.string.live),
                        fontSize = 10.sp,
                        color = accentColor,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DOT MODE ENUM
// ─────────────────────────────────────────────────────────────────────────────
private enum class DotMode { YEAR, MONTH, WEEK }

// ─────────────────────────────────────────────────────────────────────────────
// LIFE DOTS
// ─────────────────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun LifeDots(birthDate: LocalDate, ageYears: Double, colors: AppColors) {
    val totalYears = UZ_LIFE_EXPECTANCY.toInt()
    val filledYears = ageYears.toInt()
    val partialFraction = ageYears - filledYears

    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var dotMode by remember { mutableStateOf(DotMode.YEAR) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DotMode.entries.forEach { mode ->
            val active = dotMode == mode
            val label = when (mode) {
                DotMode.YEAR -> stringResource(R.string.year)
                DotMode.MONTH -> stringResource(R.string.month)
                DotMode.WEEK -> stringResource(R.string.week)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) colors.colorLife.copy(0.15f) else colors.progress)
                    .border(
                        1.dp,
                        if (active) colors.colorLife.copy(0.4f) else colors.cardBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { dotMode = mode; selectedYear = null }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 10.sp,
                    color = if (active) colors.colorLife else colors.textMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }

    Spacer(Modifier.height(14.dp))

    when (dotMode) {
        DotMode.YEAR -> {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 15
            ) {
                repeat(totalYears) { i ->
                    val isPast = i < filledYears
                    val isCurrent = i == filledYears
                    val isSelected = selectedYear == i
                    val dotColor = when {
                        isSelected -> Color.Gray
                        isPast -> colors.colorLife.copy(alpha = 0.85f)
                        isCurrent -> colors.colorLife
                        else -> colors.progress
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(if (isSelected) 13.dp else 10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .then(
                                if (isCurrent || isPast)
                                    Modifier.clickable {
                                        selectedYear = if (isSelected) null else i
                                    }
                                else Modifier
                            )
                    )
                }
            }

            selectedYear?.let { idx ->
                val displayYear = birthDate.year + idx
                val isPast = idx < filledYears
                val isCurrent = idx == filledYears

                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.colorLife.copy(0.06f))
                        .border(1.dp, colors.colorLife.copy(0.2f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.year_age, displayYear, idx + 1),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.colorLife,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isCurrent -> Color(0xFF92400E).copy(0.3f); isPast -> colors.colorLife.copy(
                                            0.12f
                                        ); else -> Color.Transparent
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = when {
                                        isCurrent -> stringResource(R.string.now); isPast -> stringResource(
                                            R.string.passed
                                        ); else -> ""
                                    },
                                    fontSize = 9.sp,
                                    color = if (isCurrent) Color(0xFFFBBF24) else colors.colorLife,
                                    fontFamily = FontFamily.Monospace, letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        val monthsLived: Int = when {
                            isPast -> 12
                            isCurrent -> {
                                val bd = birthDate.withYear(displayYear)
                                val today = LocalDate.now()
                                if (today.year == displayYear) today.monthValue - bd.monthValue + 1 else 12
                            }

                            else -> 0
                        }.coerceIn(0, 12)

                        Text(
                            stringResource(R.string.month),
                            fontSize = 9.sp,
                            color = colors.textDim,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            val monthNames =
                                listOf("Y", "F", "M", "A", "M", "I", "I", "A", "S", "O", "N", "D")
                            repeat(12) { m ->
                                val mFilled = m < monthsLived
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (mFilled) colors.colorMonth.copy(0.7f) else colors.progress)
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        monthNames[m],
                                        fontSize = 7.sp,
                                        color = if (mFilled) colors.textMuted else colors.textDim,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        val weeksLived: Int = when {
                            isPast -> 52
                            isCurrent -> {
                                val yearStart = LocalDate.of(
                                    displayYear,
                                    birthDate.monthValue,
                                    birthDate.dayOfMonth
                                )
                                    .coerceAtLeast(LocalDate.of(displayYear, 1, 1))
                                ChronoUnit.WEEKS.between(yearStart, LocalDate.now()).toInt()
                                    .coerceIn(0, 52)
                            }

                            else -> 0
                        }

                        Text(
                            stringResource(R.string.weeks_word),
                            fontSize = 9.sp,
                            color = colors.textDim,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            maxItemsInEachRow = 13
                        ) {
                            repeat(52) { w ->
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (w < weeksLived) colors.colorWeek.copy(0.7f) else colors.progress)
                                )
                            }
                        }
                    }
                }
            }
        }

        DotMode.MONTH -> {
            val totalMonths = (UZ_LIFE_EXPECTANCY * 12).toInt()
            val filledMonths = (ageYears * 12).toInt()
            Text(
                stringResource(R.string.each_square_one_month, filledMonths, totalMonths),
                fontSize = 9.sp,
                color = colors.textDim,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(3.dp),
                maxItemsInEachRow = 30
            ) {
                repeat(totalMonths) { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 1.dp)
                            .size(7.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(if (i < filledMonths) colors.colorMonth.copy(0.75f) else colors.progress)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.months_left, totalMonths - filledMonths),
                fontSize = 9.sp,
                color = colors.textMuted,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        DotMode.WEEK -> {
            val totalWeeks = (UZ_LIFE_EXPECTANCY * 52.18).toInt()
            val filledWeeks = (ageYears * 52.18).toInt()
            Text(
                stringResource(R.string.each_dot_one_week, filledWeeks, totalWeeks),
                fontSize = 9.sp,
                color = colors.textDim,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                maxItemsInEachRow = 52
            ) {
                repeat(totalWeeks) { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 1.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (i < filledWeeks) colors.colorWeek.copy(0.7f) else colors.progress)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.weeks_left, totalWeeks - filledWeeks),
                fontSize = 9.sp,
                color = colors.textMuted,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LIFE SECTION
// ─────────────────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun LifeSection(birthDate: LocalDate, onReset: () -> Unit, colors: AppColors) {
    var now by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000); now = LocalDate.now()
        }
    }

    val lp = lifeProgress(birthDate)
    val animLp = animatedProgressFloat(lp.toFloat())
    val (years, months, days) = ageComponents(birthDate)
    val ageYears = ChronoUnit.DAYS.between(birthDate, now) / 365.25
    val remaining = UZ_LIFE_EXPECTANCY - ageYears
    val remYears = remaining.toInt()
    val remWeeks = (remaining * 52.18).toInt()
    val remDays = (remaining * 365.25).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            stringResource(R.string.life_analysis_uzbekistan),
            fontSize = 10.sp,
            color = colors.textMuted,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                years to stringResource(R.string.year),
                months to stringResource(R.string.month),
                days to stringResource(R.string.day)
            ).forEach { (v, l) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.progress)
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        v.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        l,
                        fontSize = 9.sp,
                        color = colors.textMuted,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.age_years_old, ageYears),
                fontSize = 11.sp,
                color = colors.textMuted,
                fontFamily = FontFamily.Monospace
            )
            Text(
                stringResource(R.string.average_life_expectancy, UZ_LIFE_EXPECTANCY),
                fontSize = 11.sp,
                color = colors.textMuted,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(colors.progress)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animLp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF34D399),
                                Color(0xFF10B981),
                                Color(0xFF059669)
                            )
                        )
                    )
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.colorLife,
                        fontFamily = FontFamily.Monospace
                    )
                ) { append(String.format(Locale.US, "%.4f", lp * 100)) }
                withStyle(
                    SpanStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.colorLife.copy(0.7f),
                        fontFamily = FontFamily.Monospace
                    )
                ) { append("%") }
            },
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.life_is_gone),
            fontSize = 12.sp,
            color = colors.textMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        LifeDots(birthDate = birthDate, ageYears = ageYears, colors = colors)

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.colorLife.copy(0.06f))
                .border(1.dp, colors.colorLife.copy(0.15f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    remYears to stringResource(R.string.year),
                    remWeeks to stringResource(R.string.week),
                    remDays to stringResource(R.string.day)
                ).forEach { (v, l) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            String.format(Locale.US, "%,d", v),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.colorLife,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            stringResource(R.string.remaining_years_remaning, l),
                            fontSize = 8.sp,
                            color = colors.textMuted,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.bgDark)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            colors.colorLife.copy(0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(14.dp)
        ) {
            Text(
                stringResource(R.string.quote),
                fontSize = 12.sp,
                color = colors.textMuted,
                fontStyle = FontStyle.Italic,
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textMuted),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Text(
                stringResource(R.string.change_date),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BIRTH DATE INPUT
// ─────────────────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun BirthDateInput(onSubmit: (LocalDate) -> Unit, colors: AppColors) {
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val dayFocus = remember { FocusRequester() }
    val monthFocus = remember { FocusRequester() }
    val yearFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        focusedBorderColor = colors.colorLife.copy(0.5f),
        unfocusedBorderColor = colors.cardBorder,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        cursorColor = colors.colorLife,
        focusedLabelColor = colors.textMuted,
        unfocusedLabelColor = colors.textMuted,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            stringResource(R.string.life_analysis),
            fontSize = 10.sp,
            color = colors.textMuted,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.enter_your_birth_date),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = day, onValueChange = {
                    if (it.length <= 2 && it.all(Char::isDigit)) {
                        day = it; if (it.length == 2) monthFocus.requestFocus()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(dayFocus),
                label = {
                    Text(
                        stringResource(R.string.day),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                placeholder = {
                    Text(
                        "01",
                        fontSize = 18.sp,
                        color = colors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, shape = RoundedCornerShape(12.dp), colors = fieldColors,
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    color = colors.textPrimary
                )
            )
            Text("/", fontSize = 22.sp, color = colors.textDim)
            OutlinedTextField(
                value = month, onValueChange = {
                    if (it.length <= 2 && it.all(Char::isDigit)) {
                        month = it; if (it.length == 2) yearFocus.requestFocus()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(monthFocus),
                label = {
                    Text(
                        stringResource(R.string.month),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                placeholder = {
                    Text(
                        "09",
                        fontSize = 18.sp,
                        color = colors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, shape = RoundedCornerShape(12.dp), colors = fieldColors,
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    color = colors.textPrimary
                )
            )
            Text("/", fontSize = 22.sp, color = colors.textDim)
            OutlinedTextField(
                value = year, onValueChange = {
                    if (it.length <= 4 && it.all(Char::isDigit)) {
                        year = it; if (it.length == 4) keyboard?.hide()
                    }
                },
                modifier = Modifier
                    .weight(2f)
                    .focusRequester(yearFocus),
                label = {
                    Text(
                        stringResource(R.string.year),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                placeholder = {
                    Text(
                        "1995",
                        fontSize = 18.sp,
                        color = colors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, shape = RoundedCornerShape(12.dp), colors = fieldColors,
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Start,
                    color = colors.textPrimary
                )
            )
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                error,
                fontSize = 11.sp,
                color = Color(0xFFF87171),
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(16.dp))

        val invalidDateText = stringResource(R.string.please_enter_valid_date)
        val futureDateText = stringResource(R.string.date_cannot_be_in_the_future)
        val wrongDateText = stringResource(R.string.invalid_date)

        Button(
            onClick = {
                val d = day.toIntOrNull() ?: 0
                val m = month.toIntOrNull() ?: 0
                val y = year.toIntOrNull() ?: 0
                when {
                    d !in 1..31 || m !in 1..12 || y !in 1900..LocalDate.now().year -> error =
                        invalidDateText

                    else -> runCatching {
                        val date = LocalDate.of(y, m, d)
                        if (date.isAfter(LocalDate.now())) error = futureDateText
                        else {
                            error = ""; onSubmit(date)
                        }
                    }.onFailure { error = wrongDateText }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.colorLife,
                contentColor = Color.Black
            )
        ) {
            Text(
                stringResource(R.string.show_my_life),
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                ),
                letterSpacing = 1.sp
            )
        }
    }
}