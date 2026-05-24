package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.closenessColor
import kotlin.math.abs

@Composable
internal fun StatsRow(
    streak: Int,
    weeklyAvg: Double?,
    targetKg: Double,
    tolerance: Double,
    score: Int?,
    weightUnit: WeightUnit,
    isWeekly: Boolean,
) {
    val darkTheme = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatPill(
            modifier = Modifier.weight(1f),
            label = appString(R.string.trends_streak),
            info = appString(if (isWeekly) R.string.trends_streak_info_weekly else R.string.trends_streak_info_daily),
        ) {
            val onSurface = MaterialTheme.colorScheme.onSurface
            val streakColor = if (streak >= 5) Color(0xFF4BB543) else onSurface
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

        StatPill(modifier = Modifier.weight(1f), label = appString(R.string.stat_delta_target)) {
            val deltaDisplay = weeklyAvg?.let { abs(weightUnit.scaleDiff(it - targetKg)) }
            val deltaColor = weeklyAvg?.let { w ->
                closenessColor((1.0 - abs(w - targetKg) / tolerance).coerceIn(0.0, 1.0).toFloat(), darkTheme)
            } ?: MaterialTheme.colorScheme.onSurface
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = deltaDisplay?.let { "%.1f".format(it) } ?: "−",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = deltaColor,
                    ),
                )
                if (deltaDisplay != null) {
                    Text(
                        text = " ${weightUnit.label}",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = deltaColor.copy(alpha = 0.75f),
                        ),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
        }

        StatPill(
            modifier = Modifier.weight(1f),
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
}

@Composable
internal fun StatPill(
    modifier: Modifier = Modifier,
    label: String,
    info: String? = null,
    content: @Composable () -> Unit,
) {
    var showInfo by remember { mutableStateOf(false) }
    if (showInfo && info != null) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) {
                    Text(appString(R.string.label_ok))
                }
            },
            title = { Text(label) },
            text = { Text(info) },
        )
    }
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    ),
                )
                if (info != null) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier
                            .size(12.dp)
                            .clickable { showInfo = true },
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}
