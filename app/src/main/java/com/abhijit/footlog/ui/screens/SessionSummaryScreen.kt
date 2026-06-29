package com.abhijit.footlog.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.SessionSummaryViewModel
import com.abhijit.footlog.ui.components.MapLibreView
import com.abhijit.footlog.util.estimateCalories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSummaryScreen(
    sessionId: String,
    onDone: () -> Unit,
    onShare: () -> Unit,
    vm: SessionSummaryViewModel = viewModel(
        factory = SessionSummaryViewModel.Factory(sessionId)
    )
) {
    val session by vm.session.collectAsState()
    val isPersonalBest by vm.isPersonalBest.collectAsState()
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val surfaceColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight
    val onPrimary = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight

    var titleText by remember(session) { mutableStateOf(session?.title ?: "") }

    LaunchedEffect(session?.title) {
        if (session?.title != null && titleText.isBlank()) {
            titleText = session!!.title
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${(session?.activityType ?: "session").replaceFirstChar { it.uppercase() }} complete") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (session != null) {
                        MapLibreView(
                            routePoints = session!!.routePoints,
                            currentLocation = null,
                            highlights = emptyList(),
                            routeColor = routeColor,
                            isInteractive = false,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        MapPlaceholder(
                            routeColor = routeColor,
                            textSecondary = textSecondary
                        )
                    }
                }
            }

            if (isPersonalBest) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = routeColor.copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🏆", style = MaterialTheme.typography.titleLarge)
                        Column {
                            Text(
                                "New personal best!",
                                style = MaterialTheme.typography.labelLarge,
                                color = routeColor
                            )
                            Text(
                                "Longest session yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("Session title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = routeColor,
                    unfocusedBorderColor = if (isDark) FootlogColors.borderDark else FootlogColors.borderLight,
                    focusedLabelColor = routeColor,
                    unfocusedTextColor = textPrimary,
                    focusedTextColor = textPrimary
                )
            )

            session?.let { s ->
                val durationMin = ((s.endTime - s.startTime) / 60000)
                val pace = if (s.distanceMeters > 0 && durationMin > 0)
                    "%.1f min/km".format(durationMin / (s.distanceMeters / 1000f)) else "—"
                val calories = estimateCalories(s.distanceMeters, s.activityType)

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
                    StatChip(pace, "Pace",
                        surfaceColor, textPrimary, textSecondary, Modifier.weight(1f), 2)
                    StatChip("$calories kcal", "Calories",
                        surfaceColor, textPrimary, textSecondary, Modifier.weight(1f), 3)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Save as favorite route",
                        style = MaterialTheme.typography.bodyLarge, color = textPrimary,
                        modifier = Modifier.weight(1f))
                    Switch(
                        checked = s.isFavoriteRoute,
                        onCheckedChange = { vm.toggleFavorite(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = onPrimary,
                            checkedTrackColor = routeColor)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.saveTitle(titleText)
                        onDone()
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
                ) { Text("Done") }
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.saveTitle(titleText)
                        onShare()
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = routeColor, contentColor = onPrimary)
                ) { Text("Share") }
            }
        }
    }
}

@Composable
private fun MapPlaceholder(routeColor: androidx.compose.ui.graphics.Color,
                           textSecondary: androidx.compose.ui.graphics.Color) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("Route map", style = MaterialTheme.typography.bodySmall, color = textSecondary)
    }
}

@Composable
fun StatChip(
    value: String,
    label: String,
    surfaceColor: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    delayIndex: Int = 0
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayIndex * 100L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 },
        modifier = modifier
    ) {
        Card(
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = surfaceColor)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(value, style = MaterialTheme.typography.titleMedium, color = textPrimary)
                Text(label, style = MaterialTheme.typography.labelSmall, color = textSecondary)
            }
        }
    }
}
