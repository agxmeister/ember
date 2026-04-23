package com.agxmeister.ember.presentation.trends

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.agxmeister.ember.presentation.calendar.CalendarScreen
import com.agxmeister.ember.presentation.chart.ChartScreen
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TrendsScreen(onNavigateToHome: () -> Unit = {}) {
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    var visualizationDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CalendarScreen(
            visualizationDate = visualizationDate,
            onSetVisualizationDate = { visualizationDate = it },
        )
        ChartScreen(
            visualizationDate = visualizationDate ?: today,
            onNavigateToHome = onNavigateToHome,
        )
    }
}
