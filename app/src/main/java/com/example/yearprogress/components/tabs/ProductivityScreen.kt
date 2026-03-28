package com.example.yearprogress.components.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yearprogress.R
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.utils.GoalCountdown
import com.example.yearprogress.utils.HabitTracker
import com.example.yearprogress.utils.PreferenceManager
import com.example.yearprogress.utils.TimePeriod
import com.example.yearprogress.utils.formatRemainingTime
import com.example.yearprogress.utils.monthlyMinutes
import com.example.yearprogress.utils.progress
import com.example.yearprogress.utils.remainingDays
import com.example.yearprogress.utils.weeklyMinutes
import com.example.yearprogress.utils.yearlyMinutes
import android.content.Context
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

internal enum class HabitUnit { MINUTES, HOURS }

@Composable
fun ProductivityScreen() {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context.applicationContext) }

    var goals by remember { mutableStateOf(preferenceManager.getGoals()) }
    var habits by remember { mutableStateOf(preferenceManager.getHabits()) }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf(stringResource(R.string.goal_deadlines), stringResource(R.string.habit_tracker))

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ProgressColors.bgCard)
                .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(20.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) ProgressColors.colorMonth.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) ProgressColors.colorMonth else ProgressColors.textMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            pageSpacing = 16.dp
        ) { page ->
            Column(modifier = Modifier.fillMaxSize()) {
                when (page) {
                    0 -> {
                        GoalCountdownSection(
                            goals = goals,
                            onAddGoal = { goal ->
                                goals = (goals + goal).sortedBy { it.targetDate }
                                preferenceManager.saveGoals(goals)
                            },
                            onRemoveGoal = { id ->
                                goals = goals.filterNot { it.id == id }
                                preferenceManager.saveGoals(goals)
                            }
                        )
                    }
                    1 -> {
                        HabitTrackerSection(
                            habits = habits,
                            onAddHabit = { habit ->
                                habits = habits + habit
                                preferenceManager.saveHabits(habits)
                            },
                            onRemoveHabit = { id ->
                                habits = habits.filterNot { it.id == id }
                                preferenceManager.saveHabits(habits)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun GoalCountdownSection(
    goals: List<GoalCountdown>,
    onAddGoal: (GoalCountdown) -> Unit,
    onRemoveGoal: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val goalNameRequired = stringResource(R.string.goal_name_required)
    val goalDateRequired = stringResource(R.string.goal_date_required)
    val goalDateInvalid = stringResource(R.string.goal_date_invalid)
    val now = rememberDateTimeTicker(1000L)
    val tomorrow = remember { LocalDate.now().plusDays(1) }

    Column {
        goals.forEach { goal ->
            key(goal.id) {
                GoalCard(goal = goal, now = now, onRemove = { onRemoveGoal(goal.id) })
                Spacer(Modifier.height(10.dp))
            }
        }

        DashboardSection(
            title = stringResource(R.string.goal_deadlines),
            subtitle = stringResource(R.string.goal_deadlines_desc)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.goal_name)) },
                singleLine = true,
                colors = dashboardFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallNumberField(
                    day,
                    { if (it.length <= 2 && it.all(Char::isDigit)) day = it },
                    stringResource(R.string.day),
                    Modifier.weight(1f)
                )
                SmallNumberField(
                    month,
                    { if (it.length <= 2 && it.all(Char::isDigit)) month = it },
                    stringResource(R.string.month),
                    Modifier.weight(1f)
                )
                SmallNumberField(
                    year,
                    { if (it.length <= 4 && it.all(Char::isDigit)) year = it },
                    stringResource(R.string.year),
                    Modifier.weight(1.4f)
                )
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    it,
                    color = Color(0xFFF87171),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    val d = day.toIntOrNull()
                    val m = month.toIntOrNull()
                    val y = year.toIntOrNull()
                    when {
                        name.isBlank() -> error = goalNameRequired
                        d == null || m == null || y == null -> error = goalDateRequired
                        else -> runCatching {
                            val target = LocalDate.of(y, m, d)
                            require(target >= tomorrow)
                            onAddGoal(
                                GoalCountdown(
                                    name = name.trim(),
                                    createdDate = LocalDate.now(),
                                    targetDate = target
                                )
                            )
                            name = ""
                            day = ""
                            month = ""
                            year = ""
                            error = null
                        }.onFailure {
                            error = goalDateInvalid
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProgressColors.colorYear,
                    contentColor = Color.Black
                )
            ) {
                Text(stringResource(R.string.add_goal))
            }
        }
    }
}

@Composable
internal fun GoalCard(goal: GoalCountdown, now: LocalDateTime, onRemove: () -> Unit) {
    val context = LocalContext.current
    val progress = remember(goal, now) { goal.progress(now) }
    val endOfTargetDate =
        remember(goal.targetDate) { goal.targetDate.plusDays(1).atStartOfDay().minusSeconds(1) }
    val remainingSecondsToGoal = ChronoUnit.SECONDS.between(now, endOfTargetDate).coerceAtLeast(0)
    val totalSeconds = remember(goal.createdDate, endOfTargetDate) {
        ChronoUnit.SECONDS.between(goal.createdDate.atStartOfDay(), endOfTargetDate)
            .coerceAtLeast(1)
    }
    val elapsedSeconds = (totalSeconds - remainingSecondsToGoal).coerceAtLeast(0)

    val dynamicRemainingText = remember(now, goal.targetDate) {
        formatDynamicRemainingTime(now, goal.targetDate, context)
    }

    TimeCardFrame(
        title = stringResource(R.string.goal_deadlines),
        label = goal.name,
        progress = progress,
        totalSeconds = totalSeconds,
        elapsedSeconds = elapsedSeconds,
        secondaryLine = "${goal.targetDate} • $dynamicRemainingText",
        accentColor = ProgressColors.colorYear,
        footer = {
            Text(
                text = "${stringResource(R.string.remaining)}: ${
                    formatRemainingTime(
                        remainingSecondsToGoal,
                        TimePeriod.DAY
                    )
                }",
                fontSize = 10.sp,
                color = ProgressColors.textDim,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.goal_progress_summary, (progress * 100).toInt()),
                color = ProgressColors.textMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onRemove) {
                Text(stringResource(R.string.remove))
            }
        }
    )
}

@Composable
internal fun HabitTrackerSection(
    habits: List<HabitTracker>,
    onAddHabit: (HabitTracker) -> Unit,
    onRemoveHabit: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(HabitUnit.MINUTES) }
    var error by remember { mutableStateOf<String?>(null) }
    val habitNameRequired = stringResource(R.string.habit_name_required)
    val habitMinutesRequired = stringResource(R.string.habit_minutes_required)

    DashboardSection(
        title = stringResource(R.string.habit_tracker),
        subtitle = stringResource(R.string.habit_tracker_desc)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.habit_name)) },
                singleLine = true,
                colors = dashboardFieldColors()
            )
            OutlinedTextField(
                value = quantity,
                onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) quantity = it },
                modifier = Modifier.weight(0.7f),
                label = { Text(stringResource(R.string.habit_amount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = dashboardFieldColors()
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HabitUnit.entries.forEach { option ->
                val selected = option == unit
                OutlinedButton(
                    onClick = { unit = option },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (selected) ProgressColors.colorLife else ProgressColors.textMuted
                    )
                ) {
                    Text(
                        when (option) {
                            HabitUnit.MINUTES -> stringResource(R.string.unit_minutes)
                            HabitUnit.HOURS -> stringResource(R.string.unit_hours)
                        }
                    )
                }
            }
        }
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Color(0xFFF87171), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                val amount = quantity.toIntOrNull()
                val minutesPerDay = when (unit) {
                    HabitUnit.MINUTES -> amount
                    HabitUnit.HOURS -> amount?.times(60)
                }
                when {
                    name.isBlank() -> error = habitNameRequired
                    minutesPerDay == null || minutesPerDay <= 0 -> error = habitMinutesRequired
                    else -> {
                        onAddHabit(HabitTracker(name = name.trim(), minutesPerDay = minutesPerDay))
                        name = ""
                        quantity = ""
                        unit = HabitUnit.MINUTES
                        error = null
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = ProgressColors.colorLife,
                contentColor = Color.Black
            )
        ) {
            Text(stringResource(R.string.add_habit))
        }

        if (habits.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            habits.forEach { habit ->
                key(habit.id) {
                    HabitCard(habit = habit, onRemove = { onRemoveHabit(habit.id) })
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
internal fun HabitCard(habit: HabitTracker, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ProgressColors.bgCard)
            .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(20.dp))
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
                        stringResource(R.string.habit_tracker),
                        fontSize = 10.sp,
                        color = ProgressColors.textDim,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        habit.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProgressColors.textPrimary
                    )
                    Text(
                        formatHabitDailyAmount(
                            habit.minutesPerDay,
                            stringResource(R.string.per_day)
                        ),
                        fontSize = 10.sp,
                        color = ProgressColors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                }
                OutlinedButton(onClick = onRemove) {
                    Text(stringResource(R.string.remove))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HabitMetric(
                    formatMinutesOrHours(habit.weeklyMinutes()),
                    stringResource(R.string.week)
                )
                HabitMetric(
                    formatMinutesOrHours(habit.monthlyMinutes()),
                    stringResource(R.string.month)
                )
                HabitMetric("${habit.yearlyMinutes() / 60}h", stringResource(R.string.year))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.habit_projection_summary, habit.yearlyMinutes() / 60),
                color = ProgressColors.textMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
internal fun HabitMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = ProgressColors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            label,
            color = ProgressColors.textDim,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
internal fun DashboardSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ProgressColors.bgCard)
            .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Text(
                title,
                fontSize = 12.sp,
                color = ProgressColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                fontSize = 10.sp,
                color = ProgressColors.textDim,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
internal fun SmallNumberField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = dashboardFieldColors()
    )
}

@Composable
internal fun dashboardFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = ProgressColors.textPrimary,
    unfocusedTextColor = ProgressColors.textPrimary,
    focusedBorderColor = ProgressColors.cardBorder,
    unfocusedBorderColor = ProgressColors.cardBorder,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    cursorColor = ProgressColors.textPrimary,
)

internal fun formatHabitDailyAmount(minutesPerDay: Int, perDayLabel: String): String {
    return if (minutesPerDay >= 60 && minutesPerDay % 60 == 0) {
        "${minutesPerDay / 60}h / $perDayLabel"
    } else {
        "${minutesPerDay}m / $perDayLabel"
    }
}

internal fun formatMinutesOrHours(totalMinutes: Int): String {
    return if (totalMinutes >= 60 && totalMinutes % 60 == 0) {
        "${totalMinutes / 60}h"
    } else {
        "${totalMinutes}m"
    }
}

internal fun formatDynamicRemainingTime(now: LocalDateTime, targetDate: LocalDate, context: Context): String {
    val end = targetDate.plusDays(1).atStartOfDay().minusSeconds(1)
    if (now.isAfter(end) || now.isEqual(end)) return context.getString(R.string.goal_completed)

    val remainingMonths = ChronoUnit.MONTHS.between(now, end)
    val remainingDays = ChronoUnit.DAYS.between(now, end)

    return when {
        remainingMonths >= 1 -> {
            val then = now.plusMonths(remainingMonths)
            val extraDays = ChronoUnit.DAYS.between(then, end)
            context.getString(R.string.time_left_months_days, remainingMonths, extraDays)
        }
        remainingDays >= 1 -> {
            context.getString(R.string.time_left_days, remainingDays)
        }
        else -> {
            val remainingHours = ChronoUnit.HOURS.between(now, end)
            val then = now.plusHours(remainingHours)
            val remainingMinutes = ChronoUnit.MINUTES.between(then, end)
            context.getString(R.string.time_left_hours_mins, remainingHours, remainingMinutes)
        }
    }
}
