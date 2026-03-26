package com.example.yearprogress.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.example.yearprogress.R
import com.example.yearprogress.components.ProgressWidgetContent
import com.example.yearprogress.utils.calculateDayProgress
import com.example.yearprogress.utils.formatRemainingTime
import com.example.yearprogress.utils.getCurrentDayText
import com.example.yearprogress.utils.LanguageManager
import com.example.yearprogress.utils.remainingSeconds
import com.example.yearprogress.utils.TimePeriod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DayProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = DayProgressWidget

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_TIME_TICK ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val manager = GlanceAppWidgetManager(context)
            val coroutineScope = CoroutineScope(Dispatchers.IO)
            coroutineScope.launch {
                manager.getGlanceIds(DayProgressWidget::class.java).forEach { id ->
                    DayProgressWidget.update(context, id)
                }
            }
        }
    }
}

object DayProgressWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(60.dp, 60.dp),
            DpSize(250.dp, 60.dp),
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val language = sharedPrefs.getString("language", "en") ?: "en"
        val localizedContext = LanguageManager.changeLanguage(context, language)

        provideContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ProgressWidgetContent(
                    context = localizedContext,
                    progress = calculateDayProgress(),
                    label = localizedContext.getString(R.string.day),
                    extraInfo = "${getCurrentDayText()} | " +
                        formatRemainingTime(
                            remainingSeconds(TimePeriod.DAY),
                            TimePeriod.DAY
                        )
                )
            }
        }
    }
}
