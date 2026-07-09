package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.luminance
import com.agxmeister.ember.presentation.common.InfoIcon
import com.agxmeister.ember.presentation.common.LocalHelpIconsVisible
import com.agxmeister.ember.presentation.theme.InfoIconSize
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Explanation and "N more to go" count shown in a [PendingOverlaySplit] scrim. */
internal data class PendingOverlaySplit(
    val explanation: String,
    val count: Int,
    val countLabel: String,
    val countFontSize: TextUnit = 52.sp,
    val countOnLeft: Boolean = false,
)

/**
 * Card chrome shared by all equalizer stat cards: surface color, rounding, and inner padding.
 * When pending, a scrim covers the whole card with a lock icon top-left, plus either
 * [pendingOverlayText] centered, or [pendingOverlaySplit] laid out as explanation | count.
 */
@Composable
internal fun StatCardSurface(
    modifier: Modifier = Modifier,
    pendingOverlayText: String? = null,
    pendingOverlaySplit: PendingOverlaySplit? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(10.dp),
    ) {
        Box {
            Column(modifier = Modifier.padding(12.dp).fillMaxHeight(), content = content)
            if (pendingOverlayText != null || pendingOverlaySplit != null) {
                val darkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                val scrimColor = if (darkTheme) Color(0xFF242424) else Color.LightGray
                val textColor = if (darkTheme) Color(0xFFAEAEAE) else Color.DarkGray
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(scrimColor),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.padding(12.dp).size(14.dp).align(Alignment.TopStart),
                    )
                    if (pendingOverlaySplit != null) {
                        // Weighted 2:1 split over the full (unpadded) card width so the divider
                        // lands where the 2/3 boundary falls in the stat-card row above.
                        PendingOverlaySplitRow(
                            split = pendingOverlaySplit,
                            textColor = textColor,
                            modifier = Modifier.align(Alignment.Center).fillMaxSize().padding(vertical = 12.dp),
                        )
                    } else if (pendingOverlayText != null) {
                        Text(
                            text = pendingOverlayText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center).padding(12.dp),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingOverlaySplitRow(split: PendingOverlaySplit, textColor: Color, modifier: Modifier = Modifier) {
    val explanationText: @Composable (Modifier) -> Unit = { textModifier ->
        Text(
            text = split.explanation,
            modifier = textModifier,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = textColor,
            ),
        )
    }
    val countColumn: @Composable (Modifier) -> Unit = { columnModifier ->
        Column(modifier = columnModifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = split.count.toString(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = split.countFontSize,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                ),
            )
            Text(
                text = split.countLabel,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = textColor,
                ),
            )
        }
    }
    val divider: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(textColor.copy(alpha = 0.35f)),
        )
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (split.countOnLeft) {
            countColumn(Modifier.weight(1f).padding(start = 12.dp, end = 10.dp))
            divider()
            explanationText(Modifier.weight(2f).padding(start = 10.dp, end = 12.dp))
        } else {
            explanationText(Modifier.weight(2f).padding(start = 12.dp, end = 10.dp))
            divider()
            countColumn(Modifier.weight(1f).padding(start = 10.dp, end = 12.dp))
        }
    }
}

/**
 * Stat card header: a label with a trailing help icon opening [onInfo]. When [pending] is true,
 * the icon renders as a solid [PendingHelpBadge] instead, since data is missing rather than just
 * unexplained.
 */
@Composable
internal fun CardLabelRow(
    label: String,
    modifier: Modifier = Modifier,
    onInfo: (() -> Unit)? = null,
    helpKey: String? = null,
    pending: Boolean = false,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = cardLabelStyle())
        Spacer(Modifier.weight(1f))
        if (pending && onInfo != null) {
            PendingHelpBadge(onClick = onInfo)
        } else if (onInfo != null && helpKey != null) {
            InfoIcon(onClick = onInfo, helpKey = helpKey)
        }
    }
}

/**
 * Solid "?" badge marking a widget as pending due to missing data. Unlike [InfoIcon] it never
 * dims: pending is an ongoing state, not a one-time hint, so opening the dialog doesn't affect it.
 */
@Composable
internal fun PendingHelpBadge(onClick: () -> Unit, modifier: Modifier = Modifier) {
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

/** Small monospace caption used for stat card labels. */
@Composable
internal fun cardLabelStyle(): TextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 9.sp,
    letterSpacing = 0.8.sp,
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
)

/** Large monospace value used for the headline number on stat cards. */
internal fun statValueStyle(color: Color): TextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 28.sp,
    fontWeight = FontWeight.Bold,
    color = color,
)

/** Dimmed monospace caption trailing a stat value (unit, "wks", "%", …). */
internal fun statUnitStyle(color: Color): TextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 12.sp,
    color = color.copy(alpha = 0.75f),
)

/** Upward-pointing cursor triangle sitting on a horizontal track at [cursorX], [trackY]. */
internal fun DrawScope.drawCursorArrow(cursorX: Float, trackY: Float, color: Color) {
    val arrowHalf = 5.dp.toPx()
    val arrowHeight = 8.dp.toPx()
    val path = Path().apply {
        moveTo(cursorX, trackY)
        lineTo(cursorX - arrowHalf, trackY + arrowHeight)
        lineTo(cursorX + arrowHalf, trackY + arrowHeight)
        close()
    }
    drawPath(path, color)
}

/** A headline [value] with an optional trailing [unit], sharing one [color]. */
@Composable
internal fun StatValueRow(
    value: String,
    unit: String?,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(text = value, style = statValueStyle(color))
        if (unit != null) {
            Text(
                text = " $unit",
                style = statUnitStyle(color),
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
    }
}
