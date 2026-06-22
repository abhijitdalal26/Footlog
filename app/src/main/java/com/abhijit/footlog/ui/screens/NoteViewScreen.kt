package com.abhijit.footlog.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.data.entity.NoteType
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteViewScreen(
    sessionId: String,
    onBack: () -> Unit,
    vm: NoteViewModel = viewModel(factory = NoteViewModel.Factory(sessionId))
) {
    val note by vm.existingNote.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val surfaceColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight
    val onPrimary = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Note") },
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
        note?.let { n ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val dateStr = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
                    .format(Date(n.createdAt))
                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = textSecondary)

                if (n.type == NoteType.VOICE) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = surfaceColor)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FilledIconButton(
                                onClick = { vm.togglePlayback() },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = routeColor, contentColor = onPrimary)
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play"
                                )
                            }
                            Column {
                                Text("Voice note", style = MaterialTheme.typography.bodyMedium,
                                    color = textPrimary)
                                LinearProgressIndicator(
                                    progress = { if (isPlaying) 0.5f else 0f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = routeColor,
                                    trackColor = if (isDark) FootlogColors.borderDark else FootlogColors.borderLight
                                )
                            }
                        }
                    }
                }

                if (n.type == NoteType.TEXT && n.content.isNotBlank()) {
                    Text(n.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textPrimary,
                        modifier = Modifier.fillMaxWidth())
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No note found", color = textSecondary)
        }
    }
}
