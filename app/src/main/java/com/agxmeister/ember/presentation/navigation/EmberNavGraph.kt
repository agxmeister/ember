package com.agxmeister.ember.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.agxmeister.ember.presentation.appString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.AppViewModel
import com.agxmeister.ember.presentation.trends.TrendsScreen
import com.agxmeister.ember.presentation.home.HomeScreen
import com.agxmeister.ember.presentation.onboarding.OnboardingScreen
import com.agxmeister.ember.presentation.onboarding.SeedMeasuresOnboardingScreen
import com.agxmeister.ember.presentation.settings.SettingsScreen

private const val ROUTE_ONBOARDING = "onboarding"

sealed class Screen(val route: String, @StringRes val labelRes: Int, val icon: ImageVector) {
    data object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    data object Trends : Screen("trends", R.string.nav_trends, Icons.Default.ShowChart)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)
}

private const val TRENDS_ROUTE_PATTERN = "trends?animateEntry={animateEntry}"
private fun trendsRoute(animateEntry: Boolean = false) = "trends?animateEntry=$animateEntry"

private val screens = listOf(Screen.Home, Screen.Trends)

@Composable
fun EmberNavGraph(viewModel: AppViewModel = hiltViewModel()) {
    val onboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
    val hasCheckedIn by viewModel.hasCheckedIn.collectAsStateWithLifecycle()

    // Don't render until both states are known (DataStore + DB reads are near-instant)
    onboardingCompleted ?: return
    hasCheckedIn ?: return

    // Computed once — no keys — so NavHost never recreates its graph mid-session.
    // Both values are guaranteed non-null by the guards above.
    val startDestination = remember {
        when {
            onboardingCompleted != true -> ROUTE_ONBOARDING
            hasCheckedIn == true -> trendsRoute()
            else -> Screen.Home.route
        }
    }

    val navController = rememberNavController()

    var seedMeasuresMode by remember { mutableStateOf(false) }
    val navigateToOnboarding: (Boolean) -> Unit = { seedMeasures ->
        seedMeasuresMode = seedMeasures
        navController.navigate(ROUTE_ONBOARDING) {
            popUpTo(0) { inclusive = true }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != ROUTE_ONBOARDING

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    screens.forEach { screen ->
                        val label = appString(screen.labelRes)
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = navBackStackEntry?.destination?.hierarchy?.any {
                                val effectiveRoute = if (screen == Screen.Trends) TRENDS_ROUTE_PATTERN else screen.route
                                it.route == effectiveRoute
                            } == true,
                            onClick = {
                                val route = if (screen == Screen.Trends) trendsRoute() else screen.route
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(ROUTE_ONBOARDING) {
                val onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                    }
                }
                if (seedMeasuresMode) {
                    SeedMeasuresOnboardingScreen(onComplete = onComplete)
                } else {
                    OnboardingScreen(onComplete = onComplete)
                }
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToTrends = {
                        navController.navigate(trendsRoute(animateEntry = true)) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = false
                            }
                            launchSingleTop = false
                            restoreState = false
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(
                route = TRENDS_ROUTE_PATTERN,
                arguments = listOf(navArgument("animateEntry") {
                    type = NavType.BoolType
                    defaultValue = false
                }),
            ) { backStackEntry ->
                val animateEntry = backStackEntry.arguments?.getBoolean("animateEntry") == true
                TrendsScreen(
                    animateEntry = animateEntry,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Screen.Settings.route) { SettingsScreen(onNavigateToOnboarding = navigateToOnboarding) }
        }
    }
}
