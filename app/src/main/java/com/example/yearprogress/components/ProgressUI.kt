@file:OptIn(ExperimentalLayoutApi::class)

package com.example.yearprogress.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlinx.coroutines.delay

// ─── Constants ───────────────────────────────────────────────────────────────
private const val UZ_LIFE_EXPECTANCY = 75.1
private val BG_DARK      = Color(0xFF0A0A0F)
private val BG_CARD      = Color(0xFF111118)
private val CARD_BORDER  = Color(0xFF1E1E2A)
private val TEXT_PRIMARY = Color(0xFFFFFFFF)
private val TEXT_MUTED   = Color(0xFF6B7280)
private val TEXT_DIM     = Color(0xFF374151)
private val COLOR_YEAR   = Color(0xFF818CF8)
private val COLOR_MONTH  = Color(0xFFFB923C)
private val COLOR_WEEK   = Color(0xFFF472B6)
private val COLOR_DAY    = Color(0xFF34D399)
private val COLOR_LIFE   = Color(0xFF34D399)

// ─── Helpers ─────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
private fun getDaySuffix(day: Int) = when {
    day in 11..13 -> "th"
    day % 10 == 1 -> "st"
    day % 10 == 2 -> "nd"
    day % 10 == 3 -> "rd"
    else           -> "th"
}

@RequiresApi(Build.VERSION_CODES.O)
private fun yearProgress(now: LocalDateTime): Double {
    val start = LocalDateTime.of(now.year, 1, 1, 0, 0)
    val end   = LocalDateTime.of(now.year, 12, 31, 23, 59, 59)
    return ChronoUnit.SECONDS.between(start, now).toDouble() /
            ChronoUnit.SECONDS.between(start, end).toDouble()
}

@RequiresApi(Build.VERSION_CODES.O)
private fun monthProgress(now: LocalDateTime): Double {
    val start = LocalDateTime.of(now.year, now.month, 1, 0, 0)
    val end   = start.plusMonths(1).minusSeconds(1)
    return ChronoUnit.SECONDS.between(start, now).toDouble() /
            ChronoUnit.SECONDS.between(start, end).toDouble()
}

@RequiresApi(Build.VERSION_CODES.O)
private fun weekProgress(now: LocalDateTime): Double {
    val dayOfWeek = now.dayOfWeek.value % 7  // Sunday = 0
    val start = now.toLocalDate().atStartOfDay().minusDays(dayOfWeek.toLong())
    val end   = start.plusDays(7).minusSeconds(1)
    return ChronoUnit.SECONDS.between(start, now).toDouble() /
            ChronoUnit.SECONDS.between(start, end).toDouble()
}

@RequiresApi(Build.VERSION_CODES.O)
private fun dayProgress(now: LocalDateTime): Double {
    val start = now.toLocalDate().atStartOfDay()
    val end   = start.plusDays(1).minusSeconds(1)
    return ChronoUnit.SECONDS.between(start, now).toDouble() /
            ChronoUnit.SECONDS.between(start, end).toDouble()
}

@RequiresApi(Build.VERSION_CODES.O)
private fun lifeProgress(birthDate: LocalDate): Double {
    val now = LocalDate.now()
    val ageYears = ChronoUnit.DAYS.between(birthDate, now) / 365.25
    return (ageYears / UZ_LIFE_EXPECTANCY).coerceIn(0.0, 1.0)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun ageComponents(birthDate: LocalDate): Triple<Int, Int, Int> {
    val now = LocalDate.now()
    var years  = now.year - birthDate.year
    var months = now.monthValue - birthDate.monthValue
    var days   = now.dayOfMonth - birthDate.dayOfMonth
    if (days   < 0) { months--; days   += java.time.YearMonth.of(now.year, now.month.minus(1)).lengthOfMonth() }
    if (months < 0) { years--;  months += 12 }
    return Triple(years, months, days)
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
            .background(COLOR_LIFE.copy(alpha = alpha))
    )
}

// ─── Section header chip ─────────────────────────────────────────────────────
@Composable
private fun LiveChip() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(Color(0xFF1A1A24))
            .border(1.dp, CARD_BORDER, RoundedCornerShape(99.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        PulsingDot()
        Spacer(Modifier.width(6.dp))
        Text("LIVE", fontSize = 10.sp, color = TEXT_MUTED,
            letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)
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
    val elapsed  = (progress * totalSeconds).toLong()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BG_CARD)
            .border(1.dp, CARD_BORDER, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        // Subtle glow in top-right
//        Box(
//            modifier = Modifier
//                .size(80.dp)
//                .align(Alignment.TopEnd)
//                .offset(x = 20.dp, y = (-20).dp)
//                .background(
//                    Brush.radialGradient(listOf(accentColor.copy(0.10f), Color.Transparent)),
//                    CircleShape
//                )
//        )
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(title, fontSize = 10.sp, color = TEXT_DIM,
                        letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(4.dp))
                    Text(label, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TEXT_DIM)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${String.format(Locale.US, "%,d", elapsed)}s",
                        fontSize = 11.sp, color = TEXT_DIM,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "/ ${String.format(Locale.US, "%,d", totalSeconds)}s",
                        fontSize = 10.sp, color = TEXT_DIM,
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
                    .background(Color(0xFF1E1E2E))
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
            val decPart = String.format(Locale.US, "%.6f", pct - pct.toLong()).substring(1) // ".xxxxxx"

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 30.sp, fontWeight = FontWeight.Black,
                            color = TEXT_PRIMARY, fontFamily = FontFamily.Monospace)) {
                            append(intPart)
                        }
                        withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium,
                            color = TEXT_MUTED, fontFamily = FontFamily.Monospace)) {
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
                    Text("LIVE", fontSize = 10.sp, color = accentColor,
                        fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
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
    val partialFraction = ageYears - filledYears          // 0..1 within current year

    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var dotMode by remember { mutableStateOf(DotMode.YEAR) }

    // ── Mode toggle chips ──────────────────────────────────────────────────
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DotMode.entries.forEach { mode ->
            val active = dotMode == mode
            val label  = when (mode) {
                DotMode.YEAR  -> "YIL"
                DotMode.MONTH -> "OY"
                DotMode.WEEK  -> "HAFTA"
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) COLOR_LIFE.copy(0.15f) else Color(0xFF16161F))
                    .border(
                        1.dp,
                        if (active) COLOR_LIFE.copy(0.4f) else CARD_BORDER,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { dotMode = mode; selectedYear = null }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 10.sp,
                    color = if (active) COLOR_LIFE else TEXT_MUTED,
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
                    val yearNum   = birthDate.year + i
                    val isPast    = i < filledYears
                    val isCurrent = i == filledYears
                    val isSelected = selectedYear == i
                    val dotColor = when {
                        isSelected -> Color.White
                        isPast     -> COLOR_LIFE.copy(alpha = 0.85f)
                        isCurrent  -> COLOR_LIFE.copy(alpha = partialFraction.toFloat())
                        else       -> Color(0xFF1E1E2E)
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
                val isPast      = idx < filledYears
                val isCurrent   = idx == filledYears

                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(COLOR_LIFE.copy(0.06f))
                        .border(1.dp, COLOR_LIFE.copy(0.2f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$displayYear — ${idx + 1}-yosh",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = COLOR_LIFE,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isCurrent -> Color(0xFF92400E).copy(0.3f)
                                            isPast    -> COLOR_LIFE.copy(0.12f)
                                            else      -> Color.Transparent
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    when {
                                        isCurrent -> "HOZIR"
                                        isPast    -> "O'TDI"
                                        else      -> ""
                                    },
                                    fontSize = 9.sp,
                                    color = if (isCurrent) Color(0xFFFBBF24) else COLOR_LIFE,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // 12 month dots for selected year
                        val monthsLived: Int = when {
                            isPast    -> 12
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
                            "OYLAR",
                            fontSize = 9.sp,
                            color = TEXT_DIM,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            val monthNames = listOf("Y","F","M","A","M","I","I","A","S","O","N","D")
                            repeat(12) { m ->
                                val mFilled = m < monthsLived
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (mFilled) COLOR_MONTH.copy(0.7f)
                                                else Color(0xFF1A1A24)
                                            )
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        monthNames[m],
                                        fontSize = 7.sp,
                                        color = if (mFilled) TEXT_MUTED else TEXT_DIM,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // 52 week dots
                        val weeksLived: Int = when {
                            isPast    -> 52
                            isCurrent -> {
                                val yearStart = LocalDate.of(displayYear, birthDate.monthValue, birthDate.dayOfMonth)
                                    .coerceAtLeast(LocalDate.of(displayYear, 1, 1))
                                ChronoUnit.WEEKS.between(yearStart, LocalDate.now()).toInt().coerceIn(0, 52)
                            }
                            else -> 0
                        }

                        Text(
                            "HAFTALAR (52)",
                            fontSize = 9.sp,
                            color = TEXT_DIM,
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
                                            if (w < weeksLived) COLOR_WEEK.copy(0.7f)
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
            val totalMonths  = (UZ_LIFE_EXPECTANCY * 12).toInt()
            val filledMonths = (ageYears * 12).toInt()

            Text(
                "HER KVADRAT = 1 OY  ·  ${filledMonths} / ${totalMonths} OY",
                fontSize = 9.sp, color = TEXT_DIM,
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
                                if (i < filledMonths) COLOR_MONTH.copy(0.75f)
                                else Color(0xFF1A1A24)
                            )
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${totalMonths - filledMonths} OY QOLDI",
                fontSize = 9.sp, color = TEXT_MUTED,
                letterSpacing = 1.sp, fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
        }

        // ── HAFTA mode: 75*52 = 3900 tiny dots ────────────────────────────
        DotMode.WEEK -> {
            val totalWeeks  = (UZ_LIFE_EXPECTANCY * 52.18).toInt()
            val filledWeeks = (ageYears * 52.18).toInt()

            Text(
                "HER NUQTA = 1 HAFTA  ·  ${filledWeeks} / ${totalWeeks} HAFTA",
                fontSize = 9.sp, color = TEXT_DIM,
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
                                if (i < filledWeeks) COLOR_WEEK.copy(0.7f)
                                else Color(0xFF16161F)
                            )
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${totalWeeks - filledWeeks} HAFTA QOLDI",
                fontSize = 9.sp, color = TEXT_MUTED,
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
        while (true) { delay(60_000); now = LocalDate.now() }
    }

    val lp = lifeProgress(birthDate)
    val animLp = animatedProgressFloat(lp.toFloat())
    val (years, months, days) = ageComponents(birthDate)
    val ageYears = ChronoUnit.DAYS.between(birthDate, now) / 365.25
    val remaining = UZ_LIFE_EXPECTANCY - ageYears
    val remYears  = remaining.toInt()
    val remWeeks  = (remaining * 52.18).toInt()
    val remDays   = (remaining * 365.25).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(BG_CARD)
            .border(1.dp, CARD_BORDER, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text("HAYOT TAHLILI · O'ZBEKISTON",
            fontSize = 10.sp, color = TEXT_MUTED,
            letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)

        Spacer(Modifier.height(16.dp))

        // Age chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(years to "YIL", months to "OY", days to "KUN").forEach { (v, l) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF16161F))
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        v.toString(), fontSize = 28.sp,
                        fontWeight = FontWeight.Black, color = TEXT_PRIMARY,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(l, fontSize = 9.sp, color = TEXT_MUTED,
                        letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Life bar header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${String.format(Locale.US,"%.2f", ageYears)} yosh",
                fontSize = 11.sp, color = TEXT_MUTED, fontFamily = FontFamily.Monospace)
            Text("$UZ_LIFE_EXPECTANCY yosh o'rtacha",
                fontSize = 11.sp, color = TEXT_MUTED, fontFamily = FontFamily.Monospace)
        }

        Spacer(Modifier.height(8.dp))

        // Life progress bar (thick)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0xFF1E1E2E))
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
                withStyle(SpanStyle(fontSize = 36.sp, fontWeight = FontWeight.Black,
                    color = COLOR_LIFE, fontFamily = FontFamily.Monospace)) {
                    append(String.format(Locale.US, "%.4f", lp * 100))
                }
                withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium,
                    color = COLOR_LIFE.copy(0.7f), fontFamily = FontFamily.Monospace)) {
                    append("%")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text("hayot o'tdi",
            fontSize = 12.sp, color = TEXT_MUTED,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

        Spacer(Modifier.height(20.dp))

        // Life dots
        LifeDots(birthDate = birthDate, ageYears = ageYears)

        Spacer(Modifier.height(20.dp))

        // Remaining stats
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(COLOR_LIFE.copy(0.06f))
                .border(1.dp, COLOR_LIFE.copy(0.15f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(remYears to "YIL", remWeeks to "HAFTA", remDays to "KUN").forEach { (v, l) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            String.format(Locale.US, "%,d", v),
                            fontSize = 22.sp, fontWeight = FontWeight.Black,
                            color = COLOR_LIFE, fontFamily = FontFamily.Monospace
                        )
                        Text("QOLDI ($l)", fontSize = 8.sp, color = TEXT_MUTED,
                            letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
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
                .background(Color(0xFF111118))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(listOf(COLOR_LIFE.copy(0.3f), Color.Transparent)),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(14.dp)
        ) {
            Text(
                "\"Daraxt ekish uchun eng yaxshi vaqt 20 yil oldin edi. Ikkinchi eng yaxshi vaqt — hozirdir.\"",
                fontSize = 12.sp, color = TEXT_MUTED,
                fontStyle = FontStyle.Italic, lineHeight = 18.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        // Reset button
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TEXT_MUTED),
            border = BorderStroke(1.dp, CARD_BORDER)
        ) {
            Text("← SANANI O'ZGARTIRISH",
                fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
        }
    }
}

// ─── Birth Date Input ─────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun BirthDateInput(onSubmit: (LocalDate) -> Unit) {
    var day   by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year  by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val dayFocus = remember { FocusRequester() }
    val monthFocus = remember { FocusRequester() }
    val yearFocus = remember { FocusRequester() }

    val keyboard = LocalSoftwareKeyboardController.current

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor      = TEXT_PRIMARY,
        unfocusedTextColor    = TEXT_PRIMARY,
        focusedBorderColor    = COLOR_LIFE.copy(0.5f),
        unfocusedBorderColor  = CARD_BORDER,
        focusedContainerColor = Color(0xFF16161F),
        unfocusedContainerColor = Color(0xFF16161F),
        cursorColor           = COLOR_LIFE,
        focusedLabelColor     = TEXT_MUTED,
        unfocusedLabelColor   = TEXT_MUTED,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(BG_CARD)
            .border(1.dp, CARD_BORDER, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text("HAYOT TAHLILI",
            fontSize = 10.sp, color = TEXT_MUTED,
            letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)

        Spacer(Modifier.height(6.dp))

        Text("Tug'ilgan sanangizni kiriting",
            fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TEXT_PRIMARY)

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
                label = { Text("KUN", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                placeholder = { Text("01", fontSize = 18.sp, color = TEXT_DIM,
                    fontFamily = FontFamily.Monospace) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center, color = TEXT_PRIMARY)
            )

            Text("/", fontSize = 22.sp, color = TEXT_DIM)

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
                label = { Text("OY", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                placeholder = { Text("09", fontSize = 18.sp, color = TEXT_DIM,
                    fontFamily = FontFamily.Monospace) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center, color = TEXT_PRIMARY)
            )

            Text("/", fontSize = 22.sp, color = TEXT_DIM)

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
                label = { Text("YIL", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                placeholder = { Text("1995", fontSize = 18.sp, color = TEXT_DIM,
                    fontFamily = FontFamily.Monospace) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace, textAlign = TextAlign.Start, color = TEXT_PRIMARY)
            )
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, fontSize = 11.sp, color = Color(0xFFF87171),
                fontFamily = FontFamily.Monospace)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val d = day.toIntOrNull() ?: 0
                val m = month.toIntOrNull() ?: 0
                val y = year.toIntOrNull() ?: 0
                when {
                    d !in 1..31 || m !in 1..12 || y !in 1900..LocalDate.now().year ->
                        error = "Iltimos to'g'ri sana kiriting"
                    else -> {
                        runCatching {
                            val date = LocalDate.of(y, m, d)
                            if (date.isAfter(LocalDate.now()))
                                error = "Sana kelajakda bo'lishi mumkin emas"
                            else { error = ""; onSubmit(date) }
                        }.onFailure { error = "Noto'g'ri sana" }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = COLOR_LIFE,
                contentColor   = Color.Black
            )
        ) {
            Text("HAYOTIMNI KO'RSAT →",
                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
        }
    }
}

// ─── Main Screen ──────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProgressTracker() {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); now = LocalDateTime.now() } }

    var birthDate by remember { mutableStateOf<LocalDate?>(null) }

    val months = listOf("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC")
    val dayNames = listOf("SUN","MON","TUE","WED","THU","FRI","SAT")

    val cards = listOf(
        Triple("YEAR",  now.year.toString(),
            yearProgress(now) to ChronoUnit.SECONDS.between(
                LocalDateTime.of(now.year,1,1,0,0),
                LocalDateTime.of(now.year,12,31,23,59,59)
            ) to COLOR_YEAR),
        Triple("MONTH", months[now.monthValue - 1],
            monthProgress(now) to ChronoUnit.SECONDS.between(
                LocalDateTime.of(now.year, now.month,1,0,0),
                LocalDateTime.of(now.year, now.month,1,0,0).plusMonths(1).minusSeconds(1)
            ) to COLOR_MONTH),
        Triple("WEEK",  dayNames[now.dayOfWeek.value % 7],
            weekProgress(now) to 604800L to COLOR_WEEK),
        Triple("DAY",   "${now.dayOfMonth}${getDaySuffix(now.dayOfMonth)}",
            dayProgress(now) to 86400L to COLOR_DAY),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BG_DARK)
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
                    withStyle(SpanStyle(fontSize = 36.sp, fontWeight = FontWeight.Black,
                        color = TEXT_PRIMARY, letterSpacing = (-1.5).sp)) {
                        append("Vaqt\n")
                    }
                    withStyle(SpanStyle(fontSize = 36.sp, fontWeight = FontWeight.Black,
                        color = TEXT_PRIMARY.copy(0.25f), letterSpacing = (-1.5).sp)) {
                        append("o'tmoqda.")
                    }
                },
                lineHeight = 42.sp
            )

            Spacer(Modifier.height(8.dp))

            Text("Har soniya, daqiqa, soat — qaytmaydi.",
                fontSize = 13.sp, color = TEXT_MUTED, lineHeight = 20.sp)

            Spacer(Modifier.height(24.dp))

            // Time cards
            cards.forEach { (title, label, rest) ->
                val (progressPair, color) = rest
                val (progress, totalSec) = progressPair
                TimeCard(
                    title        = title,
                    label        = label,
                    progress     = progress,
                    totalSeconds = totalSec,
                    accentColor  = color
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
                Text("O'ZBEKISTON O'RTACHA UMRI: $UZ_LIFE_EXPECTANCY YOSH",
                    fontSize = 10.sp, color = TEXT_DIM,
                    letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(4.dp))
                Text("JAHON BANKI MA'LUMOTLARI ASOSIDA",
                    fontSize = 9.sp, color = TEXT_DIM.copy(0.6f),
                    letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}