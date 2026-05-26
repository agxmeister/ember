package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.closenessColor
import kotlin.math.abs

@Composable
internal fun DeltaTargetCard(
    modifier: Modifier = Modifier,
    weeklyAvg: Double?,
    targetKg: Double,
    tolerance: Double,
    weightUnit: WeightUnit,
) {
    val darkTheme = isSystemInDarkTheme()
    StatCard(
        modifier = modifier,
        label = appString(R.string.stat_delta_target),
    ) {
        val deltaDisplay = weeklyAvg?.let { abs(weightUnit.scaleDiff(it - targetKg)) }
        val deltaColor = weeklyAvg?.let { w ->
            closenessColor((1.0 - abs(w - targetKg) / tolerance).coerceIn(0.0, 1.0).toFloat(), darkTheme)
        } ?: MaterialTheme.colorScheme.onSurface
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = deltaDisplay?.let { "%.1f".format(it) } ?: "−",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = deltaColor,
                ),
            )
            if (deltaDisplay != null) {
                Text(
                    text = " ${weightUnit.label}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = deltaColor.copy(alpha = 0.75f),
                    ),
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
    }
}
