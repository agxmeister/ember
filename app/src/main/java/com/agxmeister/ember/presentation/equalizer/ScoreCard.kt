package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.closenessColor

@Composable
internal fun ScoreCard(
    modifier: Modifier = Modifier,
    score: Int?,
    isWeekly: Boolean,
) {
    val darkTheme = isSystemInDarkTheme()
    StatCard(
        modifier = modifier,
        label = appString(R.string.trends_score),
        info = appString(if (isWeekly) R.string.trends_score_info_weekly else R.string.trends_score_info_daily),
        helpKey = "trends_score",
        pendingInfo = if (score == null) appString(R.string.trends_score_pending_info) else null,
    ) {
        val scoreColor = if (score != null) closenessColor(score / 100f, darkTheme) else MaterialTheme.colorScheme.onSurface
        Text(
            text = score?.toString() ?: "--",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor,
                shadow = Shadow(color = scoreColor.copy(alpha = 0.5f), blurRadius = 10f),
            ),
        )
    }
}
