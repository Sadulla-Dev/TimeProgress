package com.example.yearprogress.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetRefreshManager {

    fun refreshGoalWidgets(context: Context) {
        refresh(context, MainGoalWidget::class.java) { id ->
            MainGoalWidget.update(context, id)
        }
    }

    fun refreshWeekBasedWidgets(context: Context) {
        refresh(context, WeekProgressWidget::class.java) { id ->
            WeekProgressWidget.update(context, id)
        }
        refresh(context, AllProgressWidget::class.java) { id ->
            AllProgressWidget.update(context, id)
        }
    }

    private fun refresh(
        context: Context,
        widgetClass: Class<out GlanceAppWidget>,
        update: suspend (GlanceId) -> Unit
    ) {
        val manager = GlanceAppWidgetManager(context)
        CoroutineScope(Dispatchers.IO).launch {
            manager.getGlanceIds(widgetClass).forEach { glanceId ->
                update(glanceId)
            }
        }
    }
}
