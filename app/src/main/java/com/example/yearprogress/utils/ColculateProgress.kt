package com.example.yearprogress.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


// Helper functions
@RequiresApi(Build.VERSION_CODES.O)
fun calculateYearProgress(): Double {
    val now = LocalDateTime.now()
    val startOfYear = LocalDateTime.of(now.year, 1, 1, 0, 0)
    val endOfYear = LocalDateTime.of(now.year, 12, 31, 23, 59, 59)

    val totalSeconds = ChronoUnit.SECONDS.between(startOfYear, endOfYear)
    val secondsPassed = ChronoUnit.SECONDS.between(startOfYear, now)

    return secondsPassed.toDouble() / totalSeconds
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateMonthProgress(): Double {
    val now = LocalDateTime.now()
    val startOfMonth = LocalDateTime.of(now.year, now.month, 1, 0, 0)
    val endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1)

    val totalSeconds = ChronoUnit.SECONDS.between(startOfMonth, endOfMonth)
    val secondsPassed = ChronoUnit.SECONDS.between(startOfMonth, now)

    return secondsPassed.toDouble() / totalSeconds
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateWeekProgress(): Double {
    val now = LocalDateTime.now()
    val currentDay = now.dayOfWeek.value
    val startOfWeek = now.minusDays(currentDay - 1L).withHour(0).withMinute(0).withSecond(0)
    val endOfWeek = startOfWeek.plusDays(6).withHour(23).withMinute(59).withSecond(59)

    val totalSeconds = ChronoUnit.SECONDS.between(startOfWeek, endOfWeek)
    val secondsPassed = ChronoUnit.SECONDS.between(startOfWeek, now)

    return secondsPassed.toDouble() / totalSeconds
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateDayProgress(): Double {
    val now = LocalDateTime.now()
    val startOfDay = now.withHour(0).withMinute(0).withSecond(0)
    val endOfDay = now.withHour(23).withMinute(59).withSecond(59)

    val totalSeconds = ChronoUnit.SECONDS.between(startOfDay, endOfDay)
    val secondsPassed = ChronoUnit.SECONDS.between(startOfDay, now)

    return secondsPassed.toDouble() / totalSeconds
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