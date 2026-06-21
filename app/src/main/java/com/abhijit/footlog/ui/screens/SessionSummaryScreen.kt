package com.abhijit.footlog.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.SessionSummaryViewModel

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
                title = { Text("Walk complete") },
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
                modifier = Modifier.fillMaxWidth().height(200.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MapPlaceholder(
                        routeColor = routeColor,
                        textSecondary = textSecondary
                    )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val durationMin = ((s.endTime - s.startTime) / 60000)
                    val pace = if (s.distanceMeters > 0 && durationMin > 0)
                        "%.1f min/km".format(durationMin / (s.distanceMeters / 1000f)) else "—"

                    StatChip("%.1f km".format(s.distanceMeters / 1000f), "Distance",
                        surfaceColor, textPrimary, textSecondary)
                    StatChip("${durationMin}m", "Duration",
                        surfaceColor, textPrimary, textSecondary)
                    StatChip(pace, "Pace", surfaceColor, textPrimary, textSecondary)
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
                    onClick = { vm.saveTitle(titleText); onDone() },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
                ) { Text("Done") }
                Button(
                    onClick = { vm.saveTitle(titleText); onShare() },
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = textPrimary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = textSecondary)
        }
    }
}
