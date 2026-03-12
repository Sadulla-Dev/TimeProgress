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
import android.graphics.*
import android.view.WindowManager
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.glance.*
import com.example.yearprogress.utils.calculateMonthProgress
import com.example.yearprogress.utils.calculateWeekProgress
import com.example.yearprogress.utils.calculateYearProgress

class AllProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AllProgressWidget
}

object AllProgressWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(120.dp, 60.dp),  // 2x1
            DpSize(120.dp, 120.dp), // 2x2
            DpSize(180.dp, 60.dp),  // 3x1
            DpSize(250.dp, 60.dp),  // 4x1
        )
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = LocalSize.current

            val isWide = size.width >= 110.dp   // 3x1, 4x1
            val isTall = size.height >= 100.dp  // 2x2

            if (isWide && !isTall) {
                FourByOneLayout()  // 3x1, 4x1
            } else {
                TwoByTwoLayout()   // 2x1, 2x2
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FourByOneLayout() {
    val context = LocalContext.current
    // Dinamik ranglarni olish
    val colors = GlanceTheme.colors
    val primary = colors.primary.getColor(context).toArgb()
    val onSurface = colors.onSurface.getColor(context).toArgb()
    val outline = colors.outline.getColor(context).toArgb()

    val day = createCircleProgressBitmap(300, calculateDayProgress(), "Day", primary, outline)
    val week = createCircleProgressBitmap(300, calculateWeekProgress(), "Week", primary, outline)
    val month = createCircleProgressBitmap(300, calculateMonthProgress(), "Month", primary, outline)
    val year = createCircleProgressBitmap(300, calculateYearProgress(), "Year", primary, outline)

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
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
    val context = LocalContext.current
    val colors = GlanceTheme.colors
    val primary = colors.primary.getColor(context).toArgb()
    val outline = colors.outline.getColor(context).toArgb()

    val day = createCircleProgressBitmap(300, calculateDayProgress(), "Day", primary, outline)
    val week = createCircleProgressBitmap(300, calculateWeekProgress(), "Week", primary, outline)
    val month = createCircleProgressBitmap(300, calculateMonthProgress(), "Month", primary, outline)
    val year = createCircleProgressBitmap(300, calculateYearProgress(), "Year", primary, outline)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(8.dp)
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            Image(ImageProvider(year), null, GlanceModifier.defaultWeight())
            Image(ImageProvider(month), null, GlanceModifier.defaultWeight())
        }
        Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            Image(ImageProvider(week), null, GlanceModifier.defaultWeight())
            Image(ImageProvider(day), null, GlanceModifier.defaultWeight())
        }
    }
}


fun createCircleProgressBitmap(
    size: Int,
    progress: Double,
    label: String,
    mainColor: Int,     // Progress va matn rangi
    secondaryColor: Int // Orqa fon aylana rangi
): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val stroke = size * 0.08f

    val backgroundPaint = Paint().apply {
        color = secondaryColor
        style = Paint.Style.STROKE
        strokeWidth = stroke
        isAntiAlias = true
        alpha = 70 // Orqa fon aylanasini biroz shaffof qilish
    }

    val progressPaint = Paint().apply {
        color = mainColor
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    val textPaint = Paint().apply {
        color = mainColor
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    val rect = RectF(stroke, stroke, size - stroke, size - stroke)
    val missingAngle = 90f
    val startAngle = 90f + missingAngle / 2
    val sweepAngle = 360f - missingAngle

    // Chizish
    canvas.drawArc(rect, startAngle, sweepAngle, false, backgroundPaint)
    canvas.drawArc(rect, startAngle, (sweepAngle * progress).toFloat(), false, progressPaint)

    val cx = size / 2f

    // Foiz matni
    textPaint.textSize = size * 0.22f
    textPaint.isFakeBoldText = true
    val percentY = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2 - size * 0.05f
    canvas.drawText("${(progress * 100).toInt()}%", cx, percentY, textPaint)

    // Label matni
    textPaint.textSize = size * 0.15f
    textPaint.isFakeBoldText = false
    val labelY = percentY + size * 0.18f
    canvas.drawText(label, cx, labelY, textPaint)

    return bitmap
}