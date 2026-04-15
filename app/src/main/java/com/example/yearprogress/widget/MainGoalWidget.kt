package com.example.yearprogress.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.yearprogress.MainActivity
import com.example.yearprogress.R
import com.example.yearprogress.utils.GoalCountdown
import com.example.yearprogress.utils.LanguageManager
import com.example.yearprogress.utils.PreferenceManager
import com.example.yearprogress.utils.progress
import com.example.yearprogress.utils.remainingLabel
import com.example.yearprogress.utils.resolvePinnedGoal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MainGoalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = MainGoalWidget

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (
            intent.action == Intent.ACTION_TIME_TICK ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val manager = GlanceAppWidgetManager(context)
            CoroutineScope(Dispatchers.IO).launch {
                manager.getGlanceIds(MainGoalWidget::class.java).forEach { id ->
                    MainGoalWidget.update(context, id)
                }
            }
        }
    }
}

object MainGoalWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(60.dp, 60.dp),
            DpSize(250.dp, 90.dp),
        )
    )

    override suspend fun provideGlance(context: Context, id: androidx.glance.GlanceId) {
        val preferenceManager = PreferenceManager(context.applicationContext)
        val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val language = sharedPrefs.getString("language", "en") ?: "en"
        val localizedContext = LanguageManager.changeLanguage(context, language)
        val pinnedGoal = resolvePinnedGoal(
            preferenceManager.getGoals(),
            preferenceManager.getPinnedGoalId()
        )

        provideContent {
            MainGoalWidgetContent(localizedContext, pinnedGoal)
        }
    }
}

@Composable
private fun MainGoalWidgetContent(context: Context, goal: GoalCountdown?) {
    val size = LocalSize.current
    if (goal == null) {
        MainGoalEmptyWidget(context)
    } else if (size.width >= 200.dp) {
        MainGoalWideWidget(context, goal)
    } else {
        MainGoalSmallWidget(context, goal)
    }
}

@Composable
private fun MainGoalWideWidget(context: Context, goal: GoalCountdown) {
    val colors = GlanceTheme.colors
    val progress = goal.progress()
    val percentText = String.format(Locale.US, "%.1f%%", progress * 100)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.widgetBackground)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Text(
            text = context.getString(R.string.main_goal),
            style = TextStyle(
                color = colors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = goal.name,
            style = TextStyle(
                color = colors.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.defaultWeight())
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = percentText,
                style = TextStyle(
                    color = colors.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.defaultWeight())
            Text(
                text = goal.remainingLabel(context),
                style = TextStyle(color = colors.onSurface, fontSize = 11.sp)
            )
        }
        Spacer(GlanceModifier.height(6.dp))
        LinearProgressIndicator(
            modifier = GlanceModifier.fillMaxWidth().height(5.dp),
            progress = progress.toFloat(),
            backgroundColor = colors.outline,
            color = colors.primary
        )
    }
}

@Composable
private fun MainGoalSmallWidget(context: Context, goal: GoalCountdown) {
    val colors = GlanceTheme.colors
    val bitmap = createCircleProgressBitmap(
        size = 300,
        progress = goal.progress(),
        label = context.getString(R.string.main_goal_widget_short),
        mainColor = colors.primary.getColor(context).toArgb(),
        secondaryColor = colors.outline.getColor(context).toArgb()
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.widgetBackground)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize()
        )
    }
}

@Composable
private fun MainGoalEmptyWidget(context: Context) {
    val colors = GlanceTheme.colors

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.widgetBackground)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Text(
            text = context.getString(R.string.main_goal),
            style = TextStyle(
                color = colors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(GlanceModifier.height(8.dp))
        Text(
            text = context.getString(R.string.main_goal_widget_empty),
            style = TextStyle(color = colors.onSurface, fontSize = 13.sp)
        )
    }
}
