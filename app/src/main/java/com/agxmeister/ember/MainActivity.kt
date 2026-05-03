package com.agxmeister.ember

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.agxmeister.ember.presentation.AppViewModel
import com.agxmeister.ember.presentation.navigation.EmberNavGraph
import com.agxmeister.ember.presentation.theme.EmberTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val isDark by appViewModel.isDarkTheme.collectAsStateWithLifecycle()
            EmberTheme(darkTheme = isDark) {
                EmberNavGraph(viewModel = appViewModel)
            }
        }
    }
}
