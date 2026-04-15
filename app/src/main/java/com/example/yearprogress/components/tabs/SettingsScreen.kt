package com.example.yearprogress.components.tabs

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.yearprogress.R
import com.example.yearprogress.components.LanguageDialog
import com.example.yearprogress.notifications.ReminderScheduler
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.ui.theme.ThemeMode
import com.example.yearprogress.ui.theme.YearProgressTheme
import com.example.yearprogress.utils.PreferenceManager
import com.example.yearprogress.utils.UZ_LIFE_EXPECTANCY
import com.example.yearprogress.utils.WeekStartDay
import com.example.yearprogress.utils.resolvePinnedGoal
import com.example.yearprogress.utils.safeClickable
import com.example.yearprogress.widget.WidgetRefreshManager
import java.util.Calendar

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
    var dailyReminderEnabled by remember { mutableStateOf(preferenceManager.isDailyReminderEnabled()) }
    var dailyReminderHour by remember { mutableStateOf(preferenceManager.getDailyReminderHour()) }
    var dailyReminderMinute by remember { mutableStateOf(preferenceManager.getDailyReminderMinute()) }
    var weeklyReminderEnabled by remember { mutableStateOf(preferenceManager.isWeeklyReminderEnabled()) }
    var weeklyReminderHour by remember { mutableStateOf(preferenceManager.getWeeklyReminderHour()) }
    var weeklyReminderMinute by remember { mutableStateOf(preferenceManager.getWeeklyReminderMinute()) }
    var goalReminderEnabled by remember { mutableStateOf(preferenceManager.isGoalReminderEnabled()) }
    var goalReminderHour by remember { mutableStateOf(preferenceManager.getGoalReminderHour()) }
    var goalReminderMinute by remember { mutableStateOf(preferenceManager.getGoalReminderMinute()) }
    val hasPinnedGoal = remember {
        resolvePinnedGoal(preferenceManager.getGoals(), preferenceManager.getPinnedGoalId()) != null
    }

    var pendingNotificationAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingNotificationAction?.invoke()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.notification_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
        pendingNotificationAction = null
    }

    fun ensureNotificationPermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            action()
        } else {
            pendingNotificationAction = action
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun rescheduleReminders() {
        ReminderScheduler.rescheduleAll(context.applicationContext)
    }

    fun pickTime(
        initialHour: Int,
        initialMinute: Int,
        onSelected: (Int, Int) -> Unit
    ) {
        TimePickerDialog(
            context,
            { _, hour, minute -> onSelected(hour, minute) },
            initialHour,
            initialMinute,
            DateFormat.is24HourFormat(context)
        ).show()
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        SettingsSectionLabel(label = stringResource(R.string.settings_appearance))
        Spacer(Modifier.height(10.dp))

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
                        color = if (isSelected) {
                            ProgressColors.colorWeek.copy(alpha = 0.4f)
                        } else {
                            ProgressColors.cardBorder
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                    .safeClickable { onChangeTheme(mode) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp, 38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(previewBg)
                        .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp, 22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(previewCard)
                    )
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

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) ProgressColors.colorWeek else Color.Transparent)
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
                                    WidgetRefreshManager.refreshWeekBasedWidgets(context)
                                    rescheduleReminders()
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

        SettingsSectionLabel(label = stringResource(R.string.settings_reminders))
        Spacer(Modifier.height(10.dp))

        ReminderSettingCard(
            title = stringResource(R.string.reminder_day_title),
            subtitle = stringResource(R.string.reminder_day_desc),
            enabled = dailyReminderEnabled,
            timeLabel = formatReminderTime(context, dailyReminderHour, dailyReminderMinute),
            accentColor = ProgressColors.colorDay,
            onToggle = { enabled ->
                if (enabled) {
                    ensureNotificationPermission {
                        dailyReminderEnabled = true
                        preferenceManager.setDailyReminderEnabled(true)
                        rescheduleReminders()
                    }
                } else {
                    dailyReminderEnabled = false
                    preferenceManager.setDailyReminderEnabled(false)
                    rescheduleReminders()
                }
            },
            onEditTime = {
                pickTime(dailyReminderHour, dailyReminderMinute) { hour, minute ->
                    dailyReminderHour = hour
                    dailyReminderMinute = minute
                    preferenceManager.setDailyReminderTime(hour, minute)
                    rescheduleReminders()
                }
            }
        )

        Spacer(Modifier.height(10.dp))

        ReminderSettingCard(
            title = stringResource(R.string.reminder_week_title),
            subtitle = stringResource(R.string.reminder_week_desc),
            enabled = weeklyReminderEnabled,
            timeLabel = formatReminderTime(context, weeklyReminderHour, weeklyReminderMinute),
            accentColor = ProgressColors.colorWeek,
            onToggle = { enabled ->
                if (enabled) {
                    ensureNotificationPermission {
                        weeklyReminderEnabled = true
                        preferenceManager.setWeeklyReminderEnabled(true)
                        rescheduleReminders()
                    }
                } else {
                    weeklyReminderEnabled = false
                    preferenceManager.setWeeklyReminderEnabled(false)
                    rescheduleReminders()
                }
            },
            onEditTime = {
                pickTime(weeklyReminderHour, weeklyReminderMinute) { hour, minute ->
                    weeklyReminderHour = hour
                    weeklyReminderMinute = minute
                    preferenceManager.setWeeklyReminderTime(hour, minute)
                    rescheduleReminders()
                }
            }
        )

        Spacer(Modifier.height(10.dp))

        ReminderSettingCard(
            title = stringResource(R.string.reminder_goal_title),
            subtitle = if (hasPinnedGoal) {
                stringResource(R.string.reminder_goal_desc)
            } else {
                stringResource(R.string.reminder_goal_requires_goal)
            },
            enabled = goalReminderEnabled && hasPinnedGoal,
            timeLabel = formatReminderTime(context, goalReminderHour, goalReminderMinute),
            accentColor = ProgressColors.colorYear,
            controlsEnabled = hasPinnedGoal,
            onToggle = { enabled ->
                if (!hasPinnedGoal) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.reminder_goal_requires_goal),
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (enabled) {
                    ensureNotificationPermission {
                        goalReminderEnabled = true
                        preferenceManager.setGoalReminderEnabled(true)
                        rescheduleReminders()
                    }
                } else {
                    goalReminderEnabled = false
                    preferenceManager.setGoalReminderEnabled(false)
                    rescheduleReminders()
                }
            },
            onEditTime = {
                if (hasPinnedGoal) {
                    pickTime(goalReminderHour, goalReminderMinute) { hour, minute ->
                        goalReminderHour = hour
                        goalReminderMinute = minute
                        preferenceManager.setGoalReminderTime(hour, minute)
                        rescheduleReminders()
                    }
                }
            }
        )

        Spacer(Modifier.height(20.dp))

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

    if (showLanguageDialog) {
        LanguageDialog(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = onChangeLanguage
        )
    }
}

@Composable
private fun ReminderSettingCard(
    title: String,
    subtitle: String,
    enabled: Boolean,
    timeLabel: String,
    accentColor: Color,
    controlsEnabled: Boolean = true,
    onToggle: (Boolean) -> Unit,
    onEditTime: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ProgressColors.bgCard)
            .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (controlsEnabled) ProgressColors.textPrimary else ProgressColors.textMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = ProgressColors.textDim,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    enabled = controlsEnabled
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (enabled) accentColor.copy(0.12f) else ProgressColors.progress)
                    .border(
                        1.dp,
                        if (enabled) accentColor.copy(0.3f) else ProgressColors.cardBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .safeClickable(enabled = controlsEnabled) { onEditTime() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = timeLabel,
                    fontSize = 11.sp,
                    color = if (enabled) accentColor else ProgressColors.textMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
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
            text = label,
            fontSize = 12.sp,
            color = ProgressColors.textMuted,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = ProgressColors.textPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatReminderTime(context: android.content.Context, hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return DateFormat.getTimeFormat(context).format(calendar.time)
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
