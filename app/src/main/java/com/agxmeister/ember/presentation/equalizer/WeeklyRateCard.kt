package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.common.InfoDialog
import com.agxmeister.ember.presentation.theme.DangerRed
import com.agxmeister.ember.presentation.theme.SuccessGreen
import com.agxmeister.ember.presentation.theme.WarningAmber
import kotlin.math.abs

@Composable
internal fun WeeklyRateCard(
    modifier: Modifier = Modifier,
    weeklyRateKg: Double?,
    rateZone: WeeklyRateZone,
    measurementsNeeded: Int? = null,
) {
    var showInfo by remember { mutableStateOf(false) }
    if (showInfo) {
        InfoDialog(
            title = appString(R.string.trends_weekly_rate),
            text = appString(R.string.trends_weekly_rate_info),
            onDismiss = { showInfo = false },
        )
    }

    var showPendingInfo by remember { mutableStateOf(false) }
    if (showPendingInfo) {
        InfoDialog(
            title = appString(R.string.trends_weekly_rate),
            text = appString(R.string.trends_weekly_rate_pending_info, measurementsNeeded ?: 1),
            onDismiss = { showPendingInfo = false },
        )
    }

    val zoneBadge = when (rateZone) {
        WeeklyRateZone.TooSlow -> appString(R.string.trends_rate_zone_too_slow)
        WeeklyRateZone.Healthy -> appString(R.string.trends_rate_zone_healthy)
        WeeklyRateZone.Aggressive -> appString(R.string.trends_rate_zone_aggressive)
        WeeklyRateZone.TooFast -> appString(R.string.trends_rate_zone_too_fast)
        WeeklyRateZone.WrongDirection -> appString(R.string.trends_rate_zone_wrong_direction)
        WeeklyRateZone.Unavailable -> ""
    }
    StatCardSurface(modifier = modifier) {
        val pending = weeklyRateKg == null
        CardLabelRow(
            label = appString(R.string.trends_weekly_rate),
            onInfo = { showInfo = true },
            helpKey = "trends_weekly_rate",
            onPending = if (pending) ({ showPendingInfo = true }) else null,
            pendingHelpKey = if (pending) "trends_weekly_rate_pending" else null,
        )
        Spacer(Modifier.weight(1f))
        RateZoneBar(
            absRateKg = weeklyRateKg?.let { abs(it) },
            zoneBadge = zoneBadge,
        )
    }
}

@Composable
private fun RateZoneBar(absRateKg: Double?, zoneBadge: String) {
    val barRange = 2.0
    val markerFraction = absRateKg?.let { (it / barRange).toFloat().coerceIn(0.02f, 0.98f) }
    val onSurface = MaterialTheme.colorScheme.onSurface
    val dimColor = onSurface.copy(alpha = 0.15f)
    val tooSlowFrac = (0.25 / barRange).toFloat()
    val healthyFrac = (1.0 / barRange).toFloat()
    val aggressiveFrac = (1.5 / barRange).toFloat()
    val badgeColor = when {
        markerFraction == null || markerFraction < tooSlowFrac -> onSurface.copy(alpha = 0.6f)
        markerFraction < healthyFrac -> SuccessGreen
        markerFraction < aggressiveFrac -> WarningAmber
        else -> DangerRed
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

        val tooSlowX    = trackStart + trackWidth * tooSlowFrac
        val healthyX    = trackStart + trackWidth * healthyFrac
        val aggressiveX = trackStart + trackWidth * aggressiveFrac

        drawRect(dimColor,                          topLeft = Offset(trackStart,   barY), size = Size(tooSlowX - trackStart,    barH))
        drawRect(SuccessGreen.copy(alpha = 0.35f),  topLeft = Offset(tooSlowX,    barY), size = Size(healthyX - tooSlowX,      barH))
        drawRect(WarningAmber.copy(alpha = 0.35f),  topLeft = Offset(healthyX,    barY), size = Size(aggressiveX - healthyX,   barH))
        drawRect(DangerRed.copy(alpha = 0.35f),     topLeft = Offset(aggressiveX, barY), size = Size(trackEnd - aggressiveX,   barH))

        val dividerColor = onSurface.copy(alpha = 0.50f)
        val dividerW = 1.5.dp.toPx()
        listOf(tooSlowX, healthyX, aggressiveX).forEach { x ->
            drawLine(dividerColor, start = Offset(x, barY), end = Offset(x, barY + barH), strokeWidth = dividerW)
        }

        // Cursor and zone badge appear once there's a rate to place on the scale.
        if (markerFraction != null) {
            val cursorX = trackStart + trackWidth * markerFraction
            drawCursorArrow(cursorX, trackY, badgeColor)

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
}
