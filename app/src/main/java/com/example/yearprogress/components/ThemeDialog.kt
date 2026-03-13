package com.example.yearprogress.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yearprogress.R
import com.example.yearprogress.ui.theme.AppTheme
import com.example.yearprogress.ui.theme.BG_CARD
import com.example.yearprogress.ui.theme.BG_DARK
import com.example.yearprogress.ui.theme.CARD_BORDER
import com.example.yearprogress.ui.theme.COLOR_LIFE
import com.example.yearprogress.ui.theme.TEXT_DIM
import com.example.yearprogress.ui.theme.TEXT_MUTED
import com.example.yearprogress.ui.theme.TEXT_PRIMARY
import com.example.yearprogress.ui.theme.ThemeMode

@Composable
fun ThemeDialog(
    currentMode: ThemeMode,
    onDismiss: () -> Unit,
    onModeSelected: (ThemeMode) -> Unit
) {
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.bgCard)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
        ) {
            Text(
                stringResource(R.string.theme),
                fontSize = 10.sp,
                color = colors.textMuted,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(16.dp))

            val options = listOf(
                ThemeMode.CUSTOM_DARK  to stringResource(R.string.dark),
                ThemeMode.SYSTEM_LIGHT to stringResource(R.string.light),
            )

            options.forEach { (mode, label) ->
                val isSelected = currentMode == mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) colors.colorLife.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                        .border(
                            1.dp,
                            if (isSelected) colors.colorLife.copy(alpha = 0.3f)
                            else colors.cardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onModeSelected(mode); onDismiss() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Mode icon
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when (mode) {
                                        ThemeMode.CUSTOM_DARK  -> Color(0xFF0A0A0F)
                                        ThemeMode.SYSTEM_LIGHT -> Color(0xFFF8F8FC)
                                    }
                                )
                                .border(1.dp, colors.cardBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (mode) {
                                            ThemeMode.CUSTOM_DARK  -> Color(0xFF818CF8)
                                            ThemeMode.SYSTEM_LIGHT -> Color(0xFF6366F1)
                                        }
                                    )
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            label,
                            fontSize = 14.sp,
                            color = if (isSelected) colors.textPrimary else colors.textMuted,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.colorLife)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(4.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.close),
                    fontSize = 11.sp,
                    color = colors.textMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}