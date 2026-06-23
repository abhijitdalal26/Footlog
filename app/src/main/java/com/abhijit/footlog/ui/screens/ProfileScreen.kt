package com.abhijit.footlog.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.abhijit.footlog.ui.theme.FootlogColors
import com.abhijit.footlog.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    vm: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val surfaceColor = if (isDark) FootlogColors.surfaceDark else FootlogColors.surfaceLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight

    val userName by vm.userName.collectAsState()
    val userEmail by vm.userEmail.collectAsState()
    val profilePhotoUri by vm.profilePhotoUri.collectAsState()
    val isSignedIn by vm.isSignedIn.collectAsState()
    val isSyncing by vm.isSyncing.collectAsState()
    val signInError by vm.signInError.collectAsState()

    val context = LocalContext.current
    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                vm.setProfilePhoto(uri.toString())
            }
        }
    )

    signInError?.let { error ->
        LaunchedEffect(error) {
            kotlinx.coroutines.delay(3000)
            vm.clearSignInError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // Avatar
            Box(contentAlignment = Alignment.BottomEnd) {
                if (profilePhotoUri != null) {
                    AsyncImage(
                        model = profilePhotoUri,
                        contentDescription = "Profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(surfaceColor)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = textSecondary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(routeColor)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = "Change photo",
                        modifier = Modifier.size(15.dp),
                        tint = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = userName ?: "Your name",
                style = MaterialTheme.typography.titleLarge,
                color = if (userName != null) textPrimary else textSecondary
            )

            if (isSignedIn && userEmail != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = userEmail!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary
                )
            }

            Spacer(Modifier.height(40.dp))

            // Account section
            Text(
                "Account",
                style = MaterialTheme.typography.labelMedium,
                color = textSecondary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            ProfileRow(
                icon = Icons.Filled.Edit,
                label = if (userName != null) "Change name" else "Set your name",
                iconTint = routeColor,
                labelColor = textPrimary,
                surfaceColor = surfaceColor,
                showBorder = !isDark,
                onClick = {
                    nameInput = userName ?: ""
                    showNameDialog = true
                }
            )
            Spacer(Modifier.height(8.dp))
            ProfileRow(
                icon = Icons.Filled.PhotoCamera,
                label = "Change photo",
                iconTint = textPrimary,
                labelColor = textPrimary,
                surfaceColor = surfaceColor,
                showBorder = !isDark,
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            Spacer(Modifier.height(32.dp))

            // Cloud sync section
            Text(
                "Cloud sync",
                style = MaterialTheme.typography.labelMedium,
                color = textSecondary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            if (!isSignedIn) {
                // Sign-in card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    border = if (!isDark) BorderStroke(1.dp, FootlogColors.borderLight) else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Sign in to sync your walks across devices",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = routeColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Button(
                                onClick = { vm.signInWithGoogle(context) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = routeColor),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Icon(Icons.Filled.AccountCircle, contentDescription = null,
                                    modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Sign in with Google", fontWeight = FontWeight.Medium)
                            }
                        }
                        signInError?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = FootlogColors.danger)
                        }
                    }
                }
            } else {
                // Signed-in state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    border = if (!isDark) BorderStroke(1.dp, FootlogColors.borderLight) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Cloud, contentDescription = null,
                            tint = routeColor, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Syncing to cloud", style = MaterialTheme.typography.bodyMedium,
                                color = textPrimary)
                            userEmail?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = textSecondary)
                            }
                        }
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp),
                                color = routeColor, strokeWidth = 2.dp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                ProfileRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    label = "Sign out",
                    iconTint = FootlogColors.danger,
                    labelColor = FootlogColors.danger,
                    surfaceColor = surfaceColor,
                    showBorder = !isDark,
                    onClick = { showSignOutDialog = true }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Name dialog
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Your name", color = textPrimary) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = { Text("Enter your name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = routeColor,
                        unfocusedBorderColor = if (isDark) FootlogColors.borderDark else FootlogColors.borderLight,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        cursorColor = routeColor
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.setUserName(nameInput.trim().ifBlank { null })
                    showNameDialog = false
                }) { Text("Save", color = routeColor) }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel", color = textSecondary)
                }
            },
            containerColor = surfaceColor,
            titleContentColor = textPrimary,
            textContentColor = textPrimary
        )
    }

    // Sign-out confirmation
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign out?", color = textPrimary) },
            text = {
                Text(
                    "Your walks stay on this device. Sign back in anytime to re-sync.",
                    color = textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.signOut()
                        showSignOutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FootlogColors.danger)
                ) { Text("Sign out") }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = textSecondary)
                }
            },
            containerColor = surfaceColor,
            titleContentColor = textPrimary,
            textContentColor = textSecondary
        )
    }
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    label: String,
    iconTint: Color,
    labelColor: Color,
    surfaceColor: Color,
    showBorder: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val inactiveColor = if (isDark) FootlogColors.navInactiveDark else FootlogColors.navInactiveLight

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = if (showBorder) BorderStroke(1.dp, FootlogColors.borderLight) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(14.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = labelColor,
                modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, contentDescription = null,
                modifier = Modifier.size(20.dp), tint = inactiveColor)
        }
    }
}
