package com.abhijit.footlog.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abhijit.footlog.data.preferences.AppPreferences
import com.abhijit.footlog.ui.theme.FootlogColors
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight
    val onPrimary = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight

    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }

    var locationDenied by remember { mutableStateOf(false) }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locationDenied = false
            scope.launch { pagerState.animateScrollToPage(2) }
        } else {
            locationDenied = true
        }
    }

    val finalLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        scope.launch {
            prefs.setOnboardingComplete()
            onComplete()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (page) {
                        0 -> {
                            Icon(Icons.Filled.Map, contentDescription = null,
                                modifier = Modifier.size(80.dp), tint = routeColor)
                            Spacer(Modifier.height(24.dp))
                            Text("Track your world", style = MaterialTheme.typography.headlineMedium,
                                color = textPrimary, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Record walks, runs, and cycles. Tag highlights along the way. Build a lifetime map of everywhere you've been.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textSecondary, textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = routeColor, contentColor = onPrimary),
                                shape = MaterialTheme.shapes.small
                            ) { Text("Next") }
                        }
                        1 -> {
                            Icon(Icons.Filled.LocationOn, contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = if (locationDenied) FootlogColors.danger else routeColor)
                            Spacer(Modifier.height(24.dp))
                            Text("Allow location access", style = MaterialTheme.typography.headlineMedium,
                                color = textPrimary, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (locationDenied)
                                    "Location access is required to track your routes. Please allow it to continue."
                                else
                                    "Footlog uses location only while you're actively tracking. Nothing is shared or sent anywhere.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (locationDenied) FootlogColors.danger else textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    locationDenied = false
                                    locationLauncher.launch(arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (locationDenied) FootlogColors.danger else routeColor,
                                    contentColor = onPrimary),
                                shape = MaterialTheme.shapes.small
                            ) { Text(if (locationDenied) "Try again" else "Allow location access") }
                        }
                        2 -> {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null,
                                modifier = Modifier.size(80.dp), tint = routeColor)
                            Spacer(Modifier.height(24.dp))
                            Text("Camera & notifications", style = MaterialTheme.typography.headlineMedium,
                                color = textPrimary, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Camera lets you photo-pin highlights. Notifications show your tracking status in the background.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textSecondary, textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    val perms = mutableListOf(Manifest.permission.CAMERA)
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        perms.add(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    finalLauncher.launch(perms.toTypedArray())
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = routeColor, contentColor = onPrimary),
                                shape = MaterialTheme.shapes.small
                            ) { Text("Get started") }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isSelected) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .then(
                                if (isSelected) Modifier
                                else Modifier
                            ),
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = if (isSelected) routeColor
                            else if (isDark) FootlogColors.navInactiveDark else FootlogColors.navInactiveLight,
                            shape = CircleShape
                        ) {}
                    }
                }
            }
        }
    }
}
