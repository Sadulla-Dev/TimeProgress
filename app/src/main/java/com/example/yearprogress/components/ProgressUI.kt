package com.example.yearprogress.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yearprogress.utils.calculateDayProgress
import com.example.yearprogress.utils.calculateMonthProgress
import com.example.yearprogress.utils.calculateWeekProgress
import com.example.yearprogress.utils.calculateYearProgress
import com.example.yearprogress.utils.getDaySuffix
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProgressTracker() {
    val currentTime = LocalDateTime.now()
    val currentYear = currentTime.year
    val currentMonth = currentTime.month
    val currentDayOfMonth = currentTime.dayOfMonth
    val currentDayOfWeek = currentTime.dayOfWeek

    val startOfMonth = LocalDateTime.of(currentYear, currentMonth, 1, 0, 0)
    val endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1)
    val monthTotalSeconds = ChronoUnit.SECONDS.between(startOfMonth, endOfMonth).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Progress",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 16.dp, bottom = 16.dp)
                .background(Color(0xFFF9F9F9))
        )
        ProgressCard(
            title = "YEAR",
            value = currentYear.toString(),
            progressCount = ::calculateYearProgress,
            totalSeconds = ChronoUnit.SECONDS.between(
                LocalDateTime.of(LocalDateTime.now().year, 1, 1, 0, 0),
                LocalDateTime.of(LocalDateTime.now().year, 12, 31, 23, 59, 59)
            ).toInt()
        )

        ProgressCard(
            title = "MONTH",
            value = currentMonth.toString().substring(0, 3),
            progressCount = ::calculateMonthProgress,
            totalSeconds = monthTotalSeconds,
        )

        ProgressCard(
            title = "WEEK",
            value = currentDayOfWeek.toString().substring(0, 3),
            progressCount = ::calculateWeekProgress,
            totalSeconds = 604800,
        )

        ProgressCard(
            title = "DAY",
            value = "$currentDayOfMonth${getDaySuffix(currentDayOfMonth)}",
            progressCount = ::calculateDayProgress,
            totalSeconds = 86400,
        )

    }
}

@Composable
fun ProgressCard(
    title: String,
    value: String,
    progressCount: () -> Double,
    totalSeconds: Int,
) {
    var progress by remember { mutableDoubleStateOf(progressCount()) }
    var animatedProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(progress) {
        animate(
            initialValue = animatedProgress,
            targetValue = progress.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedProgress = value
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            progress = progressCount()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 5.dp, horizontal = 16.dp),
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(30.dp)),
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            color = Color(0xFFD4D4D4),
            trackColor = Color(0xFFEEEEEE)
        )
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xFF5D5D5D)
                )
            )

            Text(
                text = value,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomStart),
        ) {
            FormattedProgressText(progress.toFloat())
            Text(
                text = "of ${NumberFormat.getNumberInstance(Locale.US).format(totalSeconds)}s",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            )
        }
    }
}


@Composable
fun FormattedProgressText(progress: Float) {
    val formattedValue = String.format(Locale.US, "%.12f", progress * 100)
    val parts = formattedValue.split(".")
    val intPart = parts.getOrNull(0) ?: "0"
    val decimalPart = parts.getOrNull(1) ?: "000000000"

    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            ) { append(intPart) }

            withStyle(
                style = SpanStyle(
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            ) { append(".$decimalPart%") }
        }
    )
}
