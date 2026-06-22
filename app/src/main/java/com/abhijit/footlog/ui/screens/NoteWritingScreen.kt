package com.abhijit.footlog.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteWritingScreen(
    sessionId: String,
    onBack: () -> Unit,
    vm: NoteViewModel = viewModel(factory = NoteViewModel.Factory(sessionId))
) {
    val isRecording by vm.isRecording.collectAsState()
    val transcription by vm.transcription.collectAsState()
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight
    val accent = if (isDark) FootlogColors.highlightAccentDark else FootlogColors.highlightAccentLight
    val onPrimary = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight

    var typingMode by remember { mutableStateOf(false) }
    var typedText by remember { mutableStateOf("") }

    val recordingPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) vm.toggleRecording() }

    BackHandler(enabled = isRecording) {
        vm.toggleRecording()
        onBack()
    }

    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add a note") },
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
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.weight(1f))

            if (!typingMode) {
                Box(contentAlignment = Alignment.Center) {
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(scale)
                                .border(2.dp, accent.copy(alpha = 0.5f), CircleShape)
                        )
                    }
                    FilledIconButton(
                        onClick = {
                            recordingPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        modifier = Modifier.size(80.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isRecording) accent else routeColor,
                            contentColor = onPrimary
                        )
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = "Record",
                            modifier = Modifier.size(36.dp))
                    }
                }

                if (transcription.isNotBlank()) {
                    Text(transcription, style = MaterialTheme.typography.bodyLarge,
                        color = textPrimary)
                }

                TextButton(onClick = { typingMode = true }) {
                    Text("or type instead", color = textSecondary)
                }
            } else {
                OutlinedTextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    placeholder = { Text("Write your note here...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = routeColor,
                        unfocusedBorderColor = if (isDark) FootlogColors.borderDark else FootlogColors.borderLight,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )
                TextButton(onClick = { typingMode = false }) {
                    Text("use voice instead", color = textSecondary)
                }
            }

            Spacer(Modifier.weight(1f))

            val canSave = (typingMode && typedText.isNotBlank()) ||
                    (!typingMode && !isRecording && transcription.isNotBlank()) ||
                    (!typingMode && isRecording)
            Button(
                onClick = {
                    if (typingMode) {
                        vm.saveTextNote(typedText)
                    } else {
                        if (isRecording) vm.toggleRecording()
                        vm.saveVoiceNote()
                    }
                    onBack()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = routeColor, contentColor = onPrimary)
            ) { Text("Save") }
        }
    }
}
