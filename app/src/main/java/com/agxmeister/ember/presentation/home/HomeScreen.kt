package com.agxmeister.ember.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedWeight by remember(state.defaultWeightKg) {
        mutableDoubleStateOf(state.defaultWeightKg)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        state.currentCluster?.let { cluster ->
            Text(
                text = cluster.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = "How much do you weigh?",
            style = MaterialTheme.typography.headlineSmall,
        )

        Spacer(modifier = Modifier.height(32.dp))

        key(state.defaultWeightKg, state.weightUnit) {
            WeightWheelPicker(
                initialWeightKg = state.defaultWeightKg,
                unit = state.weightUnit,
                onWeightKgChanged = { selectedWeight = it },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { viewModel.save(selectedWeight) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isRechecking) "Re-check!" else "Check-in!")
        }
    }
}
