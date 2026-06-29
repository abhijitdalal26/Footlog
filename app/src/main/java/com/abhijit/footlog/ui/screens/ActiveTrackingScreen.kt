package com.abhijit.footlog.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.ActiveTrackingViewModel
import com.abhijit.footlog.ui.components.HighlightTagSheet
import com.abhijit.footlog.ui.components.MapLibreView
import java.io.File

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
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val danger = FootlogColors.danger
    val accent = if (isDark) FootlogColors.highlightAccentDark else FootlogColors.highlightAccentLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight

    val isTracking = uiState.countdownSeconds == 0
    var showHighlightSheet by remember { mutableStateOf(false) }
    var showStopConfirmation by remember { mutableStateOf(false) }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }

    BackHandler(enabled = isTracking) {
        showStopConfirmation = true
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = pendingPhotoUri
            uiState.currentLocation?.let { loc ->
                vm.addPhotoHighlight(uri?.toString(), loc.latitude, loc.longitude)
            }
            pendingPhotoUri = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapLibreView(
            routePoints = uiState.routePoints,
            currentLocation = uiState.currentLocation,
            highlights = uiState.highlights,
            routeColor = routeColor,
            showMyLocation = true,
            modifier = Modifier.fillMaxSize()
        )

        if (!isTracking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = uiState.countdownSeconds,
                        transitionSpec = {
                            (scaleIn(tween(350)) + fadeIn(tween(200))) togetherWith
                                    (scaleOut(tween(350)) + fadeOut(tween(200)))
                        },
                        label = "countdown"
                    ) { count ->
                        Text(
                            text = "$count",
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Get ready to ${activityType.lowercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        } else {
            // Stats overlay — top
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                    .background(
                        color = if (isDark) FootlogColors.surfaceDark.copy(alpha = 0.88f)
                        else FootlogColors.surfaceLight.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBlock("%.2f".format(uiState.distanceMeters / 1000f), "km", textPrimary, textSecondary)
                    StatBlock(formatDuration(uiState.elapsedSeconds), "time", textPrimary, textSecondary)
                    StatBlock(formatPace(uiState.distanceMeters, uiState.elapsedSeconds), "min/km", textPrimary, textSecondary)
                }
                HorizontalDivider(
                    color = if (isDark) FootlogColors.borderDark else FootlogColors.borderLight,
                    thickness = 0.5.dp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBlock("${uiState.caloriesBurned}", "kcal", textPrimary, textSecondary)
                    StatBlock(formatSpeed(uiState.currentSpeedKmh), "km/h", textPrimary, textSecondary)
                }
            }

            // Right FABs — camera + highlight pin
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 100.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val photoDir = File(context.cacheDir, "footlog_shares").also { it.mkdirs() }
                        val photoFile = File(photoDir, "photo_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
                        pendingPhotoUri = uri
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                            putExtra(MediaStore.EXTRA_OUTPUT, uri)
                            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        }
                        cameraLauncher.launch(intent)
                    },
                    containerColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight,
                    contentColor = accent
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Camera")
                }
                SmallFloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showHighlightSheet = true
                    },
                    containerColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight,
                    contentColor = accent
                ) {
                    Icon(Icons.Filled.Place, contentDescription = "Pin")
                }
            }

            // Left FAB — note
            SmallFloatingActionButton(
                onClick = { onNoteClick(uiState.sessionId) },
                modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 100.dp, start = 16.dp),
                containerColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight,
                contentColor = textPrimary
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Note")
            }

            // Stop button — pulsing red at bottom center
            val infiniteTransition = rememberInfiniteTransition(label = "stop_pulse")
            val stopPulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.03f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "stop_scale"
            )

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showStopConfirmation = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .scale(stopPulseScale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = danger,
                    contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Filled.Stop, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Stop", fontWeight = FontWeight.Medium)
            }
        }
    }

    if (showHighlightSheet) {
        HighlightTagSheet(
            onDismiss = { showHighlightSheet = false },
            onConfirm = { category, emoji, name, note ->
                uiState.currentLocation?.let { loc ->
                    vm.addHighlight(loc.latitude, loc.longitude, category, emoji, name, note)
                }
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showHighlightSheet = false
            }
        )
    }

    if (showStopConfirmation) {
        AlertDialog(
            onDismissRequest = { showStopConfirmation = false },
            title = { Text("Stop tracking?") },
            text = { Text("Are you sure you want to end your current session?") },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.stopTracking()
                        onStop(uiState.sessionId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = danger)
                ) {
                    Text("End session")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirmation = false }) {
                    Text("Cancel", color = textSecondary)
                }
            },
            containerColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight,
            titleContentColor = textPrimary,
            textContentColor = textSecondary
        )
    }
}

@Composable
private fun StatBlock(value: String, label: String, textPrimary: Color, textSecondary: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                        slideInVertically(animationSpec = tween(220, delayMillis = 90)) { it } togetherWith
                        fadeOut(animationSpec = tween(220)) +
                        slideOutVertically(animationSpec = tween(220)) { -it }
            },
            label = "stat_anim"
        ) { targetValue ->
            Text(targetValue, style = MaterialTheme.typography.headlineSmall, color = textPrimary)
        }
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

private fun formatSpeed(speedKmh: Float): String {
    return if (speedKmh < 0.5f) "--" else "%.1f".format(speedKmh)
}
