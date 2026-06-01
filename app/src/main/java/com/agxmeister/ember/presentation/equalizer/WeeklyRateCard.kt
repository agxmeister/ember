package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.appString
import kotlin.math.abs

@Composable
internal fun WeeklyRateCard(
    modifier: Modifier = Modifier,
    weeklyRateKg: Double?,
    rateZone: WeeklyRateZone,
) {
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

    val greenColor = Color(0xFF4BB543)
    val amberColor = Color(0xFFFFC107)
    val redColor = Color(0xFFE53935)
    val markerColor = when (rateZone) {
        WeeklyRateZone.Healthy -> greenColor
        WeeklyRateZone.TooSlow -> onSurface.copy(alpha = 0.55f)
        WeeklyRateZone.Aggressive -> amberColor
        WeeklyRateZone.TooFast, WeeklyRateZone.WrongDirection -> redColor
        WeeklyRateZone.Unavailable -> onSurface.copy(alpha = 0.15f)
    }
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
        Column(modifier = Modifier.padding(12.dp).fillMaxHeight()) {
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
            Spacer(Modifier.weight(1f))
            if (weeklyRateKg != null) {
                RateZoneBar(
                    absRateKg = abs(weeklyRateKg),
                    rateZone = rateZone,
                    markerColor = markerColor,
                    zoneBadge = zoneBadge,
                )
            }
        }
    }
}

@Composable
private fun RateZoneBar(absRateKg: Double, rateZone: WeeklyRateZone, markerColor: Color, zoneBadge: String) {
    val barRange = 2.0
    val markerFraction = (absRateKg / barRange).toFloat().coerceIn(0.02f, 0.98f)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val dimColor = onSurface.copy(alpha = 0.15f)
    val greenColor = Color(0xFF4BB543)
    val amberColor = Color(0xFFFFC107)
    val redColor = Color(0xFFE53935)
    val tooSlowFrac = (0.25 / barRange).toFloat()
    val healthyFrac = (1.0 / barRange).toFloat()
    val aggressiveFrac = (1.5 / barRange).toFloat()
    val badgeColor = when {
        markerFraction < tooSlowFrac -> dimColor
        markerFraction < healthyFrac -> greenColor
        markerFraction < aggressiveFrac -> amberColor
        else -> redColor
    }
    val textMeasurer = rememberTextMeasurer()
    val badgeStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp,
        letterSpacing = 0.8.sp,
        color = badgeColor,
    )

    Canvas(modifier = Modifier.fillMaxWidth().height(34.dp)) {
        val padH = 14.dp.toPx()
        val trackStart = padH
        val trackEnd = size.width - padH
        val trackWidth = trackEnd - trackStart
        val trackY = size.height * 0.65f
        val barH = 3.dp.toPx()
        val barY = trackY - barH / 2f

        val tooSlowFrac = (0.25 / barRange).toFloat()
        val healthyFrac = (1.0 / barRange).toFloat()
        val aggressiveFrac = (1.5 / barRange).toFloat()

        val tooSlowX    = trackStart + trackWidth * tooSlowFrac
        val healthyX    = trackStart + trackWidth * healthyFrac
        val aggressiveX = trackStart + trackWidth * aggressiveFrac

        drawRect(dimColor,                       topLeft = Offset(trackStart,   barY), size = Size(tooSlowX - trackStart,    barH))
        drawRect(greenColor.copy(alpha = 0.35f), topLeft = Offset(tooSlowX,    barY), size = Size(healthyX - tooSlowX,      barH))
        drawRect(amberColor.copy(alpha = 0.35f), topLeft = Offset(healthyX,    barY), size = Size(aggressiveX - healthyX,   barH))
        drawRect(redColor.copy(alpha = 0.35f),   topLeft = Offset(aggressiveX, barY), size = Size(trackEnd - aggressiveX,   barH))

        val dividerColor = onSurface.copy(alpha = 0.50f)
        val dividerW = 1.5.dp.toPx()
        listOf(tooSlowX, healthyX, aggressiveX).forEach { x ->
            drawLine(dividerColor, start = Offset(x, barY), end = Offset(x, barY + barH), strokeWidth = dividerW)
        }

        val cursorX = trackStart + trackWidth * markerFraction
        val arrowHalf = 5.dp.toPx()
        val arrowHeight = 8.dp.toPx()
        val arrowPath = Path().apply {
            moveTo(cursorX, trackY)
            lineTo(cursorX - arrowHalf, trackY + arrowHeight)
            lineTo(cursorX + arrowHalf, trackY + arrowHeight)
            close()
        }
        drawPath(arrowPath, color = badgeColor)

        if (zoneBadge.isNotEmpty()) {
            val measured = textMeasurer.measure(zoneBadge, badgeStyle)
            val textW = measured.size.width.toFloat()
            val textH = measured.size.height.toFloat()
            val gap = 7.dp.toPx()
            val textY = trackY - textH - gap
            val rawX = cursorX - textW / 2f
            val textX = rawX.coerceIn(trackStart, trackEnd - textW)
            drawText(measured, topLeft = Offset(textX, textY))
        }
    }
}
