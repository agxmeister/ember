package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Card chrome shared by all equalizer stat cards: surface color, rounding, and inner padding.
 * When [pendingOverlayText] is non-null, a scrim covers the whole card with a lock icon
 * top-left and the explanatory text centered on top.
 */
@Composable
internal fun StatCardSurface(
    modifier: Modifier = Modifier,
    pendingOverlayText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(10.dp),
    ) {
        Box {
            Column(modifier = Modifier.padding(12.dp).fillMaxHeight(), content = content)
            if (pendingOverlayText != null) {
                val darkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                val scrimColor = if (darkTheme) Color(0xFF242424) else Color.LightGray
                val textColor = if (darkTheme) Color(0xFFAEAEAE) else Color.DarkGray
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(scrimColor)
                        .padding(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(14.dp).align(Alignment.TopStart),
                    )
                    Text(
                        text = pendingOverlayText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center),
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

/** Stat card header: a label with an optional trailing help icon opening [onInfo]. */
@Composable
internal fun CardLabelRow(
    label: String,
    modifier: Modifier = Modifier,
    onInfo: (() -> Unit)? = null,
    helpKey: String? = null,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = cardLabelStyle())
        Spacer(Modifier.weight(1f))
        if (onInfo != null && helpKey != null) InfoIcon(onClick = onInfo, helpKey = helpKey)
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
