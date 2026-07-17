package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.closenessColor
import com.agxmeister.ember.presentation.theme.pendingPlaceholderColor

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
        val scoreColor = if (score != null) closenessColor(score / 100f, darkTheme) else pendingPlaceholderColor()
        Text(
            text = score?.toString() ?: "--",
            style = statValueStyle(scoreColor).copy(
                shadow = Shadow(color = scoreColor.copy(alpha = 0.5f), blurRadius = 10f),
            ),
        )
    }
}
