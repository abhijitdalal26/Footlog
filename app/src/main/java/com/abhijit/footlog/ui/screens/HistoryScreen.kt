package com.abhijit.footlog.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.data.entity.SessionEntity
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

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
                Text("No sessions yet", style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary)
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
                        SessionRow(
                            session = session,
                            surfaceColor = surfaceColor,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = { onSessionClick(session.id) }
                        )
                        HorizontalDivider(color = if (isDark) FootlogColors.borderDark else FootlogColors.borderLight,
                            thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(
    session: SessionEntity,
    surfaceColor: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(session.startTime))
    val distKm = "%.1f km".format(session.distanceMeters / 1000f)

    Surface(
        onClick = onClick,
        color = surfaceColor,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.title.ifBlank { session.activityType.replaceFirstChar { it.uppercase() } },
                    style = MaterialTheme.typography.bodyLarge,
                    color = textPrimary
                )
                Text("$distKm · $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = textSecondary)
        }
    }
}
