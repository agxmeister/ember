package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.common.InfoDialog
import com.agxmeister.ember.presentation.common.InfoIcon
import com.agxmeister.ember.presentation.common.LocalHelpIconsVisible
import com.agxmeister.ember.presentation.theme.InfoIconSize
import com.agxmeister.ember.presentation.theme.closenessColor
import com.agxmeister.ember.presentation.theme.trendSpeedColor
import kotlinx.datetime.LocalDate
import kotlin.math.abs

@Composable
internal fun Readout(
    modifier: Modifier = Modifier,
    displayWeight: Double?,
    label: String,
    weeklyRateKg: Double?,
    goalIsLoss: Boolean,
    trendPending: TrendPending?,
    displayColor: Color,
    weightUnit: WeightUnit,
    isFocused: Boolean,
    onTap: () -> Unit,
    days: List<EqualizerDayData>,
    targetKg: Double,
    tolerance: Double,
    today: LocalDate,
    selectedDate: LocalDate?,
    isWeekly: Boolean,
    trendLine: TrendLineData?,
    canScrollLeft: Boolean,
    canScrollRight: Boolean,
    todayColumnProgress: Float = 1f,
    onDayToggle: (LocalDate) -> Unit,
    onScroll: (Int) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        WeightDisplay(
            modifier = Modifier.fillMaxWidth(),
            displayWeight = displayWeight,
            label = label,
            weeklyRateKg = weeklyRateKg,
            goalIsLoss = goalIsLoss,
            trendPending = trendPending,
            displayColor = displayColor,
            weightUnit = weightUnit,
            isFocused = isFocused,
            isWeekly = isWeekly,
            onTap = onTap,
        )
        EqualizerCard(
            modifier = Modifier.fillMaxWidth().weight(1f),
            days = days,
            targetKg = targetKg,
            tolerance = tolerance,
            weightUnit = weightUnit,
            today = today,
            selectedDate = selectedDate,
            isWeekly = isWeekly,
            trendLine = trendLine,
            canScrollLeft = canScrollLeft,
            canScrollRight = canScrollRight,
            todayColumnProgress = todayColumnProgress,
            onDayToggle = onDayToggle,
            onScroll = onScroll,
        )
        ContextHint(selectedDate = selectedDate, isWeekly = isWeekly)
    }
}

@Composable
private fun WeightDisplay(
    modifier: Modifier = Modifier,
    displayWeight: Double?,
    label: String,
    weeklyRateKg: Double?,
    goalIsLoss: Boolean,
    trendPending: TrendPending?,
    displayColor: Color,
    weightUnit: WeightUnit,
    isFocused: Boolean,
    isWeekly: Boolean,
    onTap: () -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()
    val weightStr = displayWeight?.let { "%.1f".format(weightUnit.fromKg(it)) } ?: "−.−"
    val diffDisplay = weeklyRateKg?.let { weightUnit.scaleDiff(it) }
    val arrow = when {
        diffDisplay == null -> null
        diffDisplay > 0.005 -> "▲"
        diffDisplay < -0.005 -> "▼"
        else -> null
    }
    val deltaStr = when {
        diffDisplay == null -> ".−−"
        abs(diffDisplay) > 0.005 -> "%.2f".format(abs(diffDisplay)).let { if (abs(diffDisplay) < 1.0) it.removePrefix("0") else it }
        else -> ".00"
    }
    val trendColor = trendSpeedColor(weeklyRateKg, goalIsLoss, darkTheme)

    val onBg = MaterialTheme.colorScheme.onBackground
    val glow = Shadow(color = displayColor.copy(alpha = 0.65f), blurRadius = 22f)
    val dimColor = onBg.copy(alpha = 0.6f)
    val labelStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp,
        color = dimColor,
    )

    var showTrendInfo by remember { mutableStateOf(false) }
    if (showTrendInfo && trendPending != null) {
        InfoDialog(
            title = appString(R.string.trends_delta_target),
            text = trendPendingText(trendPending, R.string.trends_trend_pending_info),
            onDismiss = { showTrendInfo = false },
        )
    }

    var showAvgInfo by remember { mutableStateOf(false) }
    if (showAvgInfo) {
        InfoDialog(
            title = label,
            text = appString(if (isWeekly) R.string.trends_avg_info_weekly else R.string.trends_avg_info_daily),
            onDismiss = { showAvgInfo = false },
        )
    }

    var showTrendLabelInfo by remember { mutableStateOf(false) }
    if (showTrendLabelInfo) {
        InfoDialog(
            title = appString(R.string.trends_delta_target),
            text = appString(R.string.trends_trend_info),
            onDismiss = { showTrendLabelInfo = false },
        )
    }

    Column(
        modifier = modifier.then(if (isFocused) Modifier.clickable(onClick = onTap) else Modifier),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = labelStyle)
            InfoIcon(
                onClick = { showAvgInfo = true },
                helpKey = "trends_avg",
                modifier = Modifier.padding(start = 4.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(text = appString(R.string.trends_delta_target), style = labelStyle)
            if (trendPending != null) {
                PendingHelpBadge(
                    onClick = { showTrendInfo = true },
                    modifier = Modifier.padding(start = 4.dp),
                )
            } else {
                InfoIcon(
                    onClick = { showTrendLabelInfo = true },
                    helpKey = "trends_trend_label",
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.weight(1f)) {
                Text(
                    text = weightStr,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = displayColor,
                        shadow = glow,
                    ),
                )
                Text(
                    text = " ${weightUnit.label}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        color = displayColor.copy(alpha = 0.85f),
                        shadow = Shadow(color = displayColor.copy(alpha = 0.5f), blurRadius = 12f),
                    ),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                val trendGlow = Shadow(color = trendColor.copy(alpha = 0.65f), blurRadius = 22f)
                if (arrow != null) {
                    Text(
                        text = arrow,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            color = trendColor,
                            shadow = trendGlow,
                        ),
                        modifier = Modifier.padding(bottom = 6.dp, end = 2.dp),
                    )
                }
                Text(
                    text = deltaStr,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = trendColor,
                        shadow = trendGlow,
                    ),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = appString(R.string.trends_tap_to_edit),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = if (isFocused) onBg.copy(alpha = 0.50f) else Color.Transparent,
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Solid "?" badge marking a widget as pending due to missing data. Unlike [InfoIcon] it never
 * dims: pending is an ongoing state, not a one-time hint, so opening the dialog doesn't affect it.
 */
@Composable
private fun PendingHelpBadge(onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (!LocalHelpIconsVisible.current) return
    Box(
        modifier = modifier
            .size(InfoIconSize)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.HelpOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(InfoIconSize * 0.75f),
        )
    }
}

@Composable
private fun ContextHint(selectedDate: LocalDate?, isWeekly: Boolean) {
    Text(
        text = if (selectedDate != null) appString(R.string.trends_tap_again_to_clear)
               else appString(if (isWeekly) R.string.trends_tap_a_week else R.string.trends_tap_a_day),
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.50f),
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}
