package com.abhijit.footlog.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.data.entity.SessionEntity
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.HomeViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import coil.compose.AsyncImage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartActivity: (String) -> Unit,
    onSessionClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val sessions by vm.recentSessions.collectAsState()
    val todayDistanceKm by vm.todayDistanceKm.collectAsState()
    val currentStreak by vm.currentStreak.collectAsState()
    val isDark = isSystemInDarkTheme()
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val surfaceColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight
    val onPrimary = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight

    val userName by vm.userName.collectAsState()
    val profilePhotoUri by vm.profilePhotoUri.collectAsState()

    val context = LocalContext.current
    var pendingActivityType by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted && pendingActivityType != null) {
            onStartActivity(pendingActivityType!!)
            pendingActivityType = null
        }
    }

    val tryStartActivity: (String) -> Unit = { type ->
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            onStartActivity(type)
        } else {
            pendingActivityType = type
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (userName != null) "Hi, $userName" else "Good to see you",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Ready to move?",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                },
                actions = {
                    if (profilePhotoUri != null) {
                        AsyncImage(
                            model = profilePhotoUri,
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { onProfileClick() }
                        )
                    } else {
                        IconButton(onClick = onProfileClick) {
                            Icon(
                                Icons.Filled.AccountCircle,
                                contentDescription = "Profile",
                                tint = textSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
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
            if (todayDistanceKm > 0f || currentStreak >= 2) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (todayDistanceKm > 0f) {
                            TodayStatStrip(
                                distanceKm = todayDistanceKm,
                                surfaceColor = surfaceColor,
                                routeColor = routeColor,
                                textSecondary = textSecondary,
                                modifier = if (currentStreak >= 2) Modifier.weight(1f) else Modifier.fillMaxWidth()
                            )
                        }
                        if (currentStreak >= 2) {
                            StreakChip(
                                streak = currentStreak,
                                surfaceColor = surfaceColor,
                                textSecondary = textSecondary,
                                modifier = if (todayDistanceKm > 0f) Modifier.weight(1f) else Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedActivityCard(
                        label = "Walk",
                        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        bgColor = routeColor,
                        textColor = onPrimary,
                        modifier = Modifier.weight(1f),
                        delayIndex = 0,
                        onClick = { tryStartActivity("walk") }
                    )
                    AnimatedActivityCard(
                        label = "Run",
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        bgColor = routeColor,
                        textColor = onPrimary,
                        modifier = Modifier.weight(1f),
                        delayIndex = 1,
                        onClick = { tryStartActivity("run") }
                    )
                    AnimatedActivityCard(
                        label = "Cycle",
                        icon = Icons.AutoMirrored.Filled.DirectionsBike,
                        bgColor = routeColor,
                        textColor = onPrimary,
                        modifier = Modifier.weight(1f),
                        delayIndex = 2,
                        onClick = { tryStartActivity("cycle") }
                    )
                }
            }

            item {
                Text(
                    "Recent sessions",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
                )
            }

            item {
                if (sessions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No walks yet — start one above",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(sessions) { session ->
                            RecentSessionCard(
                                session = session,
                                surfaceColor = surfaceColor,
                                textSecondary = textSecondary,
                                routeColor = routeColor,
                                onClick = { onSessionClick(session.id) }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Your explored map",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Map, contentDescription = null,
                                modifier = Modifier.size(40.dp), tint = textSecondary)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Your map fills in as you walk",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayStatStrip(
    distanceKm: Float,
    surfaceColor: androidx.compose.ui.graphics.Color,
    routeColor: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + expandVertically(tween(400)),
        modifier = modifier
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = surfaceColor)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = routeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Today", style = MaterialTheme.typography.labelSmall, color = textSecondary)
                }
                Text(
                    "%.1f km".format(distanceKm),
                    style = MaterialTheme.typography.titleMedium,
                    color = routeColor
                )
            }
        }
    }
}

@Composable
private fun StreakChip(
    streak: Int,
    surfaceColor: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400, delayMillis = 80)) + expandVertically(tween(400, delayMillis = 80)),
        modifier = modifier
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = surfaceColor)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text("🔥 Streak", style = MaterialTheme.typography.labelSmall, color = textSecondary)
                Text(
                    "$streak days",
                    style = MaterialTheme.typography.titleMedium,
                    color = textSecondary
                )
            }
        }
    }
}

@Composable
private fun AnimatedActivityCard(
    label: String,
    icon: ImageVector,
    bgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    delayIndex: Int = 0,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayIndex * 80L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + scaleIn(tween(500), initialScale = 0.8f),
        modifier = modifier
    ) {
        Card(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
            modifier = Modifier.height(90.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = bgColor)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(icon, contentDescription = label, tint = textColor, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(4.dp))
                Text(label, style = MaterialTheme.typography.labelLarge, color = textColor)
            }
        }
    }
}


@Composable
private fun RecentSessionCard(
    session: SessionEntity,
    surfaceColor: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    routeColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val dateStr = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
        .format(Instant.ofEpochMilli(session.startTime).atZone(ZoneId.systemDefault()).toLocalDate())
    val distKm = "%.1f km".format(session.distanceMeters / 1000f)
    val activityIcon = when (session.activityType.lowercase()) {
        "run" -> Icons.AutoMirrored.Filled.DirectionsRun
        "cycle" -> Icons.AutoMirrored.Filled.DirectionsBike
        else -> Icons.AutoMirrored.Filled.DirectionsWalk
    }

    Card(
        onClick = onClick,
        modifier = Modifier.width(150.dp).height(120.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(activityIcon, contentDescription = null,
                    tint = routeColor, modifier = Modifier.size(16.dp))
                Text(
                    session.title.ifBlank { session.activityType.replaceFirstChar { it.uppercase() } },
                    style = MaterialTheme.typography.labelLarge, color = textPrimary,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(distKm, style = MaterialTheme.typography.titleSmall, color = textPrimary)
            Text(dateStr, style = MaterialTheme.typography.bodySmall, color = textSecondary)
        }
    }
}
