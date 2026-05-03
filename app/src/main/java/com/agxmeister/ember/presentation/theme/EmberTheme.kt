package com.agxmeister.ember.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun closenessColor(closeness: Float): Color =
    Color.hsl(hue = 8f + closeness * 112f, saturation = 0.82f, lightness = 0.57f)

@Composable
fun EmberTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content,
    )
}
