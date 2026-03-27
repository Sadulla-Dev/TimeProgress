package com.example.yearprogress.utils

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
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
    val createdDate: LocalDate = LocalDate.now()
)

fun GoalCountdown.progress(today: LocalDate = LocalDate.now()): Double {
    val totalDays = ChronoUnit.DAYS.between(createdDate, targetDate).coerceAtLeast(1)
    val elapsed = ChronoUnit.DAYS.between(createdDate, today).coerceIn(0, totalDays)
    return elapsed.toDouble() / totalDays.toDouble()
}

fun GoalCountdown.remainingDays(today: LocalDate = LocalDate.now()): Long {
    return ChronoUnit.DAYS.between(today, targetDate).coerceAtLeast(0)
}

fun HabitTracker.weeklyMinutes(): Int = minutesPerDay * 7
fun HabitTracker.monthlyMinutes(): Int = minutesPerDay * 30
fun HabitTracker.yearlyMinutes(): Int = minutesPerDay * 365

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
                        createdDate = LocalDate.parse(item.getString("createdDate"))
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}
