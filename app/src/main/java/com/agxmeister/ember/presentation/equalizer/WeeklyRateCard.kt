package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.trendSpeedColor
import kotlin.math.abs

@Composable
internal fun WeeklyRateCard(
    modifier: Modifier = Modifier,
    weeklyRateKg: Double?,
    rateZone: WeeklyRateZone,
    goalIsLoss: Boolean,
    weightUnit: WeightUnit,
) {
    val darkTheme = isSystemInDarkTheme()
    val onSurface = MaterialTheme.colorScheme.onSurface
    val labelStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp,
        letterSpacing = 0.8.sp,
        color = onSurface.copy(alpha = 0.35f),
    )
    var showInfo by remember { mutableStateOf(false) }
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text(appString(R.string.label_ok)) } },
            title = { Text(appString(R.string.trends_weekly_rate)) },
            text = { Text(appString(R.string.trends_weekly_rate_info)) },
        )
    }

    val rateColor = if (weeklyRateKg != null) trendSpeedColor(weeklyRateKg, goalIsLoss, darkTheme)
                   else onSurface.copy(alpha = 0.30f)
    val rateDisplay = weeklyRateKg?.let { weightUnit.scaleDiff(it) }
    val rateStr = rateDisplay?.let { "%.2f ${weightUnit.label}/wk".format(it) } ?: "—"
    val zoneBadge = when (rateZone) {
        WeeklyRateZone.TooSlow -> appString(R.string.trends_rate_zone_too_slow)
        WeeklyRateZone.Healthy -> appString(R.string.trends_rate_zone_healthy)
        WeeklyRateZone.Aggressive -> appString(R.string.trends_rate_zone_aggressive)
        WeeklyRateZone.TooFast -> appString(R.string.trends_rate_zone_too_fast)
        WeeklyRateZone.WrongDirection -> appString(R.string.trends_rate_zone_wrong_direction)
        WeeklyRateZone.Unavailable -> ""
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = appString(R.string.trends_weekly_rate),
                    style = labelStyle,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp).clickable { showInfo = true },
                    tint = onSurface.copy(alpha = 0.35f),
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = rateStr,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = rateColor,
                    ),
                    modifier = Modifier.weight(1f),
                )
                if (zoneBadge.isNotEmpty()) {
                    Text(
                        text = zoneBadge,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            color = rateColor.copy(alpha = 0.70f),
                        ),
                        modifier = Modifier.padding(bottom = 3.dp),
                    )
                }
            }
            if (weeklyRateKg != null) {
                Spacer(Modifier.height(8.dp))
                RateZoneBar(absRateKg = abs(weeklyRateKg), rateZone = rateZone)
            }
        }
    }
}

@Composable
private fun RateZoneBar(absRateKg: Double, rateZone: WeeklyRateZone) {
    val barRange = 2.0
    val markerFraction = (absRateKg / barRange).toFloat().coerceIn(0.02f, 0.98f)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val dimColor = onSurface.copy(alpha = 0.15f)
    val greenColor = Color(0xFF4BB543)
    val amberColor = Color(0xFFFFC107)
    val redColor = Color(0xFFE53935)
    val markerColor = when (rateZone) {
        WeeklyRateZone.Healthy -> greenColor
        WeeklyRateZone.TooSlow -> onSurface.copy(alpha = 0.55f)
        WeeklyRateZone.Aggressive -> amberColor
        WeeklyRateZone.TooFast, WeeklyRateZone.WrongDirection -> redColor
        WeeklyRateZone.Unavailable -> dimColor
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
        val w = size.width
        val h = size.height
        val barH = h * 0.40f
        val barY = (h - barH) / 2f

        val tooSlowFrac = (0.25 / barRange).toFloat()
        val healthyFrac = (1.0 / barRange).toFloat()
        val aggressiveFrac = (1.5 / barRange).toFloat()

        drawRect(dimColor, topLeft = Offset(0f, barY), size = Size(w * tooSlowFrac, barH))
        drawRect(greenColor.copy(alpha = 0.35f), topLeft = Offset(w * tooSlowFrac, barY), size = Size(w * (healthyFrac - tooSlowFrac), barH))
        drawRect(amberColor.copy(alpha = 0.35f), topLeft = Offset(w * healthyFrac, barY), size = Size(w * (aggressiveFrac - healthyFrac), barH))
        drawRect(redColor.copy(alpha = 0.35f), topLeft = Offset(w * aggressiveFrac, barY), size = Size(w * (1f - aggressiveFrac), barH))

        drawCircle(color = markerColor, radius = h / 2f, center = Offset(w * markerFraction, h / 2f))
    }
}
