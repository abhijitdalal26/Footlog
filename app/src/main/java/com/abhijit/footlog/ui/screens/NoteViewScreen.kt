package com.abhijit.footlog.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.data.entity.NoteType
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.NoteViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteViewScreen(
    sessionId: String,
    onBack: () -> Unit,
    vm: NoteViewModel = viewModel(factory = NoteViewModel.Factory(sessionId))
) {
    val note by vm.existingNote.collectAsState()
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Note", color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                            tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        note?.let { n ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    DateTimeFormatter.ofPattern("MMMM d, yyyy '·' h:mm a", Locale.getDefault())
                        .format(Instant.ofEpochMilli(n.createdAt).atZone(ZoneId.systemDefault()).toLocalDateTime()),
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary
                )

                if (n.type == NoteType.TEXT && n.content.isNotBlank()) {
                    Text(
                        text = n.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textPrimary,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f
                    )
                } else if (n.type == NoteType.VOICE) {
                    Text(
                        "Voice note",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                    )
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("No note yet", color = textSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
