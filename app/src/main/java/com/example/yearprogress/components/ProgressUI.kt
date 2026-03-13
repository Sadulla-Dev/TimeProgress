@file:OptIn(ExperimentalLayoutApi::class)

package com.example.yearprogress.components

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.yearprogress.MainViewModel
import com.example.yearprogress.R
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.ui.theme.ThemeMode
import com.example.yearprogress.utils.UZ_LIFE_EXPECTANCY
import com.example.yearprogress.utils.ageComponents
import com.example.yearprogress.utils.dayProgress
import com.example.yearprogress.utils.getDaySuffix
import com.example.yearprogress.utils.lifeProgress
import com.example.yearprogress.utils.monthProgress
import com.example.yearprogress.utils.weekProgress
import com.example.yearprogress.utils.yearProgress
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProgressTracker(
    currentMode: ThemeMode,
    onChangeTheme: (ThemeMode) -> Unit,
    onChangeLanguage: (String) -> Unit
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000); now = LocalDateTime.now()
        }
    }
    var showThemeDialog by remember { mutableStateOf(false) }

    var birthDate by remember { mutableStateOf<LocalDate?>(null) }

    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val months =
        listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
    val dayNames = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    val cards = listOf(
        Triple(
            stringResource(R.string.year), now.year.toString(),
            yearProgress(now) to ChronoUnit.SECONDS.between(
                LocalDateTime.of(now.year, 1, 1, 0, 0),
                LocalDateTime.of(now.year, 12, 31, 23, 59, 59)
            ) to ProgressColors.colorYear
        ),
        Triple(
            stringResource(R.string.month), months[now.monthValue - 1],
            monthProgress(now) to ChronoUnit.SECONDS.between(
                LocalDateTime.of(now.year, now.month, 1, 0, 0),
                LocalDateTime.of(now.year, now.month, 1, 0, 0).plusMonths(1).minusSeconds(1)
            ) to ProgressColors.colorMonth
        ),
        Triple(
            stringResource(R.string.week), dayNames[now.dayOfWeek.value % 7],
            weekProgress(now) to 604800L to ProgressColors.colorWeek
        ),
        Triple(
            stringResource(R.string.day), "${now.dayOfMonth}${getDaySuffix(now.dayOfMonth)}",
            dayProgress(now) to 86400L to ProgressColors.colorDay
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProgressColors.bgDark)
    ) {

        // Background glow top
        Box(
            modifier = Modifier
                .size(500.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-200).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF6366F1).copy(0.06f), Color.Transparent)
                    ), CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(52.dp))

            // Live chip
            LiveChip()

            Spacer(Modifier.height(16.dp))

            // Header
            Text(
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = ProgressColors.textPrimary
                        )
                    ) {
                        append(stringResource(R.string.time) + "\n")
                    }

                    withStyle(
                        SpanStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = ProgressColors.textPrimary.copy(0.25f)
                        )
                    ) {
                        append(stringResource(R.string.is_passing))
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(R.string.every_second_minute_hour_is_not_coming_back),
                fontSize = 13.sp,
                color = ProgressColors.textMuted
            )
            Spacer(Modifier.height(24.dp))

            // Time cards
            cards.forEach { (title, label, rest) ->
                val (progressPair, color) = rest
                val (progress, totalSec) = progressPair
                TimeCard(
                    title = title,
                    label = label,
                    progress = progress,
                    totalSeconds = totalSec,
                    accentColor = color
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(6.dp))

            // Life section
            if (birthDate != null) {
                LifeSection(birthDate = birthDate!!, onReset = { birthDate = null })
            } else {
                BirthDateInput(onSubmit = { birthDate = it })
            }

            Spacer(Modifier.height(32.dp))

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(
                        R.string.uzbekistan_average_life_expectancy,
                        UZ_LIFE_EXPECTANCY
                    ),
                    fontSize = 10.sp,
                    color = ProgressColors.textDim,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.based_on_world_bank_data),
                    fontSize = 9.sp,
                    color = ProgressColors.textDim.copy(0.6f),
                    fontFamily = FontFamily.Monospace
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showDialog = true }
                ) {
                    Text(
                        stringResource(R.string.change_language),
                        fontSize = 10.sp,
                        color = ProgressColors.textMuted,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(5.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showThemeDialog = true }
                ) {
                    Text(
                        stringResource(R.string.change_theme),
                        fontSize = 10.sp,
                        color = ProgressColors.textMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(5.dp),
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

        }
        // Dialog
        if (showThemeDialog) {
            ThemeDialog(
                currentMode = currentMode,
                onDismiss = { showThemeDialog = false },
                onModeSelected = onChangeTheme
            )
        }
        // ─── Language Dialog ─────────────────────────────────────────────
        if (showDialog) {
            LanguageDialog(
                onDismiss = { showDialog = false },
                onLanguageSelected = onChangeLanguage
            )
        }
    }
}

// ─── Animated progress value ─────────────────────────────────────────────────
@Composable
private fun animatedProgressFloat(target: Float): Float {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(target) {
        animated.animateTo(target, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }
    return animated.value
}

// ─── Pulsing dot ─────────────────────────────────────────────────────────────
@Composable
private fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(ProgressColors.colorLife.copy(alpha = alpha))
    )
}

// ─── Section header chip ─────────────────────────────────────────────────────
@Composable
private fun LiveChip() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(ProgressColors.bgCard)
            .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(99.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        PulsingDot()
        Spacer(Modifier.width(6.dp))

        Text(
            text = stringResource(R.string.live),
            fontSize = 10.sp,
            color = ProgressColors.textMuted,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ─── Time Progress Card ───────────────────────────────────────────────────────
@Composable
private fun TimeCard(
    title: String,
    label: String,
    progress: Double,
    totalSeconds: Long,
    accentColor: Color,
) {
    val animProg = animatedProgressFloat(progress.toFloat())
    val elapsed = (progress * totalSeconds).toLong()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ProgressColors.bgCard)
            .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(20.dp))
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
                        title, fontSize = 10.sp, color = ProgressColors.textDim,
                        letterSpacing = 2.sp, fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProgressColors.textDim
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${String.format(Locale.US, "%,d", elapsed)}s",
                        fontSize = 11.sp, color = ProgressColors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "/ ${String.format(Locale.US, "%,d", totalSeconds)}s",
                        fontSize = 10.sp, color = ProgressColors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(ProgressColors.progress)
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

            // Percentage
            val pct = progress * 100
            val intPart = pct.toLong().toString()
            val decPart =
                String.format(Locale.US, "%.6f", pct - pct.toLong()).substring(1) // ".xxxxxx"

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = ProgressColors.textPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        ) {
                            append(intPart)
                        }
                        withStyle(
                            SpanStyle(
                                fontSize = 20.sp, fontWeight = FontWeight.Medium,
                                color = ProgressColors.textMuted, fontFamily = FontFamily.Monospace
                            )
                        ) {
                            append("$decPart%")
                        }
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

// ─── Dot view mode ───────────────────────────────────────────────────────────
private enum class DotMode { YEAR, MONTH, WEEK }

// ─── Life Dots ────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun LifeDots(birthDate: LocalDate, ageYears: Double) {
    val totalYears = UZ_LIFE_EXPECTANCY.toInt()          // 75 circles
    val filledYears = ageYears.toInt()

    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var dotMode by remember { mutableStateOf(DotMode.YEAR) }

    // ── Mode toggle chips ──────────────────────────────────────────────────
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DotMode.entries.forEach { mode ->
            val active = dotMode == mode
            val label = when (mode) {
                DotMode.YEAR -> stringResource(R.string.year)
                DotMode.MONTH -> stringResource(R.string.month)
                DotMode.WEEK -> stringResource(R.string.week)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (active) ProgressColors.colorLife.copy(0.15f) else ProgressColors.progress
                    )
                    .border(
                        1.dp,
                        if (active) ProgressColors.colorLife.copy(0.4f) else ProgressColors.cardBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { dotMode = mode; selectedYear = null }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 10.sp,
                    color = if (active) ProgressColors.colorLife else ProgressColors.textMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }

    Spacer(Modifier.height(14.dp))

    when (dotMode) {
        // ── YIL mode: 75 circles, tap to select ───────────────────────────
        DotMode.YEAR -> {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 15
            ) {
                repeat(totalYears) { i ->
                    val isPast = i < filledYears
                    val isCurrent = i == filledYears
                    val isSelected = selectedYear == i
                    val dotColor = when {
                        isSelected -> Color.Gray
                        isPast -> ProgressColors.colorLife.copy(alpha = 0.85f)
                        isCurrent -> ProgressColors.colorLife
                        else -> ProgressColors.progress
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(if (isSelected) 13.dp else 10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .then(
                                if (isCurrent || isPast)
                                    Modifier.clickable {
                                        selectedYear = if (isSelected) null else i
                                    }
                                else Modifier
                            )
                    )
                }
            }

            // ── Selected year detail ───────────────────────────────────────
            selectedYear?.let { idx ->
                val displayYear = birthDate.year + idx
                val isPast = idx < filledYears
                val isCurrent = idx == filledYears

                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ProgressColors.colorLife.copy(0.06f))
                        .border(
                            1.dp,
                            ProgressColors.colorLife.copy(0.2f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.year_age,
                                    displayYear,
                                    idx + 1
                                ),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProgressColors.colorLife,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isCurrent -> Color(0xFF92400E).copy(0.3f)
                                            isPast -> ProgressColors.colorLife.copy(0.12f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = when {
                                        isCurrent -> stringResource(R.string.now)
                                        isPast -> stringResource(R.string.passed)
                                        else -> ""
                                    },
                                    fontSize = 9.sp,
                                    color = if (isCurrent) Color(0xFFFBBF24) else ProgressColors.colorLife,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // 12 month dots for selected year
                        val monthsLived: Int = when {
                            isPast -> 12
                            isCurrent -> {
                                val bd = birthDate.withYear(displayYear)
                                val today = LocalDate.now()
                                if (today.year == displayYear)
                                    today.monthValue - bd.monthValue + 1
                                else 12
                            }

                            else -> 0
                        }.coerceIn(0, 12)

                        Text(
                            stringResource(R.string.month),
                            fontSize = 9.sp,
                            color = ProgressColors.textDim,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            val monthNames =
                                listOf("Y", "F", "M", "A", "M", "I", "I", "A", "S", "O", "N", "D")
                            repeat(12) { m ->
                                val mFilled = m < monthsLived
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (mFilled) ProgressColors.colorMonth.copy(0.7f)
                                                else Color(0xFF1A1A24)
                                            )
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        monthNames[m],
                                        fontSize = 7.sp,
                                        color = if (mFilled) ProgressColors.textMuted else ProgressColors.textDim,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // 52 week dots
                        val weeksLived: Int = when {
                            isPast -> 52
                            isCurrent -> {
                                val yearStart = LocalDate.of(
                                    displayYear,
                                    birthDate.monthValue,
                                    birthDate.dayOfMonth
                                )
                                    .coerceAtLeast(LocalDate.of(displayYear, 1, 1))
                                ChronoUnit.WEEKS.between(yearStart, LocalDate.now()).toInt()
                                    .coerceIn(0, 52)
                            }

                            else -> 0
                        }

                        Text(
                            stringResource(R.string.weeks_word),
                            fontSize = 9.sp,
                            color = ProgressColors.textDim,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            maxItemsInEachRow = 13
                        ) {
                            repeat(52) { w ->
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (w < weeksLived) ProgressColors.colorWeek.copy(0.7f)
                                            else Color(0xFF1A1A24)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── OY mode: 75*12 = 900 small squares ────────────────────────────
        DotMode.MONTH -> {
            val totalMonths = (UZ_LIFE_EXPECTANCY * 12).toInt()
            val filledMonths = (ageYears * 12).toInt()

            Text(
                stringResource(
                    R.string.each_square_one_month,
                    filledMonths,
                    totalMonths
                ),
                fontSize = 9.sp, color = ProgressColors.textDim,
                letterSpacing = 1.sp, fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(3.dp),
                maxItemsInEachRow = 30
            ) {
                repeat(totalMonths) { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 1.dp)
                            .size(7.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (i < filledMonths) ProgressColors.colorMonth.copy(0.75f)
                                else Color(0xFF1A1A24)
                            )
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(
                    R.string.months_left,
                    totalMonths - filledMonths
                ),
                fontSize = 9.sp, color = ProgressColors.textMuted,
                letterSpacing = 1.sp, fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
        }

        // ── HAFTA mode: 75*52 = 3900 tiny dots ────────────────────────────
        DotMode.WEEK -> {
            val totalWeeks = (UZ_LIFE_EXPECTANCY * 52.18).toInt()
            val filledWeeks = (ageYears * 52.18).toInt()

            Text(
                stringResource(
                    R.string.each_dot_one_week,
                    filledWeeks,
                    totalWeeks
                ),
                fontSize = 9.sp, color = ProgressColors.textDim,
                letterSpacing = 1.sp, fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                maxItemsInEachRow = 52
            ) {
                repeat(totalWeeks) { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 1.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (i < filledWeeks) ProgressColors.colorWeek.copy(0.7f)
                                else Color(0xFF16161F)
                            )
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(
                    R.string.weeks_left,
                    totalWeeks - filledWeeks
                ),
                fontSize = 9.sp, color = ProgressColors.textMuted,
                letterSpacing = 1.sp, fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Life Section ─────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun LifeSection(birthDate: LocalDate, onReset: () -> Unit) {
    var now by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000); now = LocalDate.now()
        }
    }

    val lp = lifeProgress(birthDate)
    val animLp = animatedProgressFloat(lp.toFloat())
    val (years, months, days) = ageComponents(birthDate)
    val ageYears = ChronoUnit.DAYS.between(birthDate, now) / 365.25
    val remaining = UZ_LIFE_EXPECTANCY - ageYears
    val remYears = remaining.toInt()
    val remWeeks = (remaining * 52.18).toInt()
    val remDays = (remaining * 365.25).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ProgressColors.bgCard)
            .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            stringResource(R.string.life_analysis_uzbekistan),
            fontSize = 10.sp,
            color = ProgressColors.textMuted,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(Modifier.height(16.dp))

        // Age chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                years to stringResource(R.string.year),
                months to stringResource(R.string.month),
                days to stringResource(R.string.day)
            ).forEach { (v, l) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ProgressColors.progress)
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        v.toString(), fontSize = 28.sp,
                        fontWeight = FontWeight.Black, color = ProgressColors.textPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        l, fontSize = 9.sp, color = ProgressColors.textMuted,
                        letterSpacing = 2.sp, fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Life bar header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = stringResource(
                    R.string.age_years_old,
                    ageYears
                ),
                fontSize = 11.sp,
                color = ProgressColors.textMuted,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = stringResource(
                    R.string.average_life_expectancy,
                    UZ_LIFE_EXPECTANCY
                ),
                fontSize = 11.sp,
                color = ProgressColors.textMuted,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(8.dp))

        // Life progress bar (thick)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(ProgressColors.progress)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animLp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669))
                        )
                    )
            )
        }

        Spacer(Modifier.height(10.dp))

        // Life percentage
        Text(
            buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontSize = 36.sp, fontWeight = FontWeight.Black,
                        color = ProgressColors.colorLife, fontFamily = FontFamily.Monospace
                    )
                ) {
                    append(String.format(Locale.US, "%.4f", lp * 100))
                }
                withStyle(
                    SpanStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = ProgressColors.colorLife.copy(0.7f),
                        fontFamily = FontFamily.Monospace
                    )
                ) {
                    append("%")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.life_is_gone),
            fontSize = 12.sp,
            color = ProgressColors.textMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        // Life dots
        LifeDots(birthDate = birthDate, ageYears = ageYears)

        Spacer(Modifier.height(20.dp))

        // Remaining stats
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ProgressColors.colorLife.copy(0.06f))
                .border(1.dp, ProgressColors.colorLife.copy(0.15f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                listOf(
                    remYears to stringResource(R.string.year),
                    remWeeks to stringResource(R.string.week),
                    remDays to stringResource(R.string.day)
                ).forEach { (v, l) ->

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(
                            text = String.format(Locale.US, "%,d", v),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = ProgressColors.colorLife,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = stringResource(
                                R.string.remaining_years_remaning,
                                l
                            ),
                            fontSize = 8.sp,
                            color = ProgressColors.textMuted,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Quote
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ProgressColors.progress)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            ProgressColors.colorLife.copy(0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(14.dp)
        ) {
            Text(
                stringResource(R.string.quote),
                fontSize = 12.sp,
                color = ProgressColors.textMuted,
                fontStyle = FontStyle.Italic,
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        // Reset button
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ProgressColors.textMuted),
            border = BorderStroke(1.dp, ProgressColors.cardBorder)
        ) {
            Text(
                stringResource(R.string.change_date),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

// ─── Birth Date Input ─────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun BirthDateInput(onSubmit: (LocalDate) -> Unit) {
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val dayFocus = remember { FocusRequester() }
    val monthFocus = remember { FocusRequester() }
    val yearFocus = remember { FocusRequester() }

    val keyboard = LocalSoftwareKeyboardController.current

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = ProgressColors.textPrimary,
        unfocusedTextColor = ProgressColors.textPrimary,
        focusedBorderColor = ProgressColors.colorLife.copy(0.5f),
        unfocusedBorderColor = ProgressColors.cardBorder,
        focusedContainerColor = ProgressColors.fieldBackground,
        unfocusedContainerColor = ProgressColors.fieldBackground,
        cursorColor = ProgressColors.colorLife,
        focusedLabelColor = ProgressColors.textMuted,
        unfocusedLabelColor = ProgressColors.textMuted,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ProgressColors.bgCard)
            .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            stringResource(R.string.life_analysis),
            fontSize = 10.sp,
            color = ProgressColors.textMuted,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(Modifier.height(6.dp))

        Text(
            stringResource(R.string.enter_your_birth_date),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = ProgressColors.textPrimary
        )
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = day,
                onValueChange = {
                    if (it.length <= 2 && it.all(Char::isDigit)) {
                        day = it
                        if (it.length == 2) {
                            monthFocus.requestFocus()
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(dayFocus),
                label = {
                    Text(
                        stringResource(R.string.day),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                placeholder = {
                    Text(
                        "01", fontSize = 18.sp, color = ProgressColors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    color = ProgressColors.textPrimary
                )
            )

            Text("/", fontSize = 22.sp, color = ProgressColors.textDim)

            OutlinedTextField(
                value = month,
                onValueChange = {
                    if (it.length <= 2 && it.all(Char::isDigit)) {
                        month = it
                        if (it.length == 2) {
                            yearFocus.requestFocus()
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(monthFocus),
                label = {
                    Text(
                        stringResource(R.string.month),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                placeholder = {
                    Text(
                        "09", fontSize = 18.sp, color = ProgressColors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    color = ProgressColors.textPrimary
                )
            )

            Text("/", fontSize = 22.sp, color = ProgressColors.textDim)

            OutlinedTextField(
                value = year,
                onValueChange = {
                    if (it.length <= 4 && it.all(Char::isDigit)) {
                        year = it
                        if (it.length == 4) {
                            keyboard?.hide()
                        }
                    }
                },
                modifier = Modifier
                    .weight(2f)
                    .focusRequester(yearFocus),
                label = {
                    Text(
                        stringResource(R.string.year),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                placeholder = {
                    Text(
                        "1995", fontSize = 18.sp, color = ProgressColors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Start,
                    color = ProgressColors.textPrimary
                )
            )
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                error, fontSize = 11.sp, color = Color(0xFFF87171),
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(16.dp))

        val invalidDateText = stringResource(R.string.please_enter_valid_date)
        val futureDateText = stringResource(R.string.date_cannot_be_in_the_future)
        val wrongDateText = stringResource(R.string.invalid_date)
        Button(
            onClick = {
                val d = day.toIntOrNull() ?: 0
                val m = month.toIntOrNull() ?: 0
                val y = year.toIntOrNull() ?: 0

                when {
                    d !in 1..31 || m !in 1..12 || y !in 1900..LocalDate.now().year ->
                        error = invalidDateText

                    else -> {
                        runCatching {
                            val date = LocalDate.of(y, m, d)

                            if (date.isAfter(LocalDate.now()))
                                error = futureDateText
                            else {
                                error = ""
                                onSubmit(date)
                            }

                        }.onFailure {
                            error = wrongDateText
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ProgressColors.colorLife,
                contentColor = Color.Black
            )
        ) {
            Text(
                stringResource(R.string.show_my_life),
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White,
                ),
                letterSpacing = 1.sp
            )
        }
    }
}