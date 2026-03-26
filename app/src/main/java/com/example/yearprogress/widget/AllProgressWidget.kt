package com.example.yearprogress.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import com.example.yearprogress.MainActivity
import com.example.yearprogress.R
import com.example.yearprogress.utils.LanguageManager
import com.example.yearprogress.utils.PreferenceManager
import com.example.yearprogress.utils.calculateDayProgress
import com.example.yearprogress.utils.calculateMonthProgress
import com.example.yearprogress.utils.calculateWeekProgress
import com.example.yearprogress.utils.calculateYearProgress
import com.example.yearprogress.utils.formatRemainingTimeCompact
import com.example.yearprogress.utils.remainingSeconds
import com.example.yearprogress.utils.TimePeriod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AllProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AllProgressWidget

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (
            intent.action == Intent.ACTION_TIME_TICK ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val manager = GlanceAppWidgetManager(context)
            val coroutineScope = CoroutineScope(Dispatchers.IO)
            coroutineScope.launch {
                manager.getGlanceIds(AllProgressWidget::class.java).forEach { id ->
                    AllProgressWidget.update(context, id)
                }
            }
        }
    }
}

object AllProgressWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(60.dp, 60.dp),   // 2x1  ← width ni kichikroq qiling
            DpSize(60.dp, 120.dp),  // 2x2
            DpSize(180.dp, 60.dp),  // 3x1
            DpSize(250.dp, 60.dp),  // 4x1
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val language = sharedPrefs.getString("language", "en") ?: "en"
        val localizedContext = LanguageManager.changeLanguage(context, language)
        val weekStartDay = PreferenceManager(context.applicationContext).getWeekStartDay()

        provideContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val size = LocalSize.current

                when {
                    size.width >= 180.dp && size.height < 120.dp -> FourByOneLayout(localizedContext, weekStartDay)
                    size.height >= 120.dp -> TwoByTwoLayout(localizedContext, weekStartDay)
                    else -> TwoByTwoLayout(localizedContext, weekStartDay)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FourByOneLayout(context: Context, weekStartDay: com.example.yearprogress.utils.WeekStartDay) {
    // Dinamik ranglarni olish
    val colors = GlanceTheme.colors
    val primary = colors.primary.getColor(context).toArgb()
    val outline = colors.outline.getColor(context).toArgb()

    val dayRemaining = remainingSeconds(TimePeriod.DAY)
    val weekRemaining = remainingSeconds(TimePeriod.WEEK, weekStartDay = weekStartDay)
    val monthRemaining = remainingSeconds(TimePeriod.MONTH)
    val yearRemaining = remainingSeconds(TimePeriod.YEAR)

    val day = createCircleProgressBitmap(
        size = 300,
        progress = calculateDayProgress(),
        label = "${context.getString(R.string.day)} ${formatRemainingTimeCompact(dayRemaining, TimePeriod.DAY)}",
        mainColor = primary,
        secondaryColor = outline
    )
    val week = createCircleProgressBitmap(
        size = 300,
        progress = calculateWeekProgress(weekStartDay),
        label = "${context.getString(R.string.week)} ${formatRemainingTimeCompact(weekRemaining, TimePeriod.WEEK)}",
        mainColor = primary,
        secondaryColor = outline
    )
    val month = createCircleProgressBitmap(
        size = 300,
        progress = calculateMonthProgress(),
        label = "${context.getString(R.string.month)} ${formatRemainingTimeCompact(monthRemaining, TimePeriod.MONTH)}",
        mainColor = primary,
        secondaryColor = outline
    )
    val year = createCircleProgressBitmap(
        size = 300,
        progress = calculateYearProgress(),
        label = "${context.getString(R.string.year)} ${formatRemainingTimeCompact(yearRemaining, TimePeriod.YEAR)}",
        mainColor = primary,
        secondaryColor = outline
    )

    Row(
        modifier = GlanceModifier
            .fillMaxSize().clickable(actionStartActivity<MainActivity>())
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
fun TwoByTwoLayout(context: Context, weekStartDay: com.example.yearprogress.utils.WeekStartDay) {
    val colors = GlanceTheme.colors
    val primary = colors.primary.getColor(context).toArgb()
    val outline = colors.outline.getColor(context).toArgb()

    val dayRemaining = remainingSeconds(TimePeriod.DAY)
    val weekRemaining = remainingSeconds(TimePeriod.WEEK, weekStartDay = weekStartDay)
    val monthRemaining = remainingSeconds(TimePeriod.MONTH)
    val yearRemaining = remainingSeconds(TimePeriod.YEAR)

    val day = createCircleProgressBitmap(
        size = 300,
        progress = calculateDayProgress(),
        label = "${context.getString(R.string.day)} ${formatRemainingTimeCompact(dayRemaining, TimePeriod.DAY)}",
        mainColor = primary,
        secondaryColor = outline
    )
    val week = createCircleProgressBitmap(
        size = 300,
        progress = calculateWeekProgress(weekStartDay),
        label = "${context.getString(R.string.week)} ${formatRemainingTimeCompact(weekRemaining, TimePeriod.WEEK)}",
        mainColor = primary,
        secondaryColor = outline
    )
    val month = createCircleProgressBitmap(
        size = 300,
        progress = calculateMonthProgress(),
        label = "${context.getString(R.string.month)} ${formatRemainingTimeCompact(monthRemaining, TimePeriod.MONTH)}",
        mainColor = primary,
        secondaryColor = outline
    )
    val year = createCircleProgressBitmap(
        size = 300,
        progress = calculateYearProgress(),
        label = "${context.getString(R.string.year)} ${formatRemainingTimeCompact(yearRemaining, TimePeriod.YEAR)}",
        mainColor = primary,
        secondaryColor = outline
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize().clickable(actionStartActivity<MainActivity>())
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
