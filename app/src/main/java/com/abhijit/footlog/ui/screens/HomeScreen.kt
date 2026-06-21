package com.abhijit.footlog.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.HomeViewModel
import com.abhijit.footlog.data.entity.SessionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartActivity: (String) -> Unit,
    onSessionClick: (String) -> Unit,
    vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val sessions by vm.recentSessions.collectAsState()
    val isDark = isSystemInDarkTheme()
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val surfaceColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight
    val onPrimary = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Good to see you", style = MaterialTheme.typography.titleMedium)
                        Text("Ready to move?", style = MaterialTheme.typography.bodySmall,
                            color = textSecondary)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings",
                            tint = textSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActivityCard(
                        label = "Walk",
                        icon = Icons.Filled.DirectionsWalk,
                        bgColor = routeColor,
                        textColor = onPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onStartActivity("walk") }
                    )
                    ActivityCard(
                        label = "Run",
                        icon = Icons.Filled.DirectionsRun,
                        bgColor = routeColor,
                        textColor = onPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onStartActivity("run") }
                    )
                    ActivityCard(
                        label = "Cycle",
                        icon = Icons.Filled.DirectionsBike,
                        bgColor = routeColor,
                        textColor = onPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onStartActivity("cycle") }
                    )
                }
            }

            item {
                Text(
                    "Recent sessions",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
                )
            }

            item {
                if (sessions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No walks yet — start one above",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(sessions) { session ->
                            RecentSessionCard(
                                session = session,
                                surfaceColor = surfaceColor,
                                textSecondary = textSecondary,
                                onClick = { onSessionClick(session.id) }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Your explored map",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Map, contentDescription = null,
                                modifier = Modifier.size(40.dp), tint = textSecondary)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Your map fills in as you walk",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(
    label: String,
    icon: ImageVector,
    bgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = textColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = textColor)
        }
    }
}

@Composable
private fun RecentSessionCard(
    session: SessionEntity,
    surfaceColor: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(session.startTime))
    val distKm = "%.1f km".format(session.distanceMeters / 1000f)

    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp).height(120.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Text(session.title.ifBlank { session.activityType.replaceFirstChar { it.uppercase() } },
                style = MaterialTheme.typography.labelLarge, color = textPrimary,
                maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(distKm, style = MaterialTheme.typography.bodySmall, color = textSecondary)
            Text(dateStr, style = MaterialTheme.typography.bodySmall, color = textSecondary)
        }
    }
}
