package com.abhijit.footlog.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.data.entity.HighlightEntity
import com.abhijit.footlog.ui.components.MapLibreView
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.SessionDetailViewModel
import com.abhijit.footlog.util.estimateCalories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: String,
    onBack: () -> Unit,
    onNoteClick: () -> Unit,
    onHighlightClick: (String) -> Unit,
    vm: SessionDetailViewModel = viewModel(factory = SessionDetailViewModel.Factory(sessionId))
) {
    val session by vm.session.collectAsState()
    val hasNote by vm.hasNote.collectAsState()
    val highlights by vm.highlights.collectAsState()
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val surfaceColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight
    val accent = if (isDark) FootlogColors.highlightAccentDark else FootlogColors.highlightAccentLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(session?.title?.ifBlank { null }
                        ?: session?.activityType?.replaceFirstChar { it.uppercase() }
                        ?: "Session")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val s = session
                        if (s != null && s.routePoints.isNotEmpty()) {
                            MapLibreView(
                                routePoints = s.routePoints,
                                currentLocation = null,
                                highlights = highlights,
                                routeColor = routeColor,
                                isInteractive = false,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Route map",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }
            }

            session?.let { s ->
                item {
                    val durationMin = ((s.endTime - s.startTime) / 60000)
                    val pace = if (s.distanceMeters > 0 && durationMin > 0)
                        "%.1f min/km".format(durationMin / (s.distanceMeters / 1000f)) else "—"
                    val calories = estimateCalories(s.distanceMeters, s.activityType)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatChip("%.1f km".format(s.distanceMeters / 1000f), "Distance",
                                surfaceColor, textPrimary, textSecondary, Modifier.weight(1f), 0)
                            StatChip("${durationMin}m", "Duration",
                                surfaceColor, textPrimary, textSecondary, Modifier.weight(1f), 1)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatChip(pace, "Pace", surfaceColor, textPrimary, textSecondary,
                                Modifier.weight(1f), 2)
                            StatChip("$calories kcal", "Calories", surfaceColor, textPrimary,
                                textSecondary, Modifier.weight(1f), 3)
                        }
                    }
                }
            }

            if (hasNote) {
                item {
                    OutlinedButton(
                        onClick = onNoteClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
                    ) {
                        Icon(Icons.Filled.MicNone, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("View note")
                    }
                }
            }

            if (highlights.isNotEmpty()) {
                item {
                    Text(
                        "Highlights",
                        style = MaterialTheme.typography.titleSmall,
                        color = textSecondary
                    )
                }
                items(highlights, key = { it.id }) { highlight ->
                    HighlightRow(
                        highlight = highlight,
                        surfaceColor = surfaceColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accent = accent,
                        onClick = { onHighlightClick(highlight.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightRow(
    highlight: HighlightEntity,
    surfaceColor: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = surfaceColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(highlight.emoji, style = MaterialTheme.typography.titleLarge)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    highlight.name.ifBlank { highlight.category },
                    style = MaterialTheme.typography.bodyMedium,
                    color = textPrimary
                )
                if (!highlight.note.isNullOrBlank()) {
                    Text(
                        highlight.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary,
                        maxLines = 1
                    )
                }
            }
            Icon(Icons.Filled.Place, contentDescription = null,
                tint = accent, modifier = Modifier.size(18.dp))
        }
    }
}
