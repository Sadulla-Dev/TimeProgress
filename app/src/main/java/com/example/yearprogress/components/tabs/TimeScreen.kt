package com.example.yearprogress.components.tabs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yearprogress.R
import com.example.yearprogress.ui.theme.AppColors
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.ui.theme.YearProgressTheme
import com.example.yearprogress.utils.dayProgress
import com.example.yearprogress.utils.getDaySuffix
import com.example.yearprogress.utils.monthProgress
import com.example.yearprogress.utils.weekProgress
import com.example.yearprogress.utils.yearProgress
import kotlinx.coroutines.delay
import java.lang.String.format
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimeScreen() {
    val colors = ProgressColors

    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000); now = LocalDateTime.now()
        }
    }
    val months =
        listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
    val dayNames = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    val cards = listOf(
        Triple(
            stringResource(R.string.year), now.year.toString(),
            yearProgress(now) to ChronoUnit.SECONDS.between(
                LocalDateTime.of(now.year, 1, 1, 0, 0),
                LocalDateTime.of(now.year, 12, 31, 23, 59, 59)
            ) to colors.colorYear
        ),
        Triple(
            stringResource(R.string.month), months[now.monthValue - 1],
            monthProgress(now) to ChronoUnit.SECONDS.between(
                LocalDateTime.of(now.year, now.month, 1, 0, 0),
                LocalDateTime.of(now.year, now.month, 1, 0, 0).plusMonths(1).minusSeconds(1)
            ) to colors.colorMonth
        ),
        Triple(
            stringResource(R.string.week), dayNames[now.dayOfWeek.value % 7],
            weekProgress(now) to 604800L to colors.colorWeek
        ),
        Triple(
            stringResource(R.string.day), "${now.dayOfMonth}${getDaySuffix(now.dayOfMonth)}",
            dayProgress(now) to 86400L to colors.colorDay
        ),
    )
    Column {
        cards.forEach { (title, label, rest) ->
            val (progressPair, color) = rest
            val (progress, totalSec) = progressPair

            TimeCard(
                title = title,
                label = label,
                progress = progress,
                totalSeconds = totalSec,
                accentColor = color,
                colors = colors
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}


@Composable
fun TimeCard(
    title: String,
    label: String,
    progress: Double,
    totalSeconds: Long,
    accentColor: Color,
    colors: AppColors
) {
    val animProg = animatedProgressFloat(progress.toFloat())
    val elapsed = (progress * totalSeconds).toLong()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        title,
                        fontSize = 10.sp,
                        color = colors.textDim,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${format(Locale.US, "%,d", elapsed)}s",
                        fontSize = 11.sp,
                        color = colors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "/ ${format(Locale.US, "%,d", totalSeconds)}s",
                        fontSize = 10.sp,
                        color = colors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(colors.progress)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animProg)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(99.dp))
                        .background(accentColor)
                )
            }

            Spacer(Modifier.height(10.dp))

            val pct = progress * 100
            val intPart = pct.toLong().toString()
            val decPart = format(Locale.US, "%.6f", pct - pct.toLong()).substring(1)

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.textPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        ) { append(intPart) }
                        withStyle(
                            SpanStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        ) { append("$decPart%") }
                    }
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        stringResource(R.string.live),
                        fontSize = 10.sp,
                        color = accentColor,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}


@Composable
fun animatedProgressFloat(target: Float): Float {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(target) {
        animated.animateTo(target, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }
    return animated.value
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
private fun TimeScreenPreview() = YearProgressTheme {
    TimeScreen()
}