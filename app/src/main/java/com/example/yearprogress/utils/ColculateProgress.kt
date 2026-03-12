package com.example.yearprogress.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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
fun calculateWeekProgress(): Double {
    val now = LocalDateTime.now()
    val dayOfWeek = now.dayOfWeek.value // 1=Monday, 7=Sunday
    val startOfWeek = now.minusDays((dayOfWeek - 1).toLong())
        .withHour(0).withMinute(0).withSecond(0)
    val endOfWeek = startOfWeek.plusDays(6)
        .withHour(23).withMinute(59).withSecond(59)
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
fun getCurrentWeekText(): String {
    val dayOfWeek = LocalDateTime.now().dayOfWeek
    return dayOfWeek.name
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentMonthText(): String {
    val month = LocalDateTime.now().month
    return month.name
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentYearText(): String {
    return LocalDateTime.now().year.toString() // 2026
}


fun getDaySuffix(day: Int): String {
    return when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}

// ─── Constants ───────────────────────────────────────────────────────────────
const val UZ_LIFE_EXPECTANCY = 75.1

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
fun weekProgress(now: LocalDateTime): Double {
    val dayOfWeek = now.dayOfWeek.value % 7  // Sunday = 0
    val start = now.toLocalDate().atStartOfDay().minusDays(dayOfWeek.toLong())
    val end   = start.plusDays(7).minusSeconds(1)
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
fun lifeProgress(birthDate: LocalDate): Double {
    val now = LocalDate.now()
    val ageYears = ChronoUnit.DAYS.between(birthDate, now) / 365.25
    return (ageYears / UZ_LIFE_EXPECTANCY).coerceIn(0.0, 1.0)
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
