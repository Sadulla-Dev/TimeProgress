package com.example.yearprogress.components.tabs


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yearprogress.R
import com.example.yearprogress.components.LifeDots
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.ui.theme.YearProgressTheme
import com.example.yearprogress.utils.UZ_LIFE_EXPECTANCY
import com.example.yearprogress.utils.ageComponents
import com.example.yearprogress.utils.lifeProgress
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LifeScreen() {
    var birthDate by remember { mutableStateOf<LocalDate?>(null) }
    Column() {
        if (birthDate != null) {
            LifeSection(
                birthDate = birthDate!!,
                onReset = { birthDate = null },
            )
        } else {
            BirthDateInput(onSubmit = { birthDate = it })
        }

        Spacer(Modifier.height(24.dp))

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
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.based_on_world_bank_data),
                fontSize = 9.sp,
                color = ProgressColors.textDim.copy(0.6f),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LifeSection(birthDate: LocalDate, onReset: () -> Unit) {
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
                        v.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = ProgressColors.textPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        l,
                        fontSize = 9.sp,
                        color = ProgressColors.textMuted,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.age_years_old, ageYears),
                fontSize = 11.sp,
                color = ProgressColors.textMuted,
                fontFamily = FontFamily.Monospace
            )
            Text(
                stringResource(R.string.average_life_expectancy, UZ_LIFE_EXPECTANCY),
                fontSize = 11.sp,
                color = ProgressColors.textMuted,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(8.dp))

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
                            listOf(
                                Color(0xFF34D399),
                                Color(0xFF10B981),
                                Color(0xFF059669)
                            )
                        )
                    )
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = ProgressColors.colorLife,
                        fontFamily = FontFamily.Monospace
                    )
                ) { append(String.format(Locale.US, "%.4f", lp * 100)) }
                withStyle(
                    SpanStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = ProgressColors.colorLife.copy(0.7f),
                        fontFamily = FontFamily.Monospace
                    )
                ) { append("%") }
            },
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.life_is_gone),
            fontSize = 12.sp,
            color = ProgressColors.textMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        LifeDots(birthDate = birthDate, ageYears = ageYears, colors = ProgressColors)

        Spacer(Modifier.height(20.dp))

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
                            String.format(Locale.US, "%,d", v),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = ProgressColors.colorLife,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            stringResource(R.string.remaining_years_remaning, l),
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ProgressColors.bgDark)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BirthDateInput(onSubmit: (LocalDate) -> Unit) {
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
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
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
                value = day, onValueChange = {
                    if (it.length <= 2 && it.all(Char::isDigit)) {
                        day = it; if (it.length == 2) monthFocus.requestFocus()
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
                        "01",
                        fontSize = 18.sp,
                        color = ProgressColors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, shape = RoundedCornerShape(12.dp), colors = fieldColors,
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
                value = month, onValueChange = {
                    if (it.length <= 2 && it.all(Char::isDigit)) {
                        month = it; if (it.length == 2) yearFocus.requestFocus()
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
                        "09",
                        fontSize = 18.sp,
                        color = ProgressColors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, shape = RoundedCornerShape(12.dp), colors = fieldColors,
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
                value = year, onValueChange = {
                    if (it.length <= 4 && it.all(Char::isDigit)) {
                        year = it; if (it.length == 4) keyboard?.hide()
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
                        "1995",
                        fontSize = 18.sp,
                        color = ProgressColors.textDim,
                        fontFamily = FontFamily.Monospace
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, shape = RoundedCornerShape(12.dp), colors = fieldColors,
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
                error,
                fontSize = 11.sp,
                color = Color(0xFFF87171),
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
                    d !in 1..31 || m !in 1..12 || y !in 1900..LocalDate.now().year -> error =
                        invalidDateText

                    else -> runCatching {
                        val date = LocalDate.of(y, m, d)
                        if (date.isAfter(LocalDate.now())) error = futureDateText
                        else {
                            error = ""; onSubmit(date)
                        }
                    }.onFailure { error = wrongDateText }
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
                    color = Color.White
                ),
                letterSpacing = 1.sp
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
private fun LifeScreenPreview() = YearProgressTheme {
    LifeScreen()
}