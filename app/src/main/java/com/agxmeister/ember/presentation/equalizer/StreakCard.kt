package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.appString

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
    ) {
        val streakColor = if (streak >= 5) Color(0xFF4BB543) else MaterialTheme.colorScheme.onSurface
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = streak.toString(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = streakColor,
                ),
            )
            Text(
                text = " ${appString(if (isWeekly) R.string.trends_wks else R.string.trends_days)}",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = streakColor.copy(alpha = 0.75f),
                ),
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
    }
}
