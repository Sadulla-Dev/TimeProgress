package com.example.yearprogress.widget

import android.content.Context
import android.os.Build
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.example.yearprogress.R
import com.example.yearprogress.components.ProgressWidgetContent
import com.example.yearprogress.utils.calculateWeekProgress
import com.example.yearprogress.utils.getCurrentWeekText

class WeekProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = WeekProgressWidget
}

object WeekProgressWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(60.dp, 60.dp),
            DpSize(250.dp, 60.dp),
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ProgressWidgetContent(
                    context = context,
                    progress = calculateWeekProgress(),
                    label = context.getString(R.string.week),
                    extraInfo = getCurrentWeekText()
                )
            }
        }
    }
}

