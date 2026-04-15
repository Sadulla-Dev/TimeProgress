package com.example.yearprogress.components.tabs

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yearprogress.notifications.ReminderScheduler
import com.example.yearprogress.R
import com.example.yearprogress.ui.theme.AppColors
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.ui.theme.YearProgressTheme
import com.example.yearprogress.utils.GoalCountdown
import com.example.yearprogress.utils.PreferenceManager
import com.example.yearprogress.utils.TimePeriod
import com.example.yearprogress.utils.dayProgress
import com.example.yearprogress.utils.elapsedSeconds
import com.example.yearprogress.utils.formatRemainingTime
import com.example.yearprogress.utils.getDaySuffix
import com.example.yearprogress.utils.monthProgress
import com.example.yearprogress.utils.progress
import com.example.yearprogress.utils.remainingLabel
import com.example.yearprogress.utils.resolvePinnedGoal
import com.example.yearprogress.utils.totalSeconds
import com.example.yearprogress.utils.weekBounds
import com.example.yearprogress.utils.weekProgress
import com.example.yearprogress.utils.yearProgress
import com.example.yearprogress.widget.WidgetRefreshManager
import kotlinx.coroutines.delay
import java.lang.String.format
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.time.format.TextStyle as DateTextStyle

private data class CardInfo(
    val period: TimePeriod,
    val title: String,
    val label: String,
    val progress: Double,
    val totalSeconds: Long,
    val remainingSeconds: Long,
    val accentColor: Color,
)

@Composable
fun TimeScreen(onOpenProductivity: () -> Unit = {}) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales.get(0) ?: Locale.getDefault()
    val preferenceManager = remember { PreferenceManager(context.applicationContext) }
    var goals by remember { mutableStateOf(preferenceManager.getGoals()) }
    var pinnedGoalId by remember { mutableStateOf(preferenceManager.getPinnedGoalId()) }
    val weekStartDay = preferenceManager.getWeekStartDay()
    val pinnedGoal = remember(goals, pinnedGoalId) {
        resolvePinnedGoal(goals, pinnedGoalId)
    }

    LaunchedEffect(goals, pinnedGoalId) {
        val resolvedGoal = resolvePinnedGoal(goals, pinnedGoalId) ?: return@LaunchedEffect
        if (resolvedGoal.id != pinnedGoalId) {
            preferenceManager.setPinnedGoalId(resolvedGoal.id)
            pinnedGoalId = resolvedGoal.id
            WidgetRefreshManager.refreshGoalWidgets(context)
            ReminderScheduler.rescheduleAll(context)
        }
    }

    Column {
        MainGoalSection(
            goal = pinnedGoal,
            locale = locale,
            onOpenProductivity = onOpenProductivity
        )

        Spacer(Modifier.height(10.dp))

        LiveTimeCardsSection(
            weekStartDayName = weekStartDay.name,
            locale = locale,
            colors = ProgressColors
        )

        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun MainGoalSection(
    goal: GoalCountdown?,
    locale: Locale,
    onOpenProductivity: () -> Unit
) {
    val context = LocalContext.current
    if (goal == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ProgressColors.bgCard)
                .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    stringResource(R.string.main_goal),
                    fontSize = 10.sp,
                    color = ProgressColors.textDim,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.main_goal_empty_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProgressColors.textPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.main_goal_empty_body),
                    fontSize = 12.sp,
                    color = ProgressColors.textMuted,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onOpenProductivity,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProgressColors.colorYear,
                        contentColor = Color.Black
                    )
                ) {
                    Text(stringResource(R.string.open_tracker))
                }
            }
        }
        return
    }

    val now = rememberDateTimeTicker(1000L)
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("dd MMM yyyy", locale) }

    TimeCardFrame(
        title = stringResource(R.string.main_goal),
        label = goal.name,
        progress = goal.progress(now),
        totalSeconds = goal.totalSeconds(),
        elapsedSeconds = goal.elapsedSeconds(now),
        secondaryLine = "${goal.targetDate.format(dateFormatter)} • ${goal.remainingLabel(context, now)}",
        accentColor = ProgressColors.colorYear,
        footer = {
            Text(
                text = stringResource(R.string.main_goal_hint),
                fontSize = 10.sp,
                color = ProgressColors.textDim,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onOpenProductivity,
                border = androidx.compose.foundation.BorderStroke(1.dp, ProgressColors.cardBorder)
            ) {
                Text(stringResource(R.string.open_tracker))
            }
        }
    )
}

@Composable
private fun LiveTimeCardsSection(
    weekStartDayName: String,
    locale: Locale,
    colors: AppColors
) {
    val now = rememberDateTimeTicker(1000L)
    val weekStartDay = remember(weekStartDayName) {
        runCatching { com.example.yearprogress.utils.WeekStartDay.valueOf(weekStartDayName) }
            .getOrDefault(com.example.yearprogress.utils.WeekStartDay.SUNDAY)
    }

    val monthShort =
        now.month.getDisplayName(DateTextStyle.SHORT_STANDALONE, locale).uppercase(locale)
    val dayShort =
        now.dayOfWeek.getDisplayName(DateTextStyle.SHORT_STANDALONE, locale).uppercase(locale)
    val yearStart = LocalDateTime.of(now.year, 1, 1, 0, 0)
    val yearEnd = LocalDateTime.of(now.year, 12, 31, 23, 59, 59)
    val monthStart = LocalDateTime.of(now.year, now.monthValue, 1, 0, 0)
    val monthEnd = monthStart.plusMonths(1).minusSeconds(1)
    val dayStart = now.toLocalDate().atStartOfDay()
    val dayEnd = dayStart.plusDays(1).minusSeconds(1)
    val (weekStart, weekEnd) = weekBounds(now, weekStartDay)

    val cards = remember(
        now,
        monthShort,
        dayShort,
        yearStart,
        yearEnd,
        monthStart,
        monthEnd,
        dayStart,
        dayEnd,
        weekStart,
        weekEnd,
        colors
    ) {
        listOf(
            CardInfo(
                TimePeriod.YEAR,
                "YEAR",
                now.year.toString(),
                yearProgress(now),
                ChronoUnit.SECONDS.between(yearStart, yearEnd),
                ChronoUnit.SECONDS.between(now, yearEnd).coerceAtLeast(0),
                colors.colorYear
            ),
            CardInfo(
                TimePeriod.MONTH,
                "MONTH",
                monthShort,
                monthProgress(now),
                ChronoUnit.SECONDS.between(monthStart, monthEnd),
                ChronoUnit.SECONDS.between(now, monthEnd).coerceAtLeast(0),
                colors.colorMonth
            ),
            CardInfo(
                TimePeriod.WEEK,
                "WEEK",
                dayShort,
                weekProgress(now, weekStartDay),
                ChronoUnit.SECONDS.between(weekStart, weekEnd),
                ChronoUnit.SECONDS.between(now, weekEnd).coerceAtLeast(0),
                colors.colorWeek
            ),
            CardInfo(
                TimePeriod.DAY,
                "DAY",
                "${now.dayOfMonth}${getDaySuffix(now.dayOfMonth)}",
                dayProgress(now),
                ChronoUnit.SECONDS.between(dayStart, dayEnd),
                ChronoUnit.SECONDS.between(now, dayEnd).coerceAtLeast(0),
                colors.colorDay
            ),
        )
    }

    Column {
        cards.forEachIndexed { index, card ->
            TimeCard(
                title = when (card.period) {
                    TimePeriod.YEAR -> stringResource(R.string.year)
                    TimePeriod.MONTH -> stringResource(R.string.month)
                    TimePeriod.WEEK -> stringResource(R.string.week)
                    TimePeriod.DAY -> stringResource(R.string.day)
                },
                label = card.label,
                progress = card.progress,
                totalSeconds = card.totalSeconds,
                remainingSeconds = card.remainingSeconds,
                period = card.period,
                accentColor = card.accentColor,
                colors = colors
            )
            if (index != cards.lastIndex) Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
internal fun TimeCardFrame(
    title: String,
    label: String,
    progress: Double,
    totalSeconds: Long,
    elapsedSeconds: Long,
    secondaryLine: String,
    accentColor: Color,
    footer: @Composable ColumnScope.() -> Unit
) {
    val clampedProgress = progress.coerceIn(0.0, 1.0)
    val animProg = animatedProgressFloat(clampedProgress.toFloat())
    val pct = clampedProgress * 100
    val intPart = pct.toLong().toString()
    val isComplete = pct >= 100.0
    val decPart = if (isComplete) "" else format(Locale.US, "%.6f", pct - pct.toLong()).substring(1)

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
                        title,
                        fontSize = 10.sp,
                        color = ProgressColors.textDim,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProgressColors.textPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${format(Locale.US, "%,d", elapsedSeconds)}s",
                        fontSize = 11.sp,
                        color = ProgressColors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "/ ${format(Locale.US, "%,d", totalSeconds)}s",
                        fontSize = 10.sp,
                        color = ProgressColors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                secondaryLine,
                color = ProgressColors.textDim,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
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
                        withStyle(
                            SpanStyle(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = ProgressColors.textPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        ) {
                            append(intPart)
                        }
                        withStyle(
                            SpanStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = ProgressColors.textMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        ) {
                            append(if (isComplete) "%" else "$decPart%")
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
                    Text(
                        stringResource(R.string.live),
                        fontSize = 10.sp,
                        color = accentColor,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
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
    TimeCardFrame(
        title = title,
        label = label,
        progress = progress,
        totalSeconds = totalSeconds,
        elapsedSeconds = elapsed,
        secondaryLine = "${stringResource(R.string.remaining)}: ${
            formatRemainingTime(
                remainingSeconds,
                period
            )
        }",
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

@Composable
internal fun rememberDateTimeTicker(intervalMillis: Long): LocalDateTime {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(intervalMillis) {
        while (true) {
            delay(intervalMillis)
            now = LocalDateTime.now()
        }
    }
    return now
}

@Preview
@Composable
private fun TimeScreenPreview() = YearProgressTheme {
    TimeScreen()
}
