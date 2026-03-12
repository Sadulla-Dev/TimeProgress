package com.example.yearprogress.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.example.yearprogress.MainActivity
import com.example.yearprogress.widget.createCircleProgressBitmap
import java.util.Locale

@Composable
fun ProgressWidgetContent(
    context: Context,
    progress: Double,
    label: String,
    extraInfo: String
) {
    val size = LocalSize.current
    val isWide = size.width >= 200.dp

    val formattedValue = String.format(Locale.US, "%.3f", progress * 100)
    val parts = formattedValue.split(".")
    val intPart = parts.getOrNull(0) ?: "0"
    val decimalPart = parts.getOrNull(1) ?: "000"

    if (isWide) {
        WideProgressWidget(
            intPart = intPart,
            decimalPart = decimalPart,
            extraInfo = extraInfo,
            label = label,
            progress = progress
        )
    } else {
        SmallProgressWidget(
            context = context,
            progress = progress,
            label = label
        )
    }
}

@Composable
fun WideProgressWidget(
    intPart: String,
    decimalPart: String,
    extraInfo: String,
    label: String,
    progress: Double
) {
    val colors = GlanceTheme.colors

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.widgetBackground)
            .padding(15.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Text(
            text = label,
            style = TextStyle(color = colors.onSurface, fontSize = 16.sp)
        )
        Spacer(GlanceModifier.defaultWeight())
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = intPart,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            )
            Text(
                text = ".$decimalPart%",
                style = TextStyle(fontSize = 12.sp, color = colors.onSurface)
            )
            Spacer(GlanceModifier.defaultWeight())
            Text(
                text = extraInfo,
                style = TextStyle(color = colors.onSurface, fontSize = 12.sp)
            )
        }
        Spacer(GlanceModifier.height(4.dp))
        LinearProgressIndicator(
            modifier = GlanceModifier.defaultWeight().height(5.dp).fillMaxWidth(),
            progress = progress.toFloat(),
            backgroundColor = colors.outline,
            color = colors.primary
        )
    }
}

@Composable
fun SmallProgressWidget(
    context: Context,
    progress: Double,
    label: String
) {
    val colors = GlanceTheme.colors
    val mainColor = colors.primary.getColor(context).toArgb()
    val secondaryColor = colors.outline.getColor(context).toArgb()

    val bitmap = createCircleProgressBitmap(
        size = 300,
        progress = progress,
        label = label,
        mainColor = mainColor,
        secondaryColor = secondaryColor
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.widgetBackground)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize()
        )
    }
}