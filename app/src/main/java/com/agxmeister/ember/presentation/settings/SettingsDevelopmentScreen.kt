package com.agxmeister.ember.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.appString

private enum class AlgorithmField {
    MinClusterSize, RegressionWindow, StaleCutoff, MaxGap, StreakWindow, ScoreWindow, VolatilityWindow,
    MinMeasuredForVolatility, MinMeasuredForRate, MinMeasuredForEta,
}

@Composable
fun SettingsDevelopmentScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToOnboarding: (seedMeasures: Boolean) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val algorithmConfig by viewModel.algorithmConfig.collectAsStateWithLifecycle()
    val accentCloseness by viewModel.accentCloseness.collectAsStateWithLifecycle()

    var editingField by remember { mutableStateOf<AlgorithmField?>(null) }
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

        SettingsSectionHeader(appString(R.string.settings_smart_tracking))
        LabeledTappableSetting(
            label = appString(R.string.settings_min_cluster_size),
            value = algorithmConfig.minClusterSize.toString(),
            onClick = { editingField = AlgorithmField.MinClusterSize },
        )
        SettingsDivider()

        SettingsSectionHeader(appString(R.string.settings_trend))
        LabeledTappableSetting(
            label = appString(R.string.settings_regression_window),
            value = algorithmConfig.regressionWindow.toString(),
            onClick = { editingField = AlgorithmField.RegressionWindow },
        )
        LabeledTappableSetting(
            label = appString(R.string.settings_stale_cutoff_window),
            value = algorithmConfig.staleCutoffPeriods.toString(),
            onClick = { editingField = AlgorithmField.StaleCutoff },
        )
        LabeledTappableSetting(
            label = appString(R.string.settings_max_gap),
            value = algorithmConfig.maxGapDays.toString(),
            onClick = { editingField = AlgorithmField.MaxGap },
        )
        LabeledTappableSetting(
            label = appString(R.string.settings_min_measured_for_rate),
            value = algorithmConfig.minMeasuredForRate.toString(),
            onClick = { editingField = AlgorithmField.MinMeasuredForRate },
        )
        LabeledTappableSetting(
            label = appString(R.string.settings_min_measured_for_eta),
            value = algorithmConfig.minMeasuredForEta.toString(),
            onClick = { editingField = AlgorithmField.MinMeasuredForEta },
        )
        SettingsDivider()

        SettingsSectionHeader(appString(R.string.settings_windows))
        LabeledTappableSetting(
            label = appString(R.string.settings_streak_window),
            value = algorithmConfig.streakWindow.toString(),
            onClick = { editingField = AlgorithmField.StreakWindow },
        )
        LabeledTappableSetting(
            label = appString(R.string.settings_score_window),
            value = algorithmConfig.scoreWindow.toString(),
            onClick = { editingField = AlgorithmField.ScoreWindow },
        )
        LabeledTappableSetting(
            label = appString(R.string.settings_volatility_window),
            value = algorithmConfig.volatilityWindow.toString(),
            onClick = { editingField = AlgorithmField.VolatilityWindow },
        )
        LabeledTappableSetting(
            label = appString(R.string.settings_min_measured_for_volatility),
            value = algorithmConfig.minMeasuredForVolatility.toString(),
            onClick = { editingField = AlgorithmField.MinMeasuredForVolatility },
        )

        when (editingField) {
            AlgorithmField.MinClusterSize -> IntSettingDialog(
                title = appString(R.string.settings_min_cluster_size),
                initialValue = algorithmConfig.minClusterSize,
                validRange = 1..365,
                onConfirm = {
                    viewModel.onAlgorithmConfigChanged(algorithmConfig.copy(minClusterSize = it))
                    editingField = null
                },
                onDismiss = { editingField = null },
            )
            AlgorithmField.RegressionWindow -> IntSettingDialog(
                title = appString(R.string.settings_regression_window),
                initialValue = algorithmConfig.regressionWindow,
                validRange = 7..365,
                onConfirm = {
                    viewModel.onAlgorithmConfigChanged(algorithmConfig.copy(regressionWindow = it))
                    editingField = null
                },
                onDismiss = { editingField = null },
            )
            AlgorithmField.StaleCutoff -> IntSettingDialog(
                title = appString(R.string.settings_stale_cutoff_window),
                initialValue = algorithmConfig.staleCutoffPeriods,
                validRange = 1..14,
                onConfirm = {
                    viewModel.onAlgorithmConfigChanged(algorithmConfig.copy(staleCutoffPeriods = it))
                    editingField = null
                },
                onDismiss = { editingField = null },
            )
            AlgorithmField.MaxGap -> IntSettingDialog(
                title = appString(R.string.settings_max_gap),
                initialValue = algorithmConfig.maxGapDays,
                validRange = 1..30,
                onConfirm = {
                    viewModel.onAlgorithmConfigChanged(algorithmConfig.copy(maxGapDays = it))
                    editingField = null
                },
                onDismiss = { editingField = null },
            )
            AlgorithmField.MinMeasuredForRate -> IntSettingDialog(
                title = appString(R.string.settings_min_measured_for_rate),
                initialValue = algorithmConfig.minMeasuredForRate,
                validRange = 2..365,
                onConfirm = {
                    viewModel.onAlgorithmConfigChanged(algorithmConfig.copy(minMeasuredForRate = it))
                    editingField = null
                },
                onDismiss = { editingField = null },
            )
            AlgorithmField.MinMeasuredForEta -> IntSettingDialog(
                title = appString(R.string.settings_min_measured_for_eta),
                initialValue = algorithmConfig.minMeasuredForEta,
                validRange = 2..365,
                onConfirm = {
                    viewModel.onAlgorithmConfigChanged(algorithmConfig.copy(minMeasuredForEta = it))
                    editingField = null
                },
                onDismiss = { editingField = null },
            )
            AlgorithmField.StreakWindow -> IntSettingDialog(
                title = appString(R.string.settings_streak_window),
                initialValue = algorithmConfig.streakWindow,
                validRange = 2..365,
                onConfirm = {
                    viewModel.onAlgorithmConfigChanged(algorithmConfig.copy(streakWindow = it))
                    editingField = null
                },
                onDismiss = { editingField = null },
            )
            AlgorithmField.ScoreWindow -> IntSettingDialog(
                title = appString(R.string.settings_score_window),
                initialValue = algorithmConfig.scoreWindow,
                validRange = 1..365,
                onConfirm = {
                    viewModel.onAlgorithmConfigChanged(algorithmConfig.copy(scoreWindow = it))
                    editingField = null
                },
                onDismiss = { editingField = null },
            )
            AlgorithmField.VolatilityWindow -> IntSettingDialog(
                title = appString(R.string.settings_volatility_window),
                initialValue = algorithmConfig.volatilityWindow,
                validRange = 2..365,
                onConfirm = {
                    viewModel.onAlgorithmConfigChanged(algorithmConfig.copy(volatilityWindow = it))
                    editingField = null
                },
                onDismiss = { editingField = null },
            )
            AlgorithmField.MinMeasuredForVolatility -> IntSettingDialog(
                title = appString(R.string.settings_min_measured_for_volatility),
                initialValue = algorithmConfig.minMeasuredForVolatility,
                validRange = 2..algorithmConfig.volatilityWindow,
                onConfirm = {
                    viewModel.onAlgorithmConfigChanged(algorithmConfig.copy(minMeasuredForVolatility = it))
                    editingField = null
                },
                onDismiss = { editingField = null },
            )
            null -> Unit
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
