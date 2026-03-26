@file:OptIn(ExperimentalLayoutApi::class)

package com.example.yearprogress.components

import com.example.yearprogress.R
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yearprogress.ui.theme.AppColors
import com.example.yearprogress.utils.findAchievementExample
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class DotMode { YEAR, MONTH, WEEK }
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LifeDots(
    birthDate: LocalDate,
    ageYears: Double,
    lifeExpectancy: Double,
    colors: AppColors
) {
    val language = LocalContext.current.resources.configuration.locales.get(0)?.language ?: "en"

    val totalYears = remember(lifeExpectancy) { lifeExpectancy.toInt() }
    val filledYears = remember(ageYears) { ageYears.toInt() }

    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var dotMode by remember { mutableStateOf(DotMode.YEAR) }

    // 🔹 Header (mode switch)
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
                    .background(if (active) colors.colorLife.copy(0.15f) else colors.progress)
                    .border(
                        1.dp,
                        if (active) colors.colorLife.copy(0.4f) else colors.cardBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        dotMode = mode
                        selectedYear = null
                    }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 10.sp,
                    color = if (active) colors.colorLife else colors.textMuted
                )
            }
        }
    }

    Spacer(Modifier.height(14.dp))

    when (dotMode) {

        // ================= YEAR =================
        DotMode.YEAR -> {

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 15
            ) {
                repeat(totalYears) { i ->

                    val ageNumber = i + 1
                    val example = remember(ageNumber) { findAchievementExample(ageNumber) }
                    val isImportant = example != null

                    val isPast = i < filledYears
                    val isCurrent = i == filledYears
                    val isSelected = selectedYear == i

                    val dotColor = when {
                        isSelected -> Color.Gray
                        isPast -> colors.colorLife.copy(0.85f)
                        isCurrent -> colors.colorLife
                        else -> colors.progress
                    }

                    val scale by animateFloatAsState(
                        targetValue = when {
                            isSelected -> 1.25f
                            isImportant -> 1.15f
                            else -> 1f
                        }, label = ""
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(
                                    when {
                                        isSelected -> 13.dp
                                        isImportant -> 12.dp
                                        else -> 10.dp
                                    }
                                )
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(
                                    width = if (isImportant) 1.5.dp else 0.dp,
                                    color = if (isImportant) colors.colorLife.copy(0.6f) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .then(
                                    if (isImportant) Modifier.shadow(
                                        4.dp,
                                        CircleShape,
                                        ambientColor = colors.colorLife.copy(0.4f),
                                        spotColor = colors.colorLife.copy(0.4f)
                                    ) else Modifier
                                )
                                .clickable(enabled = isPast || isCurrent) {
                                    selectedYear = if (isSelected) null else i
                                }
                        )

                        if (isImportant) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(2.dp, (-2).dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(colors.colorLife)
                            )
                        }
                    }
                }
            }

            // 🔥 Detail card
            selectedYear?.let { idx ->

                val displayYear = birthDate.year + idx
                val ageNumber = idx + 1
                val example = findAchievementExample(ageNumber)

                Spacer(Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.colorLife.copy(0.06f))
                        .border(1.dp, colors.colorLife.copy(0.2f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {

                        Text(
                            "$displayYear • $ageNumber",
                            fontWeight = FontWeight.Bold,
                            color = colors.colorLife
                        )

                        example?.let {
                            Spacer(Modifier.height(10.dp))
                            Text(it.name, fontWeight = FontWeight.Bold)
                            Text(it.localizedAchievement(language))
                        }
                    }
                }
            }
        }

        // ================= MONTH =================
        DotMode.MONTH -> {
            val totalMonths = (lifeExpectancy * 12).toInt()
            val filledMonths = (ageYears * 12).toInt()

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(3.dp),
                maxItemsInEachRow = 30
            ) {
                repeat(totalMonths) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (it < filledMonths)
                                    colors.colorMonth.copy(0.75f)
                                else colors.progress
                            )
                    )
                }
            }
        }

        // ================= WEEK =================
        DotMode.WEEK -> {
            val totalWeeks = (lifeExpectancy * 52.18).toInt()
            val filledWeeks = (ageYears * 52.18).toInt()

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                maxItemsInEachRow = 52
            ) {
                repeat(totalWeeks) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (it < filledWeeks)
                                    colors.colorWeek.copy(0.7f)
                                else colors.progress
                            )
                    )
                }
            }
        }
    }
}