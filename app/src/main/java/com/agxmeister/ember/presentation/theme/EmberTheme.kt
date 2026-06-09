package com.agxmeister.ember.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sqrt

/** Accent color for tappable info ("i") affordances. */
val InfoAccent = Color(0xFF2196F3)

/** Size for tappable info/help ("i" and "?") icons. */
val InfoIconSize = 18.dp

/** Status accent colors shared by rate, streak, and projection indicators. */
val SuccessGreen = Color(0xFF4BB543)
val WarningAmber = Color(0xFFFFC107)
val DangerRed = Color(0xFFE53935)

fun closenessColor(closeness: Float, darkTheme: Boolean = true): Color {
    val lightness = if (darkTheme) 0.57f else 0.40f
    return Color.hsl(hue = 8f + sqrt(closeness) * 112f, saturation = 0.82f, lightness = lightness)
}

fun trendSpeedColor(weeklyRateKg: Double?, goalIsLoss: Boolean, darkTheme: Boolean = true): Color {
    val lightness = if (darkTheme) 0.57f else 0.40f
    if (weeklyRateKg == null) return Color.hsl(hue = 8f, saturation = 0.0f, lightness = 0.45f)
    val towardTarget = if (goalIsLoss) weeklyRateKg < 0.0 else weeklyRateKg > 0.0
    if (!towardTarget) return Color.hsl(hue = 8f, saturation = 0.82f, lightness = lightness)
    val speedProgress = (abs(weeklyRateKg) / 0.5).coerceIn(0.0, 1.0).toFloat()
    return Color.hsl(hue = 50f + speedProgress * 70f, saturation = 0.82f, lightness = lightness)
}

private val darkColors = darkColorScheme()
private val lightColors = lightColorScheme()

@Composable
fun EmberTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColors else lightColors,
        content = content,
    )
}
