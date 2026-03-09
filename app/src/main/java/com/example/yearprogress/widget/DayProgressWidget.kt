package com.example.yearprogress.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
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
import com.example.yearprogress.utils.calculateDayProgress
import com.example.yearprogress.utils.getDaySuffix
import java.time.LocalDateTime
import java.util.Locale

object DayProgressWidget : GlanceAppWidget() {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val currentTime = LocalDateTime.now()
            val currentDayOfMonth = currentTime.dayOfMonth
            val formattedValue =
                String.format(Locale.US, "%.3f", calculateDayProgress().toFloat() * 100)
            val parts = formattedValue.split(".")
            val intPart = parts.getOrNull(0) ?: "0"
            val decimalPart = parts.getOrNull(1) ?: "000000000"

            Column(
                modifier = GlanceModifier.fillMaxSize().background(Color.DarkGray).padding(15.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                Text(
                    text = "Day",
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
                        text = "$currentDayOfMonth${getDaySuffix(currentDayOfMonth)}",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 12.sp,
                        ),
                    )
                }

                Spacer(GlanceModifier.height(4.dp))
                LinearProgressIndicator(
                    modifier = GlanceModifier.defaultWeight().height(5.dp).fillMaxWidth(),
                    progress = calculateDayProgress().toFloat(),
                    backgroundColor = ColorProvider(Color.Gray),
                    color = ColorProvider(Color.White)
                )
            }
        }
    }
}

class DayProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = DayProgressWidget
}



//class DayProgressWidgetReceiver : GlanceAppWidgetReceiver() {
//    override val glanceAppWidget: GlanceAppWidget
//        get() = DayProgressWidget
//}
//
//object DayProgressWidget : GlanceAppWidget() {
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override suspend fun provideGlance(context: Context, id: GlanceId) {
//        val currentTime = LocalDateTime.now()
//        val currentDayOfMonth = currentTime.dayOfMonth
//        val formattedValue =
//            String.format(Locale.US, "%.3f", calculateDayProgress().toFloat() * 100)
//        val parts = formattedValue.split(".")
//        val intPart = parts.getOrNull(0) ?: "0"
//        val decimalPart = parts.getOrNull(1) ?: "000000000"
//
//        val progress = calculateDayProgress()
//
//        val bitmap = createCircleProgressBitmap(
//            size = 800,
//            progress = progress.toFloat(),
//            percentText =  ".$decimalPart%"
//        )
//
//        provideContent {
//
//            Column(
//                modifier = GlanceModifier
//                    .fillMaxSize()
//                    .background(Color.DarkGray)
//                    .padding(16.dp)
//                    .clickable(actionStartActivity<MainActivity>()),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//
//                Text(
//                    text = "Day Progress",
//                    style = TextStyle(
//                        color = ColorProvider(Color.White),
//                        fontSize = 16.sp
//                    )
//                )
//
//
//                Image(
//                    provider = ImageProvider(bitmap),
//                    contentDescription = "Day progress"
//                )
//            }
//        }
//    }
//}
//
//fun createCircleProgressBitmap(
//    size: Int,
//    progress: Float,
//    percentText: String
//): Bitmap {
//
//    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
//    val canvas = Canvas(bitmap)
//
//    val stroke = size * 0.08f
//
//    val backgroundPaint = Paint().apply {
//        color = android.graphics.Color.GRAY
//        style = Paint.Style.STROKE
//        strokeWidth = stroke
//        isAntiAlias = true
//    }
//
//    val progressPaint = Paint().apply {
//        color = android.graphics.Color.WHITE
//        style = Paint.Style.STROKE
//        strokeWidth = stroke
//        strokeCap = Paint.Cap.ROUND
//        isAntiAlias = true
//    }
//
//    val textPaint = Paint().apply {
//        color = android.graphics.Color.WHITE
//        textSize = size * 0.18f
//        textAlign = Paint.Align.CENTER
//        isAntiAlias = true
//    }
//
//    val rect = RectF(
//        stroke,
//        stroke,
//        size - stroke,
//        size - stroke
//    )
//
//    val missingAngle = 96f
//    val startAngle = 90f + missingAngle / 2
//    val sweepAngle = 360f - missingAngle
//
//    // background
//    canvas.drawArc(rect, startAngle, sweepAngle, false, backgroundPaint)
//
//    // progress
//    canvas.drawArc(rect, startAngle, sweepAngle * progress, false, progressPaint)
//
//    val x = size / 2f
//    val y = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2
//    canvas.drawText(percentText, x, y, textPaint)
//
//    return bitmap
//}