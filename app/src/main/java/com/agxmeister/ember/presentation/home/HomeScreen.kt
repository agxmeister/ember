package com.agxmeister.ember.presentation.home

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.domain.model.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs

private fun ThemeMode.next() = when (this) {
    ThemeMode.Light -> ThemeMode.Dark
    ThemeMode.Dark -> ThemeMode.Auto
    ThemeMode.Auto -> ThemeMode.Light
}

private fun ThemeMode.icon(): ImageVector = when (this) {
    ThemeMode.Light -> Icons.Outlined.LightMode
    ThemeMode.Dark -> Icons.Outlined.DarkMode
    ThemeMode.Auto -> Icons.Outlined.BrightnessAuto
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val defaultWeight = state.defaultWeightKg ?: return

    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val dow = today.dayOfWeek.name.take(3)
    val mon = today.month.name.take(3)
    val day = today.dayOfMonth.toString().padStart(2, '0')

    var selectedWeight by remember(defaultWeight) { mutableDoubleStateOf(defaultWeight) }
    val coroutineScope = rememberCoroutineScope()
    val checkmarkAlpha = remember { Animatable(0f) }

    val colorWeight = state.todayWeightKg ?: defaultWeight
    val closeness = (1.0 - abs(colorWeight - state.targetKg) / state.tolerance)
        .coerceIn(0.0, 1.0).toFloat()
    val accentColor = Color.hsl(hue = 8f + closeness * 112f, saturation = 0.82f, lightness = 0.57f)

    val onBg = MaterialTheme.colorScheme.onBackground

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Date + cluster pinned to top-left
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .blur(radius = (checkmarkAlpha.value * 16).dp),
        ) {
            Text(
                text = "$dow  $mon $day  ${today.year}",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    letterSpacing = 1.5.sp,
                    color = onBg.copy(alpha = 0.80f),
                    fontWeight = FontWeight.Bold,
                ),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.currentCluster?.label ?: "",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = onBg.copy(alpha = 0.55f),
                    ),
                )
                state.currentCluster?.let { cluster ->
                    val tooltipState = rememberTooltipState(isPersistent = true)
                    val scope = rememberCoroutineScope()
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(cluster.dayCluster.description) } },
                        state = tooltipState,
                    ) {
                        IconButton(
                            onClick = { scope.launch { tooltipState.show() } },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HelpOutline,
                                contentDescription = "What is ${cluster.label}?",
                                modifier = Modifier.size(16.dp),
                                tint = onBg.copy(alpha = 0.30f),
                            )
                        }
                    }
                }
            }
        }

        // Theme switcher pinned to top-right
        IconButton(
            onClick = { viewModel.setThemeMode(state.themeMode.next()) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .blur(radius = (checkmarkAlpha.value * 16).dp),
        ) {
            Icon(
                imageVector = state.themeMode.icon(),
                contentDescription = state.themeMode.name,
                tint = onBg.copy(alpha = 0.55f),
            )
        }

        // Centered content + bottom button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .blur(radius = (checkmarkAlpha.value * 16).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            Text(
                text = "How much do you weigh?",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp,
                    color = onBg.copy(alpha = 0.90f),
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(Modifier.height(24.dp))

            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    primary = onBg,
                    onSurface = onBg,
                ),
            ) {
                key(defaultWeight, state.weightUnit) {
                    WeightWheelPicker(
                        initialWeightKg = defaultWeight,
                        unit = state.weightUnit,
                        onWeightKgChanged = { selectedWeight = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                enabled = checkmarkAlpha.value == 0f,
                onClick = {
                    viewModel.save(selectedWeight)
                    coroutineScope.launch {
                        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)
                        toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 300)
                        checkmarkAlpha.snapTo(1f)
                        delay(400)
                        checkmarkAlpha.animateTo(0f, animationSpec = tween(700))
                        toneGen.release()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            ) {
                Text(
                    text = if (state.isRechecking) "RE-CHECK" else "CHECK-IN",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A0A0A),
                    ),
                )
            }

            Spacer(Modifier.height(32.dp))
        }

        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.Center)
                .graphicsLayer { alpha = checkmarkAlpha.value },
            tint = accentColor,
        )
    }
}
