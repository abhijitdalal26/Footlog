package com.abhijit.footlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.data.entity.SessionEntity
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.HistoryViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onSessionClick: (String) -> Unit,
    vm: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val grouped by vm.groupedSessions.collectAsState()
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val surfaceColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight
    val danger = FootlogColors.danger

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        if (grouped.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = textSecondary.copy(alpha = 0.4f)
                    )
                    Text("No sessions yet", style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary)
                    Text("Start a walk, run, or cycle",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                grouped.forEach { (dateLabel, sessions) ->
                    item(key = "header_$dateLabel") {
                        Text(
                            dateLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = textSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(sessions, key = { it.id }) { session ->
                        val dismissState = rememberSwipeToDismissBoxState()

                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                vm.deleteSession(session)
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 4.dp)
                                        .background(
                                            color = danger,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .padding(end = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        ) {
                            SessionRow(
                                session = session,
                                surfaceColor = surfaceColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                routeColor = routeColor,
                                onClick = { onSessionClick(session.id) }
                            )
                        }
                        HorizontalDivider(
                            color = if (isDark) FootlogColors.borderDark else FootlogColors.borderLight,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

private fun activityIcon(type: String): ImageVector = when (type.lowercase()) {
    "run" -> Icons.AutoMirrored.Filled.DirectionsRun
    "cycle" -> Icons.AutoMirrored.Filled.DirectionsBike
    else -> Icons.AutoMirrored.Filled.DirectionsWalk
}

private fun formatDurationShort(startMs: Long, endMs: Long): String {
    val totalMin = ((endMs - startMs) / 60000).coerceAtLeast(0)
    return if (totalMin >= 60) {
        val h = totalMin / 60
        val m = totalMin % 60
        if (m == 0L) "${h}h" else "${h}h ${m}m"
    } else {
        "${totalMin}m"
    }
}

@Composable
private fun SessionRow(
    session: SessionEntity,
    surfaceColor: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    routeColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val timeStr = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
        .format(Instant.ofEpochMilli(session.startTime).atZone(ZoneId.systemDefault()).toLocalDateTime())
    val distKm = "%.1f km".format(session.distanceMeters / 1000f)
    val duration = formatDurationShort(session.startTime, session.endTime)

    Surface(
        onClick = onClick,
        color = surfaceColor,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = activityIcon(session.activityType),
                contentDescription = session.activityType,
                tint = routeColor,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.title.ifBlank { session.activityType.replaceFirstChar { it.uppercase() } },
                    style = MaterialTheme.typography.bodyLarge,
                    color = textPrimary
                )
                Text(
                    "$distKm · $duration · $timeStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary
                )
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = textSecondary)
        }
    }
}
