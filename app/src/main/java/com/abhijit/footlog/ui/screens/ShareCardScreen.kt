package com.abhijit.footlog.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.ShareCardViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareCardScreen(
    sessionId: String,
    onClose: () -> Unit,
    vm: ShareCardViewModel = viewModel(factory = ShareCardViewModel.Factory(sessionId))
) {
    val session by vm.session.collectAsState()
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight

    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Share card — always dark, captured to bitmap
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .clip(MaterialTheme.shapes.medium)
                    .drawWithContent {
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    }
                    .background(FootlogColors.backgroundDark),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    "footlog",
                    style = MaterialTheme.typography.labelSmall,
                    color = FootlogColors.navInactiveDark,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                )
                session?.let { s ->
                    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        .format(Date(s.startTime))
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "%.1f km".format(s.distanceMeters / 1000f),
                            style = MaterialTheme.typography.displaySmall,
                            color = FootlogColors.textPrimaryDark
                        )
                        val durationMin = (s.endTime - s.startTime) / 60000
                        Text(
                            "${durationMin}m · $dateStr",
                            style = MaterialTheme.typography.bodyLarge,
                            color = FootlogColors.textSecondaryDark
                        )
                        Text(
                            s.activityType.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelLarge,
                            color = FootlogColors.routeLineDark
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShareAction(Icons.Filled.Share, "Share", textSecondary) {
                    scope.launch {
                        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                        shareImage(context, bitmap)
                    }
                }
                ShareAction(Icons.Filled.Download, "Save", textSecondary) {
                    scope.launch {
                        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                        saveToGallery(context, bitmap)
                    }
                }
                ShareAction(Icons.Filled.ContentCopy, "Copy stats", textSecondary) {
                    session?.let { s ->
                        val durationMin = (s.endTime - s.startTime) / 60000
                        val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                            .format(Date(s.startTime))
                        clipboard.setText(
                            AnnotatedString("%.1f km ${s.activityType} · ${durationMin}m · $dateStr".format(s.distanceMeters / 1000f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) { Icon(icon, contentDescription = label) }
        Text(label, style = MaterialTheme.typography.labelSmall, color = textSecondary)
    }
}

private fun shareImage(context: Context, bitmap: Bitmap) {
    val dir = File(context.cacheDir, "footlog_shares").also { it.mkdirs() }
    val file = File(dir, "footlog_share_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}

private fun saveToGallery(context: Context, bitmap: Bitmap) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Footlog_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Footlog")
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        }
    }
}
