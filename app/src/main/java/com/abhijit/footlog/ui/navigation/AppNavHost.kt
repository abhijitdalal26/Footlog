package com.abhijit.footlog.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.abhijit.footlog.ui.screens.*
import com.abhijit.footlog.ui.theme.FootlogColors

private data class NavItem(val screen: Screen, val icon: ImageVector, val label: String)

private val bottomNavItems = listOf(
    NavItem(Screen.Home, Icons.Filled.Home, "Home"),
    NavItem(Screen.History, Icons.Filled.Schedule, "History"),
    NavItem(Screen.Routes, Icons.Filled.Map, "Routes"),
    NavItem(Screen.Stats, Icons.Filled.BarChart, "Stats"),
)

@Composable
fun AppNavHost(startDestination: Screen) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { item ->
        currentDest?.hasRoute(item.screen::class) == true
    }

    val isDark = isSystemInDarkTheme()
    val inactiveColor = if (isDark) FootlogColors.navInactiveDark else FootlogColors.navInactiveLight
    val activeColor = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val navBarBg = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = navBarBg) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDest?.hasRoute(item.screen::class) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = activeColor,
                                selectedTextColor = activeColor,
                                unselectedIconColor = inactiveColor,
                                unselectedTextColor = inactiveColor,
                                indicatorColor = navBarBg,
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn() + slideInHorizontally { it / 2 } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it / 2 } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it / 2 } },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 2 } }
        ) {
            composable<Screen.Home> {
                HomeScreen(
                    onStartActivity = { type -> navController.navigate(Screen.ActiveTracking(type)) },
                    onSessionClick = { id -> navController.navigate(Screen.SessionDetail(id)) },
                    onProfileClick = { navController.navigate(Screen.Profile) }
                )
            }
            composable<Screen.Profile> {
                com.abhijit.footlog.ui.screens.ProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable<Screen.History> {
                HistoryScreen(
                    onSessionClick = { id -> navController.navigate(Screen.SessionDetail(id)) }
                )
            }
            composable<Screen.Routes> {
                RoutesScreen(
                    onSessionClick = { id -> navController.navigate(Screen.SessionDetail(id)) }
                )
            }
            composable<Screen.Stats> { StatsScreen() }
            composable<Screen.ActiveTracking> { backStack ->
                val activityType = backStack.toRoute<Screen.ActiveTracking>().activityType
                ActiveTrackingScreen(
                    activityType = activityType,
                    onStop = { sessionId ->
                        navController.navigate(Screen.SessionSummary(sessionId)) {
                            popUpTo<Screen.Home>()
                        }
                    },
                    onNoteClick = { sessionId -> navController.navigate(Screen.NoteWriting(sessionId)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable<Screen.SessionSummary> { backStack ->
                val sessionId = backStack.toRoute<Screen.SessionSummary>().sessionId
                SessionSummaryScreen(
                    sessionId = sessionId,
                    onDone = { navController.navigate(Screen.Home) { popUpTo(0) { inclusive = true } } },
                    onShare = { navController.navigate(Screen.ShareCard(sessionId)) }
                )
            }
            composable<Screen.ShareCard> { backStack ->
                val sessionId = backStack.toRoute<Screen.ShareCard>().sessionId
                ShareCardScreen(sessionId = sessionId, onClose = { navController.popBackStack() })
            }
            composable<Screen.SessionDetail> { backStack ->
                val sessionId = backStack.toRoute<Screen.SessionDetail>().sessionId
                SessionDetailScreen(
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() },
                    onNoteClick = { navController.navigate(Screen.NoteView(sessionId)) },
                    onHighlightClick = { hId -> navController.navigate(Screen.HighlightDetail(hId)) }
                )
            }
            composable<Screen.NoteWriting> { backStack ->
                val sessionId = backStack.toRoute<Screen.NoteWriting>().sessionId
                NoteWritingScreen(sessionId = sessionId, onBack = { navController.popBackStack() })
            }
            composable<Screen.NoteView> { backStack ->
                val sessionId = backStack.toRoute<Screen.NoteView>().sessionId
                NoteViewScreen(sessionId = sessionId, onBack = { navController.popBackStack() })
            }
            composable<Screen.HighlightDetail> { backStack ->
                val highlightId = backStack.toRoute<Screen.HighlightDetail>().highlightId
                HighlightDetailScreen(highlightId = highlightId, onBack = { navController.popBackStack() })
            }
            composable<Screen.Onboarding> {
                OnboardingScreen(onComplete = {
                    navController.navigate(Screen.Home) { popUpTo(0) { inclusive = true } }
                })
            }
        }
    }
}
