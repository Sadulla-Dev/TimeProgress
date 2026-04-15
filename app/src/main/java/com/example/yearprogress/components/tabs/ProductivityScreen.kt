package com.example.yearprogress.components.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yearprogress.R
import com.example.yearprogress.notifications.ReminderScheduler
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.utils.GoalCountdown
import com.example.yearprogress.utils.PreferenceManager
import com.example.yearprogress.utils.elapsedSeconds
import com.example.yearprogress.utils.progress
import com.example.yearprogress.utils.remainingLabel
import com.example.yearprogress.utils.resolvePinnedGoal
import com.example.yearprogress.utils.totalSeconds
import com.example.yearprogress.widget.WidgetRefreshManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProductivityScreen() {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context.applicationContext) }

    var goals by remember { mutableStateOf(preferenceManager.getGoals()) }
    var pinnedGoalId by remember { mutableStateOf(preferenceManager.getPinnedGoalId()) }

    val pinnedGoal = remember(goals, pinnedGoalId) {
        resolvePinnedGoal(goals, pinnedGoalId)
    }

    LaunchedEffect(goals, pinnedGoalId) {
        val resolvedGoal = resolvePinnedGoal(goals, pinnedGoalId)
        if (resolvedGoal == null && pinnedGoalId != null) {
            preferenceManager.clearPinnedGoalId()
            pinnedGoalId = null
            WidgetRefreshManager.refreshGoalWidgets(context)
            ReminderScheduler.rescheduleAll(context)
        } else if (resolvedGoal != null && resolvedGoal.id != pinnedGoalId) {
            preferenceManager.setPinnedGoalId(resolvedGoal.id)
            pinnedGoalId = resolvedGoal.id
            WidgetRefreshManager.refreshGoalWidgets(context)
            ReminderScheduler.rescheduleAll(context)
        }
    }

    fun persistGoals(updatedGoals: List<GoalCountdown>, newPinnedGoalId: String?) {
        goals = updatedGoals
        pinnedGoalId = newPinnedGoalId
        preferenceManager.saveGoals(updatedGoals)
        if (newPinnedGoalId == null) {
            preferenceManager.clearPinnedGoalId()
        } else {
            preferenceManager.setPinnedGoalId(newPinnedGoalId)
        }
        WidgetRefreshManager.refreshGoalWidgets(context)
        ReminderScheduler.rescheduleAll(context)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        pinnedGoal?.let {
            PinnedGoalOverviewCard(goal = it)
            Spacer(modifier = Modifier.height(12.dp))
        }

        GoalCountdownSection(
            goals = goals,
            pinnedGoalId = pinnedGoalId,
            onAddGoal = { goal ->
                val updatedGoals = (goals + goal).sortedBy { it.targetDate }
                val updatedPinnedGoalId = pinnedGoalId ?: goal.id
                persistGoals(updatedGoals, updatedPinnedGoalId)
            },
            onRemoveGoal = { id ->
                val updatedGoals = goals.filterNot { it.id == id }
                val updatedPinnedGoalId = if (pinnedGoalId == id) {
                    updatedGoals.minByOrNull { it.targetDate }?.id
                } else {
                    pinnedGoalId
                }
                persistGoals(updatedGoals, updatedPinnedGoalId)
            },
            onPinGoal = { id ->
                persistGoals(goals, if (pinnedGoalId == id) null else id)
            }
        )
    }
}

@Composable
private fun PinnedGoalOverviewCard(goal: GoalCountdown) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ProgressColors.colorYear.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .border(1.dp, ProgressColors.colorYear.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.main_goal),
                fontSize = 10.sp,
                color = ProgressColors.colorYear,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = goal.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ProgressColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.main_goal_synced, goal.remainingLabel(context)),
                fontSize = 11.sp,
                color = ProgressColors.textMuted,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
internal fun GoalCountdownSection(
    goals: List<GoalCountdown>,
    pinnedGoalId: String?,
    onAddGoal: (GoalCountdown) -> Unit,
    onRemoveGoal: (String) -> Unit,
    onPinGoal: (String) -> Unit,
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
                GoalCard(
                    goal = goal,
                    now = now,
                    isPinned = goal.id == pinnedGoalId,
                    onPinGoal = { onPinGoal(goal.id) },
                    onRemove = { onRemoveGoal(goal.id) }
                )
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
                    value = day,
                    onChange = { if (it.length <= 2 && it.all(Char::isDigit)) day = it },
                    label = stringResource(R.string.day),
                    modifier = Modifier.weight(1f)
                )
                SmallNumberField(
                    value = month,
                    onChange = { if (it.length <= 2 && it.all(Char::isDigit)) month = it },
                    label = stringResource(R.string.month),
                    modifier = Modifier.weight(1f)
                )
                SmallNumberField(
                    value = year,
                    onChange = { if (it.length <= 4 && it.all(Char::isDigit)) year = it },
                    label = stringResource(R.string.year),
                    modifier = Modifier.weight(1.4f)
                )
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
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
internal fun GoalCard(
    goal: GoalCountdown,
    now: LocalDateTime,
    isPinned: Boolean,
    onPinGoal: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales.get(0) ?: Locale.getDefault()
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("dd MMM yyyy", locale) }
    val progress = remember(goal, now) { goal.progress(now) }

    TimeCardFrame(
        title = stringResource(R.string.goal_deadlines),
        label = goal.name,
        progress = progress,
        totalSeconds = goal.totalSeconds(),
        elapsedSeconds = goal.elapsedSeconds(now),
        secondaryLine = "${goal.targetDate.format(dateFormatter)} • ${goal.remainingLabel(context, now)}",
        accentColor = ProgressColors.colorYear,
        footer = {
            if (isPinned) {
                Box(
                    modifier = Modifier
                        .background(ProgressColors.colorYear.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.pinned),
                        fontSize = 10.sp,
                        color = ProgressColors.colorYear,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = stringResource(R.string.goal_progress_summary, (progress * 100).toInt()),
                color = ProgressColors.textMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPinGoal) {
                    Text(
                        text = if (isPinned) {
                            stringResource(R.string.unpin_main_goal)
                        } else {
                            stringResource(R.string.pin_as_main)
                        }
                    )
                }
                OutlinedButton(onClick = onRemove) {
                    Text(stringResource(R.string.remove))
                }
            }
        }
    )
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
            .background(ProgressColors.bgCard, RoundedCornerShape(20.dp))
            .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                color = ProgressColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
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
