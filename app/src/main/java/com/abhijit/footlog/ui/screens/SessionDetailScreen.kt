package com.abhijit.footlog.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.SessionDetailViewModel

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
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val surfaceColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.title ?: "Session") },
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
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Box(modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Route map", style = MaterialTheme.typography.bodySmall, color = textSecondary)
                }
            }

            session?.let { s ->
                val durationMin = ((s.endTime - s.startTime) / 60000)
                val pace = if (s.distanceMeters > 0 && durationMin > 0)
                    "%.1f min/km".format(durationMin / (s.distanceMeters / 1000f)) else "—"
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip("%.1f km".format(s.distanceMeters / 1000f), "Distance",
                        surfaceColor, textPrimary, textSecondary)
                    StatChip("${durationMin}m", "Duration",
                        surfaceColor, textPrimary, textSecondary)
                    StatChip(pace, "Pace", surfaceColor, textPrimary, textSecondary)
                }
            }

            if (hasNote) {
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
    }
}
