package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.isSystemInDarkTheme
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
    ) {
        val scoreColor = closenessColor((score ?: 0) / 100f, darkTheme)
        Text(
            text = score?.toString() ?: "−",
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
