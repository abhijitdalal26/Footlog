package com.abhijit.footlog.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: StatsViewModel = viewModel(factory = StatsViewModel.Factory)) {
    val stats by vm.stats.collectAsState()
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val surfaceColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight
    val barBg = if (isDark) FootlogColors.borderDark else FootlogColors.borderLight

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stats") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("This week") }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("All time") }
            }

            Card(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    WeeklyBarChart(
                        distances = stats.weeklyDistances,
                        barColor = routeColor,
                        barBgColor = barBg
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("%.1f km".format(stats.totalDistanceMeters / 1000f), "Total distance",
                    surfaceColor, textPrimary, textSecondary, Modifier.weight(1f))
                StatCard("${stats.totalSessions}", "Sessions",
                    surfaceColor, textPrimary, textSecondary, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("${stats.currentStreak} days", "Streak",
                    surfaceColor, textPrimary, textSecondary, Modifier.weight(1f))
                StatCard("%.2f km²".format(stats.exploredAreaKm2), "Areas explored",
                    surfaceColor, textPrimary, textSecondary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WeeklyBarChart(
    distances: List<Float>,
    barColor: Color,
    barBgColor: Color
) {
    val maxDist = distances.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val days = listOf("M", "T", "W", "T", "F", "S", "S")

    Canvas(modifier = Modifier.fillMaxSize()) {
        val barCount = distances.size
        val barAreaWidth = size.width / barCount
        val barWidth = barAreaWidth * 0.5f
        val maxBarHeight = size.height - 20f

        distances.forEachIndexed { i, dist ->
            val x = i * barAreaWidth + (barAreaWidth - barWidth) / 2
            val normalizedHeight = (dist / maxDist) * maxBarHeight
            // Background bar
            drawRect(color = barBgColor, topLeft = Offset(x, 0f), size = Size(barWidth, maxBarHeight))
            // Value bar
            if (dist > 0f) {
                drawRect(
                    color = barColor,
                    topLeft = Offset(x, maxBarHeight - normalizedHeight),
                    size = Size(barWidth, normalizedHeight)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    surfaceColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = textPrimary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = textSecondary)
        }
    }
}
