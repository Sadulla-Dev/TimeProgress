package com.example.yearprogress.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun calculateDayProgress(): Double {
    val now = LocalDateTime.now()
    val startOfDay = now.withHour(0).withMinute(0).withSecond(0)
    val endOfDay = now.withHour(23).withMinute(59).withSecond(59)
    val totalSeconds = ChronoUnit.SECONDS.between(startOfDay, endOfDay)
    val secondsPassed = ChronoUnit.SECONDS.between(startOfDay, now)
    return secondsPassed.toDouble() / totalSeconds
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateWeekProgress(weekStartDay: WeekStartDay = WeekStartDay.MONDAY): Double {
    val now = LocalDateTime.now()
    val (startOfWeek, endOfWeek) = weekBounds(now, weekStartDay)
    val totalSeconds = ChronoUnit.SECONDS.between(startOfWeek, endOfWeek)
    val secondsPassed = ChronoUnit.SECONDS.between(startOfWeek, now)
    return secondsPassed.toDouble() / totalSeconds
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateMonthProgress(): Double {
    val now = LocalDateTime.now()
    val startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
    val endOfMonth = now.withDayOfMonth(now.month.length(now.toLocalDate().isLeapYear))
        .withHour(23).withMinute(59).withSecond(59)
    val totalSeconds = ChronoUnit.SECONDS.between(startOfMonth, endOfMonth)
    val secondsPassed = ChronoUnit.SECONDS.between(startOfMonth, now)
    return secondsPassed.toDouble() / totalSeconds
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateYearProgress(): Double {
    val now = LocalDateTime.now()
    val startOfYear = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0)
    val endOfYear = now.withDayOfYear(if (now.toLocalDate().isLeapYear) 366 else 365)
        .withHour(23).withMinute(59).withSecond(59)
    val totalSeconds = ChronoUnit.SECONDS.between(startOfYear, endOfYear)
    val secondsPassed = ChronoUnit.SECONDS.between(startOfYear, now)
    return secondsPassed.toDouble() / totalSeconds
}
@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDayText(): String {
    val day = LocalDateTime.now().dayOfMonth
    return "$day${getDaySuffix(day)}"
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateDayProgress2(): Pair<Int, Int> {
    val now = LocalDate.now()
    val dayOfYear = now.dayOfYear
    val totalDays = now.lengthOfYear()
    return Pair(dayOfYear, totalDays)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentWeekText(): String {
    val locale = Locale.getDefault()
    val dayOfWeek = LocalDateTime.now().dayOfWeek
    return dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        .uppercase(locale)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentMonthText(): String {
    val locale = Locale.getDefault()
    val month = LocalDateTime.now().month
    return month.getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        .uppercase(locale)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentYearText(): String {
    return LocalDateTime.now().year.toString() // 2026
}


fun getDaySuffix(day: Int): String {
    val language = Locale.getDefault().language.lowercase(Locale.getDefault())
    // Keep ordinal suffixes only for English; other languages usually don't use the same suffix rules.
    if (!language.startsWith("en")) return ""

    return when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}

enum class TimePeriod { YEAR, MONTH, WEEK, DAY }

@RequiresApi(Build.VERSION_CODES.O)
fun remainingSeconds(
    period: TimePeriod,
    now: LocalDateTime = LocalDateTime.now(),
    weekStartDay: WeekStartDay = WeekStartDay.MONDAY
): Long {
    return when (period) {
        TimePeriod.YEAR -> {
            val end = LocalDateTime.of(now.year, 12, 31, 23, 59, 59)
            ChronoUnit.SECONDS.between(now, end)
        }

        TimePeriod.MONTH -> {
            val startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
            val endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1)
            ChronoUnit.SECONDS.between(now, endOfMonth)
        }

        TimePeriod.WEEK -> {
            val (startOfWeek, endOfWeek) = weekBounds(now, weekStartDay)
            ChronoUnit.SECONDS.between(now, endOfWeek)
        }

        TimePeriod.DAY -> {
            val startOfDay = now.withHour(0).withMinute(0).withSecond(0)
            val endOfDay = startOfDay.plusDays(1).minusSeconds(1)
            ChronoUnit.SECONDS.between(now, endOfDay)
        }
    }.coerceAtLeast(0)
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatRemainingTime(seconds: Long, period: TimePeriod): String {
    val s = seconds.coerceAtLeast(0)
    val days = s / 86_400
    val hours = (s % 86_400) / 3_600
    val minutes = (s % 3_600) / 60

    return when (period) {
        TimePeriod.DAY -> {
            // Keep it short for cards/widgets.
            if (days > 0) "${days}d ${hours}h" else "${hours}h ${minutes}m"
        }

        TimePeriod.YEAR,
        TimePeriod.MONTH,
        TimePeriod.WEEK -> {
            if (days > 0) "${days}d ${hours}h" else "${hours}h ${minutes}m"
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatRemainingTimeCompact(seconds: Long, period: TimePeriod): String {
    val s = seconds.coerceAtLeast(0)
    val days = s / 86_400
    val hours = (s % 86_400) / 3_600
    val minutes = (s % 3_600) / 60

    return when (period) {
        TimePeriod.DAY -> {
            if (hours > 0) "${hours}h" else "${minutes}m"
        }

        TimePeriod.YEAR,
        TimePeriod.MONTH,
        TimePeriod.WEEK -> {
            when {
                days > 0 -> "${days}d"
                hours > 0 -> "${hours}h"
                else -> "${minutes}m"
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun yearProgress(now: LocalDateTime): Double {
    val start = LocalDateTime.of(now.year, 1, 1, 0, 0)
    val end   = LocalDateTime.of(now.year, 12, 31, 23, 59, 59)
    return ChronoUnit.SECONDS.between(start, now).toDouble() /
            ChronoUnit.SECONDS.between(start, end).toDouble()
}

@RequiresApi(Build.VERSION_CODES.O)
fun monthProgress(now: LocalDateTime): Double {
    val start = LocalDateTime.of(now.year, now.month, 1, 0, 0)
    val end   = start.plusMonths(1).minusSeconds(1)
    return ChronoUnit.SECONDS.between(start, now).toDouble() /
            ChronoUnit.SECONDS.between(start, end).toDouble()
}

@RequiresApi(Build.VERSION_CODES.O)
fun weekProgress(
    now: LocalDateTime,
    weekStartDay: WeekStartDay = WeekStartDay.SUNDAY
): Double {
    val (start, end) = weekBounds(now, weekStartDay)
    return ChronoUnit.SECONDS.between(start, now).toDouble() /
            ChronoUnit.SECONDS.between(start, end).toDouble()
}

@RequiresApi(Build.VERSION_CODES.O)
fun dayProgress(now: LocalDateTime): Double {
    val start = now.toLocalDate().atStartOfDay()
    val end   = start.plusDays(1).minusSeconds(1)
    return ChronoUnit.SECONDS.between(start, now).toDouble() /
            ChronoUnit.SECONDS.between(start, end).toDouble()
}

@RequiresApi(Build.VERSION_CODES.O)
fun ageComponents(birthDate: LocalDate): Triple<Int, Int, Int> {
    val now = LocalDate.now()
    var years  = now.year - birthDate.year
    var months = now.monthValue - birthDate.monthValue
    var days   = now.dayOfMonth - birthDate.dayOfMonth
    if (days   < 0) { months--; days   += java.time.YearMonth.of(now.year, now.month.minus(1)).lengthOfMonth() }
    if (months < 0) { years--;  months += 12 }
    return Triple(years, months, days)
}
