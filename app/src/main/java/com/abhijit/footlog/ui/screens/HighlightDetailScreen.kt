package com.abhijit.footlog.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.HighlightDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightDetailScreen(
    highlightId: String,
    onBack: () -> Unit,
    vm: HighlightDetailViewModel = viewModel(factory = HighlightDetailViewModel.Factory(highlightId))
) {
    val highlight by vm.highlight.collectAsState()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight
    val onPrimary = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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
        highlight?.let { h ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (h.photoPath != null) {
                    AsyncImage(
                        model = h.photoPath,
                        contentDescription = "Highlight photo",
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null,
                            modifier = Modifier.size(48.dp), tint = textSecondary)
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(h.emoji, style = MaterialTheme.typography.headlineMedium)
                        Text(h.name, style = MaterialTheme.typography.headlineSmall, color = textPrimary)
                    }

                    AssistChip(
                        onClick = {},
                        label = { Text(h.category.replaceFirstChar { it.uppercase() }) }
                    )

                    if (h.note != null) {
                        Text(h.note,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textSecondary)
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val uri = Uri.parse("geo:${h.lat},${h.lng}?q=${h.lat},${h.lng}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
                    ) {
                        Icon(Icons.Filled.Navigation, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open in maps")
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
