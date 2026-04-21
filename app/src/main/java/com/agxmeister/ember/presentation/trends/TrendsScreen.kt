package com.agxmeister.ember.presentation.trends

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.agxmeister.ember.presentation.calendar.CalendarScreen
import com.agxmeister.ember.presentation.chart.ChartScreen

@Composable
fun TrendsScreen(onNavigateToHome: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CalendarScreen()
        ChartScreen(onNavigateToHome = onNavigateToHome)
    }
}
