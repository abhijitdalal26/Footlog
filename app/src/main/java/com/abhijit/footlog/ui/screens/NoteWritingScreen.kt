package com.abhijit.footlog.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
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
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight

    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (text.isNotBlank()) vm.saveTextNote(text)
                            onBack()
                        }
                    ) {
                        Text(
                            if (text.isNotBlank()) "Save" else "Done",
                            color = if (text.isNotBlank()) routeColor else textSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = textPrimary),
                cursorBrush = SolidColor(routeColor),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            "What's on your mind?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textSecondary.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}
