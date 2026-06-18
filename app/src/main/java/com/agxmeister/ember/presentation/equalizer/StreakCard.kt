package com.agxmeister.ember.presentation.equalizer

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.SuccessGreen

@Composable
internal fun StreakCard(
    modifier: Modifier = Modifier,
    streak: Int,
    isWeekly: Boolean,
) {
    StatCard(
        modifier = modifier,
        label = appString(R.string.trends_streak),
        info = appString(if (isWeekly) R.string.trends_streak_info_weekly else R.string.trends_streak_info_daily),
        helpKey = "trends_streak",
    ) {
        val streakColor = if (streak >= 5) SuccessGreen else MaterialTheme.colorScheme.onSurface
        StatValueRow(
            value = streak.toString(),
            unit = appString(if (isWeekly) R.string.trends_wks else R.string.trends_days),
            color = streakColor,
        )
    }
}
