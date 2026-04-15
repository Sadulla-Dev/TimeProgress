package com.example.yearprogress.utils

import android.content.Context
import com.example.yearprogress.R
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

data class GoalCountdown(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdDate: LocalDate,
    val targetDate: LocalDate
)

data class HabitTracker(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val minutesPerDay: Int,
    val createdDate: LocalDate = LocalDate.now(),
    val completionDates: Set<String> = emptySet()
)

fun GoalCountdown.progress(now: LocalDateTime = LocalDateTime.now()): Double {
    val start = createdDate.atStartOfDay()
    val end = targetDate.plusDays(1).atStartOfDay().minusSeconds(1)
    if (now.isAfter(end) || now.isEqual(end)) return 1.0
    val totalSeconds = ChronoUnit.SECONDS.between(start, end).coerceAtLeast(1)
    val elapsedSeconds = ChronoUnit.SECONDS.between(start, now).coerceIn(0, totalSeconds)
    return elapsedSeconds.toDouble() / totalSeconds.toDouble()
}

fun GoalCountdown.remainingDays(today: LocalDate = LocalDate.now()): Long {
    return ChronoUnit.DAYS.between(today, targetDate).coerceAtLeast(0)
}

fun GoalCountdown.endDateTime(): LocalDateTime {
    return targetDate.plusDays(1).atStartOfDay().minusSeconds(1)
}

fun GoalCountdown.totalSeconds(): Long {
    return ChronoUnit.SECONDS.between(createdDate.atStartOfDay(), endDateTime()).coerceAtLeast(1)
}

fun GoalCountdown.remainingSeconds(now: LocalDateTime = LocalDateTime.now()): Long {
    return ChronoUnit.SECONDS.between(now, endDateTime()).coerceAtLeast(0)
}

fun GoalCountdown.elapsedSeconds(now: LocalDateTime = LocalDateTime.now()): Long {
    return (totalSeconds() - remainingSeconds(now)).coerceAtLeast(0)
}

fun GoalCountdown.remainingLabel(
    context: Context,
    now: LocalDateTime = LocalDateTime.now()
): String {
    val end = endDateTime()
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

fun HabitTracker.weeklyMinutes(): Int = minutesPerDay * 7
fun HabitTracker.monthlyMinutes(): Int = minutesPerDay * 30
fun HabitTracker.yearlyMinutes(): Int = minutesPerDay * 365

fun HabitTracker.isCompletedOn(date: LocalDate = LocalDate.now()): Boolean {
    return completionDates.contains(date.toString())
}

fun HabitTracker.toggleCompletion(date: LocalDate): HabitTracker {
    val key = date.toString()
    val updatedDates = if (completionDates.contains(key)) {
        completionDates - key
    } else {
        completionDates + key
    }
    return copy(completionDates = updatedDates)
}

fun HabitTracker.completedCountLastDays(days: Int, today: LocalDate = LocalDate.now()): Int {
    return (0 until days).count { offset ->
        isCompletedOn(today.minusDays(offset.toLong()))
    }
}

fun HabitTracker.currentStreak(today: LocalDate = LocalDate.now()): Int {
    var streak = 0
    var cursor = if (isCompletedOn(today)) today else today.minusDays(1)
    while (cursor >= createdDate && isCompletedOn(cursor)) {
        streak++
        cursor = cursor.minusDays(1)
    }
    return streak
}

fun HabitTracker.bestStreak(): Int {
    val allDates = completionDates.mapNotNull { raw ->
        runCatching { LocalDate.parse(raw) }.getOrNull()
    }.sorted()
    if (allDates.isEmpty()) return 0

    var best = 1
    var current = 1
    for (index in 1 until allDates.size) {
        val previous = allDates[index - 1]
        val currentDate = allDates[index]
        if (previous.plusDays(1) == currentDate) {
            current++
            best = maxOf(best, current)
        } else if (previous != currentDate) {
            current = 1
        }
    }
    return best
}

fun HabitTracker.recentDates(days: Int, today: LocalDate = LocalDate.now()): List<LocalDate> {
    return (days - 1 downTo 0).map { offset -> today.minusDays(offset.toLong()) }
}

fun resolvePinnedGoal(goals: List<GoalCountdown>, pinnedGoalId: String?): GoalCountdown? {
    return goals.firstOrNull { it.id == pinnedGoalId } ?: goals.minByOrNull { it.targetDate }
}

fun goalsToJson(goals: List<GoalCountdown>): String {
    return JSONArray().apply {
        goals.forEach { goal ->
            put(
                JSONObject().apply {
                    put("id", goal.id)
                    put("name", goal.name)
                    put("createdDate", goal.createdDate.toString())
                    put("targetDate", goal.targetDate.toString())
                }
            )
        }
    }.toString()
}

fun goalsFromJson(raw: String?): List<GoalCountdown> {
    if (raw.isNullOrBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(
                    GoalCountdown(
                        id = item.optString("id", UUID.randomUUID().toString()),
                        name = item.getString("name"),
                        createdDate = LocalDate.parse(item.getString("createdDate")),
                        targetDate = LocalDate.parse(item.getString("targetDate"))
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

fun habitsToJson(habits: List<HabitTracker>): String {
    return JSONArray().apply {
        habits.forEach { habit ->
            put(
                JSONObject().apply {
                    put("id", habit.id)
                    put("name", habit.name)
                    put("minutesPerDay", habit.minutesPerDay)
                    put("createdDate", habit.createdDate.toString())
                    put(
                        "completionDates",
                        JSONArray().apply {
                            habit.completionDates.sorted().forEach(::put)
                        }
                    )
                }
            )
        }
    }.toString()
}

fun habitsFromJson(raw: String?): List<HabitTracker> {
    if (raw.isNullOrBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(
                    HabitTracker(
                        id = item.optString("id", UUID.randomUUID().toString()),
                        name = item.getString("name"),
                        minutesPerDay = item.getInt("minutesPerDay"),
                        createdDate = LocalDate.parse(item.getString("createdDate")),
                        completionDates = item.optJSONArray("completionDates")
                            ?.let { dates ->
                                buildSet {
                                    for (dateIndex in 0 until dates.length()) {
                                        add(dates.getString(dateIndex))
                                    }
                                }
                            }
                            ?: emptySet()
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}
