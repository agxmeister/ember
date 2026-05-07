package com.agxmeister.ember

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.presentation.AppViewModel
import com.agxmeister.ember.presentation.LocalAppResources
import com.agxmeister.ember.presentation.navigation.EmberNavGraph
import com.agxmeister.ember.presentation.theme.EmberTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val isDark by appViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val language by appViewModel.language.collectAsStateWithLifecycle()
            val localizedResources = remember(language) {
                val config = Configuration(resources.configuration).also {
                    it.setLocale(Locale(language.code))
                }
                createConfigurationContext(config).resources
            }
            CompositionLocalProvider(LocalAppResources provides localizedResources) {
                EmberTheme(darkTheme = isDark) {
                    EmberNavGraph(viewModel = appViewModel)
                }
            }
        }
    }
}
