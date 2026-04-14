package com.agxmeister.ember.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.domain.model.WeightGoal

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val clusters by viewModel.clusters.collectAsStateWithLifecycle()
    val clusteringEnabled by viewModel.clusteringEnabled.collectAsStateWithLifecycle()
    val weightGoal by viewModel.weightGoal.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Goal", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = weightGoal == WeightGoal.Decrease,
                onClick = { viewModel.onWeightGoalChanged(WeightGoal.Decrease) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) { Text("Lose weight") }
            SegmentedButton(
                selected = weightGoal == WeightGoal.Increase,
                onClick = { viewModel.onWeightGoalChanged(WeightGoal.Increase) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) { Text("Gain weight") }
        }
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text("Tracking mode", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Time-of-day clustering", style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (clusteringEnabled) "On — trends shown per time slot" else "Off — daily averages shown",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = clusteringEnabled,
                onCheckedChange = viewModel::onClusteringEnabledChanged,
                modifier = Modifier.padding(start = 16.dp),
            )
        }

        if (clusteringEnabled) {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text("Detected clusters", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (clusters.isEmpty()) {
                Text(
                    text = "No clusters detected yet. Add some measurements first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                clusters.forEach { cluster ->
                    ListItem(
                        headlineContent = { Text(cluster.label) },
                        supportingContent = { Text("${cluster.measurements.size} measurements") },
                    )
                    HorizontalDivider()
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Notification reminders will be configurable in a future update.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
