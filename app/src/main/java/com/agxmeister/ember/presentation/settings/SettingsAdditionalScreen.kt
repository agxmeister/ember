package com.agxmeister.ember.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.appString

@Composable
fun SettingsAdditionalScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToDevelopment: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val clusteringEnabled by viewModel.clusteringEnabled.collectAsStateWithLifecycle()
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()
    val weighingFrequency by viewModel.weighingFrequency.collectAsStateWithLifecycle()
    val accentCloseness by viewModel.accentCloseness.collectAsStateWithLifecycle()

    var devMode by remember { mutableStateOf(false) }
    var titleTapCount by remember { mutableIntStateOf(0) }

    SettingsAccentTheme(accentCloseness) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        SettingsTopBar(
            title = appString(R.string.settings_additional_title),
            onBack = onNavigateBack,
            titleModifier = Modifier.clickable {
                titleTapCount++
                if (titleTapCount >= 7) {
                    titleTapCount = 0
                    devMode = !devMode
                }
            },
        )
        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionHeader(appString(R.string.settings_unit))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            WeightUnit.entries.forEachIndexed { index, unit ->
                SegmentedButton(
                    selected = weightUnit == unit,
                    onClick = { viewModel.onWeightUnitChanged(unit) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = WeightUnit.entries.size),
                ) { Text(unit.label) }
            }
        }
        SettingsDivider()

        SettingsSectionHeader(appString(R.string.settings_tracking_mode))
        SettingsToggleRow(
            title = appString(R.string.label_clustering),
            subtitle = if (clusteringEnabled) appString(R.string.label_clustering_on) else appString(R.string.label_clustering_off),
            checked = clusteringEnabled,
            onCheckedChange = viewModel::onClusteringEnabledChanged,
        )
        SettingsDivider()

        SettingsSectionHeader(appString(R.string.settings_weighing_frequency))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = weighingFrequency == WeighingFrequency.Daily,
                onClick = { viewModel.onWeighingFrequencyChanged(WeighingFrequency.Daily) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) { Text(appString(R.string.label_daily)) }
            SegmentedButton(
                selected = weighingFrequency == WeighingFrequency.Weekly,
                onClick = { viewModel.onWeighingFrequencyChanged(WeighingFrequency.Weekly) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) { Text(appString(R.string.label_weekly)) }
        }

        if (devMode) {
            SettingsDivider()
            SettingsNavLabel(
                text = appString(R.string.settings_go_to_development),
                onClick = onNavigateToDevelopment,
            )
        }
    }
    }
}
