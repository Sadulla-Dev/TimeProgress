package com.example.yearprogress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.yearprogress.R
import com.example.yearprogress.ui.theme.BG_DARK
import com.example.yearprogress.ui.theme.TEXT_PRIMARY

@Composable
fun LanguageDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf("en" to "English", "ru" to "Русский", "uz" to "O'zbek")

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(BG_DARK)
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF6366F1).copy(0.06f), Color.Transparent)
                        ), CircleShape
                    )
            )
            Column {
                Text(
                    stringResource(R.string.choose_language),
                    color = TEXT_PRIMARY
                )

                Spacer(Modifier.height(12.dp))

                languages.forEach { (code, name) ->
                    Text(
                        text = name,
                        color = TEXT_PRIMARY,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageSelected(code)
                                onDismiss()
                            }
                            .padding(14.dp)
                    )
                }
            }
        }
    }
}