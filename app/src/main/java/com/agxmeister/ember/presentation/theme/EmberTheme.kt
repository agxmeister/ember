package com.agxmeister.ember.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun closenessColor(closeness: Float): Color =
    Color.hsl(hue = 8f + closeness * 112f, saturation = 0.82f, lightness = 0.57f)

private val darkColors = darkColorScheme()
private val lightColors = lightColorScheme()

@Composable
fun EmberTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColors else lightColors,
        content = content,
    )
}
