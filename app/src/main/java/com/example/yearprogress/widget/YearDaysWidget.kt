package com.example.yearprogress.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import java.time.LocalDate
import kotlin.math.ceil

class YearDaysWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = YearDaysWidget
}

object YearDaysWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(250.dp, 120.dp))
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                DayGridWidget(context)
            }
        }
    }
}


@Composable
fun DayGridWidget(context: Context) {
    val now = LocalDate.now()
    val dayOfYear = now.dayOfYear
    val totalDays = now.lengthOfYear()
    val colors = GlanceTheme.colors

    val size = LocalSize.current
    val widthDp = size.width.value.toInt().coerceAtLeast(100)
    val progressPercent = (dayOfYear * 100) / totalDays
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp)
            .background(GlanceTheme.colors.widgetBackground)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${now.year} In Days",
                    style = TextStyle(
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.onSurface
                    ),
                )
                Row {
                    Text(
                        text = "$dayOfYear",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    )
                    Text(
                        text = " / $totalDays days",
                        style = TextStyle(
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.onSurface
                        )
                    )

                }
            }
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "$progressPercent%",
                style = TextStyle(
                    fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.onSurface
                ),
            )

        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        val gridBitmap = createYearGridBitmap(context, totalDays, dayOfYear, widthDp)

        LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
            item {
                Image(
                    provider = ImageProvider(gridBitmap),
                    contentDescription = "Day Grid",
                    modifier = GlanceModifier.fillMaxWidth(),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}

fun createYearGridBitmap(
    context: Context,
    totalDays: Int,
    dayOfYear: Int,
    widthDp: Int
): Bitmap {
    val density = context.resources.displayMetrics.density
    // Vidjetning umumiy kengligi
    val widthPx = (widthDp * density).toInt()

    // Nuqta va masofa o'lchamlari
    val dotSize = 7 * density // Nuqta diametri
    val spacing = 3 * density // Nuqtalar orasidagi masofa

    // Bir qatorga nechta nuqta sig'ishini hisoblaymiz
    val dotsPerRow = (widthPx / (dotSize + spacing)).toInt().coerceAtLeast(1)
    val rows = ceil(totalDays.toDouble() / dotsPerRow).toInt()
    val heightPx = (rows * (dotSize + spacing)).toInt()

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Ranglar
    val completedColor = 0xFF3F51B5.toInt() // O'tgan kunlar uchun (Ko'k)
    val remainingColor = 0x44888888.toInt() // Kelgusi kunlar (Ring rangi)

    for (i in 0 until totalDays) {
        val row = i / dotsPerRow
        val col = i % dotsPerRow

        val centerX = col * (dotSize + spacing) + dotSize / 2
        val centerY = row * (dotSize + spacing) + dotSize / 2

        if (i < dayOfYear) {
            // O'tgan kunlar: To'ldirilgan doira
            paint.color = completedColor
            paint.style = Paint.Style.FILL
            canvas.drawCircle(centerX, centerY, dotSize / 2, paint)
        } else {
            // Kelgusi kunlar: Ring (Aylana) shakli
            paint.color = remainingColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f * density // Ring chizig'i qalinligi
            canvas.drawCircle(centerX, centerY, (dotSize / 2) - (paint.strokeWidth / 2), paint)
        }
    }

    return bitmap
}