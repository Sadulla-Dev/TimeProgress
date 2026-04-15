package com.example.yearprogress

import com.example.yearprogress.utils.GoalCountdown
import com.example.yearprogress.utils.HabitTracker
import com.example.yearprogress.utils.currentStreak
import com.example.yearprogress.utils.resolvePinnedGoal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {

    @Test
    fun currentStreak_counts_only_consecutive_days() {
        val today = LocalDate.of(2026, 4, 13)
        val habit = HabitTracker(
            name = "Reading",
            minutesPerDay = 30,
            createdDate = today.minusDays(10),
            completionDates = setOf(
                today.toString(),
                today.minusDays(1).toString(),
                today.minusDays(2).toString(),
                today.minusDays(4).toString()
            )
        )

        assertEquals(3, habit.currentStreak(today))
    }

    @Test
    fun resolvePinnedGoal_prefers_explicit_pin() {
        val today = LocalDate.of(2026, 4, 13)
        val first = GoalCountdown(name = "A", createdDate = today, targetDate = today.plusDays(10))
        val second = GoalCountdown(name = "B", createdDate = today, targetDate = today.plusDays(3))

        val resolved = resolvePinnedGoal(listOf(first, second), first.id)

        assertNotNull(resolved)
        assertEquals(first.id, resolved?.id)
    }

    @Test
    fun resolvePinnedGoal_falls_back_to_nearest_goal() {
        val today = LocalDate.of(2026, 4, 13)
        val later = GoalCountdown(name = "Later", createdDate = today, targetDate = today.plusDays(12))
        val sooner = GoalCountdown(name = "Sooner", createdDate = today, targetDate = today.plusDays(2))

        val resolved = resolvePinnedGoal(listOf(later, sooner), null)

        assertNotNull(resolved)
        assertEquals(sooner.id, resolved?.id)
    }
}
