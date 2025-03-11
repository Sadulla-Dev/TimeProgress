package com.example.yearprogress.widget

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.yearprogress.MainActivity
import com.example.yearprogress.utils.calculateWeekProgress
import com.example.yearprogress.utils.calculateYearProgress
import java.time.LocalDateTime
import java.util.Locale

object WeekProgressWidget : GlanceAppWidget() {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val currentTime = LocalDateTime.now()
            val currentDayOfWeek = currentTime.dayOfWeek
            val formattedValue =
                String.format(Locale.US, "%.3f", calculateWeekProgress().toFloat() * 100)
            val parts = formattedValue.split(".")
            val intPart = parts.getOrNull(0) ?: "0"
            val decimalPart = parts.getOrNull(1) ?: "000000000"

            Column(
                modifier = GlanceModifier.fillMaxSize().background(Color.DarkGray).padding(15.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                Text(
                    text = "Week",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 16.sp
                    )
                )

                Spacer(GlanceModifier.height(8.dp))

                Spacer(GlanceModifier.defaultWeight())
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        text = intPart,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color.White)
                        )
                    )

                    Text(
                        text = ".$decimalPart%",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = ColorProvider(Color.White)
                        )
                    )

                    Spacer(GlanceModifier.defaultWeight())

                    Text(
                        text = currentDayOfWeek.toString().substring(0, 3),
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 12.sp,
                        ),
                    )
                }

                Spacer(GlanceModifier.height(4.dp))
                LinearProgressIndicator(
                    modifier = GlanceModifier.defaultWeight().height(5.dp).fillMaxWidth(),
                    progress = calculateWeekProgress().toFloat(),
                    backgroundColor = ColorProvider(Color.Gray),
                    color = ColorProvider(Color.White)
                )
            }
        }
    }
}

class WeekProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = WeekProgressWidget
}