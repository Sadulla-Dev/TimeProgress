package com.example.yearprogress.components.tabs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.drawToBitmap
import com.example.yearprogress.R
import com.example.yearprogress.ui.theme.AppColors
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.ui.theme.YearProgressTheme
import com.example.yearprogress.utils.GoalCountdown
import com.example.yearprogress.utils.HabitTracker
import com.example.yearprogress.utils.PreferenceManager
import com.example.yearprogress.utils.TimePeriod
import com.example.yearprogress.utils.dayProgress
import com.example.yearprogress.utils.formatRemainingTime
import com.example.yearprogress.utils.getDaySuffix
import com.example.yearprogress.utils.monthProgress
import com.example.yearprogress.utils.monthlyMinutes
import com.example.yearprogress.utils.progress
import com.example.yearprogress.utils.remainingDays
import com.example.yearprogress.utils.remainingSeconds
import com.example.yearprogress.utils.shareBitmap
import com.example.yearprogress.utils.shareText
import com.example.yearprogress.utils.weekBounds
import com.example.yearprogress.utils.weekProgress
import com.example.yearprogress.utils.weeklyMinutes
import com.example.yearprogress.utils.yearProgress
import com.example.yearprogress.utils.yearlyMinutes
import kotlinx.coroutines.delay
import java.lang.String.format
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle as DateTextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimeScreen() {
    val colors = ProgressColors
    val context = LocalContext.current
    val view = LocalView.current
    val locale = context.resources.configuration.locales.get(0) ?: Locale.getDefault()
    val preferenceManager = remember { PreferenceManager(context.applicationContext) }
    val weekStartDay = preferenceManager.getWeekStartDay()

    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var goals by remember { mutableStateOf(preferenceManager.getGoals()) }
    var habits by remember { mutableStateOf(preferenceManager.getHabits()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = LocalDateTime.now()
        }
    }

    val monthShort = now.month.getDisplayName(DateTextStyle.SHORT_STANDALONE, locale).uppercase(locale)
    val dayShort = now.dayOfWeek.getDisplayName(DateTextStyle.SHORT_STANDALONE, locale).uppercase(locale)

    val yearStart = LocalDateTime.of(now.year, 1, 1, 0, 0)
    val yearEnd = LocalDateTime.of(now.year, 12, 31, 23, 59, 59)
    val monthStart = LocalDateTime.of(now.year, now.monthValue, 1, 0, 0)
    val monthEnd = monthStart.plusMonths(1).minusSeconds(1)
    val dayStart = now.toLocalDate().atStartOfDay()
    val dayEnd = dayStart.plusDays(1).minusSeconds(1)
    val (weekStart, weekEnd) = weekBounds(now, weekStartDay)

    val cards = listOf(
        CardInfo(TimePeriod.YEAR, stringResource(R.string.year), now.year.toString(), yearProgress(now), ChronoUnit.SECONDS.between(yearStart, yearEnd), ChronoUnit.SECONDS.between(now, yearEnd).coerceAtLeast(0), colors.colorYear),
        CardInfo(TimePeriod.MONTH, stringResource(R.string.month), monthShort, monthProgress(now), ChronoUnit.SECONDS.between(monthStart, monthEnd), ChronoUnit.SECONDS.between(now, monthEnd).coerceAtLeast(0), colors.colorMonth),
        CardInfo(TimePeriod.WEEK, stringResource(R.string.week), dayShort, weekProgress(now, weekStartDay), ChronoUnit.SECONDS.between(weekStart, weekEnd), ChronoUnit.SECONDS.between(now, weekEnd).coerceAtLeast(0), colors.colorWeek),
        CardInfo(TimePeriod.DAY, stringResource(R.string.day), "${now.dayOfMonth}${getDaySuffix(now.dayOfMonth)}", dayProgress(now), ChronoUnit.SECONDS.between(dayStart, dayEnd), ChronoUnit.SECONDS.between(now, dayEnd).coerceAtLeast(0), colors.colorDay),
    )

    val shareTextBody = buildString {
        append(context.getString(R.string.share_progress_title))
        append("\n\n")
        cards.forEach { card ->
            append("${card.title}: ${format(Locale.US, "%.3f", card.progress * 100)}%")
            append(" • ${context.getString(R.string.remaining)}: ${formatRemainingTime(card.remainingSeconds, card.period)}\n")
        }
        if (goals.isNotEmpty()) {
            append("\n${context.getString(R.string.goal_deadlines)}\n")
            goals.take(3).forEach { goal ->
                append("${goal.name}: ${goal.remainingDays()} ${context.getString(R.string.days_left_short)}\n")
            }
        }
    }

    Column {
        cards.forEach { card ->
            TimeCard(
                title = card.title,
                label = card.label,
                progress = card.progress,
                totalSeconds = card.totalSeconds,
                remainingSeconds = card.remainingSeconds,
                period = card.period,
                accentColor = card.accentColor,
                colors = colors
            )
            Spacer(Modifier.height(10.dp))
        }

        GoalCountdownSection(
            goals = goals,
            now = now,
            onAddGoal = { goal ->
                goals = (goals + goal).sortedBy { it.targetDate }
                preferenceManager.saveGoals(goals)
            },
            onRemoveGoal = { id ->
                goals = goals.filterNot { it.id == id }
                preferenceManager.saveGoals(goals)
            }
        )

        Spacer(Modifier.height(10.dp))

        HabitTrackerSection(
            habits = habits,
            now = now,
            onAddHabit = { habit ->
                habits = habits + habit
                preferenceManager.saveHabits(habits)
            },
            onRemoveHabit = { id ->
                habits = habits.filterNot { it.id == id }
                preferenceManager.saveHabits(habits)
            }
        )

        Spacer(Modifier.height(10.dp))

        SharePanel(
            onShareText = { shareText(context, shareTextBody, context.getString(R.string.share_text)) },
            onShareImage = { shareBitmap(context, view.rootView.drawToBitmap(), context.getString(R.string.share_image)) }
        )
    }
}

private data class CardInfo(
    val period: TimePeriod,
    val title: String,
    val label: String,
    val progress: Double,
    val totalSeconds: Long,
    val remainingSeconds: Long,
    val accentColor: Color,
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun GoalCountdownSection(
    goals: List<GoalCountdown>,
    now: LocalDateTime,
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
    val tomorrow = LocalDate.now().plusDays(1)

    Column {
        goals.forEach { goal ->
            GoalCard(goal = goal, now = now, onRemove = { onRemoveGoal(goal.id) })
            Spacer(Modifier.height(10.dp))
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
                SmallNumberField(day, { if (it.length <= 2 && it.all(Char::isDigit)) day = it }, stringResource(R.string.day), Modifier.weight(1f))
                SmallNumberField(month, { if (it.length <= 2 && it.all(Char::isDigit)) month = it }, stringResource(R.string.month), Modifier.weight(1f))
                SmallNumberField(year, { if (it.length <= 4 && it.all(Char::isDigit)) year = it }, stringResource(R.string.year), Modifier.weight(1.4f))
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color(0xFFF87171), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
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
                            onAddGoal(GoalCountdown(name = name.trim(), createdDate = LocalDate.now(), targetDate = target))
                            name = ""; day = ""; month = ""; year = ""; error = null
                        }.onFailure { error = goalDateInvalid }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ProgressColors.colorYear, contentColor = Color.Black)
            ) {
                Text(stringResource(R.string.add_goal))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun GoalCard(goal: GoalCountdown, now: LocalDateTime, onRemove: () -> Unit) {
    val progress = goal.progress()
    val endOfTargetDate = goal.targetDate.plusDays(1).atStartOfDay().minusSeconds(1)
    val remainingSecondsToGoal = ChronoUnit.SECONDS.between(now, endOfTargetDate).coerceAtLeast(0)
    val totalSeconds = ChronoUnit.SECONDS.between(goal.createdDate.atStartOfDay(), endOfTargetDate).coerceAtLeast(1)
    val elapsedSeconds = (totalSeconds - remainingSecondsToGoal).coerceAtLeast(0)

    TimeCardFrame(
        title = stringResource(R.string.goal_deadlines),
        label = goal.name,
        progress = progress,
        totalSeconds = totalSeconds,
        elapsedSeconds = elapsedSeconds,
        remainingText = "${goal.targetDate} • ${goal.remainingDays()} ${stringResource(R.string.days_left_short)}",
        accentColor = ProgressColors.colorYear,
        footer = {
            Text(
                text = "${stringResource(R.string.remaining)}: ${formatRemainingTime(remainingSecondsToGoal, TimePeriod.DAY)}",
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
private fun HabitTrackerSection(
    habits: List<HabitTracker>,
    now: LocalDateTime,
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
                val mins = when (unit) {
                    HabitUnit.MINUTES -> amount
                    HabitUnit.HOURS -> amount?.times(60)
                }
                when {
                    name.isBlank() -> error = habitNameRequired
                    mins == null || mins <= 0 -> error = habitMinutesRequired
                    else -> {
                        onAddHabit(HabitTracker(name = name.trim(), minutesPerDay = mins))
                        name = ""; quantity = ""; unit = HabitUnit.MINUTES; error = null
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ProgressColors.colorLife, contentColor = Color.Black)
        ) {
            Text(stringResource(R.string.add_habit))
        }

        if (habits.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            habits.forEach { habit ->
                HabitCard(habit = habit, onRemove = { onRemoveHabit(habit.id) })
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HabitCard(habit: HabitTracker, onRemove: () -> Unit) {
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
                    Text(habit.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ProgressColors.textPrimary)
                    Text(
                        formatHabitDailyAmount(habit.minutesPerDay, stringResource(R.string.per_day)),
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HabitMetric(formatMinutesOrHours(habit.weeklyMinutes()), stringResource(R.string.week))
                HabitMetric(formatMinutesOrHours(habit.monthlyMinutes()), stringResource(R.string.month))
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
private fun HabitMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = ProgressColors.textPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(label, color = ProgressColors.textDim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun DashboardSection(
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
            Text(title, fontSize = 12.sp, color = ProgressColors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 10.sp, color = ProgressColors.textDim, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SmallNumberField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
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
private fun dashboardFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = ProgressColors.textPrimary,
    unfocusedTextColor = ProgressColors.textPrimary,
    focusedBorderColor = ProgressColors.cardBorder,
    unfocusedBorderColor = ProgressColors.cardBorder,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    cursorColor = ProgressColors.textPrimary,
)

@Composable
private fun SharePanel(
    onShareText: () -> Unit,
    onShareImage: () -> Unit,
) {
    DashboardSection(
        title = stringResource(R.string.share_progress_title),
        subtitle = stringResource(R.string.share_progress_desc)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onShareImage,
                colors = ButtonDefaults.buttonColors(containerColor = ProgressColors.colorWeek, contentColor = Color.White)
            ) {
                Text(stringResource(R.string.share_image))
            }
            OutlinedButton(onClick = onShareText) {
                Text(stringResource(R.string.share_text))
            }
        }
    }
}

@Composable
private fun TimeCardFrame(
    title: String,
    label: String,
    progress: Double,
    totalSeconds: Long,
    elapsedSeconds: Long,
    remainingText: String,
    accentColor: Color,
    footer: @Composable ColumnScope.() -> Unit
) {
    val animProg = animatedProgressFloat(progress.toFloat())
    val pct = progress * 100
    val intPart = pct.toLong().toString()
    val decPart = format(Locale.US, "%.6f", pct - pct.toLong()).substring(1)

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
                    Text(title, fontSize = 10.sp, color = ProgressColors.textDim, letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(4.dp))
                    Text(label, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ProgressColors.textPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${format(Locale.US, "%,d", elapsedSeconds)}s", fontSize = 11.sp, color = ProgressColors.textDim, fontFamily = FontFamily.Monospace)
                    Text("/ ${format(Locale.US, "%,d", totalSeconds)}s", fontSize = 10.sp, color = ProgressColors.textDim, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(remainingText, color = ProgressColors.textDim, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(ProgressColors.progress)
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

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 30.sp, fontWeight = FontWeight.Black, color = ProgressColors.textPrimary, fontFamily = FontFamily.Monospace)) {
                            append(intPart)
                        }
                        withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, color = ProgressColors.textMuted, fontFamily = FontFamily.Monospace)) {
                            append("$decPart%")
                        }
                    }
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(stringResource(R.string.live), fontSize = 10.sp, color = accentColor, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                }
            }

            Spacer(Modifier.height(10.dp))
            footer()
        }
    }
}

@Composable
fun TimeCard(
    title: String,
    label: String,
    progress: Double,
    totalSeconds: Long,
    remainingSeconds: Long,
    period: TimePeriod,
    accentColor: Color,
    colors: AppColors
) {
    val elapsed = (totalSeconds - remainingSeconds).coerceAtLeast(0)
    val remainingLine = "${stringResource(R.string.remaining)}: ${formatRemainingTime(remainingSeconds, period)}"
    TimeCardFrame(
        title = title,
        label = label,
        progress = progress,
        totalSeconds = totalSeconds,
        elapsedSeconds = elapsed,
        remainingText = remainingLine,
        accentColor = accentColor,
        footer = {}
    )
}

@Composable
fun animatedProgressFloat(target: Float): Float {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(target) {
        animated.animateTo(target, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }
    return animated.value
}

private enum class HabitUnit { MINUTES, HOURS }

private fun formatHabitDailyAmount(minutesPerDay: Int, perDayLabel: String): String {
    return if (minutesPerDay % 60 == 0 && minutesPerDay >= 60) {
        "${minutesPerDay / 60}h / $perDayLabel"
    } else {
        "$minutesPerDay m / $perDayLabel"
    }
}

private fun formatMinutesOrHours(totalMinutes: Int): String {
    return if (totalMinutes >= 60 && totalMinutes % 60 == 0) {
        "${totalMinutes / 60}h"
    } else {
        "${totalMinutes}m"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
private fun TimeScreenPreview() = YearProgressTheme {
    TimeScreen()
}
