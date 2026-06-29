package com.abhijit.footlog.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.StatsViewModel
import java.time.LocalDate

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
        if (stats.totalSessions == 0) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Filled.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = textSecondary.copy(alpha = 0.35f)
                    )
                    Text("No data yet", style = MaterialTheme.typography.titleMedium,
                        color = textSecondary)
                    Text("Complete a session to see your stats",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary.copy(alpha = 0.6f))
                }
            }
            return@Scaffold
        }

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
                modifier = Modifier.fillMaxWidth().height(180.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    WeeklyBarChart(
                        distances = stats.weeklyDistances,
                        barColor = routeColor,
                        barBgColor = barBg,
                        textSecondary = textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = stats.totalDistanceMeters / 1000f,
                    label = "Total distance",
                    format = { "%.1f km".format(it) },
                    surfaceColor = surfaceColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f),
                    delayIndex = 0
                )
                StatCard(
                    value = stats.totalSessions.toFloat(),
                    label = "Sessions",
                    format = { "${it.toInt()}" },
                    surfaceColor = surfaceColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f),
                    delayIndex = 1
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = stats.currentStreak.toFloat(),
                    label = "Streak",
                    format = { "${it.toInt()} days" },
                    surfaceColor = surfaceColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f),
                    delayIndex = 2
                )
                StatCard(
                    value = stats.exploredAreaKm2.toFloat(),
                    label = "Areas explored",
                    format = { "%.2f km²".format(it) },
                    surfaceColor = surfaceColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f),
                    delayIndex = 3
                )
            }
        }
    }
}

private fun last7DayLabels(): List<String> {
    val letters = listOf("S", "M", "T", "W", "T", "F", "S")
    val today = LocalDate.now()
    return (6 downTo 0).map { daysAgo ->
        val dayOfWeek = today.minusDays(daysAgo.toLong()).dayOfWeek
        letters[dayOfWeek.value % 7]
    }
}

@Composable
private fun WeeklyBarChart(
    distances: List<Float>,
    barColor: Color,
    barBgColor: Color,
    textSecondary: Color
) {
    val maxDist = distances.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val days = remember { last7DayLabels() }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(distances) {
        animProgress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }

    val labelPaint = remember(textSecondary) {
        android.graphics.Paint().apply {
            textSize = 32f
            textAlign = android.graphics.Paint.Align.CENTER
            color = textSecondary.hashCode()
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val barCount = distances.size
        val barAreaWidth = size.width / barCount
        val barWidth = barAreaWidth * 0.45f
        val labelHeight = 40f
        val maxBarHeight = size.height - labelHeight

        distances.forEachIndexed { i, dist ->
            val x = i * barAreaWidth + (barAreaWidth - barWidth) / 2
            val normalizedHeight = (dist / maxDist) * maxBarHeight * animProgress.value

            drawRect(
                color = barBgColor.copy(alpha = 0.3f),
                topLeft = Offset(x, 0f),
                size = Size(barWidth, maxBarHeight)
            )

            if (dist > 0f) {
                drawRect(
                    color = barColor,
                    topLeft = Offset(x, maxBarHeight - normalizedHeight),
                    size = Size(barWidth, normalizedHeight)
                )
            }

            drawContext.canvas.nativeCanvas.drawText(
                days.getOrElse(i) { "" },
                x + barWidth / 2,
                size.height - 8f,
                labelPaint
            )
        }
    }
}

@Composable
private fun StatCard(
    value: Float,
    label: String,
    format: (Float) -> String,
    surfaceColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier,
    delayIndex: Int = 0
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayIndex * 100L)
        visible = true
    }

    val animatedValue by animateFloatAsState(
        targetValue = if (visible) value else 0f,
        animationSpec = tween(
            durationMillis = 900,
            delayMillis = delayIndex * 100,
            easing = FastOutSlowInEasing
        ),
        label = "stat_count_up"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 },
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().height(90.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = surfaceColor)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(format(animatedValue), style = MaterialTheme.typography.headlineSmall,
                    color = textPrimary)
                Text(label, style = MaterialTheme.typography.bodySmall, color = textSecondary)
            }
        }
    }
}
