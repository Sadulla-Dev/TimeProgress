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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.drawToBitmap
import com.example.yearprogress.R
import com.example.yearprogress.ui.theme.AppColors
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.ui.theme.YearProgressTheme
import com.example.yearprogress.utils.PreferenceManager
import com.example.yearprogress.utils.TimePeriod
import com.example.yearprogress.utils.WeekStartDay
import com.example.yearprogress.utils.dayProgress
import com.example.yearprogress.utils.formatRemainingTime
import com.example.yearprogress.utils.getDaySuffix
import com.example.yearprogress.utils.monthProgress
import com.example.yearprogress.utils.remainingSeconds
import com.example.yearprogress.utils.shareBitmap
import com.example.yearprogress.utils.shareText
import com.example.yearprogress.utils.weekBounds
import com.example.yearprogress.utils.weekProgress
import com.example.yearprogress.utils.yearProgress
import kotlinx.coroutines.delay
import java.lang.String.format
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimeScreen() {
    val colors = ProgressColors
    val context = LocalContext.current
    val view = LocalView.current
    val locale = context.resources.configuration.locales.get(0) ?: Locale.getDefault()
    val preferenceManager = remember { PreferenceManager(context.applicationContext) }
    val weekStartDay = preferenceManager.getWeekStartDay()

    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = LocalDateTime.now()
        }
    }

    val monthShort =
        now.month.getDisplayName(TextStyle.SHORT_STANDALONE, locale).uppercase(locale)
    val dayShort =
        now.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale).uppercase(locale)

    val yearStart = LocalDateTime.of(now.year, 1, 1, 0, 0)
    val yearEnd = LocalDateTime.of(now.year, 12, 31, 23, 59, 59)
    val monthStart = LocalDateTime.of(now.year, now.monthValue, 1, 0, 0)
    val monthEnd = monthStart.plusMonths(1).minusSeconds(1)
    val dayStart = now.toLocalDate().atStartOfDay()
    val dayEnd = dayStart.plusDays(1).minusSeconds(1)
    val (weekStart, weekEnd) = weekBounds(now, weekStartDay)

    val cards = listOf(
        CardInfo(
            period = TimePeriod.YEAR,
            title = stringResource(R.string.year),
            label = now.year.toString(),
            progress = yearProgress(now),
            totalSeconds = ChronoUnit.SECONDS.between(yearStart, yearEnd),
            remainingSeconds = ChronoUnit.SECONDS.between(now, yearEnd).coerceAtLeast(0),
            accentColor = colors.colorYear,
        ),
        CardInfo(
            period = TimePeriod.MONTH,
            title = stringResource(R.string.month),
            label = monthShort,
            progress = monthProgress(now),
            totalSeconds = ChronoUnit.SECONDS.between(monthStart, monthEnd),
            remainingSeconds = ChronoUnit.SECONDS.between(now, monthEnd).coerceAtLeast(0),
            accentColor = colors.colorMonth,
        ),
        CardInfo(
            period = TimePeriod.WEEK,
            title = stringResource(R.string.week),
            label = dayShort,
            progress = weekProgress(now, weekStartDay),
            totalSeconds = ChronoUnit.SECONDS.between(weekStart, weekEnd),
            remainingSeconds = ChronoUnit.SECONDS.between(now, weekEnd).coerceAtLeast(0),
            accentColor = colors.colorWeek,
        ),
        CardInfo(
            period = TimePeriod.DAY,
            title = stringResource(R.string.day),
            label = "${now.dayOfMonth}${getDaySuffix(now.dayOfMonth)}",
            progress = dayProgress(now),
            totalSeconds = ChronoUnit.SECONDS.between(dayStart, dayEnd),
            remainingSeconds = ChronoUnit.SECONDS.between(now, dayEnd).coerceAtLeast(0),
            accentColor = colors.colorDay,
        ),
    )

    val shareTextBody = buildString {
        append(context.getString(R.string.share_progress_title))
        append("\n\n")
        cards.forEach { card ->
            append("${card.title}: ${format(Locale.US, "%.3f", card.progress * 100)}%")
            append(" • ${context.getString(R.string.remaining)}: ")
            append(formatRemainingTime(card.remainingSeconds, card.period))
            append('\n')
        }
        append("\n")
        append(
            context.getString(
                R.string.share_week_start_summary,
                if (weekStartDay == WeekStartDay.MONDAY) {
                    context.getString(R.string.week_start_monday)
                } else {
                    context.getString(R.string.week_start_sunday)
                }
            )
        )
    }

    Column {
        cards.forEach { card ->
            TimeCard(
                title = card.title,
                label = card.label,
                progress = card.progress,
                totalSeconds = card.totalSeconds,
                remainingSeconds = card.remainingSeconds,
                period = card.period,
                accentColor = card.accentColor,
                colors = colors
            )
            Spacer(Modifier.height(10.dp))
        }

//        SharePanel(
//            onShareText = {
//                shareText(
//                    context = context,
//                    text = shareTextBody,
//                    title = context.getString(R.string.share_text)
//                )
//            },
//            onShareImage = {
//                shareBitmap(
//                    context = context,
//                    bitmap = view.rootView.drawToBitmap(),
//                    chooserTitle = context.getString(R.string.share_image)
//                )
//            }
//        )
    }
}

private data class CardInfo(
    val period: TimePeriod,
    val title: String,
    val label: String,
    val progress: Double,
    val totalSeconds: Long,
    val remainingSeconds: Long,
    val accentColor: Color,
)

@Composable
private fun SharePanel(
    onShareText: () -> Unit,
    onShareImage: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ProgressColors.bgCard)
            .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Text(
                stringResource(R.string.share_progress_title),
                fontSize = 12.sp,
                color = ProgressColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.share_progress_desc),
                fontSize = 10.sp,
                color = ProgressColors.textDim,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onShareImage,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProgressColors.colorWeek,
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.share_image))
                }
                OutlinedButton(onClick = onShareText) {
                    Text(stringResource(R.string.share_text))
                }
            }
        }
    }
}

@Composable
fun TimeCard(
    title: String,
    label: String,
    progress: Double,
    totalSeconds: Long,
    remainingSeconds: Long,
    period: TimePeriod,
    accentColor: Color,
    colors: AppColors
) {
    val animProg = animatedProgressFloat(progress.toFloat())
    val elapsed = (totalSeconds - remainingSeconds).coerceAtLeast(0)

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

            Spacer(Modifier.height(10.dp))

            Text(
                text = "${stringResource(R.string.remaining)}: ${formatRemainingTime(remainingSeconds, period)}",
                fontSize = 10.sp,
                color = colors.textDim,
                fontFamily = FontFamily.Monospace
            )
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
