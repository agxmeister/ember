package com.agxmeister.ember.presentation.home

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedWeight by remember(state.defaultWeightKg) {
        mutableDoubleStateOf(state.defaultWeightKg ?: 0.0)
    }
    val coroutineScope = rememberCoroutineScope()
    val checkmarkAlpha = remember { Animatable(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .blur(radius = (checkmarkAlpha.value * 16).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.currentCluster?.label ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                state.currentCluster?.let { cluster ->
                    val tooltipState = rememberTooltipState(isPersistent = true)
                    val scope = rememberCoroutineScope()
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip { Text(cluster.dayCluster.description) }
                        },
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
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "How much do you weigh?",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth().height(WeightPickerHeight)) {
                state.defaultWeightKg?.let { defaultWeight ->
                    key(defaultWeight, state.weightUnit) {
                        WeightWheelPicker(
                            initialWeightKg = defaultWeight,
                            unit = state.weightUnit,
                            onWeightKgChanged = { selectedWeight = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                enabled = state.defaultWeightKg != null && checkmarkAlpha.value == 0f,
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
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.isRechecking) "Re-check!" else "Check-in!")
            }
        }

        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.Center)
                .graphicsLayer { alpha = checkmarkAlpha.value },
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}
