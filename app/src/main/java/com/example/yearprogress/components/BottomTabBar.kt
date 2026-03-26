package com.example.yearprogress.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yearprogress.R
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.utils.safeClickable

@Composable
fun BottomTabBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
) {
    val tabAccent = mapOf(
        MainTab.TIME to ProgressColors.colorYear,
        MainTab.LIFE to ProgressColors.colorLife,
        MainTab.SETTINGS to ProgressColors.colorWeek,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .border(
                width = 1.dp,
                color = ProgressColors.cardBorder,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val accent = tabAccent[tab] ?: ProgressColors.colorYear

                val label = when (tab) {
                    MainTab.TIME -> stringResource(R.string.tab_time)
                    MainTab.LIFE -> stringResource(R.string.tab_life)
                    MainTab.SETTINGS -> stringResource(R.string.tab_settings)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) accent.copy(alpha = 0.10f) else Color.Transparent
                        )
                        .safeClickable { onTabSelected(tab) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) accent else ProgressColors.textMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .width(if (isSelected) 20.dp else 0.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (isSelected) accent else Color.Transparent)
                    )
                }
            }
        }
    }
}
