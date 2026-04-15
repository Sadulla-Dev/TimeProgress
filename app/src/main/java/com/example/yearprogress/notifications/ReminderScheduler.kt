package com.example.yearprogress.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.yearprogress.MainActivity
import com.example.yearprogress.R
import com.example.yearprogress.utils.LanguageManager
import com.example.yearprogress.utils.PreferenceManager
import com.example.yearprogress.utils.TimePeriod
import com.example.yearprogress.utils.WeekStartDay
import com.example.yearprogress.utils.calculateWeekProgress
import com.example.yearprogress.utils.formatRemainingTime
import com.example.yearprogress.utils.remainingSeconds
import com.example.yearprogress.utils.remainingLabel
import com.example.yearprogress.utils.resolvePinnedGoal
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

private const val REMINDER_CHANNEL_ID = "time_progress_reminders"

enum class ReminderType(val requestCode: Int, val actionSuffix: String) {
    DAILY(7001, "daily"),
    WEEKLY(7002, "weekly"),
    GOAL(7003, "goal");

    fun action(packageName: String): String = "$packageName.reminder.$actionSuffix"

    companion object {
        fun fromAction(context: Context, action: String?): ReminderType? {
            return entries.firstOrNull { it.action(context.packageName) == action }
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = ReminderType.fromAction(context, intent.action) ?: return
        ReminderScheduler.showNotification(context, type)
        ReminderScheduler.reschedule(context, type)
    }
}

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.rescheduleAll(context)
    }
}

object ReminderScheduler {

    fun rescheduleAll(context: Context) {
        createNotificationChannel(context)
        reschedule(context, ReminderType.DAILY)
        reschedule(context, ReminderType.WEEKLY)
        reschedule(context, ReminderType.GOAL)
    }

    fun reschedule(context: Context, type: ReminderType) {
        when (type) {
            ReminderType.DAILY -> scheduleDailyReminder(context)
            ReminderType.WEEKLY -> scheduleWeeklyReminder(context)
            ReminderType.GOAL -> scheduleGoalReminder(context)
        }
    }

    fun showNotification(context: Context, type: ReminderType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val localizedContext = localizedContext(context)
        val payload = buildPayload(localizedContext, type) ?: return
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            type.requestCode + 100,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(payload.first)
            .setContentText(payload.second)
            .setStyle(NotificationCompat.BigTextStyle().bigText(payload.second))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(type.requestCode, notification)
    }

    private fun scheduleDailyReminder(context: Context) {
        val preferenceManager = PreferenceManager(context.applicationContext)
        if (!preferenceManager.isDailyReminderEnabled()) {
            cancel(context, ReminderType.DAILY)
            return
        }

        val trigger = nextDailyTrigger(
            now = ZonedDateTime.now(),
            hour = preferenceManager.getDailyReminderHour(),
            minute = preferenceManager.getDailyReminderMinute()
        )
        schedule(context, ReminderType.DAILY, trigger.toInstant().toEpochMilli())
    }

    private fun scheduleWeeklyReminder(context: Context) {
        val preferenceManager = PreferenceManager(context.applicationContext)
        if (!preferenceManager.isWeeklyReminderEnabled()) {
            cancel(context, ReminderType.WEEKLY)
            return
        }

        val trigger = nextWeeklyTrigger(
            now = ZonedDateTime.now(),
            weekStartDay = preferenceManager.getWeekStartDay(),
            hour = preferenceManager.getWeeklyReminderHour(),
            minute = preferenceManager.getWeeklyReminderMinute()
        )
        schedule(context, ReminderType.WEEKLY, trigger.toInstant().toEpochMilli())
    }

    private fun scheduleGoalReminder(context: Context) {
        val preferenceManager = PreferenceManager(context.applicationContext)
        val goal = resolvePinnedGoal(preferenceManager.getGoals(), preferenceManager.getPinnedGoalId())
        if (!preferenceManager.isGoalReminderEnabled() || goal == null || goal.remainingSeconds() <= 0) {
            cancel(context, ReminderType.GOAL)
            return
        }

        val trigger = nextDailyTrigger(
            now = ZonedDateTime.now(),
            hour = preferenceManager.getGoalReminderHour(),
            minute = preferenceManager.getGoalReminderMinute()
        )
        schedule(context, ReminderType.GOAL, trigger.toInstant().toEpochMilli())
    }

    private fun schedule(context: Context, type: ReminderType, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            reminderPendingIntent(context, type)
        )
    }

    private fun cancel(context: Context, type: ReminderType) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(reminderPendingIntent(context, type))
    }

    private fun reminderPendingIntent(context: Context, type: ReminderType): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = type.action(context.packageName)
        }
        return PendingIntent.getBroadcast(
            context,
            type.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildPayload(context: Context, type: ReminderType): Pair<String, String>? {
        val preferenceManager = PreferenceManager(context.applicationContext)
        return when (type) {
            ReminderType.DAILY -> {
                val remaining = formatRemainingTime(
                    remainingSeconds(TimePeriod.DAY),
                    TimePeriod.DAY
                )
                context.getString(R.string.notification_day_title) to
                    context.getString(R.string.notification_day_body, remaining)
            }

            ReminderType.WEEKLY -> {
                val weekStartDay = preferenceManager.getWeekStartDay()
                val progress = (calculateWeekProgress(weekStartDay) * 100).toInt()
                val remaining = formatRemainingTime(
                    remainingSeconds(TimePeriod.WEEK, weekStartDay = weekStartDay),
                    TimePeriod.WEEK
                )
                context.getString(R.string.notification_week_title) to
                    context.getString(R.string.notification_week_body, progress, remaining)
            }

            ReminderType.GOAL -> {
                val goal = resolvePinnedGoal(
                    preferenceManager.getGoals(),
                    preferenceManager.getPinnedGoalId()
                ) ?: return null
                if (goal.remainingSeconds() <= 0) return null
                context.getString(R.string.notification_goal_title) to
                    context.getString(
                        R.string.notification_goal_body,
                        goal.name,
                        goal.remainingLabel(context)
                    )
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
        }
        manager.createNotificationChannel(channel)
    }

    private fun localizedContext(context: Context): Context {
        val preferenceManager = PreferenceManager(context.applicationContext)
        return LanguageManager.changeLanguage(context, preferenceManager.getLanguage())
    }
}

internal fun nextDailyTrigger(
    now: ZonedDateTime,
    hour: Int,
    minute: Int
): ZonedDateTime {
    var candidate = now
        .withHour(hour)
        .withMinute(minute)
        .withSecond(0)
        .withNano(0)
    if (!candidate.isAfter(now)) {
        candidate = candidate.plusDays(1)
    }
    return candidate
}

internal fun nextWeeklyTrigger(
    now: ZonedDateTime,
    weekStartDay: WeekStartDay,
    hour: Int,
    minute: Int
): ZonedDateTime {
    val reminderDay = when (weekStartDay) {
        WeekStartDay.MONDAY -> DayOfWeek.SUNDAY
        WeekStartDay.SUNDAY -> DayOfWeek.SATURDAY
    }

    var candidate = now
        .with(TemporalAdjusters.nextOrSame(reminderDay))
        .withHour(hour)
        .withMinute(minute)
        .withSecond(0)
        .withNano(0)

    if (!candidate.isAfter(now)) {
        candidate = candidate.plusWeeks(1).with(TemporalAdjusters.nextOrSame(reminderDay))
    }

    return candidate.withZoneSameInstant(ZoneId.systemDefault())
}
