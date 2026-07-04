package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.agxmeister.ember.presentation.common.InfoDialog
import com.agxmeister.ember.presentation.theme.DangerRed
import com.agxmeister.ember.presentation.theme.SuccessGreen

@Composable
internal fun ProjectionCard(
    modifier: Modifier = Modifier,
    projection: ProjectionResult,
    targetKg: Double,
    weightUnit: WeightUnit,
    trendPending: TrendPending? = null,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    var showInfo by remember { mutableStateOf(false) }
    if (showInfo) {
        InfoDialog(
            title = appString(R.string.trends_projected_eta),
            text = appString(R.string.trends_projected_eta_info),
            onDismiss = { showInfo = false },
        )
    }
    val pendingOverlayText = if (projection == ProjectionResult.Unavailable.NotEnoughData && trendPending != null) {
        trendPendingText(trendPending, R.string.trends_eta_pending_info)
    } else null

    StatCardSurface(modifier = modifier, pendingOverlayText = pendingOverlayText) {
        CardLabelRow(
            label = appString(R.string.trends_projected_eta),
            onInfo = if (pendingOverlayText == null) ({ showInfo = true }) else null,
            helpKey = if (pendingOverlayText == null) "trends_projected_eta" else null,
        )
        Spacer(Modifier.height(6.dp))
        when (projection) {
            is ProjectionResult.Eta -> EtaContent(Modifier.weight(1f), projection, onSurface)
            ProjectionResult.Reached -> ReachedContent()
            ProjectionResult.Unavailable.NotEnoughData -> if (trendPending != null) {
                PendingContent(Modifier.weight(1f), onSurface)
            } else {
                UnavailableContent(projection, onSurface)
            }
            ProjectionResult.Unavailable.WrongDirection,
            ProjectionResult.Unavailable.TooFar -> UnavailableContent(projection, onSurface)
        }
    }
}

@Composable
private fun EtaContent(modifier: Modifier = Modifier, projection: ProjectionResult.Eta, onSurface: Color) {
    val percentNum = if (projection.progress != null) "%.1f".format(projection.progress * 100).trimStart('0') else ".-"
    val showPercent = projection.progress != null
    val bigStyle = statValueStyle(onSurface)
    val smallStyle = statUnitStyle(onSurface)
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
private fun EtaJourney(progress: Float?, onSurface: Color, modifier: Modifier = Modifier) {
    val colorStart = DangerRed
    val colorEnd = SuccessGreen
    val figurePainter = painterResource(R.drawable.ic_person_figure)
    Canvas(modifier = modifier) {
        val padH = 14.dp.toPx()
        val trackY = size.height - 12.dp.toPx()
        val trackStart = padH
        val trackEnd = size.width - padH

        if (progress == null) {
            // Empty scale: full neutral track plus start/goal caps. The cursor and
            // figure appear once there's enough data to place them.
            drawLine(
                color = onSurface.copy(alpha = 0.16f),
                start = Offset(trackStart, trackY),
                end = Offset(trackEnd, trackY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(color = colorStart.copy(alpha = 0.50f), radius = 3.5.dp.toPx(), center = Offset(trackStart, trackY))
            drawCircle(color = colorEnd.copy(alpha = 0.50f), radius = 3.5.dp.toPx(), center = Offset(trackEnd, trackY))
            return@Canvas
        }

        val accent = lerpColor(colorStart, colorEnd, progress)
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
        drawCursorArrow(cursorX, trackY, accent)

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
            color = SuccessGreen,
        ),
    )
}

@Composable
private fun PendingContent(modifier: Modifier = Modifier, onSurface: Color) {
    Column(modifier = modifier) {
        Text(
            text = ".-",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = onSurface.copy(alpha = 0.30f),
            ),
        )
        Spacer(Modifier.height(8.dp))
        EtaJourney(
            progress = null,
            onSurface = onSurface,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}

@Composable
private fun UnavailableContent(projection: ProjectionResult, onSurface: Color) {
    val secondary = when (projection) {
        ProjectionResult.Unavailable.WrongDirection -> appString(R.string.trends_eta_wrong_direction)
        ProjectionResult.Unavailable.TooFar -> appString(R.string.trends_eta_too_far)
        else -> appString(R.string.trends_eta_not_enough_data)
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
