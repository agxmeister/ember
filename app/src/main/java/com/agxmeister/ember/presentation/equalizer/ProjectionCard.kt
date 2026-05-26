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
import androidx.compose.foundation.layout.width
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
import kotlin.math.roundToInt
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.appString

@Composable
internal fun ProjectionCard(
    modifier: Modifier = Modifier,
    projection: ProjectionResult,
    targetKg: Double,
    weightUnit: WeightUnit,
    measurementsNeeded: Int? = null,
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
            title = { Text(appString(R.string.trends_projected_eta)) },
            text = { Text(appString(R.string.trends_projected_eta_info)) },
        )
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = appString(R.string.trends_projected_eta),
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
            when (projection) {
                is ProjectionResult.Eta -> EtaContent(Modifier.weight(1f), projection, onSurface)
                ProjectionResult.Reached -> ReachedContent()
                ProjectionResult.Unavailable.NotEnoughData,
                ProjectionResult.Unavailable.WrongDirection,
                ProjectionResult.Unavailable.TooFar -> UnavailableContent(projection, onSurface, measurementsNeeded)
            }
        }
    }
}

@Composable
private fun EtaContent(modifier: Modifier = Modifier, projection: ProjectionResult.Eta, onSurface: Color) {
    val daysStr = if (projection.daysAway == 0) appString(R.string.trends_today)
                  else appString(R.string.trends_in_days, projection.daysAway)
    val percentStr = if (projection.progress != null) "${"%.1f".format(projection.progress * 100).trimStart('0')}%" else ".-"
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = percentStr,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface,
                ),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = daysStr,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface,
                ),
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        EtaLadder(
            progress = projection.progress ?: 0f,
            goalIsLoss = projection.goalIsLoss,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}

private fun lerpColor(a: Color, b: Color, t: Float): Color {
    val s = t.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * s,
        green = a.green + (b.green - a.green) * s,
        blue = a.blue + (b.blue - a.blue) * s,
        alpha = 1f,
    )
}

@Composable
private fun EtaLadder(progress: Float, goalIsLoss: Boolean, modifier: Modifier = Modifier) {
    val colorRed = Color(0xFFE53935)
    val colorGreen = Color(0xFF4BB543)
    Canvas(modifier = modifier) {
        val barCount = 30
        val gap = 6.dp.toPx()
        val totalGaps = gap * (barCount - 1)
        val barWidth = (size.width - totalGaps) / barCount
        val filledCount = (progress * barCount).roundToInt()
        val maxH = size.height
        val minH = size.height * 0.30f
        for (i in 0 until barCount) {
            val x = i * (barWidth + gap)
            val t = if (barCount > 1) i.toFloat() / (barCount - 1) else 0f
            val barH = if (goalIsLoss) minH + (maxH - minH) * (1f - t) else minH + (maxH - minH) * t
            val top = size.height - barH
            val barColor = lerpColor(colorRed, colorGreen, t).copy(alpha = if (i < filledCount) 1f else 0.15f)
            drawRect(
                color = barColor,
                topLeft = Offset(x, top),
                size = Size(barWidth, barH),
            )
        }
    }
}

@Composable
private fun ReachedContent() {
    Text(
        text = appString(R.string.trends_goal_reached),
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4BB543),
        ),
    )
}

@Composable
private fun UnavailableContent(projection: ProjectionResult, onSurface: Color, measurementsNeeded: Int?) {
    val secondary = if (projection == ProjectionResult.Unavailable.NotEnoughData && measurementsNeeded != null) {
        "$measurementsNeeded TO GO"
    } else {
        when (projection) {
            ProjectionResult.Unavailable.WrongDirection -> appString(R.string.trends_eta_wrong_direction)
            ProjectionResult.Unavailable.TooFar -> appString(R.string.trends_eta_too_far)
            else -> appString(R.string.trends_eta_not_enough_data)
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = ".-",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = onSurface.copy(alpha = 0.30f),
            ),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = secondary,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = onSurface.copy(alpha = 0.40f),
            ),
        )
    }
}
