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
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.InfoAccent

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
        color = onSurface.copy(alpha = 0.55f),
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
    var showPendingInfo by remember { mutableStateOf(false) }
    if (showPendingInfo && measurementsNeeded != null) {
        AlertDialog(
            onDismissRequest = { showPendingInfo = false },
            confirmButton = { TextButton(onClick = { showPendingInfo = false }) { Text(appString(R.string.label_ok)) } },
            title = { Text(appString(R.string.trends_projected_eta)) },
            text = { Text(appString(R.string.trends_eta_pending_info, measurementsNeeded)) },
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
                ProjectionResult.Unavailable.TooFar -> UnavailableContent(
                    projection = projection,
                    onSurface = onSurface,
                    measurementsNeeded = measurementsNeeded,
                    onPendingInfo = { showPendingInfo = true },
                )
            }
        }
    }
}

@Composable
private fun EtaContent(modifier: Modifier = Modifier, projection: ProjectionResult.Eta, onSurface: Color) {
    val percentNum = if (projection.progress != null) "%.1f".format(projection.progress * 100).trimStart('0') else ".-"
    val showPercent = projection.progress != null
    val bigStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = onSurface,
    )
    val smallStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        color = onSurface.copy(alpha = 0.75f),
    )
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                Text(text = percentNum, style = bigStyle)
                if (showPercent) {
                    Text(text = " %", style = smallStyle, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
            if (projection.daysAway == 0) {
                Text(
                    text = appString(R.string.trends_today),
                    style = smallStyle,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            } else {
                val fullStr = appString(R.string.trends_in_days, projection.daysAway)
                val parts = fullStr.split(projection.daysAway.toString())
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = parts.getOrElse(0) { "" }, style = smallStyle, modifier = Modifier.padding(bottom = 4.dp))
                    Text(text = projection.daysAway.toString(), style = bigStyle)
                    Text(text = parts.getOrElse(1) { "" }, style = smallStyle, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        EtaJourney(
            progress = projection.progress ?: 0f,
            onSurface = onSurface,
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
private fun EtaJourney(progress: Float, onSurface: Color, modifier: Modifier = Modifier) {
    val colorStart = Color(0xFFE53935)
    val colorEnd = Color(0xFF4BB543)
    val accent = lerpColor(colorStart, colorEnd, progress)
    val figurePainter = painterResource(R.drawable.ic_person_figure)
    Canvas(modifier = modifier) {
        val padH = 14.dp.toPx()
        val trackY = size.height - 12.dp.toPx()
        val trackStart = padH
        val trackEnd = size.width - padH
        val cursorX = trackStart + (trackEnd - trackStart) * progress.coerceIn(0.02f, 0.98f)

        // Remaining track
        drawLine(
            color = onSurface.copy(alpha = 0.16f),
            start = Offset(cursorX, trackY),
            end = Offset(trackEnd, trackY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
        // Done track
        drawLine(
            color = accent.copy(alpha = 0.55f),
            start = Offset(trackStart, trackY),
            end = Offset(cursorX, trackY),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        // Start cap
        drawCircle(color = colorStart.copy(alpha = 0.50f), radius = 3.5.dp.toPx(), center = Offset(trackStart, trackY))
        // Goal cap
        drawCircle(color = colorEnd.copy(alpha = 0.50f), radius = 3.5.dp.toPx(), center = Offset(trackEnd, trackY))

        // Cursor arrow (upward-pointing triangle on the track line)
        val arrowHalf = 5.dp.toPx()
        val arrowHeight = 8.dp.toPx()
        val arrowPath = Path().apply {
            moveTo(cursorX, trackY)
            lineTo(cursorX - arrowHalf, trackY + arrowHeight)
            lineTo(cursorX + arrowHalf, trackY + arrowHeight)
            close()
        }
        drawPath(arrowPath, color = accent)

        // Silhouette figure above the cursor
        val figBottom = trackY - 7.dp.toPx()
        val figH = figBottom * (2f / 3f)
        val figW = figH * (100f / 180f)
        translate(cursorX - figW / 2f, figBottom - figH) {
            with(figurePainter) {
                draw(Size(figW, figH), colorFilter = ColorFilter.tint(accent))
            }
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
private fun UnavailableContent(
    projection: ProjectionResult,
    onSurface: Color,
    measurementsNeeded: Int?,
    onPendingInfo: () -> Unit,
) {
    val isPending = projection == ProjectionResult.Unavailable.NotEnoughData && measurementsNeeded != null
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
        if (isPending) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(22.dp).clickable { onPendingInfo() },
                tint = InfoAccent,
            )
        } else {
            val secondary = when (projection) {
                ProjectionResult.Unavailable.WrongDirection -> appString(R.string.trends_eta_wrong_direction)
                ProjectionResult.Unavailable.TooFar -> appString(R.string.trends_eta_too_far)
                else -> appString(R.string.trends_eta_not_enough_data)
            }
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
}
