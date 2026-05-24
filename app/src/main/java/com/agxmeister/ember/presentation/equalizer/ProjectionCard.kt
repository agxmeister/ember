package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.appString

@Composable
internal fun ProjectionCard(
    projection: ProjectionResult,
    targetKg: Double,
    weightUnit: WeightUnit,
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                is ProjectionResult.Eta -> EtaContent(projection, targetKg, onSurface)
                ProjectionResult.Reached -> ReachedContent()
                ProjectionResult.Unavailable.NotEnoughData,
                ProjectionResult.Unavailable.WrongDirection,
                ProjectionResult.Unavailable.TooFar -> UnavailableContent(projection, onSurface)
            }
        }
    }
}

@Composable
private fun EtaContent(projection: ProjectionResult.Eta, targetKg: Double, onSurface: Color) {
    val accentGreen = Color(0xFF4BB543)
    val dateStr = "${projection.date.month.name.take(3)} ${projection.date.dayOfMonth}, ${projection.date.year}"
    val daysStr = if (projection.daysAway == 0) appString(R.string.trends_today)
                  else appString(R.string.trends_in_days, projection.daysAway)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = dateStr,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = accentGreen,
            ),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = daysStr,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = onSurface.copy(alpha = 0.50f),
            ),
            modifier = Modifier.padding(bottom = 3.dp),
        )
    }
    Spacer(Modifier.height(8.dp))
    EtaSparkline(startKg = projection.currentAvgKg, endKg = targetKg, color = accentGreen)
}

@Composable
private fun EtaSparkline(startKg: Double, endKg: Double, color: Color) {
    Canvas(modifier = Modifier.fillMaxWidth().height(32.dp)) {
        val padX = 4.dp.toPx()
        val yMin = minOf(startKg, endKg) - 0.3
        val yMax = maxOf(startKg, endKg) + 0.3
        val yRange = (yMax - yMin).coerceAtLeast(0.1)
        val startY = (size.height - ((startKg - yMin) / yRange * size.height)).toFloat()
        val endY = (size.height - ((endKg - yMin) / yRange * size.height)).toFloat()
        drawLine(
            color = color.copy(alpha = 0.55f),
            start = Offset(padX, startY),
            end = Offset(size.width - padX, endY),
            strokeWidth = 2.dp.toPx(),
        )
        drawCircle(color = color, radius = 3.5.dp.toPx(), center = Offset(padX, startY))
        drawCircle(color = color.copy(alpha = 0.45f), radius = 3.5.dp.toPx(), center = Offset(size.width - padX, endY))
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
private fun UnavailableContent(projection: ProjectionResult, onSurface: Color) {
    val reason = when (projection) {
        ProjectionResult.Unavailable.WrongDirection -> appString(R.string.trends_eta_wrong_direction)
        ProjectionResult.Unavailable.TooFar -> appString(R.string.trends_eta_too_far)
        else -> appString(R.string.trends_eta_not_enough_data)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "—",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = onSurface.copy(alpha = 0.30f),
            ),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = reason,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = onSurface.copy(alpha = 0.40f),
            ),
        )
    }
}
