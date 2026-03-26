package com.example.yearprogress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.yearprogress.ui.theme.ProgressColors
import com.example.yearprogress.utils.safeClickable

@Composable
fun LanguageDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(
        "en" to "English",
        "ru" to "Русский",
        "uz" to "O'zbek"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ProgressColors.bgCard)
                .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(20.dp))
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF6366F1).copy(0.07f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.choose_language),
                    fontSize = 10.sp,
                    color = ProgressColors.textMuted,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Language / Язык / Til",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ProgressColors.textPrimary
                )

                Spacer(Modifier.height(20.dp))

                languages.forEachIndexed { index, (code, name) ->
                    LanguageItem(
                        name = name,
                        onClick = {
                            onLanguageSelected(code)
                            onDismiss()
                        }
                    )
                    if (index < languages.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ProgressColors.fieldBackground)
                        .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(12.dp))
                        .safeClickable { onDismiss() }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕ ${stringResource(R.string.close)}",
                        fontSize = 11.sp,
                        color = ProgressColors.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageItem(
    name: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ProgressColors.fieldBackground)
            .border(1.dp, ProgressColors.cardBorder, RoundedCornerShape(12.dp))
            .safeClickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(ProgressColors.colorLife.copy(alpha = 0.5f))
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ProgressColors.textPrimary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
