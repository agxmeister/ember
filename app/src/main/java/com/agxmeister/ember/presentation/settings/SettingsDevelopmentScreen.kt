package com.agxmeister.ember.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.AlgorithmConfig
import com.agxmeister.ember.presentation.appString

@Composable
fun SettingsDevelopmentScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToOnboarding: (seedMeasures: Boolean) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val algorithmConfig by viewModel.algorithmConfig.collectAsStateWithLifecycle()
    val accentCloseness by viewModel.accentCloseness.collectAsStateWithLifecycle()

    var showApproximationDialog by remember { mutableStateOf(false) }
    var showDefineGoalDialog by remember { mutableStateOf(false) }

    SettingsAccentTheme(accentCloseness) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        SettingsTopBar(
            title = appString(R.string.settings_development_title),
            onBack = onNavigateBack,
        )
        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionHeader(appString(R.string.settings_approximation))
        TappableSetting(
            value = appString(
                R.string.settings_approximation_summary,
                algorithmConfig.regressionIntervalDays,
                algorithmConfig.minClusterSize,
                algorithmConfig.streakTrendWindow,
                algorithmConfig.scoreWindow,
                algorithmConfig.volatilityWindow,
            ),
            onClick = { showApproximationDialog = true },
        )

        if (showApproximationDialog) {
            var regressionText by remember { mutableStateOf(algorithmConfig.regressionIntervalDays.toString()) }
            var clusterText by remember { mutableStateOf(algorithmConfig.minClusterSize.toString()) }
            var streakText by remember { mutableStateOf(algorithmConfig.streakTrendWindow.toString()) }
            var scoreText by remember { mutableStateOf(algorithmConfig.scoreWindow.toString()) }
            var volatilityText by remember { mutableStateOf(algorithmConfig.volatilityWindow.toString()) }
            val regressionVal = regressionText.toIntOrNull()
            val clusterVal = clusterText.toIntOrNull()
            val streakVal = streakText.toIntOrNull()
            val scoreVal = scoreText.toIntOrNull()
            val volatilityVal = volatilityText.toIntOrNull()
            val isValid = regressionVal != null && regressionVal in 7..365 &&
                clusterVal != null && clusterVal in 1..365 &&
                streakVal != null && streakVal in 2..365 &&
                scoreVal != null && scoreVal in 1..365 &&
                volatilityVal != null && volatilityVal in 2..365
            val borderColor = MaterialTheme.colorScheme.outline
            AlertDialog(
                onDismissRequest = { showApproximationDialog = false },
                title = { Text(appString(R.string.settings_adjust_approximation)) },
                text = {
                    Column {
                        Text(
                            appString(R.string.settings_regression_interval),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicTextField(
                                value = regressionText,
                                onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) regressionText = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .width(64.dp)
                                    .drawBehind {
                                        drawLine(
                                            color = borderColor,
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 1.dp.toPx(),
                                        )
                                    },
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(appString(R.string.label_days), style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            appString(R.string.settings_min_cluster_size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BasicTextField(
                            value = clusterText,
                            onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) clusterText = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .width(64.dp)
                                .drawBehind {
                                    drawLine(
                                        color = borderColor,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1.dp.toPx(),
                                    )
                                },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            appString(R.string.settings_streak_window),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BasicTextField(
                            value = streakText,
                            onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) streakText = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .width(64.dp)
                                .drawBehind {
                                    drawLine(
                                        color = borderColor,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1.dp.toPx(),
                                    )
                                },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            appString(R.string.settings_score_window),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BasicTextField(
                            value = scoreText,
                            onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) scoreText = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .width(64.dp)
                                .drawBehind {
                                    drawLine(
                                        color = borderColor,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1.dp.toPx(),
                                    )
                                },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            appString(R.string.settings_volatility_window),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BasicTextField(
                            value = volatilityText,
                            onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) volatilityText = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .width(64.dp)
                                .drawBehind {
                                    drawLine(
                                        color = borderColor,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1.dp.toPx(),
                                    )
                                },
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onAlgorithmConfigChanged(
                                AlgorithmConfig(
                                    regressionIntervalDays = regressionVal!!,
                                    minClusterSize = clusterVal!!,
                                    streakTrendWindow = streakVal!!,
                                    scoreWindow = scoreVal!!,
                                    volatilityWindow = volatilityVal!!,
                                )
                            )
                            showApproximationDialog = false
                        },
                        enabled = isValid,
                    ) { Text(appString(R.string.label_save)) }
                },
                dismissButton = {
                    TextButton(onClick = { showApproximationDialog = false }) {
                        Text(appString(R.string.label_cancel))
                    }
                },
            )
        }

        SettingsDivider()

        Text(
            appString(R.string.settings_start_over_with_import),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .clickable { showDefineGoalDialog = true }
                .padding(bottom = 8.dp),
        )

        if (showDefineGoalDialog) {
            StartOverConfirmDialog(
                onConfirm = {
                    showDefineGoalDialog = false
                    viewModel.onResetData()
                    onNavigateToOnboarding(true)
                },
                onDismiss = { showDefineGoalDialog = false },
            )
        }
    }
    }
}
