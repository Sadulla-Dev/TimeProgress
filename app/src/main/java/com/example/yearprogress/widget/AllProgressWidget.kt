package com.example.yearprogress.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import com.example.yearprogress.utils.calculateDayProgress
import java.time.LocalDateTime

class AllProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = AllProgressWidget
}

object AllProgressWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(120.dp, 120.dp), // 2x2
            DpSize(200.dp, 100.dp), // 3x1
            DpSize(300.dp, 100.dp)  // 4x1
        )
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {

            val size = LocalSize.current

            if (size.width >= 200.dp && size.height <= 120.dp) {
                // 3x1 yoki 4x1
                FourByOneLayout()
            } else {
                // 1x1, 2x1, 2x2, 3x2
                TwoByTwoLayout()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FourByOneLayout() {

    val day = createCircleProgressBitmap(300, calculateDayProgress(), "Day")
    val week = createCircleProgressBitmap(300, calculateWeekProgress(), "Week")
    val month = createCircleProgressBitmap(300, calculateMonthProgress(), "Month")
    val year = createCircleProgressBitmap(300, calculateYearProgress(), "Year")

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(ImageProvider(year), null, GlanceModifier.defaultWeight())
        Image(ImageProvider(month), null, GlanceModifier.defaultWeight())
        Image(ImageProvider(week), null, GlanceModifier.defaultWeight())
        Image(ImageProvider(day), null, GlanceModifier.defaultWeight())
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TwoByTwoLayout() {

    val day = createCircleProgressBitmap(300, calculateDayProgress(), "Day")
    val week = createCircleProgressBitmap(300, calculateWeekProgress(), "Week")
    val month = createCircleProgressBitmap(300, calculateMonthProgress(), "Month")
    val year = createCircleProgressBitmap(300, calculateYearProgress(), "Year")

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(8.dp)
    ) {

        Row(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight()
        ) {
            Image(ImageProvider(year), null, GlanceModifier.defaultWeight())
            Image(ImageProvider(month), null, GlanceModifier.defaultWeight())
        }
        Row(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight()
        ) {
            Image(ImageProvider(week), null, GlanceModifier.defaultWeight())
            Image(ImageProvider(day), null, GlanceModifier.defaultWeight())
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateWeekProgress(): Double {
    val now = LocalDateTime.now()
    val dayProgress = calculateDayProgress()
    val dayOfWeek = now.dayOfWeek.value - 1
    val totalDays = 7f
    return (dayOfWeek + dayProgress) / totalDays
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateMonthProgress(): Double {
    val now = LocalDateTime.now()
    val dayProgress = calculateDayProgress()
    val day = now.dayOfMonth - 1
    val daysInMonth = now.toLocalDate().lengthOfMonth().toFloat()
    return (day + dayProgress) / daysInMonth
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateYearProgress(): Double {
    val now = LocalDateTime.now()
    val dayProgress = calculateDayProgress()
    val day = now.dayOfYear - 1
    val daysInYear = now.toLocalDate().lengthOfYear().toFloat()
    return (day + dayProgress) / daysInYear
}

fun createCircleProgressBitmap(
    size: Int,
    progress: Double,
    label: String
): Bitmap {

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val stroke = size * 0.08f

    val backgroundPaint = Paint().apply {
        color = android.graphics.Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = stroke
        isAntiAlias = true
    }

    val progressPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    // Label matni uchun paint (masalan "Day", "Week")
    val labelPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = size * 0.16f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = false
    }

    // Foiz matni uchun paint (masalan "73%")
    val percentPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = size * 0.20f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    val rect = RectF(
        stroke,
        stroke,
        size - stroke,
        size - stroke
    )

    val missingAngle = 96f
    val startAngle = 90f + missingAngle / 2
    val sweepAngle = 360f - missingAngle

    // Orqa fon yoy
    canvas.drawArc(rect, startAngle, sweepAngle, false, backgroundPaint)

    // Progress yoy
    canvas.drawArc(rect, startAngle, (sweepAngle * progress).toFloat(), false, progressPaint)

    val cx = size / 2f

    // Foiz matni — markazdan bir oz yuqorida
    val percentText = "${(progress * 100).toInt()}%"
    val percentY = size / 2f - (percentPaint.descent() + percentPaint.ascent()) / 2 - size * 0.04f
    canvas.drawText(percentText, cx, percentY, percentPaint)

    // Label matni — foizdan pastda
    val labelY = percentY + percentPaint.descent() - labelPaint.ascent() + size * 0.01f
    canvas.drawText(label, cx, labelY, labelPaint)

    return bitmap
}