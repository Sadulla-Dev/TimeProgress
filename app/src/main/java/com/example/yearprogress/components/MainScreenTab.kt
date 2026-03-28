package com.example.yearprogress.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.yearprogress.components.tabs.LifeScreen
import com.example.yearprogress.components.tabs.ProductivityScreen
import com.example.yearprogress.components.tabs.SettingsScreen
import com.example.yearprogress.components.tabs.TimeScreen
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.ui.theme.ThemeMode
import com.example.yearprogress.ui.theme.YearProgressTheme

enum class MainTab { TIME, LIFE, PRODUCTIVITY, SETTINGS }

@Composable
fun MainScreenTab(
    currentMode: ThemeMode,
    onChangeTheme: (ThemeMode) -> Unit,
    onChangeLanguage: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.TIME) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProgressColors.bgDark)
    ) {
        Box(
            modifier = Modifier
                .size(500.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-200).dp)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF6366F1).copy(0.06f), Color.Transparent)),
                    CircleShape
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(52.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiveChip()
                }

                Spacer(Modifier.height(16.dp))

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

                Spacer(Modifier.height(6.dp))

                Text(
                    stringResource(R.string.every_second_minute_hour_is_not_coming_back),
                    fontSize = 13.sp,
                    color = ProgressColors.textMuted
                )

                Spacer(Modifier.height(24.dp))
                AnimatedContent(
                    modifier = Modifier.fillMaxWidth(),
                    targetState = selectedTab,
                    transitionSpec = {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    },
                    label = "tab_animation"
                ) { tab ->

                    Column {
                        when (tab) {

                            MainTab.TIME -> {
                                TimeScreen()
                            }


                            MainTab.LIFE -> {
                                LifeScreen()
                            }

                            MainTab.PRODUCTIVITY -> {
                                ProductivityScreen()
                            }

                            MainTab.SETTINGS -> {
                                SettingsScreen(
                                    currentMode = currentMode,
                                    onChangeTheme = onChangeTheme,
                                    onChangeLanguage = onChangeLanguage,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            BottomTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        }
    }
}


@Composable
fun PulsingDot() {
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

@Composable
fun LiveChip() {
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

@Preview
@Composable
private fun TabScreenPreview() = YearProgressTheme {
    MainScreenTab(
        currentMode = ThemeMode.CUSTOM_DARK,
        onChangeTheme = {},
        onChangeLanguage = {}
    )
}