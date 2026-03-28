@file:OptIn(ExperimentalLayoutApi::class)

package com.example.yearprogress.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yearprogress.R
import com.example.yearprogress.ui.theme.AppColors
import com.example.yearprogress.utils.findAchievementExample
import com.example.yearprogress.utils.safeClickable
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class DotMode { YEAR, MONTH, WEEK }

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LifeDots(birthDate: LocalDate, ageYears: Double, lifeExpectancy: Double, colors: AppColors) {
    val language = LocalContext.current.resources.configuration.locales.get(0)?.language ?: "en"
    val totalYears = lifeExpectancy.toInt()
    val filledYears = ageYears.toInt()
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var dotMode by remember { mutableStateOf(DotMode.YEAR) }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    .background(if (active) colors.colorLife.copy(0.15f) else colors.progress)
                    .border(
                        1.dp,
                        if (active) colors.colorLife.copy(0.4f) else colors.cardBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .safeClickable { dotMode = mode; selectedYear = null }
                    .padding(
                        horizontal = 14.dp,
                        vertical = 7.dp
                    ), contentAlignment = Alignment.Center) {
                Text(
                    label,
                    fontSize = 10.sp,
                    color = if (active) colors.colorLife else colors.textMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    when (dotMode) {
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
                        isPast -> colors.colorLife.copy(alpha = 0.85f)
                        isCurrent -> colors.colorLife
                        else -> colors.progress
                    }
                    Box(
                        modifier =
                            Modifier
                                .padding(horizontal = 2.dp)
                                .size(if (isSelected) 13.dp else 10.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .then(if (isCurrent || isPast) Modifier.safeClickable {
                                    selectedYear = if (isSelected) null else i
                                } else Modifier))
                }
            }
            selectedYear?.let { idx ->
                val displayYear = birthDate.year + idx
                val isPast = idx < filledYears
                val isCurrent = idx == filledYears
                val ageNumber = idx + 1
                val example =
                    findAchievementExample(ageNumber)
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.colorLife.copy(0.06f))
                            .border(1.dp, colors.colorLife.copy(0.2f), RoundedCornerShape(14.dp))
                            .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.year_age, displayYear, ageNumber),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.colorLife,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier =
                                    Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when {
                                                isCurrent -> Color(0xFF92400E).copy(0.3f); isPast -> colors.colorLife.copy(
                                                0.12f
                                            ); else -> Color.Transparent
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = when {
                                        isCurrent -> stringResource(R.string.now); isPast -> stringResource(
                                            R.string.passed
                                        ); else -> ""
                                    },
                                    fontSize = 9.sp,
                                    color = if (isCurrent) Color(0xFFFBBF24) else colors.colorLife,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        if (example?.actualAge != ageNumber) {
                            example?.let {
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.bgDark)
                                            .border(
                                                1.dp,
                                                colors.cardBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            stringResource(
                                                R.string.achievement_example_title,
                                                ageNumber
                                            ),
                                            fontSize = 9.sp,
                                            color = colors.textDim,
                                            letterSpacing = 2.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            it.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            it.localizedAchievement(language),
                                            fontSize = 11.sp,
                                            color = colors.textMuted
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        val monthsLived: Int = when {
                            isPast -> 12
                            isCurrent -> {
                                val bd = birthDate.withYear(displayYear)
                                val today =
                                    LocalDate.now()
                                if (today.year == displayYear) today.monthValue - bd.monthValue + 1 else 12
                            }

                            else -> 0
                        }.coerceIn(
                            0,
                            12
                        )
                        Text(
                            stringResource(R.string.month),
                            fontSize = 9.sp,
                            color = colors.textDim,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            val monthNames = listOf(
                                "Y",
                                "F",
                                "M",
                                "A",
                                "M",
                                "I",
                                "I",
                                "A",
                                "S",
                                "O",
                                "N",
                                "D"
                            )
                            repeat(12) { m ->
                                val mFilled = m < monthsLived
                                Column(
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (mFilled) colors.colorMonth.copy(0.7f) else colors.progress)
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        monthNames[m],
                                        fontSize = 7.sp,
                                        color = if (mFilled) colors.textMuted else colors.textDim,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        val weeksLived: Int = when {
                            isPast -> 52
                            isCurrent -> {
                                val yearStart = LocalDate.of(
                                    displayYear,
                                    birthDate.monthValue,
                                    birthDate.dayOfMonth
                                ).coerceAtLeast(
                                    LocalDate.of(
                                        displayYear,
                                        1,
                                        1
                                    )
                                )
                                ChronoUnit.WEEKS.between(yearStart, LocalDate.now()).toInt()
                                    .coerceIn(0, 52)
                            }

                            else -> 0
                        }
                        Text(
                            stringResource(R.string.weeks_word),
                            fontSize = 9.sp,
                            color = colors.textDim,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            maxItemsInEachRow = 13
                        ) {
                            repeat(
                                52
                            ) { w ->
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (w < weeksLived) colors.colorWeek.copy(0.7f) else colors.progress)
                                )
                            }
                        }
                    }
                }
            }
        }

        DotMode.MONTH -> {
            val totalMonths = (lifeExpectancy * 12).toInt()
            val filledMonths = (ageYears * 12).toInt()
            Text(
                stringResource(
                    R.string.each_square_one_month,
                    filledMonths,
                    totalMonths
                ),
                fontSize = 9.sp,
                color = colors.textDim,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            DotCanvasGrid(
                totalItems = totalMonths,
                filledItems = filledMonths,
                columns = 30,
                filledColor = colors.colorMonth.copy(0.75f),
                emptyColor = colors.progress,
                isCircle = false
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(
                    R.string.months_left,
                    totalMonths - filledMonths
                ),
                fontSize = 9.sp,
                color = colors.textMuted,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        DotMode.WEEK -> {
            val totalWeeks = (lifeExpectancy * 52.18).toInt()
            val filledWeeks = (ageYears * 52.18).toInt()
            Text(
                stringResource(
                    R.string.each_dot_one_week,
                    filledWeeks,
                    totalWeeks
                ),
                fontSize = 9.sp,
                color = colors.textDim,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            DotCanvasGrid(
                totalItems = totalWeeks,
                filledItems = filledWeeks,
                columns = 52,
                filledColor = colors.colorWeek.copy(0.7f),
                emptyColor = colors.progress,
                isCircle = true
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(
                    R.string.weeks_left,
                    totalWeeks - filledWeeks
                ),
                fontSize = 9.sp,
                color = colors.textMuted,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DotCanvasGrid(
    totalItems: Int,
    filledItems: Int,
    columns: Int,
    filledColor: Color,
    emptyColor: Color,
    isCircle: Boolean,
) {
    val rows = (totalItems + columns - 1) / columns
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val horizontalGap = 2.dp
        val verticalGap = 2.dp
        val cellSize = (maxWidth - horizontalGap * (columns - 1)) / columns
        val canvasHeight = cellSize * rows + verticalGap * (rows - 1)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(canvasHeight)
        ) {
            val cellPx = size.width / columns
            val dotSizePx = cellPx - horizontalGap.toPx()
            val radius = dotSizePx / 2f

            repeat(totalItems) { index ->
                val row = index / columns
                val col = index % columns
                val left = col * cellPx
                val top = row * (dotSizePx + verticalGap.toPx())
                val color = if (index < filledItems) filledColor else emptyColor

                if (isCircle) {
                    drawCircle(
                        color = color,
                        radius = radius / 1.15f,
                        center = androidx.compose.ui.geometry.Offset(
                            x = left + cellPx / 2f,
                            y = top + dotSizePx / 2f
                        )
                    )
                } else {
                    drawRoundRect(
                        color = color,
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(dotSizePx, dotSizePx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                    )
                }
            }
        }
    }
}
