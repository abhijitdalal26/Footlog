package com.abhijit.footlog.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.ActiveTrackingViewModel
import com.abhijit.footlog.ui.components.HighlightTagSheet
import com.abhijit.footlog.ui.components.MapLibreView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTrackingScreen(
    activityType: String,
    onStop: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onBack: () -> Unit,
    vm: ActiveTrackingViewModel = viewModel(factory = ActiveTrackingViewModel.Factory(activityType))
) {
    val uiState by vm.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val danger = FootlogColors.danger
    val accent = if (isDark) FootlogColors.highlightAccentDark else FootlogColors.highlightAccentLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight

    var showHighlightSheet by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photoUri = result.data?.data
            uiState.currentLocation?.let { loc ->
                vm.addPhotoHighlight(photoUri?.toString(), loc.latitude, loc.longitude)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Map
        MapLibreView(
            routePoints = uiState.routePoints,
            currentLocation = uiState.currentLocation,
            highlights = uiState.highlights,
            routeColor = routeColor,
            modifier = Modifier.fillMaxSize()
        )

        // Stat overlay top
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                .background(
                    color = if (isDark) FootlogColors.surfaceDark.copy(alpha = 0.85f)
                    else FootlogColors.surfaceLight.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBlock("%.2f".format(uiState.distanceMeters / 1000f), "km", textPrimary, textSecondary)
                StatBlock(formatDuration(uiState.elapsedSeconds), "time", textPrimary, textSecondary)
                StatBlock(formatPace(uiState.distanceMeters, uiState.elapsedSeconds), "min/km", textPrimary, textSecondary)
            }
        }

        // Right FABs
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 100.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraLauncher.launch(intent)
                },
                containerColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight,
                contentColor = accent
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Camera")
            }
            SmallFloatingActionButton(
                onClick = { showHighlightSheet = true },
                containerColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight,
                contentColor = accent
            ) {
                Icon(Icons.Filled.Place, contentDescription = "Pin")
            }
        }

        // Left FAB - note
        SmallFloatingActionButton(
            onClick = { onNoteClick(uiState.sessionId) },
            modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 100.dp, start = 16.dp),
            containerColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight,
            contentColor = textPrimary
        ) {
            Icon(Icons.Filled.MicNone, contentDescription = "Note")
        }

        // Stop button
        Button(
            onClick = {
                vm.stopTracking()
                onStop(uiState.sessionId)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = danger,
                contentColor = Color.White
            ),
            shape = MaterialTheme.shapes.small
        ) {
            Icon(Icons.Filled.Stop, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Stop")
        }
    }

    if (showHighlightSheet) {
        HighlightTagSheet(
            onDismiss = { showHighlightSheet = false },
            onConfirm = { category, emoji, name, note ->
                uiState.currentLocation?.let { loc ->
                    vm.addHighlight(loc.latitude, loc.longitude, category, emoji, name, note)
                }
                showHighlightSheet = false
            }
        )
    }
}

@Composable
private fun StatBlock(value: String, label: String, textPrimary: Color, textSecondary: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = textPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = textSecondary)
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

private fun formatPace(distanceMeters: Float, elapsedSeconds: Long): String {
    if (distanceMeters < 10f || elapsedSeconds < 1) return "--"
    val minPerKm = (elapsedSeconds / 60.0) / (distanceMeters / 1000.0)
    val min = minPerKm.toInt()
    val sec = ((minPerKm - min) * 60).toInt()
    return "%d'%02d\"".format(min, sec)
}
